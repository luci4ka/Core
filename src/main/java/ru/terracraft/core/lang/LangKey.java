package ru.terracraft.core.lang;

import org.jetbrains.annotations.NotNull;

/**
 * Все ключи сообщений TerraCore.
 *
 * <p>Имя enum = ключ в .lang: {@code NO_PERMISSION → no.permission}
 *
 * <p>Секции:
 * <ol>
 *   <li>Общие</li>
 *   <li>Мир (current / all)</li>
 *   <li>God / Fly / Vanish</li>
 *   <li>Heal / Feed / Speed / Repair</li>
 *   <li>Enchant / Give / SpawnMob</li>
 *   <li>Gamemode</li>
 *   <li>Верстаки / Эндер-сундук / Hat / Clear</li>
 *   <li>Подтверждения</li>
 *   <li>Шансы</li>
 *   <li>Погода / Время / Личное время-погода</li>
 *   <li>Названия пресетов (время, погода)</li>
 *   <li>Сервер / Позиция / Информация</li>
 *   <li>Утилиты (top, jump, more, tree, suicide, burn, ice, ext, skull)</li>
 *   <li>Игроки (whois, seen, near, list, ping)</li>
 *   <li>Предметы (itemname, itemlore)</li>
 *   <li>Направления (compass)</li>
 *   <li>Форматы времени (seen)</li>
 *   <li>Кулдаун</li>
 * </ol>
 */
public enum LangKey {

    // ============ 1. Общие ============
    PREFIX,
    NO_PERMISSION,
    PLAYER_NOT_FOUND,
    INVALID_ARGUMENT,
    USAGE,
    RELOADED,
    RELOAD_FAILED,
    INFO_HEADER,
    INFO_VERSION,
    INFO_SERVER,
    INFO_AUTHORS,

    // ============ 2. Мир (current / all) ============
    /** «текущий» — мир по умолчанию для /time, /weather. */
    CURRENT_WORLD,
    /** «все миры» — для /time all, /weather all. */
    ALL_WORLDS,

    // ============ 3. God / Fly / Vanish ============
    GOD_ENABLED_SELF,
    GOD_ENABLED_OTHER,
    GOD_DISABLED_SELF,
    GOD_DISABLED_OTHER,
    GOD_RECEIVED_ENABLED,
    GOD_RECEIVED_DISABLED,
    /** Локализованное имя godmode — для /suicide в god. */
    GOD_MODE_NAME,

    FLY_ENABLED_SELF,
    FLY_ENABLED_OTHER,
    FLY_DISABLED_SELF,
    FLY_DISABLED_OTHER,
    FLY_NOT_ALLOWED_WORLD,

    VANISH_ENABLED,
    VANISH_DISABLED,
    VANISH_OTHER_ENABLED,
    VANISH_OTHER_DISABLED,
    VANISH_DISABLED_WORLD,
    VANISH_LIST_HEADER,
    VANISH_LIST_EMPTY,
    VANISH_LIST_HOVER,

    // ============ 4. Heal / Feed / Speed / Repair ============
    HEAL_SELF,
    HEAL_OTHER,
    HEAL_RECEIVED,
    HEAL_FAILED,
    HEAL_AMOUNT,

    FEED_SELF,
    FEED_OTHER,
    FEED_RECEIVED,
    FEED_FAILED,
    FEED_AMOUNT,

    SPEED_SET_WALK,
    SPEED_SET_FLY,
    SPEED_RESET,
    SPEED_INVALID_VALUE,

    REPAIR_SUCCESS,
    REPAIR_NO_ITEM,
    REPAIR_FAILED,
    REPAIR_BROKEN,
    REPAIR_SUMMARY_HEADER,
    REPAIR_SUMMARY_REPAIRED,
    REPAIR_SUMMARY_BROKEN,
    REPAIR_SUMMARY_FAILED,

    // ============ 5. Enchant / Give / SpawnMob ============
    ENCHANT_SUCCESS,
    ENCHANT_INVALID,
    ENCHANT_EMPTY_HAND,

    GIVE_SUCCESS,
    GIVE_RECEIVED,

    SPAWNMOB_SUCCESS,
    SPAWNMOB_INVALID_TYPE,

    // ============ 6. Gamemode ============
    GAMEMODE_SET_SELF,
    GAMEMODE_SET_OTHER,
    GAMEMODE_RECEIVED,
    GAMEMODE_INVALID,
    GAMEMODE_RESTORED,
    GAMEMODE_NAME_SURVIVAL,
    GAMEMODE_NAME_CREATIVE,
    GAMEMODE_NAME_ADVENTURE,
    GAMEMODE_NAME_SPECTATOR,

    // ============ 7. Верстаки / Эндер-сундук / Hat / Clear ============
    WORKBENCH_OPENED,
    WORKBENCH_DISABLED_WORLD,
    WORKBENCH_TITLE,
    ANVIL_OPENED,
    ANVIL_TITLE,
    LOOM_OPENED,
    LOOM_TITLE,
    GRINDSTONE_OPENED,
    GRINDSTONE_TITLE,
    STONECUTTER_OPENED,
    STONECUTTER_TITLE,
    SMITHINGTABLE_OPENED,
    SMITHINGTABLE_TITLE,

    ENDERCHEST_OPENED_SELF,
    ENDERCHEST_OPENED_OTHER,
    ENDERCHEST_OPENED_FOR_TARGET,
    ENDERCHEST_DISABLED_WORLD,

    HAT_SUCCESS,
    HAT_NO_ITEM,
    HAT_RETURNED,
    HAT_REMOVED,
    HAT_CANNOT_REMOVE,
    /** «На голову можно надеть только блок». */
    HAT_NOT_BLOCK,

    CLEAR_SUCCESS,

    // ============ 8. Подтверждения ============
    CONFIRM_BUTTON_YES,
    CONFIRM_BUTTON_NO,
    CONFIRM_BUTTON_YES_HOVER,
    CONFIRM_BUTTON_NO_HOVER,
    CONFIRM_HINT,
    CONFIRM_EXPIRED,
    CONFIRM_CANCELLED,
    CONFIRM_REPAIR_HEADER,
    CONFIRM_HEAL_HEADER,
    CONFIRM_FEED_HEADER,

    // ============ 9. Шансы ============
    CHANCE_REPAIR_WARN,
    CHANCE_HEAL_WARN,
    CHANCE_FEED_WARN,

    // ============ 10. Погода / Время / Личное ============
    WEATHER_SET,
    WEATHER_LOCKED,
    WEATHER_UNLOCKED,

    TIME_SET,
    TIME_ADDED,
    TIME_FROZEN,
    TIME_UNFROZEN,

    PTIME_SET,
    PTIME_RESET,
    PWEATHER_SET,
    PWEATHER_RESET,

    // ============ 11. Названия пресетов ============
    TIME_NAME_DAY,
    TIME_NAME_NOON,
    TIME_NAME_NIGHT,
    TIME_NAME_MIDNIGHT,
    TIME_NAME_DAWN,
    TIME_NAME_DUSK,

    WEATHER_NAME_SUN,
    WEATHER_NAME_RAIN,
    WEATHER_NAME_STORM,

    // ============ 12. Сервер / Позиция / Информация ============
    GC_HEADER,
    GC_SECTION_SERVER,
    GC_SECTION_MEMORY,
    GC_SECTION_WORLD,

    GC_TPS,
    GC_TPS_GOOD,
    GC_TPS_MEDIUM,
    GC_TPS_BAD,

    GC_MEMORY,
    GC_CHUNKS,
    GC_ENTITIES,
    GC_UPTIME,

    GC_MB,        // «МБ» / «MB»
    GC_PERCENT,   // «%» / «%»

    GETPOS_SELF,
    GETPOS_OTHER,
    DEPTH_SELF,
    COMPASS_SELF,

    // ============ 13. Утилиты ============
    TOP_SUCCESS,
    TOP_NO_BLOCK,

    JUMP_SUCCESS,
    JUMP_NO_TARGET,

    EXT_SUCCESS,
    EXT_OTHER,
    EXT_NOT_BURNING,

    MORE_SUCCESS,
    MORE_NO_ITEM,
    MORE_ALREADY_FULL,

    SKULL_SUCCESS,

    TREE_SUCCESS,
    TREE_INVALID,

    SUICIDE_SUCCESS,
    SUICIDE_DENIED,

    BURN_SUCCESS,
    BURN_OTHER,

    ICE_SUCCESS,
    ICE_OTHER,

    // ============ 14. Игроки (whois / seen / near / list / ping) ============
    WHOIS_HEADER,
    WHOIS_SECTION_MAIN,
    WHOIS_SECTION_POSITION,
    WHOIS_SECTION_STATUS,
    WHOIS_NAME,
    WHOIS_UUID,
    WHOIS_WORLD,
    WHOIS_POSITION,
    WHOIS_HEALTH,
    WHOIS_FOOD,
    WHOIS_GAMEMODE,
    WHOIS_PING,
    WHOIS_FIRST_JOIN,
    WHOIS_LAST_SEEN,
    WHOIS_IP,
    WHOIS_OP,
    WHOIS_FLYING,
    WHOIS_ONLINE_NOW,
    WHOIS_UNKNOWN,
    WHOIS_TRUE,
    WHOIS_FALSE,

    SEEN_HEADER,
    SEEN_ONLINE,
    SEEN_RECENT,
    SEEN_LONG_AGO,
    SEEN_LAST_JOIN,
    SEEN_UNKNOWN,
    SEEN_JUST_NOW,

    NEAR_HEADER,
    NEAR_ENTRY,
    NEAR_EMPTY,

    LIST_HEADER,
    LIST_ENTRY,
    LIST_EMPTY,

    PING_SELF,
    PING_OTHER,

    // Цветовые пороги пинга — self
    PING_SELF_GOOD,
    PING_SELF_MEDIUM,
    PING_SELF_BAD,

    // Цветовые пороги пинга — other
    PING_OTHER_GOOD,
    PING_OTHER_MEDIUM,
    PING_OTHER_BAD,

    // ============ 15. Предметы ============
    ITEMNAME_SUCCESS,
    ITEMNAME_REMOVED,
    ITEMLORE_ADDED,
    ITEMLORE_CLEARED,
    ITEMLORE_LINE,
    ITEM_INVALID_HAND,

    // ============ 16. Направления (compass) ============
    DIRECTION_NORTH,
    DIRECTION_SOUTH,
    DIRECTION_EAST,
    DIRECTION_WEST,

    // ============ 17. Форматы времени (seen) ============
    TIME_FORMAT_DAYS,
    TIME_FORMAT_HOURS,
    TIME_FORMAT_MINUTES,
    TIME_FORMAT_SECONDS,

    // ============ 18. Кулдаун ============
    COOLDOWN_ACTIVE;

    /**
     * Ключ в .lang файле: {@code GOD_ENABLED_SELF → god.enabled.self}.
     */
    public @NotNull String path() {
        return name().toLowerCase().replace('_', '.');
    }
}