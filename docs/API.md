
# TerraCraftCore — Public API

Публичный API для использования в других плагинах.
Два интерфейса: **`TerraCoreAPI`** (безопасные данные) и **`TerraCoreAdminAPI`** (чувствительные данные с правами).

---

## 📦 Подключение

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.luci4ka:TerraCraftCore:v1.0.2'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.luci4ka</groupId>
        <artifactId>TerraCraftCore</artifactId>
        <version>v1.0.2</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

> **Версия** — тег из GitHub (например `v1.0.2`). Точное имя смотрите на [JitPack](https://jitpack.io/#luci4ka/TerraCraftCore).

### `paper-plugin.yml` (обязательно)

```yaml
dependencies:
  server:
    TerraCraftCore:
      load: BEFORE
      required: true
      join-classpath: true   # ← критично для доступа к классам TerraCraftCore
```

> **Без `join-classpath: true`** Paper изолирует класс-лоадеры, и вы получите `NoClassDefFoundError` при первом вызове `TerraCoreAPI`.

> **`required: true`** — если TerraCraftCore обязателен. Если опционален — `required: false` + проверка `api == null` в коде.

---

## 🚀 Быстрый старт

```java
import org.bukkit.Bukkit;
import ru.terracraft.api.TerraCoreAPI;

public class MyPlugin extends JavaPlugin {

    private TerraCoreAPI api;

    @Override
    public void onEnable() {
        api = Bukkit.getServicesManager().load(TerraCoreAPI.class);
        if (api == null) {
            getLogger().severe("TerraCraftCore API not available!");
            return;
        }
        getLogger().info("TerraCraftCore API hooked.");
    }

    public boolean isPlayerAfk(Player player) {
        return api != null && api.isAfk(player);
    }
}
```

---

## 🔌 `TerraCoreAPI` — безопасные данные

Не требует прав. Работает и для офлайн-игроков (если данные есть в памяти/БД).

### Состояния

```java
boolean god      = api.isGod(uuid);
boolean fly      = api.isFlying(uuid);
boolean vanished = api.isVanished(uuid);
boolean afk      = api.isAfk(uuid);

String gamemode = api.gamemode(uuid);   // "SURVIVAL" / "CREATIVE" / ... / null
```

### Bukkit-данные (только онлайн)

Возвращают `null` / `0` / `false` для офлайн-игроков.

```java
String world  = api.world(uuid);
int    ping   = api.ping(uuid);
double hp     = api.health(uuid);
int    food   = api.food(uuid);
int    level  = api.level(uuid);
float  exp    = api.exp(uuid);
float  sat    = api.saturation(uuid);
float  walk   = api.walkSpeed(uuid);
float  flySpd = api.flySpeed(uuid);
```

### Проверки

```java
boolean online = api.isOnline(uuid);
boolean burning = api.isBurning(uuid);
boolean frozen  = api.isFrozen(uuid);
boolean op      = api.isOp(uuid);

// Видит ли viewer игрока target (учитывает vanish + hidePlayer от других плагинов)
boolean canSee = api.canSee(viewer, targetUuid);
```

### Почта и кулдауны

```java
int unread = api.unreadMail(uuid);
int cd     = api.cooldownRemaining(uuid, "heal");  // секунд, 0 = готово
```

### Массовые операции (только онлайн)

```java
boolean nowGod = api.toggleGod(player);
boolean nowFly = api.toggleFly(player);
boolean nowVan = api.toggleVanish(player);
api.setAfk(player, true);
```

### Списки

```java
Set<UUID> gods      = api.gods();
Set<UUID> flying    = api.flying();
Set<UUID> vanished  = api.vanished();
Set<UUID> afk       = api.afk();
```

---

## 🔒 `TerraCoreAdminAPI` — чувствительные данные

Требует право **`terracraft.core.api.sensitive`**. Без права — `null` / `0`.

```java
import ru.terracraft.api.TerraCoreAdminAPI;
import ru.terracraft.api.dto.PlayerSnapshot;

TerraCoreAdminAPI admin = Bukkit.getServicesManager().load(TerraCoreAdminAPI.class);

String ip = admin.ip(sender, uuid);           // null без прав / офлайн
var pos = admin.position(sender, uuid);       // Position(x, y, z, world)
long firstJoin = admin.firstJoin(sender, uuid);
long lastSeen  = admin.lastSeen(sender, uuid);

PlayerSnapshot snap = admin.cache(sender, uuid);
if (snap != null) {
    getLogger().info("Last seen at " + snap.lastX() + "," + snap.lastY()
                     + " in " + snap.lastWorld());
}
```

### `PlayerSnapshot` (record)

```java
public record PlayerSnapshot(
    UUID uuid,
    String name,
    long firstJoin,
    long lastSeen,
    String lastIp,
    String lastWorld,
    double lastX, double lastY, double lastZ,
    String lastGamemode
) {}
```

---

## 📡 События

Все события — в `ru.terracraft.api.event`. Наследуют `TerraStateChangeEvent` (не cancellable, вызываются **после** изменения).

```java
import ru.terracraft.api.event.*;

@EventHandler
public void onGod(TerraGodChangeEvent e) {
    if (e.getNewState()) {
        e.getPlayer().sendMessage("Godmode enabled!");
    }
}

@EventHandler
public void onFly(TerraFlyChangeEvent e) { /* ... */ }

@EventHandler
public void onVanish(TerraVanishChangeEvent e) { /* ... */ }

@EventHandler
public void onAfk(TerraAfkChangeEvent e) { /* ... */ }
```

### Общий базовый класс

```java
@EventHandler
public void onAnyState(TerraStateChangeEvent e) {
    Player p = e.getPlayer();
    boolean on = e.getNewState();
    getLogger().info(p.getName() + " state changed: " + on);
}
```

> ⚠ **`@EventHandler`** не сработает на `TerraStateChangeEvent` — Bukkit регистрирует события **по конкретному классу**. Нужен отдельный метод для каждого события (`TerraGodChangeEvent`, `TerraFlyChangeEvent`, ...) или `EventPriority.MONITOR` + ручной `instanceof`.

---

## 💡 Примеры использования

### Пример 1: префикс AFK в чате

```java
@EventHandler(priority = EventPriority.LOWEST)
public void onChat(AsyncChatEvent e) {
    if (api != null && api.isAfk(e.getPlayer())) {
        e.renderer((source, sourceDisplayName, message, viewer) ->
            Component.text("[AFK] ").append(sourceDisplayName));
    }
}
```

### Пример 2: не считать урон от vanish/god

```java
@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
public void onDamage(EntityDamageByEntityEvent e) {
    if (!(e.getDamager() instanceof Player p)) return;
    if (api == null) return;
    if (api.isVanished(p) || api.isGod(p)) {
        e.setCancelled(true);
    }
}
```

### Пример 3: скрыть vanished из scoreboard

```java
for (Player viewer : Bukkit.getOnlinePlayers()) {
    if (viewer.hasPermission("terracraft.core.vanish.see")) continue;
    // ваш код фильтрации scoreboard
    if (api != null && api.isVanished(targetUuid)) {
        // не показывать targetUuid в списке
    }
}
```

### Пример 4: `/info` команда

```java
Player target = ...;
sender.sendMessage("AFK: " + api.isAfk(target));
sender.sendMessage("God: " + api.isGod(target));
sender.sendMessage("Ping: " + api.ping(target) + "ms");

// Чувствительные данные — только с правами
TerraCoreAdminAPI admin = Bukkit.getServicesManager().load(TerraCoreAdminAPI.class);
String ip = admin.ip(sender, target.getUniqueId());   // null без прав
if (ip != null) sender.sendMessage("IP: " + ip);
```

---

## ⚠️ Важно

### Не shade'ите API

`compileOnly` / `provided` — **правильно**. Если сделаете `implementation` (Gradle) или `compile` (Maven), API-классы попадут в **ваш** JAR — и в рантайме будет **две копии** `TerraCoreAPI`, `TerraGodChangeEvent.class`. Bukkit будет кидать события из одной копии, а `instanceof` в вашем коде — из другой. **События не сработают.**

**Правило:** `compileOnly` + `join-classpath: true` в `paper-plugin.yml`.

### `join-classpath: true` обязателен

Paper изолирует класс-лоадеры плагинов. Без `join-classpath: true` ваш плагин **не увидит** `TerraCoreAPI` в рантайме, даже если `TerraCraftCore` установлен.

### Проверка `null`

`Bukkit.getServicesManager().load(TerraCoreAPI.class)` вернёт `null`, если `TerraCraftCore` не установлен или не включён. **Всегда** проверяйте:

```java
TerraCoreAPI api = Bukkit.getServicesManager().load(TerraCoreAPI.class);
if (api == null) {
    getLogger().warning("TerraCraftCore not installed — feature disabled.");
    return;
}
```

---

## 🐛 Проблемы

| Проблема | Причина | Решение |
|---|---|---|
| `NoClassDefFoundError: ru/terracraft/api/TerraCoreAPI` | Нет `join-classpath: true` | Добавьте в `paper-plugin.yml` |
| `api == null` | TerraCraftCore не установлен | `required: true` или проверка `null` |
| `ClassCastException` при событии | Shade'ите API | Уберите `implementation`, используйте `compileOnly` |
| Событие не срабатывает | `@EventHandler` на базовом классе | Отдельный метод на каждое событие |
| JitPack не находит артефакт | Неправильная версия | Проверьте тег на [JitPack](https://jitpack.io/#luci4ka/TerraCraftCore) |

---

## 🔗 Ссылки

- [JitPack — TerraCraftCore](https://jitpack.io/#luci4ka/TerraCraftCore)
- [GitHub — Issues](https://github.com/luci4ka/TerraCraftCore/issues)
- [CONTRIBUTING.md](../CONTRIBUTING.md) — правила PR