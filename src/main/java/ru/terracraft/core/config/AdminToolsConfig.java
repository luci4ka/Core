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
        Vanish vanish
) {

    public AdminToolsConfig {
        if (general      == null) general      = new General(true, true);
        if (feedback     == null) feedback     = new Feedback(true, true, true);
        if (integrations == null) integrations = new Integrations(true, true);
        if (heal         == null) heal         = new Heal(100.0, 2);
        if (feed         == null) feed         = new Feed(100.0, 10);
        if (repair       == null) repair       = new Repair(100.0, 0.0);
        if (cooldowns    == null) cooldowns    = new Cooldowns(0, 0, 0, 5, 3, 0, 0, 0, 0, 0);
        if (hat          == null) hat          = new Hat(true, false);
        if (worlds       == null) worlds       = new Worlds(
                List.of(),
                List.of("world_nether", "world_the_end"),
                List.of()
        );
        if (vanish       == null) vanish       = new Vanish(true, false, false);
    }

    // ============ GENERAL ============

    public record General(
            @Comment("Логировать ли действия админов в консоль")
            boolean logAdminActions,

            @Comment("Показывать ли подсказки (usage) при неверном вводе")
            boolean showUsageOnError
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
            int spawnmob
    ) {
        /** Возвращает кулдаун для команды. */
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

            @Comment("Объявлять в чат, когда игрок уходит в vanish")
            boolean announceOnVanish
    ) {}
}