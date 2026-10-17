package ru.terracraft.core;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.terracraft.core.command.CommandRegistrar;
import ru.terracraft.core.config.ConfigManager;
import ru.terracraft.core.hook.PlaceholderHook;
import ru.terracraft.core.hook.UpdateNotifierHook;
import ru.terracraft.core.lang.LangManager;
import ru.terracraft.core.lang.MessageService;
import ru.terracraft.core.listener.GodListener;
import ru.terracraft.core.listener.PlayerJoinListener;
import ru.terracraft.core.listener.VanishListener;
import ru.terracraft.core.manager.*;
import ru.terracraft.core.task.VanishTask;

public final class TerraCorePlugin extends JavaPlugin {

    private static final int BSTATS_ID = 00000;

    private ConfigManager configManager;
    private LangManager langManager;
    private MessageService messageService;

    private GodManager godManager;
    private FlyManager flyManager;
    private GamemodeManager gamemodeManager;
    private CooldownManager cooldownManager;
    private VanishManager vanishManager;
    private ConfirmManager confirmManager;

    private VanishTask vanishTask;

    @Override
    public void onEnable() {
        // 1. Конфиг
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        // 2. Локализация
        this.langManager = new LangManager(this);
        this.langManager.load();
        this.messageService = new MessageService(this, langManager);

        // 3. Менеджеры
        this.godManager      = new GodManager();
        this.flyManager      = new FlyManager();
        this.gamemodeManager = new GamemodeManager();
        this.cooldownManager = new CooldownManager(this);
        this.vanishManager   = new VanishManager(this);
        this.confirmManager  = new ConfirmManager();

        // 4. Команды (CommandAPI)
        new CommandRegistrar(this).registerAll();

        // 5. Слушатели
        var pm = getServer().getPluginManager();
        pm.registerEvents(new GodListener(this), this);
        pm.registerEvents(new VanishListener(this), this);
        pm.registerEvents(new PlayerJoinListener(this), this);

        // 6. Задачи
        this.vanishTask = new VanishTask(this);
        this.vanishTask.start();

        // 7. PlaceholderAPI hook
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(this).register();
            getLogger().info("PlaceholderAPI hook подключён.");
        }

        // 8. bStats (включается в конфиге)
        if (configManager.get().integrations().bStats()) {
            new Metrics(this, BSTATS_ID);
        } else {
            getLogger().info("bStats отключён в config.yml.");
        }

        // 9. Update notifier (включается в конфиге)
        if (configManager.get().integrations().updateNotifier()) {
            UpdateNotifierHook.check(this);
        }

        getLogger().info("TerraCore включён.");
    }

    @Override
    public void onDisable() {
        if (vanishTask != null) vanishTask.cancel();

        getLogger().info("TerraCore выключен.");
    }

    /** Перезагрузка конфига + языков. Вызывается из /terracore reload. */
    public void reloadAll() {
        configManager.reload();
        langManager.load();
    }

    // ============ Геттеры ============

    public ConfigManager config()      { return configManager; }
    public LangManager lang()          { return langManager; }
    public MessageService msg()        { return messageService; }

    public GodManager gods()           { return godManager; }
    public FlyManager fly()            { return flyManager; }
    public GamemodeManager gamemodes() { return gamemodeManager; }
    public CooldownManager cooldowns() { return cooldownManager; }
    public VanishManager vanish()      { return vanishManager; }
    public ConfirmManager confirms()   { return confirmManager; }
}