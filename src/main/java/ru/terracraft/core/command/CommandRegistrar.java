package ru.terracraft.core.command;

import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.impl.*;

/**
 * Регистрация всех команд TerraCore через CommandAPI.
 */
public class CommandRegistrar {

    private final TerraCorePlugin plugin;

    public CommandRegistrar(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        // ==== Управление ====
        new TerraCoreCommand(plugin).register();

        // ==== Состояния ====
        new GodCommand(plugin).register();
        new FlyCommand(plugin).register();
        new VanishCommand(plugin).register();

        // ==== Здоровье / еда / скорость ====
        new HealCommand(plugin).register();
        new FeedCommand(plugin).register();
        new SpeedCommand(plugin).register();

        // ==== Предметы ====
        new RepairCommand(plugin).register();
        new EnchantCommand(plugin).register();
        new GiveCommand(plugin).register();
        new HatCommand(plugin).register();
        new MoreCommand(plugin).register();
        new SkullCommand(plugin).register();
        new TreeCommand(plugin).register();
        new ItemNameCommand(plugin).register();
        new ItemLoreCommand(plugin).register();

        // ==== Мир / мобы ====
        new GamemodeCommand(plugin).register();
        new SpawnMobCommand(plugin).register();
        new WorkbenchCommand(plugin).register();
        new EnderchestCommand(plugin).register();

        // ==== Погода / время ====
        new WeatherCommand(plugin).register();
        new TimeCommand(plugin).register();
        new PTimeCommand(plugin).register();
        new PWeatherCommand(plugin).register();

        // ==== Перемещение ====
        new TopCommand(plugin).register();
        new JumpCommand(plugin).register();

        // ==== Действия над игроком ====
        new ExtCommand(plugin).register();
        new SuicideCommand(plugin).register();
        new BurnCommand(plugin).register();
        new IceCommand(plugin).register();

        // ==== Информация ====
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