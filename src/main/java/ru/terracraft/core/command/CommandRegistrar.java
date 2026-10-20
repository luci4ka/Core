package ru.terracraft.core.command;

import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.impl.*;
import ru.terracraft.core.lang.LangKey;

/**
 * Регистрация всех команд TerraCore через CommandAPI.
 *
 * <p>Внимание: некоторые команды перекрывают ванильные
 * ({@code /give}, {@code /list}, {@code /time}, {@code /weather},
 * {@code /gamemode}, {@code /enchant}, {@code /ext}). Это сделано намеренно:
 * CommandAPI регистрирует их с более высоким приоритетом, и ванильные
 * обработчики не вызываются.
 *
 * <p><b>Изоляция падений:</b> каждая команда регистрируется через
 * {@link #safe(String, Runnable)} — если одна упадёт, остальные зарегистрируются.
 */
public class CommandRegistrar {

    private final TerraCorePlugin plugin;

    public CommandRegistrar(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        registerCore();
        registerStates();
        registerPlayerStats();
        registerItems();
        registerWorld();
        registerWorkstations();
        registerWeatherTime();
        registerMovement();
        registerPlayerActions();
        registerInfo();
        registerMail();
    }

    // ============ Изоляция ============

    /**
     * Регистрирует команду в try/catch — одна упавшая не ломает остальные.
     *
     * @param name   имя команды (для лога)
     * @param action регистратор
     */
    private void safe(@NotNull String name, @NotNull Runnable action) {
        try {
            action.run();
        } catch (Throwable e) {
            plugin.getLogger().severe("Failed to register command '" + name
                    + "': " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============ Группы ============

    private void registerCore() {
        safe("terracore", () -> new TerraCoreCommand(plugin).register());
    }

    private void registerStates() {
        safe("god",    () -> new GodCommand(plugin).register());
        safe("fly",    () -> new FlyCommand(plugin).register());
        safe("vanish", () -> new VanishCommand(plugin).register());
    }

    private void registerPlayerStats() {
        safe("heal",  () -> new HealCommand(plugin).register());
        safe("feed",  () -> new FeedCommand(plugin).register());
        safe("speed", () -> new SpeedCommand(plugin).register());
    }

    private void registerItems() {
        safe("repair",    () -> new RepairCommand(plugin).register());
        safe("enchant",   () -> new EnchantCommand(plugin).register());
        safe("give",      () -> new GiveCommand(plugin).register());
        safe("hat",       () -> new HatCommand(plugin).register());
        safe("more",      () -> new MoreCommand(plugin).register());
        safe("skull",     () -> new SkullCommand(plugin).register());
        safe("tree",      () -> new TreeCommand(plugin).register());
        safe("itemname",  () -> new ItemNameCommand(plugin).register());
        safe("itemlore",  () -> new ItemLoreCommand(plugin).register());
        safe("clear",     () -> new ClearCommand(plugin).register());
    }

    private void registerWorld() {
        safe("gamemode",    () -> new GamemodeCommand(plugin).register());
        safe("spawnmob",    () -> new SpawnMobCommand(plugin).register());
        safe("enderchest",  () -> new EnderchestCommand(plugin).register());
    }

    /**
     * Все верстаки — через один {@link WorkstationCommand}.
     * Права: {@code terracraft.core.<name>}.
     * Кулдаун: {@code cooldowns.workbench}.
     * Миры: {@code worlds.workbenchDisabledWorlds}.
     */
    private void registerWorkstations() {
        safe("workbench", () -> new WorkstationCommand(plugin, "workbench",
                "wb", MenuType.CRAFTING, LangKey.WORKBENCH_OPENED, LangKey.WORKBENCH_TITLE).register());
        safe("anvil", () -> new WorkstationCommand(plugin, "anvil",
                null, MenuType.ANVIL, LangKey.ANVIL_OPENED, LangKey.ANVIL_TITLE).register());
        safe("loom", () -> new WorkstationCommand(plugin, "loom",
                null, MenuType.LOOM, LangKey.LOOM_OPENED, LangKey.LOOM_TITLE).register());
        safe("grindstone", () -> new WorkstationCommand(plugin, "grindstone",
                null, MenuType.GRINDSTONE, LangKey.GRINDSTONE_OPENED, LangKey.GRINDSTONE_TITLE).register());
        safe("stonecutter", () -> new WorkstationCommand(plugin, "stonecutter",
                "stonecut", MenuType.STONECUTTER, LangKey.STONECUTTER_OPENED, LangKey.STONECUTTER_TITLE).register());
        safe("smithingtable", () -> new WorkstationCommand(plugin, "smithingtable",
                "smithing", MenuType.SMITHING, LangKey.SMITHINGTABLE_OPENED, LangKey.SMITHINGTABLE_TITLE).register());
    }

    private void registerWeatherTime() {
        safe("weather",  () -> new WeatherCommand(plugin).register());
        safe("time",     () -> new TimeCommand(plugin).register());
        safe("ptime",    () -> new PTimeCommand(plugin).register());
        safe("pweather", () -> new PWeatherCommand(plugin).register());
    }

    private void registerMovement() {
        safe("top",  () -> new TopCommand(plugin).register());
        safe("jump", () -> new JumpCommand(plugin).register());
    }

    private void registerPlayerActions() {
        safe("ext",     () -> new ExtCommand(plugin).register());
        safe("suicide", () -> new SuicideCommand(plugin).register());
        safe("burn",    () -> new BurnCommand(plugin).register());
        safe("ice",     () -> new IceCommand(plugin).register());
    }

    private void registerInfo() {
        safe("gc",      () -> new GcCommand(plugin).register());
        safe("getpos",  () -> new GetPosCommand(plugin).register());
        safe("depth",   () -> new DepthCommand(plugin).register());
        safe("compass", () -> new CompassCommand(plugin).register());
        safe("whois",   () -> new WhoisCommand(plugin).register());
        safe("near",    () -> new NearCommand(plugin).register());
        safe("seen",    () -> new SeenCommand(plugin).register());
        safe("list",    () -> new ListCommand(plugin).register());
        safe("ping",    () -> new PingCommand(plugin).register());
    }

    private void registerMail() {
        safe("mail", () -> new MailCommand(plugin).register());
    }
}