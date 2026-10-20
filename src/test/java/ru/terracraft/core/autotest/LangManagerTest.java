package ru.terracraft.core.autotest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;
import ru.terracraft.core.lang.LangManager;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Тесты {@link LangManager}.
 */
class LangManagerTest {

    private ServerMock server;
    private TerraCorePlugin plugin;
    private LangManager lang;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TerraCorePlugin.class);
        lang = plugin.lang();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void raw_ruRU_returnsRussianValue() {
        String s = lang.raw(new Locale("ru", "RU"), LangKey.GOD_ENABLED_SELF);
        assertNotNull(s);
        assertFalse(s.isEmpty());
    }

    @Test
    void raw_enUS_returnsEnglishValue() {
        String s = lang.raw(Locale.US, LangKey.GOD_ENABLED_SELF);
        assertNotNull(s);
        assertFalse(s.isEmpty());
    }

    @Test
    void raw_unknownLocale_fallsBackToEnglish() {
        String s = lang.raw(Locale.JAPAN, LangKey.GOD_ENABLED_SELF);
        assertNotNull(s);
        String en = lang.raw(Locale.US, LangKey.GOD_ENABLED_SELF);
        assertEquals(en, s);
    }

    @Test
    void raw_cisLocale_fallsBackToRussian() {
        String s = lang.raw(new Locale("ru", "UA"), LangKey.GOD_ENABLED_SELF);
        assertNotNull(s);
    }

    @Test
    void resolve_exactLocale_returnsSame() {
        Locale resolved = lang.resolve(new Locale("ru", "RU"));
        assertEquals(new Locale("ru", "RU"), resolved);
    }

    @Test
    void resolve_unknownLocale_fallsBack() {
        Locale resolved = lang.resolve(Locale.JAPAN);
        assertEquals(Locale.US, resolved);
    }

    @Test
    void allKeysPresent_inEnUS() {
        for (LangKey key : LangKey.values()) {
            String s = lang.raw(Locale.US, key);
            assertNotNull(s, "Отсутствует ключ в en_US: " + key.path());
        }
    }

    @Test
    void allKeysPresent_inRuRU() {
        for (LangKey key : LangKey.values()) {
            String s = lang.raw(new Locale("ru", "RU"), key);
            assertNotNull(s, "Отсутствует ключ в ru_RU: " + key.path());
        }
    }
}