package ru.terracraft.core.hook;

import com.github.ezplugins.updater.UpdateChecker;
import com.github.ezplugins.updater.UpdateResult;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

/**
 * Проверка обновлений через GitHub Releases.
 *
 * Репозиторий задаётся в константах — поменяй под свой.
 */
public final class UpdateNotifierHook {

    /** GitHub owner/org. */
    private static final String GITHUB_OWNER = "luci4ka";
    /** GitHub repo. */
    private static final String GITHUB_REPO = "TerraCraftCore";

    private UpdateNotifierHook() {}

    /** Вызывать в onEnable(), если config.integrations().updateNotifier() == true. */
    public static void check(@NotNull TerraCorePlugin plugin) {
        String currentVersion = plugin.getDescription().getVersion();

        UpdateChecker checker = UpdateChecker
                .builder(GITHUB_OWNER, GITHUB_REPO, currentVersion)
                .timeout(java.time.Duration.ofSeconds(8))
                .userAgent("TerraCore/" + currentVersion + " (Paper)")
                .build();

        // Асинхронно — не блокируем старт сервера
        checker.checkNowAsync().thenAccept(result -> {
            if (result.hasError()) {
                plugin.getLogger().warning("Проверка обновлений не удалась: "
                        + result.getError().orElseThrow().getMessage());
                return;
            }

            if (result.isUpdateAvailable()) {
                plugin.getLogger().info("========================================");
                plugin.getLogger().info("Доступна новая версия TerraCore!");
                plugin.getLogger().info("Сейчас: " + currentVersion);
                plugin.getLogger().info("Новая:   " + result.getLatestTag().orElse("unknown"));
                plugin.getLogger().info("Скачать: " + result.getReleaseUrl().orElse("n/a"));
                plugin.getLogger().info("========================================");
            } else {
                plugin.getLogger().info("TerraCore актуален (v" + currentVersion + ").");
            }
        });
    }
}