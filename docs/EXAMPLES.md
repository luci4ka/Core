# TerraCraftCore — Примеры интеграции

Готовые примеры использования [`TerraCoreAPI`](API.md) в других плагинах.
Все примеры предполагают, что API подключён (см. [API.md](API.md)).

---

## 📋 Содержание

- [TAB / Scoreboard](#-tab--scoreboard)
- [Чат](#-чат)
- [PvP / Защита](#-pvp--защита)
- [Статистика](#-статистика)
- [Кастомная команда `/info`](#-кастомная-команда-info)
- [Проверка AFK в своих командах](#-проверка-afk-в-своих-командах)
- [Меню / GUI](#-меню--gui)
- [PlaceholderAPI — свои плейсхолдеры](#-placeholderapi--свои-плейсхолдеры)

---

## 🎨 TAB / Scoreboard

### Пример 1: префикс `[AFK]` в TAB

```java
public class TabListener implements Listener {

    private final TerraCoreAPI api;

    public TabListener(@NotNull TerraCoreAPI api) {
        this.api = api;
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        updateTab(e.getPlayer());
    }

    @EventHandler
    public void onAfkChange(@NotNull TerraAfkChangeEvent e) {
        updateTab(e.getPlayer());
    }

    private void updateTab(@NotNull Player p) {
        String prefix = api.isAfk(p) ? "&7[AFK] &f" : "";

        p.playerListName(Component.text(prefix + p.getName()));
    }
}
```

**Результат:** в TAB-листе AFK-игроки получают серый префикс `[AFK]`.

---

### Пример 2: скрыть vanished из TAB (свой, не через `hidePlayer`)

Если `vanish.hide-from-tab: false` в конфиге TerraCraftCore — TAB не скрывает. Тогда ваш плагин может сам фильтровать:

```java
public class TabUpdater implements Runnable {

    private final TerraCoreAPI api;

    public TabUpdater(@NotNull TerraCoreAPI api) {
        this.api = api;
    }

    @Override
    public void run() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (viewer.equals(target)) continue;

                boolean hidden = api.isVanished(target)
                        && !viewer.hasPermission("terracraft.core.vanish.see");

                if (hidden) viewer.hidePlayer(plugin, target);
                else        viewer.showPlayer(plugin, target);
            }
        }
    }
}
```

---

### Пример 3: Scoreboard-строка со статусом

```java
public class ScoreboardUpdater implements Runnable {

    private final TerraCoreAPI api;
    private final Scoreboard board;

    public ScoreboardUpdater(@NotNull TerraCoreAPI api, @NotNull Scoreboard board) {
        this.api = api;
        this.board = board;
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Objective obj = board.getObjective("stats");
            if (obj == null) continue;

            String line = buildStatusLine(p);
            obj.getScore("status").setScore(1);
            // в реальном коде — через Team.prefix(Component)
        }
    }

    private String buildStatusLine(Player p) {
        StringBuilder sb = new StringBuilder();
        if (api.isAfk(p))      sb.append("💤 ");
        if (api.isGod(p))      sb.append("⚡ ");
        if (api.isVanished(p)) sb.append("👻 ");
        if (api.isFlying(p))   sb.append("✈ ");
        return sb.toString().trim();
    }
}
```

---

## 💬 Чат

### Пример 4: префикс `[AFK]` в чате (Paper / Adventure)

```java
public class ChatListener implements Listener {

    private final TerraCoreAPI api;

    public ChatListener(@NotNull TerraCoreAPI api) {
        this.api = api;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(@NotNull AsyncChatEvent e) {
        if (!api.isAfk(e.getPlayer())) return;

        // Добавляем префикс к отображаемому имени
        Component prefix = Component.text("[AFK] ")
                .color(NamedTextColor.GRAY);

        e.renderer((source, sourceDisplayName, message, viewer) ->
                prefix.append(sourceDisplayName).append(Component.text(": ")).append(message));
    }
}
```

> **AsyncChatEvent** — async. `api.isAfk()` — потокобезопасно (`ConcurrentHashMap`), можно вызывать из async.

---

### Пример 5: скрыть сообщения vanished от тех, кто их не видит

Если `vanish` включён, но чат не скрывает:

```java
@EventHandler(priority = EventPriority.LOWEST)
public void onChat(@NotNull AsyncChatEvent e) {
    Player sender = e.getPlayer();
    if (!api.isVanished(sender)) return;

    e.viewers().removeIf(viewer ->
            !viewer.hasPermission("terracraft.core.vanish.see")
                    && !viewer.equals(sender));
}
```

---

### Пример 6: индикатор `[V]` для vanished в чате (только для тех, кто видит)

```java
@EventHandler(priority = EventPriority.LOWEST)
public void onChat(@NotNull AsyncChatEvent e) {
    if (!api.isVanished(e.getPlayer())) return;

    Component prefix = Component.text("[V] ")
            .color(NamedTextColor.DARK_GRAY);

    e.renderer((source, sourceDisplayName, message, viewer) -> {
        if (!viewer.hasPermission("terracraft.core.vanish.see")) {
            // Не-админ не должен видеть vanished вообще — уже отфильтровано выше
            return Component.text("");
        }
        return prefix.append(sourceDisplayName).append(Component.text(": ")).append(message);
    });
}
```

---

## ⚔ PvP / Защита

### Пример 7: запретить урон от vanished/god

```java
public class PvpListener implements Listener {

    private final TerraCoreAPI api;

    public PvpListener(@NotNull TerraCoreAPI api) {
        this.api = api;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(@NotNull EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player damager)) return;

        if (api.isVanished(damager) || api.isGod(damager)) {
            e.setCancelled(true);
        }
    }
}
```

---

### Пример 8: не считать AFK-игроков в PvP-статистике

```java
@EventHandler(priority = EventPriority.MONITOR)
public void onKill(@NotNull PlayerDeathEvent e) {
    Player killer = e.getEntity().getKiller();
    if (killer == null) return;

    // Не считать килы в AFK
    if (api.isAfk(killer)) return;

    stats.addKill(killer, e.getEntity());
}
```

---

### Пример 9: запретить атаку AFK-игроков

```java
@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
public void onDamage(@NotNull EntityDamageByEntityEvent e) {
    if (!(e.getEntity() instanceof Player victim)) return;

    if (api.isAfk(victim)) {
        e.setCancelled(true);

        if (e.getDamager() instanceof Player damager) {
            damager.sendActionBar(Component.text("Нельзя бить AFK-игроков.")
                    .color(NamedTextColor.RED));
        }
    }
}
```

---

## 📊 Статистика

### Пример 10: не считать статистику для AFK/god/vanish

```java
public class StatsListener implements Listener {

    private final TerraCoreAPI api;

    public StatsListener(@NotNull TerraCoreAPI api) {
        this.api = api;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent e) {
        Player p = e.getPlayer();

        if (api.isAfk(p))    return;   // не считать AFK
        if (api.isGod(p))    return;   // god — для тестов, не статистика
        if (api.isVanished(p)) return; // vanish — скрытая активность

        stats.addBlockBreak(p);
    }
}
```

---

### Пример 11: топ по онлайну — исключить AFK

```java
public List<Player> topByPlaytime() {
    return Bukkit.getOnlinePlayers().stream()
            .filter(p -> !api.isAfk(p))   // только активные
            .sorted(Comparator.comparingLong(
                    p -> -stats.getPlaytime(p.getUniqueId())))
            .limit(10)
            .toList();
}
```

---

## 🛠 Кастомная команда `/info`

### Пример 12: свой `/info` с данными из API

```java
public class InfoCommand implements CommandExecutor {

    private final TerraCoreAPI api;
    private final TerraCoreAdminAPI admin;

    public InfoCommand(@NotNull TerraCoreAPI api, @NotNull TerraCoreAdminAPI admin) {
        this.api = api;
        this.admin = admin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Использование: /info <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("Игрок не найден.");
            return true;
        }

        UUID id = target.getUniqueId();

        sender.sendMessage("=== " + target.getName() + " ===");
        sender.sendMessage("Мир: " + api.world(id));
        sender.sendMessage("Пинг: " + api.ping(id) + "ms");
        sender.sendMessage("HP: " + api.health(id) + " / 20");
        sender.sendMessage("Еда: " + api.food(id) + " / 20");
        sender.sendMessage("Уровень: " + api.level(id));
        sender.sendMessage("Режим: " + api.gamemode(id));

        sender.sendMessage("God: " + (api.isGod(id) ? "✓" : "✗"));
        sender.sendMessage("Fly: " + (api.isFlying(id) ? "✓" : "✗"));
        sender.sendMessage("Vanish: " + (api.isVanished(id) ? "✓" : "✗"));
        sender.sendMessage("AFK: " + (api.isAfk(id) ? "✓" : "✗"));

        // Чувствительные данные — только с правами
        String ip = admin.ip(sender, id);
        if (ip != null) sender.sendMessage("IP: " + ip);

        var pos = admin.position(sender, id);
        if (pos != null) {
            sender.sendMessage("Позиция: " + pos.x() + ", " + pos.y() + ", " + pos.z()
                    + " в " + pos.world());
        }

        long firstJoin = admin.firstJoin(sender, id);
        if (firstJoin > 0) {
            sender.sendMessage("Первый вход: " + new Date(firstJoin));
        }

        return true;
    }
}
```

**Регистрация в `onEnable()`:**

```java
TerraCoreAPI api = Bukkit.getServicesManager().load(TerraCoreAPI.class);
TerraCoreAdminAPI admin = Bukkit.getServicesManager().load(TerraCoreAdminAPI.class);

if (api != null && admin != null) {
    getCommand("info").setExecutor(new InfoCommand(api, admin));
} else {
    getLogger().warning("TerraCraftCore not installed — /info disabled.");
}
```

---

## 💤 Проверка AFK в своих командах

### Пример 13: не выполнять команду, если игрок AFK

```java
@EventHandler(priority = EventPriority.LOWEST)
public void onCommand(@NotNull PlayerCommandPreprocessEvent e) {
    String msg = e.getMessage().toLowerCase();
    if (!msg.startsWith("/kit") && !msg.startsWith("/shop")) return;

    if (api.isAfk(e.getPlayer())) {
        e.setCancelled(true);
        e.getPlayer().sendMessage(Component.text("Сначала выйдите из AFK.")
                .color(NamedTextColor.RED));
    }
}
```

---

### Пример 14: авто-кик AFK через N минут (свой, не через `afk.kick-after-seconds`)

Если `afk.kick-after-seconds: 0` в TerraCraftCore — можно сделать свой кик:

```java
public class AfkKickTask extends BukkitRunnable {

    private static final long KICK_AFTER_MS = 15 * 60 * 1000L;   // 15 минут
    private final TerraCoreAPI api;

    public AfkKickTask(@NotNull TerraCoreAPI api) {
        this.api = api;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!api.isAfk(p)) continue;
            if (p.hasPermission("myplugin.afk.bypass")) continue;

            long lastActivity = api.lastActivity(p.getUniqueId());
            if (lastActivity <= 0) continue;

            if (now - lastActivity >= KICK_AFTER_MS) {
                p.kick(Component.text("Вы были кикнуты за AFK 15 минут."));
            }
        }
    }
}

// onEnable:
new AfkKickTask(api).runTaskTimer(this, 20L * 60, 20L * 60);   // раз в минуту
```

---

## 🖱 Меню / GUI

### Пример 15: GUI игроков с индикатором AFK/Vanish

```java
public class PlayerListMenu implements InventoryHolder {

    private static final int SIZE = 54;
    private final TerraCoreAPI api;
    private final Inventory inv;

    public PlayerListMenu(@NotNull TerraCoreAPI api) {
        this.api = api;
        this.inv = Bukkit.createInventory(this, SIZE, Component.text("Игроки"));

        int slot = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (slot >= SIZE) break;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(p);

            // Индикатор статуса
            String status = buildStatus(p);
            meta.displayName(Component.text(p.getName() + " " + status));
            meta.lore(List.of(
                    Component.text("Пинг: " + api.ping(p) + "ms").color(NamedTextColor.GRAY),
                    Component.text("HP: " + api.health(p)).color(NamedTextColor.RED)
            ));

            head.setItemMeta(meta);
            inv.setItem(slot++, head);
        }
    }

    private String buildStatus(Player p) {
        StringBuilder sb = new StringBuilder();
        if (api.isAfk(p))      sb.append("💤");
        if (api.isGod(p))      sb.append("⚡");
        if (api.isVanished(p)) sb.append("👻");
        if (api.isFlying(p))   sb.append("✈");
        return sb.toString();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
```

---

## 🔌 PlaceholderAPI — свои плейсхолдеры

### Пример 16: свой expansion на базе `TerraCoreAPI`

```java
public class MyExpansion extends PlaceholderExpansion {

    private final TerraCoreAPI api;

    public MyExpansion(@NotNull TerraCoreAPI api) {
        this.api = api;
    }

    @Override public @NotNull String getIdentifier() { return "myplugin"; }
    @Override public @NotNull String getAuthor()     { return "you"; }
    @Override public @NotNull String getVersion()    { return "1.0.0"; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) return null;
        UUID id = player.getUniqueId();

        return switch (params.toLowerCase()) {
            case "afk"      -> String.valueOf(api.isAfk(id));
            case "god"      -> String.valueOf(api.isGod(id));
            case "vanish"   -> String.valueOf(api.isVanished(id));
            case "fly"      -> String.valueOf(api.isFlying(id));
            case "status"   -> statusString(id);
            default         -> null;
        };
    }

    private String statusString(UUID id) {
        if (api.isAfk(id))      return "AFK";
        if (api.isVanished(id)) return "VANISHED";
        if (api.isGod(id))      return "GOD";
        return "PLAYING";
    }
}

// onEnable:
if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
    new MyExpansion(api).register();
}
```

**Использование:** `%myplugin_status%` → `AFK` / `VANISHED` / `GOD` / `PLAYING`.

---

## ⚠️ Что важно помнить

1. **`api == null`** — если TerraCraftCore не установлен. Всегда проверяйте.
2. **`join-classpath: true`** в `paper-plugin.yml` — без него `NoClassDefFoundError`.
3. **`compileOnly`**, не `implementation` — иначе `ClassCastException` / события не сработают.
4. **`AsyncChatEvent`** — async. `api.isAfk()` потокобезопасен, можно вызывать.
5. **`TerraStateChangeEvent`** — не cancellable, вызывается **после** изменения.
6. **`TerraCoreAdminAPI`** — только с правом `terracraft.core.api.sensitive`.

---

## 🔗 Ссылки

- [API.md](API.md) — полная документация API
- [JitPack — TerraCraftCore](https://jitpack.io/#luci4ka/TerraCraftCore)
- [CONTRIBUTING.md](../CONTRIBUTING.md) — правила PR