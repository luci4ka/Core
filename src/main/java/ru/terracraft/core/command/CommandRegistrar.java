package ru.terracraft.core.command;

import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.command.impl.*;

/**
 * Регистрирует все команды через CommandAPI.
 *
 * CommandAPI сам заботится о:
 *   - правах (.withPermission)
 *   - таб-комплите
 *   - парсинге аргументов
 *   - sender-валидации (.executesPlayer)
 *
 * paper-plugin.yml НЕ содержит секцию commands — всё здесь.
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

        // ==== Мир / мобы ====
        new GamemodeCommand(plugin).register();
        new SpawnMobCommand(plugin).register();
        new WorkbenchCommand(plugin).register();
        new EnderchestCommand(plugin).register();
    }
}