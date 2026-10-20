package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GamemodeCommand {

    public static final String PERM_SELF   = "terracraft.core.gamemode";
    public static final String PERM_OTHERS = "terracraft.core.gamemode.others";

    private static final Map<String, GameMode> MODES = new LinkedHashMap<>();

    static {
        MODES.put("survival",  GameMode.SURVIVAL);
        MODES.put("s",         GameMode.SURVIVAL);
        MODES.put("0",         GameMode.SURVIVAL);
        MODES.put("creative",  GameMode.CREATIVE);
        MODES.put("c",         GameMode.CREATIVE);
        MODES.put("1",         GameMode.CREATIVE);
        MODES.put("adventure", GameMode.ADVENTURE);
        MODES.put("a",         GameMode.ADVENTURE);
        MODES.put("2",         GameMode.ADVENTURE);
        MODES.put("spectator", GameMode.SPECTATOR);
        MODES.put("sp",        GameMode.SPECTATOR);
        MODES.put("3",         GameMode.SPECTATOR);
    }

    private static final String[] MODE_ARGS;

    static {
        MODE_ARGS = new String[MODES.size() + 1];
        int i = 0;
        for (String key : MODES.keySet()) MODE_ARGS[i++] = key;
        MODE_ARGS[i] = "back";
    }

    private final TerraCorePlugin plugin;

    public GamemodeCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("gamemode")
                .withAliases("gm")
                .withPermission(PERM_SELF)
                .withArguments(new MultiLiteralArgument("mode", MODE_ARGS))
                .executesPlayer((player, args) -> {
                    apply(player, player, (String) args.get("mode"));
                })
                .register();

        new CommandAPICommand("gamemode")
                .withAliases("gm")
                .withPermission(PERM_OTHERS)
                .withArguments(new MultiLiteralArgument("mode", MODE_ARGS))
                .withArguments(new StringArgument("target")
                        .replaceSuggestions((info, builder) -> {
                            String prefix = info.currentInput().toLowerCase(Locale.ROOT);
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                                    builder.suggest(p.getName());
                                }
                            }
                            return CompletableFuture.completedFuture(builder.build());
                        }))
                .executes((sender, args) -> {
                    String name = (String) args.get("target");
                    Player target = Bukkit.getPlayerExact(name);
                    if (target == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", name);
                        return;
                    }
                    apply(sender, target, (String) args.get("mode"));
                })
                .register();
    }

    private void apply(@NotNull CommandSender sender,
                       @NotNull Player target,
                       @NotNull String raw) {
        String mode = raw.toLowerCase(Locale.ROOT);

        // ==== /gm back ====
        if (mode.equals("back")) {
            GameMode prev = plugin.gamemodes().popPrevious(target);
            if (prev == null) {
                plugin.msg().send(sender, LangKey.GAMEMODE_INVALID, "value", raw);
                return;
            }
            target.setGameMode(prev);
            plugin.gamemodes().setCurrent(target, prev);

            String localizedName = plugin.lang().raw(target.locale(), langKeyOf(prev));
            if (localizedName == null) localizedName = prev.name().toLowerCase(Locale.ROOT);

            if (!target.equals(sender)) {
                String senderName = plugin.lang().raw(senderLocale(sender), langKeyOf(prev));
                if (senderName == null) senderName = prev.name().toLowerCase(Locale.ROOT);

                plugin.msg().send(sender, LangKey.GAMEMODE_RESTORED,
                        "player", target.getName(),
                        "value", senderName);
                plugin.msg().send(target, LangKey.GAMEMODE_RECEIVED, "value", localizedName);
            } else {
                plugin.msg().send(sender, LangKey.GAMEMODE_RESTORED,
                        "player", target.getName(),
                        "value", localizedName);
            }

            plugin.msg().sound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
            return;
        }

        // ==== Обычная смена ====
        GameMode parsed = MODES.get(mode);
        if (parsed == null) {
            plugin.msg().send(sender, LangKey.GAMEMODE_INVALID, "value", raw);
            return;
        }

        if (target.getGameMode() != parsed) {
            plugin.gamemodes().remember(target, target.getGameMode());
        }
        target.setGameMode(parsed);
        plugin.gamemodes().setCurrent(target, parsed);

        String senderName = plugin.lang().raw(senderLocale(sender), langKeyOf(parsed));
        String targetName = plugin.lang().raw(target.locale(), langKeyOf(parsed));

        if (senderName == null) senderName = parsed.name().toLowerCase(Locale.ROOT);
        if (targetName == null) targetName = parsed.name().toLowerCase(Locale.ROOT);

        if (target.equals(sender)) {
            plugin.msg().send(sender, LangKey.GAMEMODE_SET_SELF, "value", senderName);
        } else {
            plugin.msg().send(sender, LangKey.GAMEMODE_SET_OTHER,
                    "player", target.getName(),
                    "value", senderName);
            plugin.msg().send(target, LangKey.GAMEMODE_RECEIVED, "value", targetName);
        }

        plugin.msg().sound(target, Sound.BLOCK_BEACON_ACTIVATE, 0.6f, 1.2f);

        if (sender instanceof Player p) {
            plugin.stats().increment(p, "gamemode");
        }
    }

    private @NotNull Locale senderLocale(@NotNull CommandSender sender) {
        return (sender instanceof Player p) ? p.locale() : Locale.US;
    }

    private @NotNull LangKey langKeyOf(@NotNull GameMode mode) {
        return switch (mode) {
            case SURVIVAL  -> LangKey.GAMEMODE_NAME_SURVIVAL;
            case CREATIVE  -> LangKey.GAMEMODE_NAME_CREATIVE;
            case ADVENTURE -> LangKey.GAMEMODE_NAME_ADVENTURE;
            case SPECTATOR -> LangKey.GAMEMODE_NAME_SPECTATOR;
        };
    }
}