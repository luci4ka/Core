package ru.terracraft.core.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;

import java.time.Duration;
import java.util.Locale;

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
        Locale locale = localeOf(sender);
        String body = lang.raw(locale, key);
        if (body == null || body.isEmpty()) return;

        String prefix = lang.raw(locale, LangKey.PREFIX);
        Component msg = MM.deserialize((prefix == null ? "" : prefix) + body, resolver(kv));

        runOnMainThread(() -> sender.sendMessage(msg));
    }

    public void sendRaw(@NotNull CommandSender sender, @NotNull LangKey key, Object... kv) {
        Locale locale = localeOf(sender);
        String raw = lang.raw(locale, key);
        if (raw == null || raw.isEmpty()) return;

        Component msg = MM.deserialize(raw, resolver(kv));
        runOnMainThread(() -> sender.sendMessage(msg));
    }

    public @NotNull Component component(@NotNull Player player, @NotNull LangKey key, Object... kv) {
        String raw = lang.raw(player.locale(), key);
        if (raw == null) raw = "<red>?" + key.path() + "?";
        return MM.deserialize(raw, resolver(kv));
    }

    /**
     * Резолвит {@link LangKey} в {@link Component} по локали сендера.
     * Для {@link Player} — его локаль, иначе {@link Locale#US}.
     *
     * <p>Полезно для мест, где нет {@link Player} (консоль, команды с {@code CommandSender}),
     * но нужен локализованный {@link Component} — например, hover-текст в {@code VanishCommand}.
     */
    public @NotNull Component componentAny(@NotNull CommandSender sender,
                                           @NotNull LangKey key,
                                           Object... kv) {
        Locale locale = localeOf(sender);
        String raw = lang.raw(locale, key);
        if (raw == null) raw = "<red>?" + key.path() + "?";
        return MM.deserialize(raw, resolver(kv));
    }

    public @NotNull Component componentPrefixed(@NotNull Player player, @NotNull LangKey key, Object... kv) {
        return Component.text()
                .append(lang.component(player, LangKey.PREFIX))
                .append(lang.component(player, key, kv))
                .build();
    }

    public @NotNull MiniMessage mini() {
        return MM;
    }

    // ============ Action bar ============

    public void actionBar(@NotNull CommandSender sender, @NotNull LangKey key, Object... kv) {
        if (!(sender instanceof Player player)) {
            send(sender, key, kv);
            return;
        }

        if (!plugin.config().get().feedback().actionBar()) {
            send(sender, key, kv);
            return;
        }

        Locale locale = player.locale();
        String body = lang.raw(locale, key);
        if (body == null || body.isEmpty()) return;

        Component msg = MM.deserialize(body, resolver(kv));
        runOnMainThread(() -> player.sendActionBar(msg));
    }

    // ============ Sound ============

    public void sound(@NotNull CommandSender sender, @NotNull Sound sound,
                      float volume, float pitch) {
        if (!plugin.config().get().feedback().sounds()) return;
        if (!(sender instanceof Player player)) return;

        runOnMainThread(() ->
                player.playSound(player.getLocation(), sound, volume, pitch));
    }

    // ============ Title ============

    public void title(@NotNull Player player,
                      @NotNull LangKey titleKey,
                      @Nullable LangKey subtitleKey,
                      Object... kv) {
        String rawTitle = lang.raw(player.locale(), titleKey);
        String rawSub   = subtitleKey == null ? null : lang.raw(player.locale(), subtitleKey);

        Component title = MM.deserialize(rawTitle == null ? "" : rawTitle, resolver(kv));
        Component subtitle = rawSub == null
                ? Component.empty()
                : MM.deserialize(rawSub, resolver(kv));

        runOnMainThread(() -> player.showTitle(Title.title(title, subtitle,
                Title.Times.times(
                        Duration.ofMillis(200),
                        Duration.ofSeconds(2),
                        Duration.ofMillis(500)))));
    }

    // ============ Confirm ============

    public void confirm(@NotNull Player player,
                        @NotNull LangKey titleKey,
                        @NotNull String confirmCmd,
                        @NotNull String cancelCmd,
                        Object... kv) {
        if (!plugin.config().get().feedback().confirmations()) {
            String cmd = confirmCmd.startsWith("/") ? confirmCmd.substring(1) : confirmCmd;
            runOnMainThread(() -> player.performCommand(cmd));
            return;
        }

        Locale locale = player.locale();
        String rawHeader = lang.raw(locale, titleKey);
        String rawYes    = lang.raw(locale, LangKey.CONFIRM_BUTTON_YES);
        String rawNo     = lang.raw(locale, LangKey.CONFIRM_BUTTON_NO);
        String rawYesH   = lang.raw(locale, LangKey.CONFIRM_BUTTON_YES_HOVER);
        String rawNoH    = lang.raw(locale, LangKey.CONFIRM_BUTTON_NO_HOVER);

        // Fallback: если ключа нет в .lang — игрок всё равно должен увидеть кнопки
        if (rawHeader == null) {
            plugin.getLogger().warning("confirm(): нет ключа в .lang для " + titleKey
                    + " — используется fallback.");
            rawHeader = "<yellow>Подтвердите действие:</yellow>";
        }
        if (rawYes == null) rawYes = "<green><bold>[✔ Да]</bold></green>";
        if (rawNo == null)  rawNo  = "<red><bold>[✘ Нет]</bold></red>";
        // hover'ы — опциональные, без fallback (пустая строка — валидно)

        Component header = MM.deserialize(rawHeader, resolver(kv));
        Component yes = MM.deserialize(rawYes)
                .hoverEvent(HoverEvent.showText(
                        MM.deserialize(rawYesH == null ? "" : rawYesH)))
                .clickEvent(ClickEvent.runCommand(confirmCmd));
        Component no = MM.deserialize(rawNo)
                .hoverEvent(HoverEvent.showText(
                        MM.deserialize(rawNoH == null ? "" : rawNoH)))
                .clickEvent(ClickEvent.runCommand(cancelCmd));

        Component line = Component.text()
                .append(header)
                .append(Component.text(" "))
                .append(yes)
                .append(Component.text(" "))
                .append(no)
                .build();

        runOnMainThread(() -> player.sendMessage(line));
    }

    public void hint(@NotNull Player player, @NotNull LangKey key) {
        Locale locale = player.locale();
        String raw = lang.raw(locale, key);
        if (raw == null || raw.isEmpty()) return;

        Component msg = MM.deserialize(raw);
        runOnMainThread(() -> player.sendMessage(msg));
    }

    // ============ Внутреннее ============

    private void runOnMainThread(@NotNull Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    private @NotNull Locale localeOf(@NotNull CommandSender sender) {
        return (sender instanceof Player p) ? p.locale() : Locale.US;
    }

    /**
     * Резолвер плейсхолдеров. Поддерживает как строки, так и {@link Component}
     * (для {@code Component.translatable(...)} — имена предметов/сущностей).
     */
    private @NotNull TagResolver resolver(Object... kv) {
        if (kv.length == 0) return TagResolver.empty();
        if (kv.length % 2 != 0) {
            throw new IllegalArgumentException("Ожидается чётное число аргументов key/value, получено: " + kv.length);
        }
        TagResolver.Builder b = TagResolver.builder();
        for (int i = 0; i < kv.length; i += 2) {
            String name = String.valueOf(kv[i]);
            Object value = kv[i + 1];

            if (value instanceof Component c) {
                b.resolver(Placeholder.component(name, c));
            } else {
                b.resolver(Placeholder.parsed(name, String.valueOf(value)));
            }
        }
        return b.build();
    }

    // ============ Логирование ============

    public void logAction(@NotNull CommandSender sender,
                          @NotNull String action,
                          Object... kv) {
        if (!plugin.config().get().general().logAdminActions()) return;

        String who = (sender instanceof Player p) ? p.getName() : "CONSOLE";
        String details = formatKv(kv);
        plugin.getLogger().info("[AdminTools] " + who + " → " + action
                + (details.isEmpty() ? "" : " (" + details + ")"));
    }

    private @NotNull String formatKv(Object... kv) {
        if (kv.length == 0) return "";
        if (kv.length % 2 != 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kv.length; i += 2) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(kv[i]).append('=').append(kv[i + 1]);
        }
        return sb.toString();
    }
}