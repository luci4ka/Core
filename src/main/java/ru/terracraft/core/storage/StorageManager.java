package ru.terracraft.core.storage;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.plugin.minesql.MineSQL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;

/**
 * Единая точка работы с хранилищем через MineSQL.
 *
 * <p>Плагин получает {@link SQLManager} из {@link MineSQL#getRegistry()}
 * по имени источника {@link #SOURCE_NAME}.
 *
 * <p>Если MineSQL не установлен — {@link #init()} тихо пропускает инициализацию.
 * Все операции с БД в этом режиме кинут {@link IllegalStateException} при
 * попытке доступа к {@link #sql()}. Это позволяет запускать тесты в MockBukkit,
 * где MineSQL отсутствует.
 */
public class StorageManager {

    public static final String SOURCE_NAME = "terracore";

    private final TerraCorePlugin plugin;
    private SQLManager sqlManager;

    public StorageManager(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        // MineSQL не установлен — тесты или отсутствующий плагин
        if (plugin.getServer().getPluginManager().getPlugin("MineSQL") == null) {
            plugin.getLogger().info("MineSQL not found — storage disabled.");
            return;
        }

        try {
            this.sqlManager = MineSQL.getRegistry().get(SOURCE_NAME);
            if (sqlManager == null) {
                plugin.getLogger().severe("MineSQL source '" + SOURCE_NAME + "' not found!");
                plugin.getLogger().severe("Add it to plugins/MineSQL/config.yml");
                return;
            }
            plugin.getLogger().info("Storage connected: MineSQL (" + SOURCE_NAME + ")");
            migrate();
        } catch (Exception e) {
            plugin.getLogger().severe("Storage init failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public @NotNull SQLManager sql() {
        if (sqlManager == null) {
            throw new IllegalStateException("StorageManager not initialized");
        }
        return sqlManager;
    }

    public void close() {
        this.sqlManager = null;
    }

    public @NotNull String prefix() {
        return plugin.config().get().storage().tablePrefix();
    }

    // ============ Миграция ============

    private void migrate() {
        String p = prefix();

        executeRaw("""
                CREATE TABLE IF NOT EXISTS %scooldowns (
                    uuid        VARCHAR(36) NOT NULL,
                    command_key VARCHAR(32) NOT NULL,
                    expires_at  BIGINT      NOT NULL,
                    PRIMARY KEY (uuid, command_key)
                )
                """.formatted(p), "cooldowns");

        executeRaw("""
                CREATE TABLE IF NOT EXISTS %sstates (
                    uuid        VARCHAR(36)  NOT NULL,
                    state_key   VARCHAR(32)  NOT NULL,
                    state_value VARCHAR(64)  NOT NULL,
                    updated_at  BIGINT       NOT NULL,
                    PRIMARY KEY (uuid, state_key)
                )
                """.formatted(p), "states");

        executeRaw("""
                CREATE TABLE IF NOT EXISTS %sstats (
                    uuid        VARCHAR(36) NOT NULL,
                    command_key VARCHAR(32) NOT NULL,
                    uses        BIGINT      NOT NULL DEFAULT 0,
                    last_used   BIGINT      NOT NULL,
                    PRIMARY KEY (uuid, command_key)
                )
                """.formatted(p), "stats");

        executeRaw("""
                CREATE TABLE IF NOT EXISTS %splayers (
                    uuid          VARCHAR(36) NOT NULL PRIMARY KEY,
                    name          VARCHAR(16) NOT NULL,
                    first_join    BIGINT      NOT NULL,
                    last_seen     BIGINT      NOT NULL,
                    last_ip       VARCHAR(45) NULL,
                    last_world    VARCHAR(64) NULL,
                    last_x        DOUBLE      NULL,
                    last_y        DOUBLE      NULL,
                    last_z        DOUBLE      NULL,
                    last_gamemode VARCHAR(16) NULL
                )
                """.formatted(p), "players");

        createIndex("idx_" + p + "cooldowns_expires", p + "cooldowns", "expires_at");
        createIndex("idx_" + p + "players_name", p + "players", "name");
    }

    private void executeRaw(@NotNull String sql, @NotNull String name) {
        try {
            sql().executeSQL(sql);
            plugin.getLogger().info("Table ready: " + name);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create table " + name + ": " + e.getMessage());
        }
    }

    private void createIndex(@NotNull String indexName, @NotNull String table, @NotNull String column) {
        try {
            sql().executeSQL("CREATE INDEX " + indexName + " ON " + table + "(" + column + ")");
        } catch (Exception ignored) {
            // Индекс уже существует — ок
        }
    }

    /**
     * @return {@link SQLManager} или {@code null}, если MineSQL не загружен.
     * Используется в сервисах для graceful skip в тестах / без MineSQL.
     */
    public @Nullable SQLManager sqlOrNull() {
        return sqlManager;
    }

}