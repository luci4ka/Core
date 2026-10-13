package ru.terracraft.core.config;

import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

import java.nio.file.Path;

/**
 * Единая точка работы с config.yml.
 * <p>
 * Использование:
 *   plugin.config().get()          — текущий конфиг
 *   plugin.config().reload()       — перечитать с диска
 *   plugin.config().save()         — записать текущий в файл
 *   plugin.config().set(newCfg)    — заменить в памяти + сохранить
 */
public class ConfigManager {

    private static final String FILE_NAME = "config.yml";
    private static final String HEADER =
            "TerraCore configuration\n" +
                    "Документация по ключам — в комментариях к полям AdminToolsConfig";

    private final TerraCorePlugin plugin;
    private final Path path;
    private final YamlConfigurationProperties props;

    private volatile AdminToolsConfig current;

    public ConfigManager(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
        this.path = plugin.getDataFolder().toPath().resolve(FILE_NAME);
        this.props = YamlConfigurationProperties.newBuilder()
                .header(HEADER)
                .build();
    }

    // ============ Загрузка ============

    /** Первичная загрузка. Создаёт файл, если его нет. */
    public void load() {
        plugin.getDataFolder().mkdirs();
        this.current = YamlConfigurations.update(path, AdminToolsConfig.class, props);
        plugin.getLogger().info("Конфиг загружен: " + path.getFileName());
    }

    /** Перечитать с диска. Бросает исключение при ошибке парсинга — ловим в команде. */
    public void reload() {
        this.current = YamlConfigurations.load(path, AdminToolsConfig.class, props);
    }

    /** Записать текущий конфиг в файл. */
    public void save() {
        if (current == null) return;
        YamlConfigurations.save(path, AdminToolsConfig.class, current, props);
    }

    /** Заменить конфиг в памяти + сразу сохранить. */
    public void set(@NotNull AdminToolsConfig cfg) {
        this.current = cfg;
        save();
    }

    // ============ Доступ ============

    public @NotNull AdminToolsConfig get() {
        AdminToolsConfig c = current;
        if (c == null) {
            // На случай, если кто-то обратился до load() — грузим на месте
            synchronized (this) {
                if (current == null) load();
                c = current;
            }
        }
        return c;
    }

    public @NotNull Path path() {
        return path;
    }
}