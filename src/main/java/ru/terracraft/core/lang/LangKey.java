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

    // ============ Кулдаун ============
    COOLDOWN_ACTIVE,
    COOLDOWN_EXPIRED;

    /** Ключ в .lang файле: GOD_ENABLED_SELF → god.enabled.self */
    public @NotNull String path() {
        return name().toLowerCase().replace('_', '.');
    }
}