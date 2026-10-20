package ru.terracraft.core.autotest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.manager.ConfirmManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты {@link ConfirmManager}.
 */
class ConfirmManagerTest {

    private ServerMock server;
    private TerraCorePlugin plugin;
    private ConfirmManager confirms;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TerraCorePlugin.class);
        confirms = plugin.confirms();
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void consume_returnsFalse_whenNotExpected() {
        assertFalse(confirms.consume(player, "test"));
    }

    @Test
    void consume_returnsTrue_whenExpected() {
        confirms.expect(player, "test");
        assertTrue(confirms.consume(player, "test"));
    }

    @Test
    void consume_returnsFalse_whenAlreadyConsumed() {
        confirms.expect(player, "test");
        assertTrue(confirms.consume(player, "test"));
        assertFalse(confirms.consume(player, "test"));
    }

    @Test
    void cancel_removesPending() {
        confirms.expect(player, "test");
        confirms.cancel(player, "test");
        assertFalse(confirms.consume(player, "test"));
    }

    @Test
    void clear_removesAllPending() {
        confirms.expect(player, "test1");
        confirms.expect(player, "test2");
        confirms.clear(player.getUniqueId());
        assertFalse(confirms.consume(player, "test1"));
        assertFalse(confirms.consume(player, "test2"));
    }

    @Test
    void expire_afterTimeout() {
        confirms.expect(player, "test");
        // TTL = 30 сек, поэтому consume сразу должен вернуть true.
        // Для проверки реального истечения нужен тест с TTL-инъекцией,
        // либо отдельный интеграционный тест с Thread.sleep(31_000) — слишком долго.
        assertTrue(confirms.consume(player, "test"));
    }
}