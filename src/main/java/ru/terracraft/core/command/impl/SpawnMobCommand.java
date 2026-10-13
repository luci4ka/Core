package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.lang.LangKey;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * /spawnmob <тип[:вариант]> [имя] [здоровье] [количество]
 * <p>
 * Тип: zombie, sheep:red, cow:baby, creeper:powered, wolf:angry, ...
 * Имя: строка с пробелами (через GreedyString) — только если первый аргумент после типа не число
 * Здоровье: 1..1024, применяется через Attribute.MAX_HEALTH
 * Количество: 1..50
 * <p>
 * Примеры:
 *   /spawnmob zombie
 *   /spawnmob zombie 5
 *   /spawnmob zombie Босс 100 2
 *   /spawnmob sheep:red 3
 *   /spawnmob cow:baby Бурёнка
 */
public class SpawnMobCommand {

    public static final String PERM = "terracraft.core.spawnmob";

    private static final int MAX_COUNT  = 50;
    private static final int MAX_HEALTH = 1024;

    private final TerraCorePlugin plugin;

    public SpawnMobCommand(@NotNull TerraCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("spawnmob")
                .withAliases("sm")
                .withPermission(PERM)
                .withArguments(new GreedyStringArgument("args")
                        .replaceSuggestions((info, builder) -> {
                            String current = info.currentInput();
                            // Подсказки только для первого слова (тип моба)
                            if (current.contains(" ")) {
                                return CompletableFuture.completedFuture(builder.build());
                            }

                            String prefix = current.toLowerCase(Locale.ROOT);
                            int count = 0;
                            for (EntityType t : EntityType.values()) {
                                if (!t.isSpawnable() || !t.isAlive()) continue;
                                String name = t.getKey().getKey().toLowerCase(Locale.ROOT);
                                if (name.startsWith(prefix)) {
                                    builder.suggest(name);
                                    if (++count >= 50) break;
                                }
                            }
                            return CompletableFuture.completedFuture(builder.build());
                        }))
                .executesPlayer((player, args) -> {
                    String raw = (String) args.get("args");
                    if (raw == null || raw.isBlank()) {
                        plugin.msg().send(player, LangKey.USAGE,
                                "value", "/spawnmob <тип[:вариант]> [имя] [здоровье] [количество]");
                        return;
                    }

                    ParsedArgs p = parseArgs(raw);
                    if (p == null) {
                        plugin.msg().send(player, LangKey.SPAWNMOB_INVALID_TYPE, "value", raw);
                        return;
                    }

                    spawn(player, p);
                })
                .register();
    }

    // ============ Парсинг ============

    private record ParsedArgs(
            @NotNull EntityType type,
            @NotNull String variant,      // "" если нет
            @Nullable String name,
            double health,                // -1 = дефолт
            int count
    ) {}

    /**
     * Парсит строку вида "zombie Босс 100 2" или "sheep:red 3".
     * <p>
     * Логика:
     *   1. Первое слово — тип (может быть "type:variant").
     *   2. Дальше — либо число (тогда это количество), либо имя + опционально число + число.
     */
    private @Nullable ParsedArgs parseArgs(@NotNull String raw) {
        String[] parts = raw.trim().split("\\s+");

        // 1. Тип + вариант
        String typePart = parts[0];
        String variant = "";
        int colon = typePart.indexOf(':');
        if (colon > 0) {
            variant = typePart.substring(colon + 1).toLowerCase(Locale.ROOT);
            typePart = typePart.substring(0, colon);
        }

        EntityType type = parseType(typePart);
        if (type == null || !type.isSpawnable() || !type.isAlive()) {
            return null;
        }

        // 2. Остальные аргументы
        String name = null;
        double health = -1;
        int count = 1;

        if (parts.length >= 2) {
            Integer asNumber = tryParseInt(parts[1]);
            if (asNumber != null) {
                // Второй аргумент — число → это count
                count = asNumber;
            } else {
                // Второй аргумент — имя. Идём справа:
                // последнее слово — если число, то count;
                // второе с конца — если число, то health.
                int nameEnd = parts.length;
                if (nameEnd > 2 && tryParseInt(parts[nameEnd - 1]) != null) {
                    count = tryParseInt(parts[nameEnd - 1]);
                    nameEnd--;
                }
                if (nameEnd > 2 && tryParseInt(parts[nameEnd - 1]) != null) {
                    health = tryParseInt(parts[nameEnd - 1]);
                    nameEnd--;
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < nameEnd; i++) {
                    if (!sb.isEmpty()) sb.append(' ');
                    sb.append(parts[i]);
                }
                name = sb.toString();
            }
        }

        // 3. Валидация
        count = Math.max(1, Math.min(count, MAX_COUNT));
        if (health < -1) health = -1;
        if (health > MAX_HEALTH) health = MAX_HEALTH;

        return new ParsedArgs(type, variant, name, health, count);
    }

    private @Nullable Integer tryParseInt(@NotNull String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private @Nullable EntityType parseType(@NotNull String input) {
        String key = input.toLowerCase(Locale.ROOT);
        for (EntityType t : EntityType.values()) {
            if (t.name().equalsIgnoreCase(key)) return t;
            if (t.getKey().getKey().equalsIgnoreCase(key)) return t;
        }
        return null;
    }

    // ============ Спавн ============

    private void spawn(@NotNull Player player, @NotNull ParsedArgs p) {
        Location base = player.getLocation();
        int spawned = 0;

        for (int i = 0; i < p.count(); i++) {
            Location loc = base.clone().add(
                    (Math.random() - 0.5) * 2,
                    0,
                    (Math.random() - 0.5) * 2
            );

            Entity entity = player.getWorld().spawnEntity(loc, p.type());
            if (!(entity instanceof LivingEntity living)) continue;

            living.setRotation(base.getYaw(), 0);

            // Имя
            if (p.name() != null && !p.name().isBlank()) {
                living.customName(plugin.msg().mini().deserialize(p.name()));
                living.setCustomNameVisible(true);
            }

            // Здоровье
            if (p.health() > 0) {
                AttributeInstance maxHp = living.getAttribute(Attribute.MAX_HEALTH);
                if (maxHp != null) {
                    maxHp.setBaseValue(p.health());
                    living.setHealth(p.health());
                }
            }

            // Вариант
            if (!p.variant().isEmpty()) {
                applyVariant(living, p.variant());
            }

            spawned++;
        }

        plugin.msg().send(player, LangKey.SPAWNMOB_SUCCESS,
                "value", p.type().name().toLowerCase(Locale.ROOT),
                "amount", spawned);
    }

    // ============ Варианты ============

    /**
     * Применяет вариант из EssentialsX-стиля: sheep:red, cow:baby, creeper:powered, wolf:angry.
     */
    private void applyVariant(@NotNull LivingEntity living, @NotNull String variant) {
        // Универсальные: baby
        if (variant.equals("baby") && living instanceof Ageable ageable) {
            ageable.setBaby();
            return;
        }

        // Овца — цвет
        if (living instanceof Sheep sheep) {
            org.bukkit.DyeColor color = parseEnum(
                    org.bukkit.DyeColor.class, variant);
            if (color != null) sheep.setColor(color);
            return;
        }

        // Крипер
        if (living instanceof Creeper creeper && variant.equals("powered")) {
            creeper.setPowered(true);
            return;
        }

        // Волк
        if (living instanceof Wolf wolf && variant.equals("angry")) {
            wolf.setAngry(true);
            return;
        }

        // Кот — в 1.21 Type это интерфейс, ищем через Registry
        if (living instanceof Cat cat) {
            Cat.Type type = parseCatType(variant);
            if (type != null) cat.setCatType(type);
            return;
        }

        // Лошадь — цвет и стиль
        if (living instanceof Horse horse) {
            Horse.Color color = parseEnum(Horse.Color.class, variant);
            if (color != null) {
                horse.setColor(color);
                return;
            }
            Horse.Style style = parseEnum(Horse.Style.class, variant);
            if (style != null) horse.setStyle(style);
            return;
        }

        // Лиса
        if (living instanceof Fox fox && variant.equals("snow")) {
            fox.setFoxType(Fox.Type.SNOW);
            return;
        }

        // Кролик — в Paper 1.21 Type остался enum, используем valueOf
        if (living instanceof Rabbit rabbit) {
            try {
                Rabbit.Type type = Rabbit.Type.valueOf(variant.toUpperCase(Locale.ROOT));
                rabbit.setRabbitType(type);
            } catch (IllegalArgumentException ignored) {
                // Неизвестный вариант — ничего не делаем
            }
            return;
        }

        // Пчела — angry
        if (living instanceof Bee bee && variant.equals("angry")) {
            bee.setAnger(400);
        }
    }

    // ============ Парсеры вариантов ============

    /** Безопасный парсинг enum по имени (UPPER_SNAKE_CASE). */
    private @Nullable <E extends Enum<E>> E parseEnum(
            @NotNull Class<E> cls,
            @NotNull String name) {
        try {
            return Enum.valueOf(cls, name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Ищет Cat.Type в реестре по имени (1.21+: Type — интерфейс, не enum). */
    @SuppressWarnings("deprecation")
    private @Nullable Cat.Type parseCatType(@NotNull String input) {
        String key = input.toLowerCase(Locale.ROOT);
        NamespacedKey nk = NamespacedKey.minecraft(key);
        return Registry.CAT_VARIANT.get(nk);
    }
}