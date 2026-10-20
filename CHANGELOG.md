
# Changelog

Все заметные изменения в TerraCore.

Формат — [Keep a Changelog](https://keepachangelog.com/ru/1.1.0/).
Версионирование — [Semantic Versioning](https://semver.org/lang/ru/).

---

## [Unreleased]

### Added
- PAPI: `%terracore_gc_*%` — 17 плейсхолдеров для `/gc` (TPS, память, чанки, сущности, аптайм).
- PAPI: `%terracore_ping_color%`, `%terracore_ping_colored%` — цвет по порогам.
- PAPI: `%terracore_compass%`, `%terracore_compass_localized%`, `%terracore_compass_sector%`.
- PAPI: `%terracore_world_time%`, `%terracore_world_time_name%`, `%terracore_world_weather%`, `%terracore_world_weather_name%`.
- PAPI: `%terracore_fire_ticks%`, `%terracore_freeze_ticks%`, `%terracore_burning%`, `%terracore_frozen%`.
- PAPI: `%terracore_walk_speed%`, `%terracore_fly_speed%`.
- PAPI: `%terracore_vanished_list%` — имена vanished через запятую.
- PAPI: `%terracore_enchant_max%`, `%terracore_give_max%`, `%terracore_spawnmob_max%`, `%terracore_near_radius%`, `%terracore_compass_duration%` — лимиты из конфига.
- Config: 13 новых разделов (`compass`, `spawn-mob`, `time`, `weather`, `speed`, `burn`, `ice`, `near`, `ping`, `enchant`, `give`, `cooldowns.cleanup-period-minutes`, `vanish.reapply-period-ticks`).
- `/compass` — BossBar-компас с настраиваемым цветом, стилем и периодом.
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
- `VanishTask` — период из `vanish.reapply-period-ticks`.
- `CooldownManager` — период очистки из `cooldowns.cleanup-period-minutes`.
- `StartupBanner` — реальная проверка Storage/Languages, статус «with warnings».
- `CHANGELOG.md`, `CONTRIBUTING.md`, `.gitignore`.

### Changed
- `WeatherCommand` — читает `weather.*` из конфига (раньше — жёсткие константы).
- `TerraCorePlugin` — `reloadAll()` перезапускает `VanishTask` и `CooldownManager` с новыми периодами.
- `TerraCorePlugin` — `cooldowns.cleanup-period-minutes` читается при старте.
- `GcCommand` — красивый вывод: блоки «Сервер / Память / Мир», TPS с цветом, память с процентом.
- `PlaceholderHook` — 30+ плейсхолдеров (было 19).
- `LangKey` — ~200 ключей (было ~150).
- `.lang` → `.yml` — плоский YAML, ключи с точками в кавычках.

### Fixed
- `PlayerJoinListener` — vanish без права: флаг снимается при `restore()`.
- `PlayerQuitListener` — `clearMemoryOnly` для fly/god (утечка памяти).
- `VanishTask` — `firstError` вместо `started` (стектрейс один раз).
- `LangManager` — `resolver()` поддерживает `Component` (для `Component.translatable`).
- `ConfirmManager` — `start()` идемпотентен, `stop()` для `onDisable`.
- `CompassCommand` — убран мёртвый код `sector`.
- `EnchantCommand` — ключ `enchantment.minecraft.*` вместо `minecraft.*`.
- `TerraCoreCommand` — `getPluginMeta()` вместо `getDescription()`.
- `GamemodeManager` — `clear()` чистит `current` (утечка памяти).

### Removed
- `H2`, `HikariCP`, `MariaDB` из релоцированных (MineSQL — внешний плагин).
- `ParsedLang` — не используется.
- `MemoryLeakTest`, `IntegrationTest` — не работают без MockBukkit.
- `storage.type`, `storage.h2-file`, `storage.mariadb-*` — заменено на MineSQL.
- `vanish.announce-on-vanish`, `general.show-usage-on-error` — не используются.

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