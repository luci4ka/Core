package ru.terracraft.core.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Загрузчик плоских .yml-файлов локализации.
 *
 * <p>Формат (ключи с точками <b>квотированы</b>):
 * <pre>
 *   "prefix": "&lt;gray&gt;[TerraCore] &lt;reset&gt;"
 *   "no.permission": "&lt;red&gt;Нет доступа."
 *   "god.enabled.self": "&lt;green&gt;Godmode включён."
 * </pre>
 *
 * <p>Ищет файлы:
 * <ol>
 *   <li>В JAR: {@code resources/lang/<code>.yml}</li>
 *   <li>В {@code plugins/TerraCore/lang/<code>.yml} (перекрывают встроенные)</li>
 * </ol>
 *
 * <p><b>Drop-in:</b> любой новый {@code <lang>.yml} в {@code plugins/TerraCore/lang/}
 * автоматически подхватывается. Имя файла → локаль:
 * <ul>
 *   <li>{@code ru_ru.yml} → {@code ru_RU}</li>
 *   <li>{@code de_de.yml} → {@code de_DE}</li>
 *   <li>{@code en.yml} → {@code en} (без страны)</li>
 *   <li>{@code pt_br.yml} → {@code pt_BR}</li>
 * </ul>
 *
 * <p>Если в кастомной локали не хватает ключа — берётся из {@code en_us}
 * (или из {@code ru_ru} для CIS-стран).
 */
public class LangManager {

    private static final Set<String> CIS_COUNTRIES = Set.of(
            "RU", "BY", "KZ", "UA", "UZ", "KG", "TJ", "TM", "AZ", "AM", "GE", "MD"
    );

    private static final Locale CIS_FALLBACK = new Locale("ru", "RU");
    private static final Locale DEFAULT_FALLBACK = Locale.US;
    private static final String FILE_EXT = ".yml";
    private static final String BUILT_IN_EN = "en_us";
    private static final String BUILT_IN_RU = "ru_ru";

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

        // 1. Создать lang/
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists() && !langDir.mkdirs()) {
            plugin.getLogger().warning("Failed to create lang/ directory");
            return;
        }

        // 2. Встроенные из JAR — эталон для дописывания
        Map<String, Map<String, String>> builtIn = new HashMap<>();
        for (String code : new String[]{BUILT_IN_EN, BUILT_IN_RU}) {
            Map<String, String> map = readFromJar(code);
            if (map != null) builtIn.put(code, map);
        }

        // 3. Создать/дополнить файлы на диске для встроенных
        for (var entry : builtIn.entrySet()) {
            String code = entry.getKey();
            Map<String, String> jarMap = entry.getValue();
            File file = new File(langDir, code + FILE_EXT);

            if (!file.exists()) {
                saveLangFile(file, jarMap);
                plugin.getLogger().info("Created " + file.getName() + " from JAR");
            } else {
                Map<String, String> userMap = readLangFile(file);
                boolean changed = addMissingKeys(userMap, jarMap);
                if (changed) {
                    saveLangFile(file, userMap);
                    plugin.getLogger().info("Updated " + file.getName() + " with new keys");
                }
            }
        }

        // 4. Загрузить ВСЕ .yml из lang/ (включая drop-in)
        File[] files = langDir.listFiles((dir, name) -> name.endsWith(FILE_EXT));
        if (files != null) {
            for (File f : files) {
                String code = f.getName().substring(0, f.getName().length() - FILE_EXT.length());
                Locale locale = localeOf(code);
                Map<String, String> map = readLangFile(f);
                translations.put(locale, map);

                boolean builtin = code.equals(BUILT_IN_EN) || code.equals(BUILT_IN_RU);
                plugin.getLogger().info("Loaded " + (builtin ? "built-in" : "custom")
                        + " locale: " + locale.toLanguageTag()
                        + " (" + f.getName() + ", " + map.size() + " keys)");
            }
        }

        // 5. Индекс по языкам
        rebuildLanguageIndex();

        // 6. Валидация — по КАЖДОЙ локали
        validateAllKeys();

        plugin.getLogger().info("Locales loaded: " + translations.size()
                + " — " + translations.keySet().stream()
                .map(Locale::toLanguageTag).sorted().toList());
    }

    // ============ Чтение ============

    private @Nullable Map<String, String> readFromJar(@NotNull String code) {
        String resource = "lang/" + code + FILE_EXT;
        try (InputStream is = plugin.getResource(resource)) {
            if (is == null) {
                plugin.getLogger().warning("Built-in lang not found: " + resource);
                return null;
            }
            return parseFlatYaml(is);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load " + resource + ": " + e.getMessage());
            return null;
        }
    }

    private @NotNull Map<String, String> readLangFile(@NotNull File file) {
        try (InputStream is = new FileInputStream(file)) {
            return parseFlatYaml(is);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load " + file.getName() + ": " + e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private @NotNull Map<String, String> parseFlatYaml(@NotNull InputStream is) {
        Map<String, String> map = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

                int keyEnd = findKeyEnd(trimmed);
                if (keyEnd < 0) {
                    plugin.getLogger().warning("Malformed line " + lineNum + ": " + trimmed);
                    continue;
                }

                String rawKey = trimmed.substring(0, keyEnd).trim();
                String rawValue = trimmed.substring(keyEnd + 1).trim();

                String key = unquote(rawKey);
                String value = unquote(rawValue);
                if (key.isEmpty()) continue;

                map.put(key, value);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("I/O error parsing YAML: " + e.getMessage());
        }
        return map;
    }

    private int findKeyEnd(@NotNull String s) {
        boolean inSingle = false, inDouble = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'' && !inDouble) inSingle = !inSingle;
            else if (c == '"' && !inSingle) inDouble = !inDouble;
            else if (c == ':' && !inSingle && !inDouble) return i;
        }
        return -1;
    }

    private @NotNull String unquote(@NotNull String s) {
        if (s.length() >= 2) {
            char first = s.charAt(0);
            char last = s.charAt(s.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                s = s.substring(1, s.length() - 1);
                if (first == '"') {
                    s = s.replace("\\\"", "\"").replace("\\\\", "\\");
                }
            }
        }
        return s;
    }

    // ============ Сохранение ============

    private void saveLangFile(@NotNull File file, @NotNull Map<String, String> map) {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write("# TerraCore language file (flat format)\n");
            w.write("# Each line: \"key\": \"MiniMessage value\"\n");
            w.write("# Keys with dots MUST be quoted.\n\n");
            for (var entry : map.entrySet()) {
                w.write('"');
                w.write(escape(entry.getKey()));
                w.write("\": \"");
                w.write(escape(entry.getValue()));
                w.write("\"\n");
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save " + file.getName() + ": " + e.getMessage());
        }
    }

    private @NotNull String escape(@NotNull String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

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

    private void rebuildLanguageIndex() {
        for (Locale locale : translations.keySet()) {
            byLanguage.merge(locale.getLanguage(), locale, (a, b) ->
                    a.getCountry().equalsIgnoreCase("RU") ? a : b);
        }
    }

    // ============ Валидация (per-locale) ============

    /**
     * Проверяет каждую загруженную локаль на наличие всех ключей из {@link LangKey}.
     * Для кастомных локалей — предупреждение с перечнем missing-ключей.
     * Для встроенных (en_us, ru_ru) — severe, если что-то отсутствует.
     */
    private void validateAllKeys() {
        Map<String, String> fallbackMap = translations.get(DEFAULT_FALLBACK);
        if (fallbackMap == null) {
            plugin.getLogger().severe("No fallback locale " + DEFAULT_FALLBACK.toLanguageTag());
            return;
        }

        for (var entry : translations.entrySet()) {
            Locale locale = entry.getKey();
            Map<String, String> map = entry.getValue();

            List<String> missing = missingKeys(locale);
            if (missing.isEmpty()) continue;

            boolean builtin = locale.equals(DEFAULT_FALLBACK) || locale.equals(CIS_FALLBACK);
            String localeName = locale.toLanguageTag();

            if (builtin) {
                plugin.getLogger().severe("[" + localeName + "] missing "
                        + missing.size() + " keys: " + String.join(", ", missing));
            } else {
                plugin.getLogger().warning("[" + localeName + "] missing "
                        + missing.size() + "/" + LangKey.values().length
                        + " keys (will fallback): " + String.join(", ", missing));
            }
        }
    }

    /**
     * Ключи, которых нет в указанной локали.
     * Используется в {@code /tc lang} и в {@link #validateAllKeys()}.
     */
    public @NotNull List<String> missingKeys(@NotNull Locale locale) {
        Map<String, String> map = translations.get(locale);
        if (map == null) return List.of();

        List<String> missing = new ArrayList<>();
        for (LangKey key : LangKey.values()) {
            if (!map.containsKey(key.path())) {
                missing.add(key.path());
            }
        }
        return missing;
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

    public @NotNull Component component(@NotNull Player player,
                                        @NotNull LangKey key,
                                        Object... kv) {
        String raw = raw(player.locale(), key);
        if (raw == null) raw = "<red>?" + key.path() + "?";
        return MiniMessage.miniMessage().deserialize(raw, resolver(kv));
    }

    public @NotNull Component component(@NotNull Locale locale,
                                        @NotNull LangKey key,
                                        Object... kv) {
        String raw = raw(locale, key);
        if (raw == null) raw = "<red>?" + key.path() + "?";
        return MiniMessage.miniMessage().deserialize(raw, resolver(kv));
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

    // ============ Статистика (для /tc lang) ============

    /** Информация о загруженной локали. */
    public record LocaleStats(
            @NotNull Locale locale,
            int totalKeys,
            int missingKeys,
            boolean builtin
    ) {
        public int percentComplete() {
            if (totalKeys == 0) return 100;
            return (int) Math.round((totalKeys - missingKeys) * 100.0 / totalKeys);
        }
    }

    /**
     * Возвращает статистику по всем локалям — для {@code /tc lang}.
     */
    public @NotNull List<LocaleStats> stats() {
        int total = LangKey.values().length;
        List<LocaleStats> list = new ArrayList<>();

        for (var entry : translations.entrySet()) {
            Locale locale = entry.getKey();
            int missing = missingKeys(locale).size();
            boolean builtin = locale.equals(DEFAULT_FALLBACK) || locale.equals(CIS_FALLBACK);
            list.add(new LocaleStats(locale, total, missing, builtin));
        }

        list.sort(Comparator.comparing(s -> s.locale().toLanguageTag()));
        return list;
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

    private @NotNull TagResolver resolver(Object... kv) {
        if (kv.length == 0) return TagResolver.empty();
        if (kv.length % 2 != 0) {
            throw new IllegalArgumentException("Expected even number of key/value arguments, got: " + kv.length);
        }
        TagResolver.Builder b = TagResolver.builder();
        for (int i = 0; i < kv.length; i += 2) {
            String name = String.valueOf(kv[i]);
            Object value = kv[i + 1];

            if (value instanceof Component c) {
                b.resolver(Placeholder.component(name, c));
            } else {
                b.resolver(Placeholder.parsed(name, String.valueOf(value)));
            }
        }
        return b.build();
    }
}