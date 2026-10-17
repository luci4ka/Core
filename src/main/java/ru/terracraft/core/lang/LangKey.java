package ru.terracraft.core.lang;

import org.jetbrains.annotations.NotNull;

/**
 * Все ключи сообщений TerraCore.
 * Имя enum = ключ в .lang: NO_PERMISSION → no.permission
 *
 * Если добавляешь новое сообщение — сначала добавь сюда, потом в .lang файлы.
 */
public enum LangKey {

    // ============ Общие ============
    PREFIX,
    NO_PERMISSION,
    PLAYER_ONLY,
    PLAYER_NOT_FOUND,
    PLAYER_OFFLINE,
    INVALID_ARGUMENT,
    USAGE,
    RELOADED,
    RELOAD_FAILED,

    // ============ God ============
    GOD_ENABLED_SELF,
    GOD_ENABLED_OTHER,
    GOD_DISABLED_SELF,
    GOD_DISABLED_OTHER,
    GOD_RECEIVED_ENABLED,
    GOD_RECEIVED_DISABLED,

    // ============ Fly ============
    FLY_ENABLED_SELF,
    FLY_ENABLED_OTHER,
    FLY_DISABLED_SELF,
    FLY_DISABLED_OTHER,
    FLY_NOT_ALLOWED_WORLD,

    // ============ Heal ============
    HEAL_SELF,
    HEAL_OTHER,
    HEAL_RECEIVED,
    HEAL_FAILED,
    HEAL_AMOUNT,

    // ============ Feed ============
    FEED_SELF,
    FEED_OTHER,
    FEED_RECEIVED,
    FEED_FAILED,
    FEED_AMOUNT,

    // ============ Speed ============
    SPEED_SET_WALK,
    SPEED_SET_FLY,
    SPEED_RESET,
    SPEED_INVALID_VALUE,

    // ============ Repair ============
    REPAIR_SUCCESS,
    REPAIR_NO_ITEM,
    REPAIR_FAILED,
    REPAIR_BROKEN,
    REPAIR_SUMMARY_HEADER,
    REPAIR_SUMMARY_REPAIRED,
    REPAIR_SUMMARY_BROKEN,
    REPAIR_SUMMARY_FAILED,

    // ============ Enchant / Give ============
    ENCHANT_SUCCESS,
    ENCHANT_INVALID,
    GIVE_SUCCESS,
    GIVE_RECEIVED,

    // ============ SpawnMob ============
    SPAWNMOB_SUCCESS,
    SPAWNMOB_INVALID_TYPE,
    SPAWNMOB_LIMIT_REACHED,

    // ============ Vanish ============
    VANISH_ENABLED,
    VANISH_DISABLED,
    VANISH_OTHER_ENABLED,
    VANISH_OTHER_DISABLED,
    VANISH_DISABLED_WORLD,
    VANISH_LIST_HEADER,
    VANISH_LIST_ENTRY,
    VANISH_LIST_EMPTY,

    // ============ Gamemode ============
    GAMEMODE_SET_SELF,
    GAMEMODE_SET_OTHER,
    GAMEMODE_RECEIVED,
    GAMEMODE_INVALID,
    GAMEMODE_RESTORED,
    GAMEMODE_NAME_SURVIVAL,
    GAMEMODE_NAME_CREATIVE,
    GAMEMODE_NAME_ADVENTURE,
    GAMEMODE_NAME_SPECTATOR,

    // ============ Workbench / Enderchest / Hat ============
    WORKBENCH_OPENED,
    WORKBENCH_DISABLED_WORLD,
    ENDERCHEST_OPENED_SELF,
    ENDERCHEST_OPENED_OTHER,
    ENDERCHEST_OPENED_FOR_TARGET,
    ENDERCHEST_DISABLED_WORLD,
    HAT_SUCCESS,
    HAT_NO_ITEM,
    HAT_RETURNED,

    // ============ Подтверждения ============
    CONFIRM_HEADER,
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

    // ============ Шансы ============
    CHANCE_REPAIR_WARN,
    CHANCE_HEAL_WARN,
    CHANCE_FEED_WARN,

    // ============ Weather ============
    WEATHER_SET,
    WEATHER_LOCKED,
    WEATHER_UNLOCKED,
    WEATHER_INVALID,
    WEATHER_ALREADY,

    // ============ Time ============
    TIME_SET,
    TIME_ADDED,
    TIME_FROZEN,
    TIME_UNFROZEN,
    TIME_INVALID,
    TIME_ALREADY,

    // ============ Personal time / weather ============
    PTIME_SET,
    PTIME_RESET,
    PWEATHER_SET,
    PWEATHER_RESET,

    // ============ Server info ============
    GC_TPS,
    GC_MEMORY,
    GC_CHUNKS,
    GC_ENTITIES,
    GC_UPTIME,
    GC_HEADER,

    // ============ Position ============
    GETPOS_SELF,
    GETPOS_OTHER,
    GETPOS_FORMAT,
    DEPTH_SELF,
    COMPASS_SELF,

    // ============ Utility ============
    TOP_SUCCESS,
    TOP_NO_BLOCK,
    EXT_SUCCESS,
    EXT_OTHER,
    EXT_NOT_BURNING,
    MORE_SUCCESS,
    MORE_NO_ITEM,
    MORE_ALREADY_FULL,
    SKULL_SUCCESS,
    SKULL_GIVEN,
    SKULL_INVALID,
    TREE_SUCCESS,
    TREE_INVALID,
    TREE_NOT_SAPLING,
    JUMP_SUCCESS,
    JUMP_NO_TARGET,
    SUICIDE_SUCCESS,
    SUICIDE_OTHER,
    BURN_SUCCESS,
    BURN_OTHER,
    ICE_SUCCESS,
    ICE_OTHER,

    // ============ Player info ============
    WHOIS_HEADER,
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

    NEAR_HEADER,
    NEAR_ENTRY,
    NEAR_EMPTY,

    SEEN_ONLINE,
    SEEN_RECENT,
    SEEN_LONG_AGO,

    LIST_HEADER,
    LIST_ENTRY,
    LIST_EMPTY,

    PING_SELF,
    PING_OTHER,
    PING_FORMAT,

    // ============ Items ============
    ITEMNAME_SUCCESS,
    ITEMNAME_REMOVED,
    ITEMLORE_ADDED,
    ITEMLORE_CLEARED,
    ITEMLORE_LINE,
    ITEM_INVALID_HAND,

    // ============ Directions ============
    DIRECTION_NORTH,
    DIRECTION_SOUTH,
    DIRECTION_EAST,
    DIRECTION_WEST,

    // ============ Seen ============
    SEEN_JUST_NOW,
    TIME_FORMAT_DAYS,
    TIME_FORMAT_HOURS,
    TIME_FORMAT_MINUTES,
    TIME_FORMAT_SECONDS,

    // ============ Whois ============
    WHOIS_UNKNOWN,
    WHOIS_TRUE,
    WHOIS_FALSE,

    // ============ Misc ============
    REPAIR_BROKEN_COUNT,

    // ============ Кулдаун ============
    COOLDOWN_ACTIVE,
    COOLDOWN_EXPIRED;

    /** Ключ в .lang файле: GOD_ENABLED_SELF → god.enabled.self */
    public @NotNull String path() {
        return name().toLowerCase().replace('_', '.');
    }
}