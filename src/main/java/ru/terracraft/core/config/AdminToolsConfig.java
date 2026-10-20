package ru.terracraft.core.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.util.List;

@Configuration
public record AdminToolsConfig(

        @Comment("Общие настройки")
        General general,

        @Comment("Обратная связь с игроком (звуки, action bar, подтверждения)")
        Feedback feedback,

        @Comment("Внешние интеграции (bStats, updater, PlaceholderAPI)")
        Integrations integrations,

        @Comment("Настройки /heal")
        Heal heal,

        @Comment("Настройки /feed")
        Feed feed,

        @Comment("Настройки /repair")
        Repair repair,

        @Comment("Кулдауны команд (в секундах)")
        Cooldowns cooldowns,

        @Comment("Настройки hat")
        Hat hat,

        @Comment("Ограничения по мирам")
        Worlds worlds,

        @Comment("Настройки vanish")
        Vanish vanish,

        @Comment("Настройки /compass (BossBar)")
        Compass compass,

        @Comment("Настройки /spawnmob")
        SpawnMob spawnMob,

        @Comment("Настройки /time")
        Time time,

        @Comment("Настройки /weather")
        Weather weather,

        @Comment("Настройки /speed")
        Speed speed,

        @Comment("Настройки /burn")
        Burn burn,

        @Comment("Настройки /ice")
        Ice ice,

        @Comment("Настройки /near")
        Near near,

        @Comment("Настройки /ping")
        Ping ping,

        @Comment("Настройки /enchant")
        Enchant enchant,

        @Comment("Настройки /give")
        Give give,

        @Comment("Хранилище: memory | h2 | mariadb")
        Storage storage
) {

    public AdminToolsConfig {
        if (general      == null) general      = new General(true);
        if (feedback     == null) feedback     = new Feedback(true, true, true);
        if (integrations == null) integrations = new Integrations(true, true);
        if (heal         == null) heal         = new Heal(100.0, 2);
        if (feed         == null) feed         = new Feed(100.0, 10);
        if (repair       == null) repair       = new Repair(100.0, 0.0);
        if (cooldowns    == null) cooldowns    = new Cooldowns(0, 0, 0, 5, 3, 0, 0, 0, 0, 0, 60);
        if (hat          == null) hat          = new Hat(true, false);
        if (worlds       == null) worlds       = new Worlds(
                List.of(),   // flyDisabledWorlds
                List.of(),   // workbenchDisabledWorlds
                List.of()    // vanishDisabledWorlds
        );
        if (vanish       == null) vanish       = new Vanish(true, false, 100);
        if (compass      == null) compass      = new Compass(30, 2, "BLUE", "PROGRESS");
        if (spawnMob     == null) spawnMob     = new SpawnMob(50, 1024, 2.0);
        if (time         == null) time         = new Time(3, 60);
        if (weather      == null) weather      = new Weather(12_000, 6_000, 3_600);
        if (speed        == null) speed        = new Speed(0.1, 0.1);
        if (burn         == null) burn         = new Burn(5, 60);
        if (ice          == null) ice          = new Ice(5, 60);
        if (near         == null) near         = new Near(200);
        if (ping         == null) ping         = new Ping(50, 100);
        if (enchant      == null) enchant      = new Enchant(255);
        if (give         == null) give         = new Give(6400);
        if (storage      == null) storage      = new Storage("tc_");
    }

    // ============ GENERAL ============

    public record General(
            @Comment("Логировать ли действия админов в консоль")
            boolean logAdminActions
    ) {}

    // ============ FEEDBACK ============

    public record Feedback(
            @Comment("Проигрывать звуки (beacon activate, level up и т.д.)")
            boolean sounds,

            @Comment("Показывать короткие уведомления в action bar (вместо чата)")
            boolean actionBar,

            @Comment("Спрашивать подтверждение для опасных действий (/repair all)")
            boolean confirmations
    ) {}

    // ============ INTEGRATIONS ============

    public record Integrations(
            @Comment("Отправлять анонимную статистику на bstats.org")
            boolean bStats,

            @Comment("Проверять обновления плагина при запуске")
            boolean updateNotifier
    ) {}

    // ============ HEAL ============

    public record Heal(
            @Comment("Шанс успешного отхила в % (100 = всегда лечит)")
            double healChance,

            @Comment("Минимальное количество сердец при успехе (не больше максимума игрока)")
            int minHearts
    ) {}

    // ============ FEED ============

    public record Feed(
            @Comment("Шанс успешного насыщения в % (100 = всегда кормит)")
            double feedChance,

            @Comment("Минимальный уровень еды при успехе (0..20). 20 = всегда полный")
            int minFoodLevel
    ) {}

    // ============ REPAIR ============

    public record Repair(
            @Comment("Шанс успешной починки в % (100 = всегда чинит)")
            double repairChance,

            @Comment("Шанс что предмет сломается при попытке в % (0 = никогда)")
            double breakChance
    ) {}

    // ============ COOLDOWNS ============

    public record Cooldowns(
            @Comment("Кулдаун /god (0 = без)")
            int god,
            @Comment("Кулдаун /fly (0 = без)")
            int fly,
            @Comment("Кулдаун /heal (0 = без)")
            int heal,
            @Comment("Кулдаун /feed (0 = без)")
            int feed,
            @Comment("Кулдаун /repair (0 = без)")
            int repair,
            @Comment("Кулдаун /hat (0 = без)")
            int hat,
            @Comment("Кулдаун /workbench (0 = без)")
            int workbench,
            @Comment("Кулдаун /enderchest (0 = без)")
            int enderchest,
            @Comment("Кулдаун /vanish (0 = без)")
            int vanish,
            @Comment("Кулдаун /spawnmob (0 = без)")
            int spawnmob,
            @Comment("Период очистки просроченных кулдаунов (в минутах, 0 = отключено)")
            int cleanupPeriodMinutes
    ) {
        public int forCommand(CommandKey key) {
            return switch (key) {
                case GOD        -> god;
                case FLY        -> fly;
                case HEAL       -> heal;
                case FEED       -> feed;
                case REPAIR     -> repair;
                case HAT        -> hat;
                case WORKBENCH  -> workbench;
                case ENDERCHEST -> enderchest;
                case VANISH     -> vanish;
                case SPAWNMOB   -> spawnmob;
            };
        }
    }

    // ============ HAT ============

    public record Hat(
            @Comment("Возвращать предыдущий шлем в инвентарь (если был)")
            boolean returnOldHelmet,

            @Comment("Разрешать снимать шлем (пустой рукой)")
            boolean allowEmptyHand
    ) {}

    // ============ WORLDS ============

    public record Worlds(
            @Comment("Миры, где /fly запрещён (пусто — везде можно)")
            List<String> flyDisabledWorlds,

            @Comment("Миры, где запрещены /workbench и /enderchest")
            List<String> workbenchDisabledWorlds,

            @Comment("Миры, где /vanish автоматически выключается при входе")
            List<String> vanishDisabledWorlds
    ) {}

    // ============ VANISH ============

    public record Vanish(
            @Comment("Скрывать vanished-игроков из табло (через hidePlayer)")
            boolean hideFromTab,

            @Comment("Могут ли vanished-игроки наносить урон")
            boolean canHitPlayers,

            @Comment("Период переприменения видимости (тики, 20 = 1 сек, 0 = отключено)")
            int reapplyPeriodTicks
    ) {}

    // ============ COMPASS ============

    public record Compass(
            @Comment("Длительность показа BossBar в секундах (0 = бесконечно)")
            int durationSeconds,

            @Comment("Период обновления BossBar в тиках (2 = 10 раз/сек)")
            int updatePeriodTicks,

            @Comment("Цвет BossBar: BLUE, RED, GREEN, YELLOW, PURPLE, WHITE, PINK")
            String color,

            @Comment("Стиль BossBar: PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20")
            String overlay
    ) {}

    // ============ SPAWNMOB ============

    public record SpawnMob(
            @Comment("Максимальное количество мобов за один вызов /spawnmob")
            int maxCount,

            @Comment("Максимальное здоровье моба (в HP, 20 = 10 сердец)")
            int maxHealth,

            @Comment("Радиус разброса спавна (в блоках, 2 = ±1 блок)")
            double spreadRadius
    ) {}

    // ============ TIME ============

    public record Time(
            @Comment("Длительность плавной смены времени по умолчанию (сек)")
            int defaultSmoothSeconds,

            @Comment("Максимальная длительность /time smooth (сек)")
            int maxSmoothSeconds
    ) {}

    // ============ WEATHER ============

    public record Weather(
            @Comment("Длительность ясной погоды после /weather unlock (тики, 12000 = 10 мин)")
            int clearDuration,

            @Comment("Длительность дождя после /weather unlock (тики, 6000 = 5 мин)")
            int rainDuration,

            @Comment("Длительность грозы после /weather unlock (тики, 3600 = 3 мин)")
            int thunderDuration
    ) {}

    // ============ SPEED ============

    public record Speed(
            @Comment("Скорость ходьбы по умолчанию (0.1 = ваниль)")
            double walkDefault,

            @Comment("Скорость полёта по умолчанию (0.1 = ваниль)")
            double flyDefault
    ) {}

    // ============ BURN ============

    public record Burn(
            @Comment("Длительность поджога по умолчанию (сек)")
            int defaultSeconds,

            @Comment("Максимальная длительность поджога (сек)")
            int maxSeconds
    ) {}

    // ============ ICE ============

    public record Ice(
            @Comment("Длительность заморозки по умолчанию (сек)")
            int defaultSeconds,

            @Comment("Максимальная длительность заморозки (сек)")
            int maxSeconds
    ) {}

    // ============ NEAR ============

    public record Near(
            @Comment("Радиус /near по умолчанию (блоки)")
            int defaultRadius
    ) {}

    // ============ PING ============

    public record Ping(
            @Comment("Порог 'хорошего' пинга (мс) — зелёный")
            int goodThreshold,

            @Comment("Порог 'плохого' пинга (мс) — красный")
            int badThreshold
    ) {}

    // ============ ENCHANT ============

    public record Enchant(
            @Comment("Максимальный уровень зачарования через /enchant (1..255)")
            int maxLevel
    ) {}

    // ============ GIVE ============

    public record Give(
            @Comment("Максимальное количество предметов за один /give")
            int maxAmount
    ) {}

    // ============ STORAGE ============

    public record Storage(
            @Comment("Префикс таблиц (например, tc_)")
            String tablePrefix
    ) {}
}