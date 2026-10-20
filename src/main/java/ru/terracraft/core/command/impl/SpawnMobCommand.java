package ru.terracraft.core.command.impl;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.terracraft.core.TerraCorePlugin;
import ru.terracraft.core.config.AdminToolsConfig;
import ru.terracraft.core.lang.LangKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class SpawnMobCommand {

    public static final String PERM = "terracraft.core.spawnmob";

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
                        plugin.msg().logAction(player, "/spawnmob",
                                "result", "invalid",
                                "input", raw);
                        return;
                    }

                    spawn(player, p);
                })
                .register();
    }

    // ============ Парсинг ============

    private record ParsedArgs(
            @NotNull EntityType type,
            @NotNull String variant,
            @Nullable String name,
            double health,
            int count
    ) {}

    private @Nullable ParsedArgs parseArgs(@NotNull String raw) {
        AdminToolsConfig.SpawnMob cfg = plugin.config().get().spawnMob();
        String[] parts = raw.trim().split("\\s+");

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

        if (parts.length == 1) {
            return new ParsedArgs(type, variant, null, -1, 1);
        }

        Integer secondAsNumber = tryParseInt(parts[1]);
        if (secondAsNumber != null) {
            int count = clampCount(secondAsNumber, cfg.maxCount());
            return new ParsedArgs(type, variant, null, -1, count);
        }

        List<String> nameWords = new ArrayList<>();
        int i = 1;
        while (i < parts.length && tryParseInt(parts[i]) == null) {
            nameWords.add(parts[i]);
            i++;
        }

        String name = nameWords.isEmpty() ? null : String.join(" ", nameWords);

        double health = -1;
        int count = 1;
        if (i < parts.length) {
            int remaining = parts.length - i;
            if (remaining == 1) {
                count = clampCount(tryParseInt(parts[i]), cfg.maxCount());
            } else if (remaining >= 2) {
                Integer h = tryParseInt(parts[i]);
                Integer c = tryParseInt(parts[i + 1]);
                if (h != null) health = clampHealth(h, cfg.maxHealth());
                if (c != null) count = clampCount(c, cfg.maxCount());
            }
        }

        return new ParsedArgs(type, variant, name, health, count);
    }

    private int clampCount(@Nullable Integer value, int maxCount) {
        if (value == null) return 1;
        return Math.max(1, Math.min(value, maxCount));
    }

    private double clampHealth(@Nullable Integer value, int maxHealth) {
        if (value == null || value < 1) return -1;
        return Math.min(value, maxHealth);
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
        AdminToolsConfig.SpawnMob cfg = plugin.config().get().spawnMob();
        double spread = Math.max(0, cfg.spreadRadius());

        Location base = player.getLocation();
        int spawned = 0;

        for (int i = 0; i < p.count(); i++) {
            Location loc = base.clone().add(
                    (Math.random() - 0.5) * spread * 2,
                    0,
                    (Math.random() - 0.5) * spread * 2
            );

            Entity entity = player.getWorld().spawnEntity(loc, p.type());
            if (!(entity instanceof LivingEntity living)) {
                spawned++;
                continue;
            }

            living.setRotation(base.getYaw(), 0);

            if (p.name() != null && !p.name().isBlank()) {
                living.customName(plugin.msg().mini().deserialize(p.name()));
                living.setCustomNameVisible(true);
            }

            if (p.health() > 0) {
                AttributeInstance maxHp = living.getAttribute(Attribute.MAX_HEALTH);
                if (maxHp != null) {
                    maxHp.setBaseValue(p.health());
                    living.setHealth(p.health());
                }
            }

            if (!p.variant().isEmpty()) {
                applyVariant(living, p.variant());
            }

            spawned++;
        }

        Component typeName = Component.translatable(p.type().translationKey());

        plugin.msg().send(player, LangKey.SPAWNMOB_SUCCESS,
                "value", typeName,
                "amount", spawned);
        plugin.msg().sound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);

        plugin.msg().logAction(player, "/spawnmob",
                "type", p.type().getKey().getKey(),
                "count", spawned);
        plugin.stats().increment(player, "spawnmob");
    }

    // ============ Варианты (без изменений) ============

    private void applyVariant(@NotNull LivingEntity living, @NotNull String variant) {
        if (variant.equals("baby") && living instanceof Ageable ageable) {
            ageable.setBaby();
            return;
        }

        if (living instanceof Sheep sheep) {
            DyeColor color = parseEnum(DyeColor.class, variant);
            if (color != null) sheep.setColor(color);
            return;
        }

        if (living instanceof Creeper creeper && variant.equals("powered")) {
            creeper.setPowered(true);
            return;
        }

        if (living instanceof Wolf wolf && variant.equals("angry")) {
            wolf.setAngry(true);
            return;
        }

        if (living instanceof Cat cat) {
            Cat.Type type = parseCatType(variant);
            if (type != null) cat.setCatType(type);
            return;
        }

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

        if (living instanceof Fox fox) {
            if (variant.equals("snow")) { fox.setFoxType(Fox.Type.SNOW); return; }
            if (variant.equals("red"))  { fox.setFoxType(Fox.Type.RED);  return; }
        }

        if (living instanceof Rabbit rabbit) {
            try {
                Rabbit.Type type = Rabbit.Type.valueOf(variant.toUpperCase(Locale.ROOT));
                rabbit.setRabbitType(type);
            } catch (IllegalArgumentException ignored) {
                // Неизвестный вариант
            }
            return;
        }

        if (living instanceof Bee bee && variant.equals("angry")) {
            bee.setAnger(400);
        }
    }

    private @Nullable <E extends Enum<E>> E parseEnum(
            @NotNull Class<E> cls,
            @NotNull String name) {
        try {
            return Enum.valueOf(cls, name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private @Nullable Cat.Type parseCatType(@NotNull String input) {
        String key = input.toLowerCase(Locale.ROOT);
        NamespacedKey nk;
        try {
            nk = NamespacedKey.minecraft(key);
        } catch (IllegalArgumentException e) {
            return null;
        }
        Registry<Cat.Type> registry = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.CAT_VARIANT);
        return registry.get(nk);
    }
}