package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * /whois <player>
 *
 * Показывает подробную информацию об игроке.
 *
 * Право: terracraft.core.whois
 */
public class WhoisCommand {

    public static final String PERM = "terracraft.core.whois";

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd.MM.yyyy HH:mm");

    private final TerraCorePlugin plugin;

    public WhoisCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("whois")
                .withAliases("playerinfo", "pinfo")
                .withPermission(PERM)
                .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                .executes((sender, args) -> {
                    Player p = (Player) args.get("target");
                    if (p == null) {
                        plugin.msg().send(sender, LangKey.PLAYER_NOT_FOUND, "player", "?");
                        return;
                    }

                    Location l = p.getLocation();
                    String firstJoin = DATE_FORMAT.format(new Date(p.getFirstPlayed()));
                    String lastSeen  = DATE_FORMAT.format(new Date(p.getLastSeen()));
                    String ip = p.getAddress() != null
                            ? p.getAddress().getAddress().getHostAddress()
                            : "?";

                    plugin.msg().sendRaw(sender, LangKey.WHOIS_HEADER);
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_NAME,
                            "value", p.getName());
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_UUID,
                            "value", p.getUniqueId().toString());
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_WORLD,
                            "value", p.getWorld().getName());
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_POSITION,
                            "x", l.getBlockX(),
                            "y", l.getBlockY(),
                            "z", l.getBlockZ());
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_HEALTH,
                            "value", String.format("%.1f", p.getHealth()));
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_FOOD,
                            "value", p.getFoodLevel());
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_GAMEMODE,
                            "value", p.getGameMode().name().toLowerCase());
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_PING,
                            "value", p.getPing());
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_FIRST_JOIN,
                            "value", firstJoin);
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_LAST_SEEN,
                            "value", lastSeen);
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_IP,
                            "value", ip);
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_OP,
                            "value", p.isOp() ? "<green>✔" : "<red>✘");
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_FLYING,
                            "value", p.isFlying() ? "<green>✔" : "<red>✘");
                    plugin.msg().sendRaw(sender, LangKey.WHOIS_HEADER);
                })
                .register();
    }
}