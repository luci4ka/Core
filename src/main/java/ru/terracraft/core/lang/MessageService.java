package ru.terracraft.core.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;

import java.time.Duration;
import java.util.Locale;

/**
 * Отдаёт игроку локализованные сообщения.
 *
 * Использование:
 *   msg.send(player, LangKey.GOD_ENABLED_SELF);
 *   msg.actionBar(player, LangKey.GOD_ENABLED_SELF);
 *   msg.sound(player, Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.0f);
 *   msg.confirm(player, LangKey.CONFIRM_REPAIR_HEADER,
 *               "/repair confirm", "/repair cancel", "count", 5);
 *
 * Поведение actionBar / sound / confirm управляется секцией feedback в config.yml:
 *   feedback.action-bar: true|false
 *   feedback.sounds:     true|false
 *   feedback.confirmations: true|false
 */
public class MessageService {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final TerraCorePlugin plugin;
    private final LangManager lang;

    public MessageService(@NotNull TerraCorePlugin plugin, @NotNull LangManager lang) {
        this.plugin = plugin;
        this.lang = lang;
    }

    // ============ Отправка ============

    public void send(@NotNull CommandSender sender, @NotNull LangKey key, Object... kv) {
        Locale locale = (sender instanceof Player p) ? p.locale() : Locale.US;
        String body = lang.raw(locale, key);
        if (body == null || body.isEmpty()) return;

        String prefix = lang.raw(locale, LangKey.PREFIX);
        sender.sendMessage(MM.deserialize((prefix == null ? "" : prefix) + body, resolver(kv)));
    }

    /** Без префикса — для заголовков, bossbar'ов, кнопок. */
    public void sendRaw(@NotNull CommandSender sender, @NotNull LangKey key, Object... kv) {
        Locale locale = (sender instanceof Player p) ? p.locale() : Locale.US;
        String raw = lang.raw(locale, key);
        if (raw == null || raw.isEmpty()) return;
        sender.sendMessage(MM.deserialize(raw, resolver(kv)));
    }

    public @NotNull Component component(@NotNull Player player, @NotNull LangKey key, Object... kv) {
        String raw = lang.raw(player.locale(), key);
        if (raw == null) raw = "<red>?" + key.path() + "?";
        return MM.deserialize(raw, resolver(kv));
    }

    public @NotNull Component componentPrefixed(@NotNull Player player, @NotNull LangKey key, Object... kv) {
        String prefix = lang.raw(player.locale(), LangKey.PREFIX);
        String body   = lang.raw(player.locale(), key);
        if (body == null) body = "<red>?" + key.path() + "?";
        return MM.deserialize((prefix == null ? "" : prefix) + body, resolver(kv));
    }

    public @NotNull MiniMessage mini() {
        return MM;
    }

    // ============ Action bar ============

    /**
     * Отправляет сообщение в action bar (над хотбаром).
     *
     * Если feedback.action-bar = false — падаем на send() в чат.
     * Если sender — не игрок — тоже в чат.
     */
    public void actionBar(@NotNull CommandSender sender, @NotNull LangKey key, Object... kv) {
        // Не игрок — в чат
        if (!(sender instanceof Player player)) {
            send(sender, key, kv);
            return;
        }

        // Отключено в конфиге — в чат
        if (!plugin.config().get().feedback().actionBar()) {
            send(sender, key, kv);
            return;
        }

        Locale locale = player.locale();
        String body = lang.raw(locale, key);
        if (body == null || body.isEmpty()) return;
        player.sendActionBar(MM.deserialize(body, resolver(kv)));
    }

    // ============ Sound ============

    /**
     * Проигрывает звук игроку.
     *
     * Если feedback.sounds = false — тихо игнорируется.
     * Для консоли — игнорируется.
     */
    public void sound(@NotNull CommandSender sender, @NotNull Sound sound,
                      float volume, float pitch) {
        if (!plugin.config().get().feedback().sounds()) return;
        if (sender instanceof Player player) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    // ============ Title ============

    /**
     * Показывает title и subtitle (по центру экрана).
     * Если subtitleKey == null — только title.
     */
    public void title(@NotNull Player player,
                      @NotNull LangKey titleKey,
                      @Nullable LangKey subtitleKey,
                      Object... kv) {
        Component title = MM.deserialize(lang.raw(player.locale(), titleKey) == null
                ? "" : lang.raw(player.locale(), titleKey), resolver(kv));
        Component subtitle = subtitleKey == null
                ? Component.empty()
                : MM.deserialize(lang.raw(player.locale(), subtitleKey) == null
                ? "" : lang.raw(player.locale(), subtitleKey), resolver(kv));

        player.showTitle(Title.title(title, subtitle,
                Title.Times.times(
                        Duration.ofMillis(200),
                        Duration.ofSeconds(2),
                        Duration.ofMillis(500))));
    }

    // ============ Confirm ============

    /**
     * Отправляет игроку подтверждение с кнопками [Да] [Нет].
     *
     * Если feedback.confirmations = false — действие выполняется сразу,
     * без запроса подтверждения (выполняется confirmCmd).
     *
     * @param player       кому показываем
     * @param titleKey     LangKey заголовка (CONFIRM_REPAIR_HEADER)
     * @param confirmCmd   команда для клика по [Да] (например, "/repair confirm")
     * @param cancelCmd    команда для клика по [Нет] (например, "/repair cancel")
     */
    public void confirm(@NotNull Player player,
                        @NotNull LangKey titleKey,
                        @NotNull String confirmCmd,
                        @NotNull String cancelCmd,
                        Object... kv) {
        // Подтверждения выключены — сразу выполняем
        if (!plugin.config().get().feedback().confirmations()) {
            String cmd = confirmCmd.startsWith("/") ? confirmCmd.substring(1) : confirmCmd;
            player.performCommand(cmd);
            return;
        }

        Locale locale = player.locale();

        Component header = MM.deserialize(lang.raw(locale, titleKey), resolver(kv));
        Component yes = MM.deserialize(lang.raw(locale, LangKey.CONFIRM_BUTTON_YES))
                .hoverEvent(HoverEvent.showText(
                        MM.deserialize(lang.raw(locale, LangKey.CONFIRM_BUTTON_YES_HOVER))))
                .clickEvent(ClickEvent.runCommand(confirmCmd));
        Component no = MM.deserialize(lang.raw(locale, LangKey.CONFIRM_BUTTON_NO))
                .hoverEvent(HoverEvent.showText(
                        MM.deserialize(lang.raw(locale, LangKey.CONFIRM_BUTTON_NO_HOVER))))
                .clickEvent(ClickEvent.runCommand(cancelCmd));

        Component line = Component.text()
                .append(header)
                .append(Component.text(" "))
                .append(yes)
                .append(Component.text(" "))
                .append(no)
                .build();

        player.sendMessage(line);
    }

    /** Отправляет подсказку «клик по кнопке» — мелким серым. */
    public void hint(@NotNull Player player, @NotNull LangKey key) {
        Locale locale = player.locale();
        String raw = lang.raw(locale, key);
        if (raw == null || raw.isEmpty()) return;
        player.sendMessage(MM.deserialize(raw));
    }

    // ============ Внутреннее ============

    private @NotNull TagResolver resolver(Object... kv) {
        if (kv.length == 0) return TagResolver.empty();
        if (kv.length % 2 != 0) {
            throw new IllegalArgumentException("Ожидается чётное число аргументов key/value, получено: " + kv.length);
        }
        TagResolver.Builder b = TagResolver.builder();
        for (int i = 0; i < kv.length; i += 2) {
            String name = String.valueOf(kv[i]);
            String value = String.valueOf(kv[i + 1]);
            b.resolver(Placeholder.parsed(name, value));
        }
        return b.build();
    }
}