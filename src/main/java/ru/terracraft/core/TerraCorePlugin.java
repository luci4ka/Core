package ru.terracraft.core;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ru.terracraft.core.command.CommandRegistrar;
import ru.terracraft.core.config.ConfigManager;
import ru.terracraft.core.hook.PlaceholderHook;
import ru.terracraft.core.hook.UpdateNotifierHook;
import ru.terracraft.core.lang.LangManager;
import ru.terracraft.core.lang.MessageService;
import ru.terracraft.core.listener.GodListener;
import ru.terracraft.core.listener.PlayerJoinListener;
import ru.terracraft.core.listener.PlayerQuitListener;
import ru.terracraft.core.listener.VanishListener;
import ru.terracraft.core.manager.*;
import ru.terracraft.core.storage.PlayerCacheService;
import ru.terracraft.core.storage.StateStore;
import ru.terracraft.core.storage.StatsService;
import ru.terracraft.core.storage.StorageManager;
import ru.terracraft.core.task.VanishTask;
import ru.terracraft.core.util.StartupBanner;

public class TerraCorePlugin extends JavaPlugin {

    private static final int BSTATS_ID = 34839;

    private final long startTime = System.currentTimeMillis();

    private ConfigManager configManager;
    private LangManager langManager;
    private MessageService messageService;

    private StorageManager storageManager;
    private StateStore stateStore;
    private StatsService statsService;
    private PlayerCacheService playerCacheService;

    private GodManager godManager;
    private FlyManager flyManager;
    private GamemodeManager gamemodeManager;
    private CooldownManager cooldownManager;
    private VanishManager vanishManager;
    private ConfirmManager confirmManager;

    private VanishTask vanishTask;
    private BukkitTask cooldownCleanupTask;

    @Override
    public void onEnable() {
        long t0 = System.currentTimeMillis();

        // 1. Конфиг
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        // 2. Локализация
        this.langManager = new LangManager(this);
        this.langManager.load();
        this.messageService = new MessageService(this, langManager);

        // 3. Хранилище (MineSQL)
        this.storageManager = new StorageManager(this);
        this.storageManager.init();
        this.stateStore = new StateStore(this);
        this.statsService = new StatsService(this);
        this.playerCacheService = new PlayerCacheService(this);

        // 4. Менеджеры
        this.godManager      = new GodManager(stateStore);
        this.flyManager      = new FlyManager(stateStore);
        this.gamemodeManager = new GamemodeManager(stateStore);
        this.cooldownManager = new CooldownManager(this);
        this.vanishManager   = new VanishManager(this, stateStore);
        this.confirmManager  = new ConfirmManager(this);
        this.confirmManager.start();

        // 5. Команды — только если CommandAPI загружен
        var pm = getServer().getPluginManager();
        if (pm.getPlugin("CommandAPI") != null) {
            new CommandRegistrar(this).registerAll();
        } else {
            getLogger().warning("CommandAPI not found — commands not registered.");
        }

        // 6. Слушатели
        pm.registerEvents(new GodListener(this), this);
        pm.registerEvents(new VanishListener(this), this);
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);

        // 7. Задачи
        this.vanishTask = new VanishTask(this);
        this.vanishTask.start();

        // Периодическая очистка просроченных кулдаунов — период из config.yml
        startCooldownCleanup();

        // 8. PlaceholderAPI hook
        if (pm.getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(this).register();
        }

        // 9. bStats
        if (configManager.get().integrations().bStats()) {
            if (BSTATS_ID > 0) {
                try {
                    new Metrics(this, BSTATS_ID);
                    getLogger().info("bStats connected (id=" + BSTATS_ID + ").");
                } catch (IllegalStateException e) {
                    String msg = e.getMessage() != null ? e.getMessage() : "not relocated";
                    getLogger().info("bStats skipped: " + msg);
                }
            } else {
                getLogger().warning("bStats enabled but BSTATS_ID is not set — metrics disabled.");
            }
        } else {
            getLogger().info("bStats disabled in config.yml.");
        }

        // 10. Update notifier
        if (configManager.get().integrations().updateNotifier()) {
            UpdateNotifierHook.check(this);
        }

        // 11. Красивая панель запуска
        long elapsed = System.currentTimeMillis() - t0;
        StartupBanner.printStartup(this, elapsed, BSTATS_ID);
    }

    @Override
    public void onDisable() {
        long uptime = System.currentTimeMillis() - startTime;

        if (vanishTask != null) vanishTask.stop();
        if (cooldownCleanupTask != null) cooldownCleanupTask.cancel();
        if (confirmManager != null) confirmManager.stop();

        StartupBanner.printShutdown(this, uptime);
    }

    /** Перезагрузка конфига + языков + перезапуск задач с новыми периодами. */
    public void reloadAll() {
        configManager.reload();
        langManager.load();

        if (vanishTask != null) {
            vanishTask.stop();
            vanishTask.start();
        }

        if (cooldownCleanupTask != null) {
            cooldownCleanupTask.cancel();
            cooldownCleanupTask = null;
        }
        startCooldownCleanup();
    }

    /** Запуск задачи очистки кулдаунов с периодом из config.yml. */
    private void startCooldownCleanup() {
        int minutes = configManager.get().cooldowns().cleanupPeriodMinutes();
        if (minutes <= 0) {
            getLogger().info("Cooldown cleanup disabled (cooldowns.cleanupPeriodMinutes = 0).");
            return;
        }
        long period = minutes * 60L * 20L;
        this.cooldownCleanupTask = getServer().getScheduler().runTaskTimerAsynchronously(
                this, () -> cooldownManager.cleanupExpired(), period, period);
        getLogger().info("Cooldown cleanup scheduled every " + minutes + " min.");
    }

    // ============ Геттеры ============

    public long getStartTime() { return startTime; }

    public ConfigManager config()      { return configManager; }
    public LangManager lang()          { return langManager; }
    public MessageService msg()        { return messageService; }

    public StorageManager storage()         { return storageManager; }
    public StateStore states()              { return stateStore; }
    public StatsService stats()             { return statsService; }
    public PlayerCacheService playerCache() { return playerCacheService; }

    public GodManager gods()           { return godManager; }
    public FlyManager fly()            { return flyManager; }
    public GamemodeManager gamemodes() { return gamemodeManager; }
    public CooldownManager cooldowns() { return cooldownManager; }
    public VanishManager vanish()      { return vanishManager; }
    public ConfirmManager confirms()   { return confirmManager; }
}