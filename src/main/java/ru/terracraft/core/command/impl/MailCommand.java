package ru.terracraft.core.command.impl;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerProfileArgument;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;
import ru.terracraft.core.storage.MailService;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * /mail — внутренняя почта.
 *
 * <pre>
 *   /mail send &lt;player&gt; &lt;message&gt;
 *   /mail inbox
 *   /mail read &lt;id&gt;
 *   /mail clear
 *   /mail unread
 * </pre>
 */
public class MailCommand {

    public static final String PERM        = "terracraft.core.mail";
    public static final String PERM_CLEAR  = "terracraft.core.mail.clear";
    public static final String PERM_OTHERS = "terracraft.core.mail.others";

    private static final int INBOX_LIMIT = 10;

    private final TerraCorePlugin plugin;

    public MailCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("mail")
                .withPermission(PERM)
                .withSubcommand(sendCmd())
                .withSubcommand(inboxCmd())
                .withSubcommand(readCmd())
                .withSubcommand(clearCmd())
                .withSubcommand(unreadCmd())
                .register();
    }

    // ============ /mail send <player> <message> ============

    private @NotNull CommandAPICommand sendCmd() {
        return new CommandAPICommand("send")
                .withArguments(new PlayerProfileArgument("player"))
                .withArguments(new GreedyStringArgument("message"))
                .executesPlayer((PlayerCommandExecutor) (player, args) -> {
                    @SuppressWarnings("unchecked")
                    List<PlayerProfile> profiles = (List<PlayerProfile>) args.get("player");
                    String message = ((String) args.get("message")).trim();

                    if (profiles == null || profiles.isEmpty()) {
                        plugin.msg().send(player, LangKey.PLAYER_NOT_FOUND, "player", "?");
                        return;
                    }

                    PlayerProfile profile = profiles.get(0);
                    UUID targetUuid = profile.getUniqueId();
                    String targetName = profile.getName();

                    if (targetName == null) targetName = "?";

                    if (targetUuid.equals(player.getUniqueId())) {
                        plugin.msg().send(player, LangKey.MAIL_SELF);
                        return;
                    }
                    if (message.isEmpty()) {
                        plugin.msg().send(player, LangKey.INVALID_ARGUMENT, "value", "message");
                        return;
                    }
                    if (message.length() > MailService.MAX_LENGTH) {
                        plugin.msg().send(player, LangKey.MAIL_TOO_LONG,
                                "value", MailService.MAX_LENGTH);
                        return;
                    }

                    boolean ok = plugin.mail().send(
                            targetUuid,
                            player.getUniqueId(),
                            player.getName(),
                            message
                    );
                    if (!ok) {
                        plugin.msg().send(player, LangKey.RELOAD_FAILED);
                        return;
                    }

                    plugin.msg().send(player, LangKey.MAIL_SENT, "player", targetName);
                    plugin.msg().logAction(player, "/mail send",
                            "to", targetName, "len", message.length());

                    // Если получатель онлайн — уведомить сразу
                    Player online = Bukkit.getPlayer(targetUuid);
                    if (online != null) {
                        plugin.msg().send(online, LangKey.MAIL_RECEIVED,
                                "value", plugin.mail().unreadCount(online.getUniqueId()));
                    }
                });
    }

    // ============ /mail inbox ============

    private @NotNull CommandAPICommand inboxCmd() {
        return new CommandAPICommand("inbox")
                .executesPlayer((PlayerCommandExecutor) (player, args) ->
                        showInbox(player, player.getUniqueId()));
    }

    // ============ /mail read <id> ============

    private @NotNull CommandAPICommand readCmd() {
        return new CommandAPICommand("read")
                .withArguments(new IntegerArgument("id", 1))
                .executesPlayer((PlayerCommandExecutor) (player, args) -> {
                    int idInt = (int) args.get("id");
                    long id = idInt;
                    MailService.MailEntry entry = plugin.mail().find(player.getUniqueId(), id);
                    if (entry == null) {
                        plugin.msg().send(player, LangKey.MAIL_NOT_FOUND, "value", id);
                        return;
                    }

                    if (!entry.read()) plugin.mail().markRead(entry.id());

                    plugin.msg().sendRaw(player, LangKey.MAIL_INBOX_ENTRY,
                            "id", entry.id(),
                            "player", entry.senderName(),
                            "time", formatTime(player, entry.sentAt()),
                            "message", entry.message());
                });
    }

    // ============ /mail clear ============

    private @NotNull CommandAPICommand clearCmd() {
        return new CommandAPICommand("clear")
                .withPermission(PERM_CLEAR)
                .executesPlayer((PlayerCommandExecutor) (player, args) -> {
                    int deleted = plugin.mail().clearRead(player.getUniqueId());
                    if (deleted < 0) {
                        plugin.msg().send(player, LangKey.RELOAD_FAILED);
                        return;
                    }
                    plugin.msg().send(player, LangKey.MAIL_CLEARED, "value", deleted);
                    plugin.msg().logAction(player, "/mail clear", "removed", deleted);
                });
    }

    // ============ /mail unread ============

    private @NotNull CommandAPICommand unreadCmd() {
        return new CommandAPICommand("unread")
                .executesPlayer((PlayerCommandExecutor) (player, args) -> {
                    int count = plugin.mail().unreadCount(player.getUniqueId());
                    plugin.msg().send(player, LangKey.MAIL_RECEIVED, "value", count);
                });
    }

    // ============ Показ inbox ============

    private void showInbox(@NotNull Player player, @NotNull UUID owner) {
        List<MailService.MailEntry> list = plugin.mail().inbox(owner, INBOX_LIMIT);

        plugin.msg().sendRaw(player, LangKey.MAIL_INBOX_HEADER);
        if (list.isEmpty()) {
            plugin.msg().sendRaw(player, LangKey.MAIL_INBOX_EMPTY);
            plugin.msg().sendRaw(player, LangKey.MAIL_INBOX_HEADER);
            return;
        }

        for (MailService.MailEntry e : list) {
            plugin.msg().sendRaw(player, LangKey.MAIL_INBOX_ENTRY,
                    "id", e.id(),
                    "player", e.senderName(),
                    "time", formatTime(player, e.sentAt()),
                    "message", e.message());
        }
        plugin.msg().sendRaw(player, LangKey.MAIL_INBOX_HEADER);
    }

    // ============ Утилиты ============

    private @NotNull String formatTime(@Nullable CommandSender sender, long millis) {
        long diff = System.currentTimeMillis() - millis;
        if (diff < 0) diff = 0;

        Duration d = Duration.ofMillis(diff);
        long days = d.toDays();
        long hours = d.toHoursPart();
        long minutes = d.toMinutesPart();

        if (days > 0)    return days + "d " + hours + "h";
        if (hours > 0)   return hours + "h " + minutes + "m";
        if (minutes > 0) return minutes + "m";
        return "<1m";
    }
}