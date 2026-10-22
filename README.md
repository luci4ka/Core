# TerraCraftCore

[![Build Status](https://github.com/luci4ka/TerraCraftCore/actions/workflows/release.yml/badge.svg)](https://github.com/luci4ka/TerraCraftCore/actions/workflows/release.yml)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/projects/jdk/25/)
[![Paper](https://img.shields.io/badge/Paper-1.21.11-blue.svg)](https://papermc.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![JitPack](https://img.shields.io/badge/JitPack-ready-brightgreen.svg)](https://jitpack.io/#luci4ka/TerraCraftCore)

> **TerraCraftCore** — ядро админ-инструментов для Paper-сервера.
> ~40 команд, публичный API, AFK-детект, локализация (RU/EN), гибкий конфиг, персистентность в MariaDB через MineSQL.

---

## 📑 Содержание

- [Возможности](#-возможности)
- [Требования](#-требования)
- [Установка](#-установка)
- [Конфигурация](#️-конфигурация)
- [Хранилище](#-хранилище)
- [Локализация](#-локализация)
- [PlaceholderAPI](#-placeholderapi)
- [Публичный API](#-публичный-api)
- [Документация для разработчиков](#-документация-для-разработчиков)
- [Команды](#-команды)
- [Права](#-права)
- [Сборка из исходников](#-сборка-из-исходников)
- [Тесты](#-тесты)
- [Архитектура](#-архитектура)
- [Лицензия](#-лицензия)

---

## ✨ Возможности

### Управление игроками

- **`/god`** — режим бессмертия (урон не проходит).
- **`/fly`** — полёт (с ограничением по мирам).
- **`/vanish`** — невидимость с `hidePlayer` и metadata для TAB.
- **`/afk`** — AFK-статус (себе или другому с правом `.others`).
- **`/gamemode`** — смена режима + `/gm back`.
- **`/heal`**, **`/feed`** — лечение/кормление с шансом.
- **`/speed`** — скорость ходьбы/полёта.
- **`/burn`**, **`/ice`**, **`/ext`**, **`/suicide`** — действия над игроком.

### Предметы

- **`/repair`** — починка предмета или всего инвентаря (с подтверждением).
- **`/enchant`** — энчант с опцией обхода ограничений (`enchant.unsafe`).
- **`/give`**, **`/more`**, **`/skull`**, **`/clear`**.
- **`/hat`** — надеть блок из руки на голову (только блоки) / снять шлем.
- **`/itemname`**, **`/itemlore`** — кастомизация предметов (MiniMessage).
- **`/tree`** — посадить дерево.

### Мир и время

- **`/weather`**, **`/time`** — управление погодой/временем (с блокировкой).
- **`/time smooth`** — плавная смена времени с настраиваемой длительностью.
- **`/ptime`**, **`/pweather`** — личное время/погода.
- **`/spawnmob`** — спавн мобов с вариантами (`sheep:red`, `creeper:powered`, ...).
- **`/workbench`**, **`/anvil`**, **`/loom`**, **`/grindstone`**, **`/stonecutter`**, **`/smithingtable`**, **`/enderchest`**.

### Перемещение

- **`/top`**, **`/jump`** — телепорт на верх/в точку взгляда.
- **`/compass`** — BossBar-компас с настраиваемым цветом и стилем.

### Информация

- **`/gc`** — TPS, память, чанки, сущности, аптайм.
- **`/whois`**, **`/seen`**, **`/near`**, **`/list`**, **`/ping`** (с цветовыми порогами).
- **`/getpos`**, **`/depth`**.
- **`/terracore reload`**, **`/terracore info`**, **`/terracore lang`**.

### Архитектура

- **Persistent state** — кулдауны, god/fly/vanish/gamemode переживают рестарт.
- **AFK-детект** — автоматически по таймауту + вручную `/afk`.
- **Статистика** — счётчик использований каждой команды.
- **Офлайн-кэш** — `/seen` и `/whois` работают для офлайн-игроков.
- **MariaDB через MineSQL** — единая схема, миграция без изменений.
- **Локализация** — RU/EN, каскад локалей, дописывание новых ключей, `/tc lang` для проверки.
- **PlaceholderAPI** — 30+ плейсхолдеров.
- **Публичный API** — `TerraCoreAPI` + `TerraCoreAdminAPI` через `ServicesManager`, события `Terra*ChangeEvent`.
- **Полная настройка через `config.yml`** — 22 раздела.

---

## 📋 Требования

| Компонент          | Версия                                                                        |
|--------------------|-------------------------------------------------------------------------------|
| **Java**           | 25+                                                                           |
| **Paper**          | 1.21.11+ (используется `RegistryAccess`, `GameRules`, `MenuType.create(...)`) |
| **CommandAPI**     | 12.1.0+ (обязательно)                                                         |
| **MineSQL**        | 1.4.2+ (обязательно)                                                          |
| **PlaceholderAPI** | 2.12.3+ (опционально)                                                         |

---

## 🚀 Установка

1. Скачайте `TerraCraftCore-x.y.z.jar` из [Releases](https://github.com/luci4ka/TerraCraftCore/releases).
2. Положите в `plugins/`.
3. Установите [CommandAPI](https://www.spigotmc.org/resources/commandapi.79504/) версии **12.1.0+**.
4. Установите [MineSQL](https://github.com/CarmJos/MineSQL) и настройте источник `terracore` в `plugins/MineSQL/config.yml`:
   ```yaml
   sources:
     terracore:
       type: MARIADB
       host: 127.0.0.1
       port: 3306
       database: terra_core
       username: terra
       password: change_me
   ```
5. (Опционально) Установите [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/).
6. Запустите сервер — создастся:
   ```
   plugins/TerraCraftCore/
   ├── config.yml
   └── lang/
       ├── en_us.yml
       └── ru_ru.yml
   ```
7. Настройте `config.yml` под себя.
8. `/tc reload` — перечитать конфиг и языки без рестарта.

---

## ⚙️ Конфигурация

Конфиг полностью переработан: **22 раздела**, каждое значение настраивается.

### `config.yml` — пример

```yaml
general:
  log-admin-actions: true      # Логировать действия в консоль

feedback:
  sounds: true                 # Звуки
  action-bar: true             # Action bar вместо чата
  confirmations: true          # Подтверждения (/repair all, /heal с шансом)

integrations:
  b-stats: true                # bStats (требует BSTATS_ID > 0)
  update-notifier: true        # Проверка обновлений на GitHub

heal:
  heal-chance: 100.0           # Шанс отхила, %
  min-hearts: 2                # Мин. сердец

feed:
  feed-chance: 100.0
  min-food-level: 10

repair:
  repair-chance: 100.0
  break-chance: 0.0            # Шанс сломать предмет

cooldowns:
  god: 0
  fly: 0
  heal: 0
  feed: 5
  repair: 3
  hat: 0
  workbench: 0
  enderchest: 0
  vanish: 0
  spawnmob: 0
  cleanup-period-minutes: 60   # Период очистки кулдаунов (0 = отключено)

hat:
  return-old-helmet: true
  allow-empty-hand: false

worlds:
  fly-disabled-worlds: []
  workbench-disabled-worlds: []
  vanish-disabled-worlds: []

vanish:
  hide-from-tab: true          # Скрывать через hidePlayer
  can-hit-players: false
  reapply-period-ticks: 100    # 20 = 1 сек, 0 = отключено

afk:
  timeout-seconds: 300         # Таймаут AFK (0 = отключено)
  kick-after-seconds: 0        # Кикать AFK через N сек (0 = не кикать)

compass:
  duration-seconds: 30         # 0 = бесконечно
  update-period-ticks: 2       # 2 = 10 раз/сек
  color: BLUE                  # BLUE, RED, GREEN, YELLOW, PURPLE, WHITE, PINK
  overlay: PROGRESS            # PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20

spawn-mob:
  max-count: 50
  max-health: 1024
  spread-radius: 2.0

time:
  default-smooth-seconds: 3
  max-smooth-seconds: 60

weather:
  clear-duration: 12000        # тики
  rain-duration: 6000
  thunder-duration: 3600

speed:
  walk-default: 0.1
  fly-default: 0.1

burn:
  default-seconds: 5
  max-seconds: 60

ice:
  default-seconds: 5
  max-seconds: 60

near:
  default-radius: 200

ping:
  good-threshold: 50           # < 50 = зелёный
  bad-threshold: 100           # >= 100 = красный

enchant:
  max-level: 255

give:
  max-amount: 6400

storage:
  table-prefix: tc_
```

---

## 💾 Хранилище

### Что сохраняется

| Данные           | `state_key` / таблица       | Переживает рестарт? |
|------------------|-----------------------------|---------------------|
| Кулдауны         | `tc_cooldowns`              | ✅                   |
| God              | `tc_states`, key=`god`      | ✅                   |
| Fly              | `tc_states`, key=`fly`      | ✅                   |
| Vanish           | `tc_states`, key=`vanish`   | ✅                   |
| Gamemode current | `tc_states`, key=`gamemode` | ✅                   |
| Статистика       | `tc_stats`                  | ✅                   |
| Офлайн-кэш       | `tc_players`                | ✅                   |
| Почта            | `tc_mail`                   | ✅                   |

### Схема БД (v2)

```sql
-- Кулдауны
CREATE TABLE tc_cooldowns (
    uuid        VARCHAR(36)  NOT NULL,
    command_key VARCHAR(32)  NOT NULL,
    expires_at  BIGINT       NOT NULL,
    PRIMARY KEY (uuid, command_key)
);

-- Состояния (god/fly/vanish/gamemode)
CREATE TABLE tc_states (
    uuid        VARCHAR(36)  NOT NULL,
    state_key   VARCHAR(32)  NOT NULL,
    state_value VARCHAR(64)  NOT NULL,
    updated_at  BIGINT       NOT NULL,
    PRIMARY KEY (uuid, state_key)
);

-- Статистика
CREATE TABLE tc_stats (
    uuid        VARCHAR(36) NOT NULL,
    command_key VARCHAR(32) NOT NULL,
    uses        BIGINT      NOT NULL DEFAULT 0,
    last_used   BIGINT      NOT NULL,
    PRIMARY KEY (uuid, command_key)
);

-- Офлайн-кэш
CREATE TABLE tc_players (
    uuid          VARCHAR(36) NOT NULL PRIMARY KEY,
    name          VARCHAR(16) NOT NULL,
    first_join    BIGINT      NOT NULL,
    last_seen     BIGINT      NOT NULL,
    last_ip       VARCHAR(45) NULL,
    last_world    VARCHAR(64) NULL,
    last_x        DOUBLE      NULL,
    last_y        DOUBLE      NULL,
    last_z        DOUBLE      NULL,
    last_gamemode VARCHAR(16) NULL
);

-- Почта
CREATE TABLE tc_mail (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    recipient   VARCHAR(36)  NOT NULL,
    sender      VARCHAR(36)  NOT NULL,
    sender_name VARCHAR(16)  NOT NULL,
    message     VARCHAR(512) NOT NULL,
    sent_at     BIGINT       NOT NULL,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);
```

Таблицы создаются автоматически при старте (`StorageManager.migrate()`).

---

## 🌐 Локализация

### Файлы

```
plugins/TerraCraftCore/lang/
├── en_us.yml
└── ru_ru.yml
```

> **Формат:** плоский YAML, ключи с точками **квотированы** (`"god.enabled.self": "..."`).

### Каскад локалей

1. **Точное совпадение** (`ru_RU` → `ru_RU`).
2. **Тот же язык** (`ru_UA` → `ru_RU`, `en_GB` → `en_US`).
3. **СНГ** → `ru_RU` (для `uk_UA`, `be_BY`, `kk_KZ`, ...).
4. **Fallback** → `en_US`.

### Дописывание новых ключей

При обновлении плагина новые ключи **дописываются** в конец `.yml` файлов на диске. **Существующие переводы не трогаются.**

### Проверка перевода

`/tc lang` — список загруженных локалей, % заполнения, missing-ключи.

### MiniMessage

Все сообщения поддерживают [MiniMessage](https://docs.advntr.dev/minimessage/format.html):

```yaml
"god.enabled.self": "<green><sprite:items:item/netherite_chestplate> <white>Godmode <green>включён</green>."
"prefix": " <gradient:#00d4ff:#7b2ff7>TerraCraftCore</gradient><sprite:items:item/emerald><reset>"
```

---

## 🔌 PlaceholderAPI

**30+ плейсхолдеров** для TAB, чата, scoreboard, DeluxeMenus, FeatherBoard, Holographic Displays.

### Краткий список

| Плейсхолдер | Значение |
|---|---|
| `%terracore_god%` / `fly` / `vanished` / `afk` | `true` / `false` |
| `%terracore_god_count%` / `fly_count` / `vanished_count` / `afk_count` | число |
| `%terracore_ping%` / `ping_color` / `ping_colored` | `42` / `green` / `<green>42</green>` |
| `%terracore_health%` / `health_hearts` / `health_percent` | `18.5` / `9.2` / `92` |
| `%terracore_food%` / `saturation` / `level` / `exp` | число |
| `%terracore_world%` / `world_time_name` / `world_weather_name` | строка |
| `%terracore_gamemode%` / `gamemode_id` | локализованное / `SURVIVAL` |
| `%terracore_cooldown_<cmd>%` / `_raw` | секунд |
| `%terracore_gc_tps%` / `gc_memory_percent` / `gc_uptime%` | сервер |
| `%terracore_mail_unread%` | число |

**Полный список (50+ плейсхолдеров) + примеры для TAB, DeluxeMenus, FeatherBoard, чата, scoreboard — в [`docs/PAPI.md`](docs/PAPI.md).**

---

## 🔌 Публичный API

TerraCraftCore предоставляет два интерфейса через `ServicesManager`.

### `TerraCoreAPI` — безопасные данные

```java
TerraCoreAPI api = Bukkit.getServicesManager().load(TerraCoreAPI.class);

// Состояния (работают и для офлайн)
boolean god = api.isGod(uuid);
boolean fly = api.isFlying(uuid);
boolean vanished = api.isVanished(uuid);
boolean afk = api.isAfk(uuid);
String gamemode = api.gamemode(uuid);

// Bukkit-данные (только онлайн)
int ping = api.ping(uuid);
double hp = api.health(uuid);
int food = api.food(uuid);
int level = api.level(uuid);

// Проверка видимости
boolean canSee = api.canSee(viewer, targetUuid);
```

### `TerraCoreAdminAPI` — чувствительные данные (права)

Требует право `terracraft.core.api.sensitive`. Без права — возвращает `null`/`0`.

```java
TerraCoreAdminAPI admin = Bukkit.getServicesManager().load(TerraCoreAdminAPI.class);

String ip = admin.ip(sender, uuid);
var pos = admin.position(sender, uuid);
long firstJoin = admin.firstJoin(sender, uuid);
long lastSeen = admin.lastSeen(sender, uuid);
PlayerSnapshot snapshot = admin.cache(sender, uuid);
```

### События

```java
@EventHandler
public void onGod(TerraGodChangeEvent e) {
    if (e.getNewState()) { ... }
}

@EventHandler
public void onFly(TerraFlyChangeEvent e) { ... }

@EventHandler
public void onVanish(TerraVanishChangeEvent e) { ... }

@EventHandler
public void onAfk(TerraAfkChangeEvent e) { ... }
```

---

## 📚 Документация для разработчиков

- **[`docs/API.md`](docs/API.md)** — полная документация API: `TerraCoreAPI`, `TerraCoreAdminAPI`, `PlayerSnapshot`, 4 события, `ServicesManager`.
- **[`docs/PAPI.md`](docs/PAPI.md)** — PlaceholderAPI: все плейсхолдеры + примеры для TAB, DeluxeMenus, FeatherBoard, Holographic Displays.
- **[`docs/EXAMPLES.md`](docs/EXAMPLES.md)** — 16 готовых примеров интеграции (TAB, чат, PvP, статистика, GUI, `/info`, PAPI).

### Быстрый старт

```groovy
// build.gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.luci4ka:TerraCraftCore:v1.0.2'
}
```

```yaml
# paper-plugin.yml
dependencies:
  server:
    TerraCraftCore:
      load: BEFORE
      required: true
      join-classpath: true   # ← критично для доступа к классам TerraCraftCore
```

```java
TerraCoreAPI api = Bukkit.getServicesManager().load(TerraCoreAPI.class);
if (api != null && api.isAfk(player)) {
    // игрок AFK
}
```

---

## 🕹 Команды

### Управление

| Команда             | Право                    | Описание                      |
|---------------------|--------------------------|-------------------------------|
| `/terracore reload` | `terracraft.core.reload` | Перечитать конфиг + языки     |
| `/terracore info`   | `terracraft.core.info`   | Версия, сервер, авторы        |
| `/terracore lang`   | `terracraft.core.lang`   | Список локалей + % заполнения |

### Состояния

| Команда                     | Право                               | Описание                       |
|-----------------------------|-------------------------------------|--------------------------------|
| `/god [player]`             | `terracraft.core.god[.others]`      | Бессмертие                     |
| `/fly [player]`             | `terracraft.core.fly[.others]`      | Полёт                          |
| `/vanish [player]`          | `terracraft.core.vanish[.others]`   | Невидимость                    |
| `/vanish list`              | `terracraft.core.vanish.see`        | Список vanished                |
| `/afk [player]`             | `terracraft.core.afk[.others]`      | AFK-статус                     |
| `/gamemode <mode> [player]` | `terracraft.core.gamemode[.others]` | Смена режима (`s/c/a/sp/back`) |

### Здоровье / еда / скорость

| Команда                              | Право                            | Описание                 |
|--------------------------------------|----------------------------------|--------------------------|
| `/heal [player]`                     | `terracraft.core.heal[.others]`  | Лечение (шанс в конфиге) |
| `/feed [player]`                     | `terracraft.core.feed[.others]`  | Кормление (шанс)         |
| `/speed <0-10> [walk\|fly] [player]` | `terracraft.core.speed[.others]` | Скорость                 |

### Предметы

| Команда                                   | Право                                 | Описание                               |
|-------------------------------------------|---------------------------------------|----------------------------------------|
| `/repair [all\|confirm\|cancel]`          | `terracraft.core.repair[.all]`        | Починка                                |
| `/enchant <ench> [level]`                 | `terracraft.core.enchant` + `.unsafe` | Энчант (обход ограничений с `.unsafe`) |
| `/give <player> <item> [amount]`          | `terracraft.core.give`                | Выдать предмет                         |
| `/hat [player]`                           | `terracraft.core.hat[.others]`        | Надеть блок / снять шлем               |
| `/more`                                   | `terracraft.core.more`                | Пополнить стак                         |
| `/skull [player]`                         | `terracraft.core.skull[.others]`      | Голова игрока (онлайн + офлайн)        |
| `/tree <type>`                            | `terracraft.core.tree`                | Посадить дерево                        |
| `/itemname <name\|reset>`                 | `terracraft.core.itemname`            | Имя предмета (MiniMessage)             |
| `/itemlore add\|set\|remove\|clear\|list` | `terracraft.core.itemlore`            | Lore предмета                          |
| `/clear [player]`                         | `terracraft.core.clear[.others]`      | Очистить инвентарь                     |

### Мир / мобы

| Команда                                                                | Право                                 | Описание                |
|------------------------------------------------------------------------|---------------------------------------|-------------------------|
| `/spawnmob <type[:variant]> [name] [health] [count]`                   | `terracraft.core.spawnmob`            | Спавн мобов             |
| `/workbench`                                                           | `terracraft.core.workbench`           | Верстак                 |
| `/anvil` / `/loom` / `/grindstone` / `/stonecutter` / `/smithingtable` | `terracraft.core.<name>`              | Соответствующий верстак |
| `/enderchest [player]`                                                 | `terracraft.core.enderchest[.others]` | Эндерчест               |

### Погода / время

| Команда                                                                 | Право                                       | Описание             |
|-------------------------------------------------------------------------|---------------------------------------------|----------------------|
| `/weather <sun\|rain\|storm> [world\|all]`                              | `terracraft.core.weather[.all]`             | Погода               |
| `/weather lock\|unlock`                                                 | `terracraft.core.weather.lock`              | Зафиксировать погоду |
| `/time <day\|noon\|night\|midnight\|dawn\|dusk> [instant] [world\|all]` | `terracraft.core.time[.all]`                | Время                |
| `/time set\|add\|freeze\|smooth`                                        | `terracraft.core.time[.freeze]` / `.smooth` | Управление временем  |
| `/ptime <preset> [player]`                                              | `terracraft.core.ptime[.others]`            | Личное время         |
| `/pweather <preset> [player]`                                           | `terracraft.core.pweather[.others]`         | Личная погода        |

### Перемещение

| Команда    | Право                     | Описание             |
|------------|---------------------------|----------------------|
| `/top`     | `terracraft.core.top`     | На верхний блок      |
| `/jump`    | `terracraft.core.jump`    | Прыжок до 100 блоков |
| `/compass` | `terracraft.core.compass` | BossBar-компас       |

### Действия над игроком

| Команда                    | Право                           | Описание     |
|----------------------------|---------------------------------|--------------|
| `/ext [player]`            | `terracraft.core.ext[.others]`  | Потушить     |
| `/suicide`                 | `terracraft.core.suicide`       | Самоубийство |
| `/burn <player> [seconds]` | `terracraft.core.burn[.others]` | Поджечь      |
| `/ice <player> [seconds]`  | `terracraft.core.ice[.others]`  | Заморозить   |

### Информация

| Команда            | Право                             | Описание                             |
|--------------------|-----------------------------------|--------------------------------------|
| `/gc`              | `terracraft.core.gc`              | TPS, память, чанки, аптайм           |
| `/getpos [player]` | `terracraft.core.getpos[.others]` | Координаты                           |
| `/depth`           | `terracraft.core.depth`           | Глубина от бедрока                   |
| `/whois <player>`  | `terracraft.core.whois[.others]`  | Подробно об игроке (онлайн + офлайн) |
| `/near [radius]`   | `terracraft.core.near`            | Игроки рядом                         |
| `/seen <player>`   | `terracraft.core.seen[.others]`   | Когда был онлайн (онлайн + офлайн)   |
| `/list`            | `terracraft.core.list`            | Кто онлайн                           |
| `/ping [player]`   | `terracraft.core.ping[.others]`   | Пинг (с цветом)                      |

---

## 🛡 Права

Все права начинаются с `terracraft.core.`. Основные:

- `terracraft.core.use` — доступ к `/tc`.
- `terracraft.core.reload` — `/tc reload`.
- `terracraft.core.info` — `/tc info`.
- `terracraft.core.lang` — `/tc lang`.

Для команд с `.others`:

- `terracraft.core.god` — себе, `terracraft.core.god.others` — другим.
- Аналогично `fly`, `vanish`, `afk`, `heal`, `feed`, `speed`, `gamemode`, `enderchest`, `ptime`, `pweather`, `ext`, `ping`, `getpos`, `hat`, `skull`, `clear`, `whois`, `seen`, `burn`, `ice`.
- `terracraft.core.repair` + `terracraft.core.repair.all`.
- `terracraft.core.weather` + `.weather.all` + `.weather.lock`.
- `terracraft.core.time` + `.time.all` + `.time.freeze` + `.time.smooth`.
- `terracraft.core.enchant` + `.enchant.unsafe` — обход ванильных ограничений (книги, зачарование топора).
- `terracraft.core.vanish.see` — видеть vanished-игроков.
- `terracraft.core.afk.bypass` — не помечать AFK автоматически.
- `terracraft.core.api.sensitive` — доступ к `TerraCoreAdminAPI`.

---

## 🔨 Сборка из исходников

### Требования

- **JDK 25+** ([Adoptium](https://adoptium.net/)).
- **Gradle 9+** (или `./gradlew`).

### Команды

```bash
# Клонировать
git clone https://github.com/luci4ka/TerraCraftCore.git
cd TerraCraftCore

# Собрать JAR (пропуская тесты)
./gradlew clean build -x test

# Собрать с тестами
./gradlew clean test build

# JAR появится в:
# build/libs/TerraCraftCore-1.0.2.jar
```

### Windows

```powershell
.\gradlew.bat clean build -x test
```

### Что внутри JAR

- Ваш код (`ru.terracraft.core.*`, `ru.terracraft.api.*`).
- Релоцированные зависимости:
    - `ru.terracraft.core.libs.bstats`
    - `ru.terracraft.core.libs.configlib`
    - `ru.terracraft.core.libs.updater`
- **Нет** `paper-api` (compileOnly) — берётся с сервера.
- **Нет** `commandapi` (compileOnly) — плагин на сервере.
- **Нет** `placeholderapi` (compileOnly) — плагин на сервере.
- **Нет** `minesql` (compileOnly) — плагин на сервере.

**Размер:** ~1 МБ.

---

## 🧪 Тесты

```bash
# Все тесты
./gradlew test

# Только конкретный класс
./gradlew test --tests "ru.terracraft.core.autotest.LangManagerTest"

# С подробным выводом
./gradlew test --console=plain
```

### Что тестируется

| Класс                | Что проверяет                                                          |
|----------------------|------------------------------------------------------------------------|
| `ConfirmManagerTest` | `consume`/`cancel`/`clear`/`expire`                                    |
| `LangManagerTest`    | Каскад локалей, полнота `.yml` (все `LangKey` есть в `en_US` и `ru_RU`) |

---

## 🏗 Архитектура

```
ru.terracraft
├── api/                       — Публичный API
│   ├── TerraCoreAPI           — безопасные данные
│   ├── TerraCoreAdminAPI      — чувствительные данные (права)
│   ├── dto/PlayerSnapshot     — DTO для офлайн-кэша
│   └── event/                 — события (God/Fly/Vanish/AFK)
├── core/
│   ├── TerraCorePlugin        — главный класс
│   ├── command/
│   │   ├── CommandRegistrar   — регистрация (safe)
│   │   ├── impl/              — ~40 команд
│   │   └── util/Args          — хелпер аргументов
│   ├── config/                — AdminToolsConfig, ConfigManager
│   ├── lang/                  — LangKey, LangManager, MessageService
│   ├── listener/              — God, Vanish, Join, Quit, Afk
│   ├── manager/               — God, Fly, Vanish, Gamemode, AFK, Cooldown, Confirm
│   ├── storage/               — StorageManager, StateStore, Stats, PlayerCache, Mail
│   ├── hook/                  — PlaceholderAPI, UpdateNotifier
│   ├── task/                  — VanishTask, AfkTask
│   └── util/                  — StartupBanner
```

### Thread-safety

- **`MessageService`** — `runOnMainThread` для всех Bukkit-вызовов.
- **Менеджеры** — `ConcurrentHashMap`, чтение из любого потока.
- **`StateStore`/`StatsService`/`PlayerCacheService`** — `runTaskAsynchronously` для записи.
- **`StorageManager`** — пул MineSQL, `try (SQLQuery q = ...)`.

---

## 📄 Лицензия

**MIT** — см. [LICENSE](LICENSE).

---

## 🙏 Благодарности

- [PaperMC](https://papermc.io/) — за отличный сервер.
- [CommandAPI](https://www.spigotmc.org/resources/commandapi.79504/) — за удобную регистрацию команд.
- [MockBukkit](https://github.com/MockBukkit/MockBukkit) — за тесты без сервера.
- [ConfigLib](https://github.com/Exlll/ConfigLib) — за YAML-конфиг из record.
- [Adventure](https://docs.advntr.dev/) — за MiniMessage.
- [MineSQL](https://github.com/CarmJos/MineSQL) — за удобную работу с БД.
- [ez-plugins](https://github.com/ez-plugins/mc-plugin-update-notifier) — за проверку обновлений.

---

<div align="center">

Сделано с ❤️

</div>