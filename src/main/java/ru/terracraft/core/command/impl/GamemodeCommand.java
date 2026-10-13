package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

/**
 * /gamemode <режим> [игрок]  (алиасы: /gm)
 *
 * Режимы:
 *   survival | s | 0
 *   creative | c | 1
 *   adventure | a | 2
 *   spectator | sp | 3
 *   back — вернуть предыдущий
 *
 * Права:
 *   terracraft.core.gamemode          — себе
 *   terracraft.core.gamemode.others   — другим
 */
public class GamemodeCommand {

    public static final String PERM_SELF   = "terracraft.core.gamemode";
    public static final String PERM_OTHERS = "terracraft.core.gamemode.others";

    private static final String[] MODES = {
            "survival", "s", "0",
            "creative", "c", "1",
            "adventure", "a", "2",
            "spectator", "sp", "3",
            "back"
    };

    private final TerraCorePlugin plugin;

    public GamemodeCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // /gm <mode> — себе
        new CommandAPICommand("gamemode")
                .withAliases("gm")
                .withPermission(PERM_SELF)
                .withArguments(new MultiLiteralArgument("mode", MODES))
                .executesPlayer((player, args) -> {
                    apply(player, player, (String) args.get("mode"));
                })
                .register();

        // /gm <mode> <target> — другому
        new CommandAPICommand("gamemode")
                .withAliases("gm")
                .withPermission(PERM_OTHERS)
                .withArguments(new MultiLiteralArgument("mode", MODES))
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) return;
                    apply(sender, target, (String) args.get("mode"));
                })
                .register();
    }

    private void apply(@NotNull CommandSender sender,
                       @NotNull Player target,
                       @NotNull String raw) {
        String mode = raw.toLowerCase();

        // ==== /gm back ====
        if (mode.equals("back")) {
            GameMode prev = plugin.gamemodes().popPrevious(target);
            if (prev == null) {
                plugin.msg().send(sender, LangKey.GAMEMODE_INVALID, "value", raw);
                return;
            }
            target.setGameMode(prev);
            plugin.gamemodes().setCurrent(target, prev);

            String modeName = plugin.msg().component(target, langKeyOf(prev)).toString();
            // ^ не сработает — нужен способ подставить Component внутрь другого Component

            // Проще: передать строку локализованного имени
            String localizedName = plugin.lang().raw(target.locale(), langKeyOf(prev));
            if (localizedName == null) localizedName = prev.name().toLowerCase();

            plugin.msg().send(sender, LangKey.GAMEMODE_RESTORED,
                    "player", target.getName(),
                    "value", localizedName);

            if (!target.equals(sender)) {
                plugin.msg().send(target, LangKey.GAMEMODE_RECEIVED, "value", localizedName);
            }
            return;
        }

        // ==== Обычная смена ====
        GameMode parsed = parseMode(mode);
        if (parsed == null) {
            plugin.msg().send(sender, LangKey.GAMEMODE_INVALID, "value", raw);
            return;
        }

        if (target.getGameMode() != parsed) {
            plugin.gamemodes().remember(target, target.getGameMode());
        }
        target.setGameMode(parsed);
        plugin.gamemodes().setCurrent(target, parsed);

        // Локализованное имя режима — берём по локали ПОЛУЧАТЕЛЯ
        String senderName = plugin.lang().raw(senderLocale(sender), langKeyOf(parsed));
        String targetName = plugin.lang().raw(target.locale(), langKeyOf(parsed));

        if (senderName == null) senderName = parsed.name().toLowerCase();
        if (targetName == null) targetName = parsed.name().toLowerCase();

        if (target.equals(sender)) {
            plugin.msg().send(sender, LangKey.GAMEMODE_SET_SELF, "value", senderName);
        } else {
            plugin.msg().send(sender, LangKey.GAMEMODE_SET_OTHER,
                    "player", target.getName(),
                    "value", senderName);
            plugin.msg().send(target, LangKey.GAMEMODE_RECEIVED, "value", targetName);
        }
    }

    /** Локаль отправителя: у игрока — своя, у консоли — en_US. */
    private @NotNull java.util.Locale senderLocale(@NotNull CommandSender sender) {
        return (sender instanceof Player p) ? p.locale() : java.util.Locale.US;
    }

    private @Nullable GameMode parseMode(@NotNull String s) {
        return switch (s) {
            case "survival", "s", "0"    -> GameMode.SURVIVAL;
            case "creative", "c", "1"    -> GameMode.CREATIVE;
            case "adventure", "a", "2"   -> GameMode.ADVENTURE;
            case "spectator", "sp", "3"  -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    /** LangKey с локализованным названием режима. */
    private @NotNull LangKey langKeyOf(@NotNull GameMode mode) {
        return switch (mode) {
            case SURVIVAL  -> LangKey.GAMEMODE_NAME_SURVIVAL;
            case CREATIVE  -> LangKey.GAMEMODE_NAME_CREATIVE;
            case ADVENTURE -> LangKey.GAMEMODE_NAME_ADVENTURE;
            case SPECTATOR -> LangKey.GAMEMODE_NAME_SPECTATOR;
        };
    }
}