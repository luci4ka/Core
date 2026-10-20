package ru.terracraft.core.storage;

import cc.carm.lib.easysql.api.SQLQuery;
import cc.carm.lib.easysql.api.SQLManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Внутренняя почта (offline-сообщения между игроками).
 *
 * <p>Graceful skip: если MineSQL не загружен — методы тихо возвращают
 * {@code null}/{@code 0}/пустой список.
 */
public class MailService {

    /** Максимальная длина сообщения (символов). */
    public static final int MAX_LENGTH = 512;

    private final TerraCorePlugin plugin;

    public MailService(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    private @NotNull String table() {
        return plugin.storage().prefix() + "mail";
    }

    // ============ Отправка ============

    /**
     * @return {@code true} — отправлено; {@code false} — БД недоступна.
     */
    public boolean send(@NotNull UUID recipient,
                        @NotNull UUID sender,
                        @NotNull String senderName,
                        @NotNull String message) {
        SQLManager sql = plugin.storage().sqlOrNull();
        if (sql == null) return false;

        String q = "INSERT INTO " + table()
                + " (recipient, sender, sender_name, message, sent_at, is_read) "
                + "VALUES (?, ?, ?, ?, ?, FALSE)";
        try {
            sql.executeSQL(q, new Object[]{
                    recipient.toString(),
                    sender.toString(),
                    senderName,
                    message,
                    System.currentTimeMillis()
            });
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Mail.send: " + e.getMessage());
            return false;
        }
    }

    // ============ Чтение ============

    /**
     * Последние {@code limit} писем получателя (свежие — первыми).
     * SQL {@code LIMIT} — БД возвращает только нужное количество строк.
     */
    public @NotNull List<MailEntry> inbox(@NotNull UUID recipient, int limit) {
        List<MailEntry> list = new ArrayList<>();
        SQLManager sql = plugin.storage().sqlOrNull();
        if (sql == null || limit <= 0) return list;

        try (SQLQuery query = sql.createQuery()
                .inTable(table())
                .addCondition("recipient", recipient.toString())
                .orderBy("sent_at", false)
                .setLimit(0, limit)
                .build()
                .execute()) {
            ResultSet rs = query.getResultSet();
            while (rs.next()) {
                list.add(readEntry(rs));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Mail.inbox: " + e.getMessage());
        }
        return list;
    }

    public @Nullable MailEntry find(@NotNull UUID recipient, long id) {
        SQLManager sql = plugin.storage().sqlOrNull();
        if (sql == null) return null;

        try (SQLQuery query = sql.createQuery()
                .inTable(table())
                .addCondition("id", id)
                .addCondition("recipient", recipient.toString())
                .build()
                .execute()) {
            ResultSet rs = query.getResultSet();
            return rs.next() ? readEntry(rs) : null;
        } catch (Exception e) {
            plugin.getLogger().warning("Mail.find: " + e.getMessage());
            return null;
        }
    }

    public int unreadCount(@NotNull UUID recipient) {
        SQLManager sql = plugin.storage().sqlOrNull();
        if (sql == null) return 0;

        try (SQLQuery query = sql.createQuery()
                .inTable(table())
                .addCondition("recipient", recipient.toString())
                .addCondition("is_read", false)
                .build()
                .execute()) {
            ResultSet rs = query.getResultSet();
            int count = 0;
            while (rs.next()) count++;
            return count;
        } catch (Exception e) {
            plugin.getLogger().warning("Mail.unreadCount: " + e.getMessage());
            return 0;
        }
    }

    // ============ Изменение ============

    public void markRead(long id) {
        SQLManager sql = plugin.storage().sqlOrNull();
        if (sql == null) return;

        String q = "UPDATE " + table() + " SET is_read = TRUE WHERE id = ?";
        try {
            sql.executeSQL(q, new Object[]{id});
        } catch (Exception e) {
            plugin.getLogger().warning("Mail.markRead: " + e.getMessage());
        }
    }

    /** Удалить ВСЕ прочитанные письма получателя. @return сколько удалено (или -1). */
    public int clearRead(@NotNull UUID recipient) {
        SQLManager sql = plugin.storage().sqlOrNull();
        if (sql == null) return -1;

        String q = "DELETE FROM " + table() + " WHERE recipient = ? AND is_read = TRUE";
        try {
            Integer deleted = sql.executeSQL(q, new Object[]{recipient.toString()});
            return deleted == null ? 0 : deleted;
        } catch (Exception e) {
            plugin.getLogger().warning("Mail.clearRead: " + e.getMessage());
            return -1;
        }
    }

    // ============ Внутреннее ============

    private @NotNull MailEntry readEntry(@NotNull ResultSet rs) throws Exception {
        return new MailEntry(
                rs.getLong("id"),
                UUID.fromString(rs.getString("sender")),
                rs.getString("sender_name"),
                rs.getString("message"),
                rs.getLong("sent_at"),
                rs.getBoolean("is_read")
        );
    }

    public record MailEntry(
            long id,
            @NotNull UUID senderUuid,
            @NotNull String senderName,
            @NotNull String message,
            long sentAt,
            boolean read
    ) {}
}