package ru.terracraft.core.lang;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Загрузка .lang файлов и выдача строк по локали игрока.
 *
 * Ищет файлы:
 *   1. В JAR: resources/lang/<code>.lang
 *   2. В plugins/TerraCore/lang/<code>.lang  (перекрывают встроенные)
 *
 * При загрузке в пользовательский файл ДОПИСЫВАЮТСЯ ключи,
 * которые есть во встроенном, но отсутствуют в пользовательском.
 * Существующие значения НЕ трогаются.
 *
 * Каскад получения строки для локали игрока:
 *   1. Точное совпадение локали (ru_RU → ru_RU).
 *   2. Тот же язык, другой регион (ru_UA → ru_RU, en_GB → en_US).
 *   3. Регион из СНГ → ru_ru.
 *   4. Всё остальное → en_us (fallback).
 */
public class LangManager {

    private static final Set<String> CIS_COUNTRIES = Set.of(
            "RU", "BY", "KZ", "UA", "UZ", "KG", "TJ", "TM", "AZ", "AM", "GE", "MD"
    );

    private static final Locale CIS_FALLBACK = new Locale("ru", "RU");
    private static final Locale DEFAULT_FALLBACK = Locale.US;

    /** Языки, для которых всегда есть встроенный .lang в JAR. */
    private static final List<String> BUILT_IN = List.of("en_us", "ru_ru");

    private final TerraCorePlugin plugin;

    private final Map<Locale, Map<String, String>> translations = new ConcurrentHashMap<>();
    private final Map<String, Locale> byLanguage = new ConcurrentHashMap<>();

    public LangManager(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    // ============ Загрузка ============

    public void load() {
        translations.clear();
        byLanguage.clear();

        // 1. Загрузить встроенные из JAR в память (нужны как эталон для дописывания)
        Map<String, Map<String, String>> builtIn = new HashMap<>();
        for (String code : BUILT_IN) {
            Map<String, String> map = readBuiltIn(code);
            if (map != null) {
                builtIn.put(code, map);
                translations.put(localeOf(code), map);
            }
        }

        // 2. Загрузить пользовательские и дописать недостающие ключи
        loadCustom(builtIn);

        // 3. Индекс по языкам
        rebuildLanguageIndex();

        // 4. Валидация (проверит, что fallback-локаль полна)
        validateKeys();

        plugin.getLogger().info("Загружено языков: " + translations.size()
                + " (" + translations.keySet().stream()
                .map(Locale::toLanguageTag)
                .sorted()
                .toList() + ")");
    }

    /** Читает встроенный .lang из JAR. null, если нет. */
    private @Nullable Map<String, String> readBuiltIn(@NotNull String code) {
        String resource = "lang/" + code + ".lang";
        try (InputStream is = plugin.getResource(resource)) {
            if (is == null) {
                plugin.getLogger().warning("Встроенный перевод не найден: " + resource);
                return null;
            }
            return parse(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка загрузки " + resource + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Загружает пользовательские .lang из plugins/TerraCore/lang/.
     * Если файла нет — создаёт из встроенного.
     * Если в файле нет ключей, которые есть во встроенном — дописывает их.
     */
    private void loadCustom(@NotNull Map<String, Map<String, String>> builtIn) {
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists() && !langDir.mkdirs()) {
            plugin.getLogger().warning("Не удалось создать папку lang/");
            return;
        }

        // Для каждого встроенного языка — убедиться, что файл на диске полон
        for (String code : BUILT_IN) {
            File file = new File(langDir, code + ".lang");
            Map<String, String> jarMap = builtIn.get(code);

            if (!file.exists()) {
                // Файла нет — сохраняем встроенный
                if (jarMap != null) {
                    saveLangFile(file, jarMap);
                }
                continue;
            }

            // Файл есть — читаем, дополняем, сохраняем если что-то добавилось
            Map<String, String> userMap = readLangFile(file);
            if (jarMap != null) {
                boolean changed = addMissingKeys(userMap, jarMap);
                if (changed) {
                    saveLangFile(file, userMap);
                    plugin.getLogger().info("Дополнен " + file.getName()
                            + " новыми ключами из JAR");
                }
            }

            // Пользовательский вариант важнее встроенного
            translations.put(localeOf(code), userMap);
        }

        // Дополнительные пользовательские языки (не из BUILT_IN)
        File[] files = langDir.listFiles((dir, name) -> name.endsWith(".lang"));
        if (files == null) return;

        for (File f : files) {
            String code = f.getName().substring(0, f.getName().length() - ".lang".length());
            if (BUILT_IN.contains(code)) continue;   // уже обработали

            Map<String, String> map = readLangFile(f);
            translations.put(localeOf(code), map);
        }
    }

    /** Читает .lang файл с диска. */
    private @NotNull Map<String, String> readLangFile(@NotNull File file) {
        try (InputStream is = new FileInputStream(file)) {
            return parse(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка загрузки " + file.getName() + ": " + e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    /**
     * Дописывает в userMap ключи из jarMap, которых нет в userMap.
     * Существующие значения не трогает.
     *
     * @return true, если что-то добавилось
     */
    private boolean addMissingKeys(@NotNull Map<String, String> userMap,
                                   @NotNull Map<String, String> jarMap) {
        boolean changed = false;
        for (var entry : jarMap.entrySet()) {
            if (!userMap.containsKey(entry.getKey())) {
                userMap.put(entry.getKey(), entry.getValue());
                changed = true;
            }
        }
        return changed;
    }

    /** Сохраняет .lang файл, группируя по секциям (# ====). */
    private void saveLangFile(@NotNull File file, @NotNull Map<String, String> map) {
        try (Writer w = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {

            // Сохраняем порядок ключей из map (он LinkedHashMap)
            for (var entry : map.entrySet()) {
                w.write(entry.getKey());
                w.write('=');
                w.write(entry.getValue());
                w.write('\n');
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить " + file.getName()
                    + ": " + e.getMessage());
        }
    }

    private void rebuildLanguageIndex() {
        for (Locale locale : translations.keySet()) {
            byLanguage.merge(locale.getLanguage(), locale, (a, b) ->
                    a.getCountry().equalsIgnoreCase("RU") ? a : b);
        }
    }

    private Map<String, String> parse(@NotNull InputStreamReader reader) {
        Map<String, String> map = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                int eq = trimmed.indexOf('=');
                if (eq < 0) continue;
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                map.put(key, value);
            }
        } catch (IOException ignored) {}
        return map;
    }

    private void validateKeys() {
        Map<String, String> fallbackMap = translations.get(DEFAULT_FALLBACK);
        if (fallbackMap == null) {
            plugin.getLogger().severe("Нет fallback-локали "
                    + DEFAULT_FALLBACK.toLanguageTag()
                    + " — сообщения работать не будут.");
            return;
        }
        List<String> missing = new ArrayList<>();
        for (LangKey key : LangKey.values()) {
            if (!fallbackMap.containsKey(key.path())) {
                missing.add(key.path());
            }
        }
        if (!missing.isEmpty()) {
            plugin.getLogger().warning("В " + DEFAULT_FALLBACK.toLanguageTag()
                    + ".lang отсутствуют ключи: " + String.join(", ", missing));
        }
    }

    // ============ Получение ============

    public @Nullable String raw(@NotNull Locale locale, @NotNull LangKey key) {
        String path = key.path();

        String value = lookup(locale, path);
        if (value != null) return value;

        Locale sameLang = byLanguage.get(locale.getLanguage());
        if (sameLang != null && !sameLang.equals(locale)) {
            value = lookup(sameLang, path);
            if (value != null) return value;
        }

        if (isCis(locale)) {
            value = lookup(CIS_FALLBACK, path);
            if (value != null) return value;
        }

        return lookup(DEFAULT_FALLBACK, path);
    }

    public @Nullable String raw(@NotNull Player player, @NotNull LangKey key) {
        return raw(player.locale(), key);
    }

    public boolean hasKey(@NotNull Locale locale, @NotNull LangKey key) {
        Map<String, String> map = translations.get(locale);
        return map != null && map.containsKey(key.path());
    }

    public @NotNull Set<Locale> locales() {
        return Set.copyOf(translations.keySet());
    }

    public @NotNull Locale resolve(@NotNull Locale requested) {
        if (translations.containsKey(requested)) return requested;
        Locale sameLang = byLanguage.get(requested.getLanguage());
        if (sameLang != null) return sameLang;
        if (isCis(requested)) return CIS_FALLBACK;
        return DEFAULT_FALLBACK;
    }

    // ============ Служебное ============

    private @Nullable String lookup(@NotNull Locale locale, @NotNull String path) {
        Map<String, String> map = translations.get(locale);
        return map == null ? null : map.get(path);
    }

    private boolean isCis(@NotNull Locale locale) {
        String country = locale.getCountry();
        return !country.isEmpty() && CIS_COUNTRIES.contains(country.toUpperCase(Locale.ROOT));
    }

    private static Locale localeOf(@NotNull String code) {
        String[] parts = code.split("_");
        return parts.length == 2
                ? new Locale(parts[0].toLowerCase(Locale.ROOT),
                parts[1].toUpperCase(Locale.ROOT))
                : Locale.forLanguageTag(code.replace('_', '-'));
    }
}