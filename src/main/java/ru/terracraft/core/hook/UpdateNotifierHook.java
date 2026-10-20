package ru.terracraft.core.hook;

import com.github.ezplugins.updater.UpdateChecker;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;

import java.time.Duration;
import java.util.Optional;

/**
 * Проверка обновлений через GitHub Releases.
 *
 * <p>Репозиторий задаётся в константах — поменяй под свой.
 *
 * <p>Вызывать в {@code onEnable()}, если
 * {@code config.integrations().updateNotifier() == true}.
 */
public final class UpdateNotifierHook {

    /** GitHub owner/org. */
    private static final String GITHUB_OWNER = "luci4ka";
    /** GitHub repo. */
    private static final String GITHUB_REPO = "TerraCraftCore";

    private UpdateNotifierHook() {}

    public static void check(@NotNull TerraCorePlugin plugin) {
        String currentVersion = plugin.getPluginMeta().getVersion();

        UpdateChecker checker;
        try {
            checker = UpdateChecker
                    .builder(GITHUB_OWNER, GITHUB_REPO, currentVersion)
                    .timeout(Duration.ofSeconds(8))
                    .userAgent("TerraCore/" + currentVersion + " (Paper)")
                    .build();
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось создать UpdateChecker: " + e.getMessage());
            return;
        }

        // Асинхронно — не блокируем старт сервера
        checker.checkNowAsync().thenAccept(result -> {
            if (result == null) {
                plugin.getLogger().warning("Проверка обновлений вернула null.");
                return;
            }

            if (result.hasError()) {
                // Безопасно достаём сообщение — getError() может быть пуст
                String msg = result.getError()
                        .map(Throwable::getMessage)
                        .orElse("неизвестная ошибка");
                plugin.getLogger().warning("Проверка обновлений не удалась: " + msg);
                return;
            }

            if (result.isUpdateAvailable()) {
                String latest = result.getLatestTag().orElse("unknown");
                String url    = result.getReleaseUrl().orElse("n/a");

                plugin.getLogger().info("========================================");
                plugin.getLogger().info("Доступна новая версия TerraCore!");
                plugin.getLogger().info("Сейчас: " + currentVersion);
                plugin.getLogger().info("Новая:   " + latest);
                plugin.getLogger().info("Скачать: " + url);
                plugin.getLogger().info("========================================");
            } else {
                plugin.getLogger().info("TerraCore актуален (v" + currentVersion + ").");
            }
        }).exceptionally(ex -> {
            // На случай, если CompletableFuture упадёт до thenAccept
            plugin.getLogger().warning("Ошибка проверки обновлений: " + ex.getMessage());
            return null;
        });
    }
}