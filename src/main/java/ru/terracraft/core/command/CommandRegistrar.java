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
    }

    // ============ Группы ============

    private void registerCore() {
        new TerraCoreCommand(plugin).register();
    }

    private void registerStates() {
        new GodCommand(plugin).register();
        new FlyCommand(plugin).register();
        new VanishCommand(plugin).register();
    }

    private void registerPlayerStats() {
        new HealCommand(plugin).register();
        new FeedCommand(plugin).register();
        new SpeedCommand(plugin).register();
    }

    private void registerItems() {
        new RepairCommand(plugin).register();
        new EnchantCommand(plugin).register();
        new GiveCommand(plugin).register();
        new HatCommand(plugin).register();
        new MoreCommand(plugin).register();
        new SkullCommand(plugin).register();
        new TreeCommand(plugin).register();
        new ItemNameCommand(plugin).register();
        new ItemLoreCommand(plugin).register();
        new ClearCommand(plugin).register();          // ← /clear
    }

    private void registerWorld() {
        new GamemodeCommand(plugin).register();
        new SpawnMobCommand(plugin).register();
        new EnderchestCommand(plugin).register();
    }

    /**
     * Все верстаки — через один {@link WorkstationCommand}.
     * Права: {@code terracraft.core.<name>}.
     * Кулдаун: {@code cooldowns.workbench}.
     * Миры: {@code worlds.workbenchDisabledWorlds}.
     */
    private void registerWorkstations() {
        new WorkstationCommand(plugin, "workbench",
                "wb", MenuType.CRAFTING, LangKey.WORKBENCH_OPENED, LangKey.WORKBENCH_TITLE).register();
        new WorkstationCommand(plugin, "anvil",
                null, MenuType.ANVIL, LangKey.ANVIL_OPENED, LangKey.ANVIL_TITLE).register();
        new WorkstationCommand(plugin, "loom",
                null, MenuType.LOOM, LangKey.LOOM_OPENED, LangKey.LOOM_TITLE).register();
        new WorkstationCommand(plugin, "grindstone",
                null, MenuType.GRINDSTONE, LangKey.GRINDSTONE_OPENED, LangKey.GRINDSTONE_TITLE).register();
        new WorkstationCommand(plugin, "stonecutter",
                "stonecut", MenuType.STONECUTTER, LangKey.STONECUTTER_OPENED, LangKey.STONECUTTER_TITLE).register();
        new WorkstationCommand(plugin, "smithingtable",
                "smithing", MenuType.SMITHING, LangKey.SMITHINGTABLE_OPENED, LangKey.SMITHINGTABLE_TITLE).register();
    }

    private void registerWeatherTime() {
        new WeatherCommand(plugin).register();
        new TimeCommand(plugin).register();
        new PTimeCommand(plugin).register();
        new PWeatherCommand(plugin).register();
    }

    private void registerMovement() {
        new TopCommand(plugin).register();
        new JumpCommand(plugin).register();
    }

    private void registerPlayerActions() {
        new ExtCommand(plugin).register();
        new SuicideCommand(plugin).register();
        new BurnCommand(plugin).register();
        new IceCommand(plugin).register();
    }

    private void registerInfo() {
        new GcCommand(plugin).register();
        new GetPosCommand(plugin).register();
        new DepthCommand(plugin).register();
        new CompassCommand(plugin).register();
        new WhoisCommand(plugin).register();
        new NearCommand(plugin).register();
        new SeenCommand(plugin).register();
        new ListCommand(plugin).register();
        new PingCommand(plugin).register();
    }
}