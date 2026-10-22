# Changelog

Все заметные изменения в TerraCraftCore.

Формат — [Keep a Changelog](https://keepachangelog.com/ru/1.1.0/).
Версионирование — [Semantic Versioning](https://semver.org/lang/ru/).

---

## [Unreleased]

### Added
- (пусто — все изменения вошли в 1.0.3)

### Changed
- (пусто)

### Fixed
- (пусто)

---

## [1.0.3] — 2026-10-22

### Added

**Публичный API**
- `TerraCoreAPI` — безопасные данные о состоянии игрока (god/fly/vanish/afk/mail/cooldown).
- `TerraCoreAdminAPI` — чувствительные данные (IP, координаты, first/last join) с проверкой права `terracraft.core.api.sensitive`.
- `PlayerSnapshot` DTO — снимок офлайн-игрока (`ru.terracraft.api.dto`).
- События: `TerraGodChangeEvent`, `TerraFlyChangeEvent`, `TerraVanishChangeEvent`, `TerraAfkChangeEvent` (наследуют `TerraStateChangeEvent`).
- Регистрация обоих API в `ServicesManager` при `onEnable()`.
- Методы `TerraCoreAPI`: `level`, `exp`, `saturation`, `walkSpeed`, `flySpeed`, `canSee`.
- Методы для офлайн-игроков: `isFlying(UUID)`, `gamemode(UUID)` — читают память или БД.

**AFK-детект**
- `AfkManager` — AFK-статус с `lastActivity`, `touch`, `set`, `toggle`, `clear`.
- `AfkTask` — авто-детект по таймауту (`afk.timeout-seconds`).
- `AfkListener` — трекинг активности (join/move/chat/command/interact).
- `/afk [player]` — toggle себе или другому (право `terracraft.core.afk.others`).
- `afk.bypass` — не помечать игрока AFK автоматически.
- PAPI: `%terracore_afk%`, `%terracore_afk_count%`.

**Почта**
- `/mail send|inbox|read|clear|unread` — внутренняя почта (offline-сообщения).
- `MailService` — таблица `tc_mail`, методы `send`, `inbox`, `find`, `unreadCount`, `markRead`, `clearRead`.
- Уведомление о непрочитанных при входе.
- PAPI: `%terracore_mail_unread%`.

**Локализация**
- `/tc lang` — список локалей, % заполнения, missing-ключи.
- Per-locale валидация: `LangManager.validateAllKeys()` проверяет **каждую** локаль.
- Метод `missingKeys(Locale)` — для команды и валидации.
- `LocaleStats` record — статистика для `/tc lang`.
- Логи: `Loaded built-in locale` vs `Loaded custom locale` + количество ключей.

**Команды**
- `/compass` — BossBar-компас с настраиваемым цветом, стилем, периодом.
- `/time smooth` — плавная смена времени с настраиваемой длительностью.
- `/hat` — только блоки, `hat.others` для других игроков.
- `/skull` — поддержка офлайн-игроков.
- `/burn`, `/ice` — права `.others`, `default-seconds` / `max-seconds` в конфиге.
- `/near` — `default-radius` в конфиге.
- `/ping` — цветовые пороги (`good-threshold`, `bad-threshold`).
- `/enchant` — `enchant.unsafe` для обхода ограничений (книги, топоры).
- `/give` — `give.max-amount` в конфиге.
- `/spawnmob` — `spawn-mob.max-count`, `max-health`, `spread-radius`.
- `/weather unlock` — длительности из конфига.
- `/speed 0` — сброс на `speed.walk-default` / `fly-default`.

**PAPI**
- `%terracore_gc_*%` — 17 плейсхолдеров для `/gc` (TPS, память, чанки, сущности, аптайм).
- `%terracore_ping_color%`, `%terracore_ping_colored%` — цвет по порогам.
- `%terracore_compass%`, `%terracore_compass_localized%`, `%terracore_compass_sector%`.
- `%terracore_world_time%`, `%terracore_world_time_name%`, `%terracore_world_weather%`, `%terracore_world_weather_name%`.
- `%terracore_fire_ticks%`, `%terracore_freeze_ticks%`, `%terracore_burning%`, `%terracore_frozen%`.
- `%terracore_walk_speed%`, `%terracore_fly_speed%`.
- `%terracore_vanished_list%` — имена vanished через запятую.
- `%terracore_enchant_max%`, `%terracore_give_max%`, `%terracore_spawnmob_max%`, `%terracore_near_radius%`, `%terracore_compass_duration%` — лимиты из конфига.

**Конфиг**
- 14 новых разделов: `afk`, `compass`, `spawn-mob`, `time`, `weather`, `speed`, `burn`, `ice`, `near`, `ping`, `enchant`, `give`, `cooldowns.cleanup-period-minutes`, `vanish.reapply-period-ticks`.

**Инфраструктура**
- `VanishTask` — период из `vanish.reapply-period-ticks`.
- `CooldownManager` — период очистки из `cooldowns.cleanup-period-minutes`.
- `StartupBanner` — реальная проверка Storage/Languages, статус «with warnings».
- `CHANGELOG.md`, `CONTRIBUTING.md`, `.gitignore`.
- GitHub Actions: авто-релиз JAR при пуше тега `v*`.
- `docs/API.md`, `docs/PAPI.md`, `docs/EXAMPLES.md` — полная документация для разработчиков.

### Changed
- **Переименование:** `Core` → `TerraCraftCore`.
  - `paper-plugin.yml`: `name: TerraCraftCore`.
  - `settings.gradle`: `rootProject.name = 'TerraCraftCore'`.
  - JAR: `TerraCraftCore-<version>.jar` (толстый) + `TerraCraftCore-<version>-api.jar` (API).
  - JitPack: `com.github.luci4ka:TerraCraftCore`.
- **JitPack-классификатор:** `thin` → `api`.
  - `compileOnly 'com.github.luci4ka:TerraCraftCore:v1.0.3:api'`
  - `docs/API.md`, `docs/EXAMPLES.md`, `README.md` обновлены.
- `WeatherCommand` — читает `weather.*` из конфига (раньше — жёсткие константы).
- `TerraCorePlugin` — `reloadAll()` перезапускает `VanishTask`, `CooldownManager`, `AfkTask` с новыми периодами.
- `TerraCorePlugin` — `onEnable()` использует единый `PluginManager` (`pm`) вместо повторных `getServer().getPluginManager()`.
- `GcCommand` — красивый вывод: блоки «Сервер / Память / Мир», TPS с цветом, память с процентом.
- `PlaceholderHook` — 30+ плейсхолдеров (было 19).
- `LangKey` — ~200 ключей (было ~150) + `AFK_*`, `MAIL_*`.
- `.lang` → `.yml` — плоский YAML, ключи с точками в кавычках.
- `StatsService` — `INSERT ... ON DUPLICATE KEY UPDATE` (атомарный upsert, без гонки).
- `MailService.inbox` — `.setLimit(limit)` в SQL вместо ручной обрезки.
- `MessageService.confirm` — fallback на дефолтные `[✔ Да]` / `[✘ Нет]` вместо тихого warning.
- `LangManager` — `readFromJar` логирует `Created` / `Updated` для встроенных локалей.

### Fixed
- `PlayerJoinListener` — vanish без права: флаг снимается при `restore()`.
- `PlayerQuitListener` — `clearMemoryOnly` для fly/god (утечка памяти).
- `PlayerQuitListener` — `afk().clear(id)` при выходе.
- `VanishTask` — `firstError` вместо `started` (стектрейс один раз).
- `LangManager` — `resolver()` поддерживает `Component` (для `Component.translatable`).
- `ConfirmManager` — `start()` идемпотентен, `stop()` для `onDisable`.
- `CompassCommand` — убран мёртвый код `sector`.
- `EnchantCommand` — ключ `enchantment.minecraft.*` вместо `minecraft.*`.
- `TerraCoreCommand` — `getPluginMeta()` вместо `getDescription()`.
- `GamemodeManager` — `clear()` чистит `current` (утечка памяти).
- `AfkListener.onMove` — защита от `IllegalArgumentException` при смене мира.
- `AfkListener.onChat` — `AsyncChatEvent` + `runTask` (не вызывать `callEvent` из async).
- `LangManager` — `getSavedGamemode(UUID)` для офлайн-игроков (API).
- **JitPack** — `compileOnly` теперь видит `ru.terracraft.api.*` (артефакт `-api.jar` публикуется корректно).
- `playerstatsnew` — добавлен `metadataSources { gradleMetadata(); mavenPom() }` для JitPack.

### Removed
- `H2`, `HikariCP`, `MariaDB` из релоцированных (MineSQL — внешний плагин).
- `ParsedLang` — не используется.
- `MemoryLeakTest`, `IntegrationTest` — не работают без MockBukkit.
- `storage.type`, `storage.h2-file`, `storage.mariadb-*` — заменено на MineSQL.
- `vanish.announce-on-vanish`, `general.show-usage-on-error` — не используются.
- Дублирующийся комментарий в `en_us.yml` (секция 12).

---

## [1.0.2] — 2026-10-21

### Added
- (без изменений — только тесты в этой версии)

### Changed
- (без изменений)

### Fixed
- (без изменений)

> **Примечание:** версия `1.0.2` была промежуточной — основные изменения вошли в `1.0.3`.

---

## [1.0.0] — 2026-04-04

### Added
- Первый релиз.
- 40+ команд: `/god`, `/fly`, `/vanish`, `/gamemode`, `/heal`, `/feed`, `/speed`, `/burn`, `/ice`, `/ext`, `/suicide`, `/repair`, `/enchant`, `/give`, `/hat`, `/more`, `/skull`, `/tree`, `/itemname`, `/itemlore`, `/spawnmob`, `/workbench`, `/anvil`, `/loom`, `/grindstone`, `/stonecutter`, `/smithingtable`, `/enderchest`, `/weather`, `/time`, `/ptime`, `/pweather`, `/top`, `/jump`, `/compass`, `/gc`, `/getpos`, `/depth`, `/whois`, `/seen`, `/near`, `/list`, `/ping`, `/terracore`.
- Локализация RU/EN, каскад локалей.
- Конфиг на 8 разделов (configlib).
- Storage через MineSQL (MariaDB).
- Persistent state: god/fly/vanish/gamemode.
- Статистика команд.
- Офлайн-кэш для `/seen` и `/whois`.
- PAPI: 19 плейсхолдеров.
- bStats, updater.
- Тесты на MockBukkit.

---

## Ссылки

- [Keep a Changelog](https://keepachangelog.com/ru/1.1.0/)
- [Semantic Versioning](https://semver.org/lang/ru/)
- [Conventional Commits](https://www.conventionalcommits.org/ru/)
- [JitPack — TerraCraftCore](https://jitpack.io/#luci4ka/TerraCraftCore)