# TerraCraftCore — PlaceholderAPI

Полный список плейсхолдеров `%terracore_*%` и готовые примеры использования
в TAB, чате, scoreboard, DeluxeMenus, TAB-плагинах и других.

> **Требования:** [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) 2.12.3+.
> Если PAPI не установлен — expansion не регистрируется, плейсхолдеры возвращают пустую строку.

---

## 📋 Содержание

- [Быстрый старт](#-быстрый-старт)
- [Список плейсхолдеров](#-список-плейсхолдеров)
- [Примеры интеграции](#-примеры-интеграции)
  - [TAB (NEZNAMY)](#пример-1-tab-neznamy)
  - [DeluxeMenus](#пример-2-deluxemenus)
  - [Chat (prefix)](#пример-3-чат-с-afk-префиксом)
  - [Scoreboard](#пример-4-scoreboard-со-статусом)
  - [FeatherBoard](#пример-5-featherboard)
  - [Motd / TAB-Header](#пример-6-motd--tab-header)
  - [BossBar (свой плагин)](#пример-7-bossbar-через-api)
  - [Holographic Displays](#пример-8-holographic-displays)
- [Условные плейсхолдеры](#-условные-плейсхолдеры)
- [Отладка](#-отладка)

---

## 🚀 Быстрый старт

1. Установите **PlaceholderAPI**.
2. Установите **TerraCraftCore**.
3. Проверьте: `/papi parse me %terracore_afk%` → `true` или `false`.
4. Используйте в любом плагине, поддерживающем PAPI.

**Если плейсхолдер не работает:**
- `/papi list` — есть ли `terracore` в списке expansion'ов?
- `/papi reload` — перезагрузить expansion'ы.
- `/papi parse me %terracore_ping%` — вернёт число.

---

## 📋 Список плейсхолдеров

### Состояния (работают и для офлайн через `player`)

| Плейсхолдер | Значение | Пример |
|---|---|---|
| `%terracore_god%` | `true` / `false` | `false` |
| `%terracore_fly%` | `true` / `false` | `true` |
| `%terracore_vanished%` | `true` / `false` | `false` |
| `%terracore_afk%` | `true` / `false` | `true` |

### Счётчики (глобальные)

| Плейсхолдер | Значение | Пример |
|---|---|---|
| `%terracore_god_count%` | Сколько в god | `2` |
| `%terracore_fly_count%` | Сколько в fly | `5` |
| `%terracore_vanished_count%` | Сколько vanished | `1` |
| `%terracore_afk_count%` | Сколько AFK | `3` |
| `%terracore_vanished_list%` | Имена vanished через `, ` | `Steve, Alex` |

### Bukkit-данные (только онлайн)

| Плейсхолдер | Значение | Пример |
|---|---|---|
| `%terracore_ping%` | Пинг (мс) | `42` |
| `%terracore_ping_color%` | `green` / `yellow` / `red` | `green` |
| `%terracore_ping_colored%` | MiniMessage `<green>42</green>` | `<green>42</green>` |
| `%terracore_world%` | Имя мира | `world` |
| `%terracore_gamemode%` | Локализованное имя режима | `Выживание` |
| `%terracore_gamemode_id%` | `SURVIVAL` / `CREATIVE` / ... | `SURVIVAL` |
| `%terracore_health%` | HP (0..20) | `18.5` |
| `%terracore_health_hearts%` | HP в сердцах (0..10) | `9.2` |
| `%terracore_health_percent%` | HP в % | `92` |
| `%terracore_food%` | Еда (0..20) | `20` |
| `%terracore_saturation%` | Сатурация | `5.0` |
| `%terracore_level%` | Уровень опыта | `30` |
| `%terracore_exp%` | Прогресс (0..1) | `0.45` |
| `%terracore_walk_speed%` | Скорость ходьбы | `0.20` |
| `%terracore_fly_speed%` | Скорость полёта | `0.10` |
| `%terracore_fire_ticks%` | Тики горения | `0` |
| `%terracore_freeze_ticks%` | Тики заморозки | `0` |
| `%terracore_burning%` | `true` / `false` | `false` |
| `%terracore_frozen%` | `true` / `false` | `false` |

### Время и погода мира

| Плейсхолдер | Значение | Пример |
|---|---|---|
| `%terracore_world_time%` | Тики времени | `6000` |
| `%terracore_world_time_name%` | Локализованное имя | `полдень` |
| `%terracore_world_weather%` | `sun` / `rain` / `storm` | `sun` |
| `%terracore_world_weather_name%` | Локализованное | `☀ ясно` |

### Компас

| Плейсхолдер | Значение | Пример |
|---|---|---|
| `%terracore_compass%` | `N` / `E` / `S` / `W` | `N` |
| `%terracore_compass_localized%` | Локализованное | `Север` |
| `%terracore_compass_sector%` | `0..3` (0=N, 1=E, 2=S, 3=W) | `0` |

### Кулдауны

| Плейсхолдер | Значение | Пример |
|---|---|---|
| `%terracore_cooldown_<cmd>%` | Секунд до конца (с `+1`) | `5` |
| `%terracore_cooldown_<cmd>_raw%` | Секунд без `+1` | `4` |

**`<cmd>`** — `god`, `fly`, `heal`, `feed`, `repair`, `hat`, `workbench`, `enderchest`, `vanish`, `spawnmob`.

**Пример:** `%terracore_cooldown_heal%` → `0`, если кулдауна нет.

### Почта

| Плейсхолдер | Значение | Пример |
|---|---|---|
| `%terracore_mail_unread%` | Непрочитанных писем | `3` |

### Лимиты из конфига

| Плейсхолдер | Значение | Пример |
|---|---|---|
| `%terracore_enchant_max%` | `enchant.max-level` | `255` |
| `%terracore_give_max%` | `give.max-amount` | `6400` |
| `%terracore_spawnmob_max%` | `spawn-mob.max-count` | `50` |
| `%terracore_near_radius%` | `near.default-radius` | `200` |
| `%terracore_compass_duration%` | `compass.duration-seconds` | `30` |

### Сервер (`/gc`)

| Плейсхолдер | Значение | Пример |
|---|---|---|
| `%terracore_gc_tps%` | TPS (1m, 5m, 15m) | `20.00, 19.98, 19.95` |
| `%terracore_gc_tps_1m%` | TPS за 1 минуту | `20.00` |
| `%terracore_gc_tps_5m%` | TPS за 5 минут | `19.98` |
| `%terracore_gc_tps_15m%` | TPS за 15 минут | `19.95` |
| `%terracore_gc_tps_color%` | `green` / `yellow` / `red` | `green` |
| `%terracore_gc_tps_colored%` | MiniMessage `<green>20.00...</green>` | `<green>...</green>` |
| `%terracore_gc_memory_used%` | Использовано МБ | `512` |
| `%terracore_gc_memory_max%` | Максимум МБ | `2048` |
| `%terracore_gc_memory_free%` | Свободно МБ | `1536` |
| `%terracore_gc_memory_percent%` | % памяти | `25` |
| `%terracore_gc_memory_colored%` | MiniMessage `<green>25%</green>` | `<green>25%</green>` |
| `%terracore_gc_chunks%` | Загружено чанков | `1024` |
| `%terracore_gc_entities%` | Загружено сущностей | `512` |
| `%terracore_gc_uptime%` | Аптайм (`1h 23m 45s`) | `2h 15m 30s` |
| `%terracore_gc_uptime_seconds%` | Аптайм в секундах | `8130` |
| `%terracore_gc_uptime_minutes%` | Аптайм в минутах | `135` |
| `%terracore_gc_uptime_hours%` | Аптайм в часах | `2` |

---

## 🔌 Примеры интеграции

### Пример 1: TAB (NEZNAMY)

В `plugins/TAB/config.yml` или через `/tab`:

```yaml
# Префикс AFK в TAB-листе
belowname-objective:
  value: "%terracore_afk%"
  title: "AFK"

# Header / Footer
header-footer:
  enabled: true
  header:
    - "<gradient:#00d4ff:#7b2ff7><bold>TerraCraft</bold></gradient>"
    - "<white>Онлайн: <green>%server_online%</green> / <gray>%server_max_players%</gray></white>"
  footer:
    - "<gray>TPS: <green>%terracore_gc_tps_1m%</green> · Пинг: <yellow>%terracore_ping%ms</yellow></gray>"
    - "<gray>AFK: <white>%terracore_afk_count%</white> · Vanish: <white>%terracore_vanished_count%</white></gray>"

# Условный префикс AFK
playerlist-objective:
  enabled: true
  value: "%terracore_afk%"
```

**Результат:** в TAB-листе AFK-игроки получают префикс, в footer — счётчики.

---

### Пример 2: DeluxeMenus

В `plugins/DeluxeMenus/gui_menus/player_info.yml`:

```yaml
menu_title: "Информация о %player_name%"
open_command: playerinfo
size: 27
items:
  afk:
    material: CLOCK
    slot: 10
    display_name: "<yellow>AFK"
    lore:
      - "<gray>Статус: <white>%terracore_afk%</white>"
  god:
    material: NETHERITE_CHESTPLATE
    slot: 11
    display_name: "<gold>God"
    lore:
      - "<gray>Статус: <white>%terracore_god%</white>"
  vanish:
    material: ENDER_PEARL
    slot: 12
    display_name: "<dark_purple>Vanish"
    lore:
      - "<gray>Статус: <white>%terracore_vanished%</white>"
  mail:
    material: PAPER
    slot: 13
    display_name: "<aqua>Почта"
    lore:
      - "<gray>Непрочитанных: <white>%terracore_mail_unread%</white>"
```

**Результат:** GUI с индикаторами состояний.

---

### Пример 3: Чат с AFK-префиксом

Через **ChatControl** / **EssentialsX Chat** / **VentureChat** — в шаблоне:

```yaml
# ChatControl / format
format: "{terracore_afk:&7[AFK] &f}{displayname}&7: &f{message}"
```

Или через **свой плагин** (см. `EXAMPLES.md` → Пример 4):

```java
@EventHandler
public void onChat(AsyncChatEvent e) {
    if (!api.isAfk(e.getPlayer())) return;

    Component prefix = Component.text("[AFK] ").color(NamedTextColor.GRAY);
    e.renderer((source, name, message, viewer) ->
            prefix.append(name).append(Component.text(": ")).append(message));
}
```

---

### Пример 4: Scoreboard со статусом

Через **FeatherBoard** / **TAB** / **Scoreboard-revision** — в строке:

```yaml
# FeatherBoard
lines:
  - "&7&m----------------"
  - "&fСтатус: {terracore_afk:&7AFK&f}{terracore_god:&6GOD&f}"
  - "&fПинг: {terracore_ping_colored}"
  - "&fHP: &c{terracore_health_percent}%"
  - "&7&m----------------"
```

**Условные плейсхолдеры** — зависит от плагина. FeatherBoard использует `{placeholder:значение_если_true}`, но не поддерживает «если false». Для сложных условий — **свой плагин** (см. `EXAMPLES.md` → Пример 3).

---

### Пример 5: FeatherBoard

```yaml
# plugins/FeatherBoard/boards/default.yml
title: "&8&lTerraCraft"
lines:
  - "&7&m━━━━━━━━━━━━━━━"
  - "&fИгрок: &e{player}"
  - "&fAFK: {terracore_afk:&7да}{terracore_afk:&aнет}"   # ❌ не сработает — FeatherBoard не поддерживает false-ветку
  - "&fПинг: {terracore_ping_colored}"
  - "&fTPS: {terracore_gc_tps_1m}"
  - "&7&m━━━━━━━━━━━━━━━"
```

**Проблема:** FeatherBoard не поддерживает условные плейсхолдеры с `false`-веткой. Для «да/нет» — используйте **свой expansion** (см. `EXAMPLES.md` → Пример 16):

```java
case "afk_display" -> api.isAfk(id) ? "§7[AFK]" : "";
```

И в FeatherBoard: `{myplugin_afk_display}`.

---

### Пример 6: MOTD / TAB-Header

В **server.properties** — MOTD не поддерживает PAPI (это vanilla). Но **TAB** / **Motd** плагины — да.

**TAB header/footer:**

```yaml
header:
  - "<gradient:#00d4ff:#7b2ff7><bold>TerraCraft</bold></gradient>"
  - "<green>TPS: %terracore_gc_tps_colored%</green>"
  - "<gray>Память: %terracore_gc_memory_colored%</gray>"
footer:
  - "<gray>AFK: <white>%terracore_afk_count%</white> · Vanish: <white>%terracore_vanished_count%</white></gray>"
  - "<gray>Онлайн: <white>%server_online%</white></gray>"
```

---

### Пример 7: BossBar через API

Если хотите **свой** BossBar с AFK-счётчиком (не через `/compass`):

```java
public class ServerBossBar implements Runnable {

    private final TerraCoreAPI api;
    private final BossBar bar;
    private final Plugin plugin;

    public ServerBossBar(@NotNull TerraCoreAPI api, @NotNull Plugin plugin) {
        this.api = api;
        this.plugin = plugin;
        this.bar = BossBar.bossBar(
                Component.text("AFK: 0"),
                1.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS
        );
    }

    @Override
    public void run() {
        int afkCount = api.afk().size();
        int vanishedCount = api.vanished().size();

        Component name = MiniMessage.miniMessage().deserialize(
                "<yellow>AFK:</yellow> <white>" + afkCount + "</white> "
                + "<dark_purple>· Vanish:</dark_purple> <white>" + vanishedCount + "</white>"
        );
        bar.name(name);
    }

    public void show(Player p) {
        p.showBossBar(bar);
    }

    public BossBar bar() {
        return bar;
    }
}
```

**Использование в `onEnable`:**

```java
ServerBossBar bossBar = new ServerBossBar(api, this);
new BukkitRunnable() {
    @Override public void run() { bossBar.run(); }
}.runTaskTimer(this, 0L, 20L * 3);   // раз в 3 секунды

@EventHandler
public void onJoin(PlayerJoinEvent e) {
    bossBar.show(e.getPlayer());
}
```

---

### Пример 8: Holographic Displays

Голограмма с AFK-счётчиком над спавном:

```yaml
# plugins/HolographicDisplays/holograms/afk.yml
lines:
  - "&7&m━━━━━━━━━━━━━━━"
  - "&f&lAFK"
  - "&f%terracore_afk_count% игроков"
  - "&7&m━━━━━━━━━━━━━━━"
location:
  world: world
  x: 0.5
  y: 65
  z: 0.5
```

**Проблема:** Holographic Displays обновляет PAPI-плейсхолдеры **раз в 10 секунд** (по умолчанию). Если нужно чаще — настройте `refresh-interval` в конфиге.

---

## 🎯 Условные плейсхолдеры

**TerraCraftCore не поддерживает** условные плейсхолдеры (`%terracore_afk_true%` / `%terracore_afk_false%`). Но есть **три способа** сделать условие:

### Способ 1: PAPI-нативный `papi:` (не работает для boolean)

PlaceholderAPI **сам** не умеет `if/else` для boolean. Только для строк:

```
%terracore_afk%   → "true" / "false"
```

### Способ 2: Свой expansion (рекомендуемый)

Добавьте в свой PAPI-expansion (см. `EXAMPLES.md` → Пример 16):

```java
case "afk_display" -> api.isAfk(id) ? "§7[AFK]" : "";
case "afk_color"   -> api.isAfk(id) ? "§7" : "§f";
case "afk_word"    -> api.isAfk(id) ? "да" : "нет";
```

**Использование:**

```yaml
"&fAFK: {myplugin_afk_word}"
"&f{myplugin_afk_display} &f{player}"
```

### Способ 3: Плагины с условиями

**DeluxeMenus** поддерживает `view_requirement`:

```yaml
view_requirement:
  requirements:
    is_afk:
      type: string equals
      input: "%terracore_afk%"
      output: "true"
```

**TAB** поддерживает условные элементы через `conditions`:

```yaml
conditions:
  afk:
    - "%terracore_afk%=true"
```

---

## 🐛 Отладка

### `/papi parse me %terracore_afk%`

Должно вернуть `true` или `false`. Если возвращает `%terracore_afk%` — expansion не загружен.

### `/papi list`

Проверьте, что `terracore` в списке. Если нет:

1. Установлен ли **PlaceholderAPI**?
2. Установлен ли **TerraCraftCore**?
3. `/papi reload` — перезагрузить.

### `/papi reload`

Перезагружает **все** expansion'ы. Если TerraCraftCore добавлен после старта — `/papi reload` подхватит.

### `/papi parse <player> %terracore_ping%`

Возвращает число (`42`), а не `%terracore_ping%`. Если возвращает плейсхолдер — expansion не видит игрока (online?) или `terracore` не загружен.

---

## 🔗 Ссылки

- [PlaceholderAPI — документация](https://wiki.placeholderapi.com/)
- [API.md](API.md) — полная документация TerraCraftCore API
- [EXAMPLES.md](EXAMPLES.md) — примеры интеграции (TAB, чат, PvP, GUI)
- [JitPack — TerraCraftCore](https://jitpack.io/#luci4ka/TerraCraftCore)