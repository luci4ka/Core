package ru.terracraft.core.command.util;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Утилита для построения {@link StringArgument} с Tab-комплитом.
 *
 * <p>Заменяет повторяющиеся блоки:
 * <pre>{@code
 * new StringArgument("target")
 *         .replaceSuggestions((info, builder) -> {
 *             String prefix = info.currentInput().toLowerCase(Locale.ROOT);
 *             for (Player p : Bukkit.getOnlinePlayers()) {
 *                 if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
 *                     builder.suggest(p.getName());
 *                 }
 *             }
 *             return CompletableFuture.completedFuture(builder.build());
 *         })
 * }</pre>
 *
 * <p>на лаконичное:
 * <pre>{@code
 * Args.playerArg("target")
 * Args.worldArg("world")
 * Args.listArg("mode", "walk", "fly")
 * }</pre>
 */
public final class Args {

    private Args() {}

    // ============ Игроки ============

    /**
     * {@link StringArgument} с саггестами по именам онлайн-игроков.
     * Префикс сравнивается регистронезависимо.
     */
    public static @NotNull StringArgument playerArg(@NotNull String name) {
        return new StringArgument(name)
                .replaceSuggestions(ArgumentSuggestions.strings(info -> {
                    String prefix = info.currentInput().toLowerCase(Locale.ROOT);
                    List<String> list = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                            list.add(p.getName());
                        }
                    }
                    return list.toArray(new String[0]);
                }));
    }

    // ============ Миры ============

    /**
     * {@link StringArgument} с саггестами по именам миров + {@code "all"}.
     */
    public static @NotNull StringArgument worldArg(@NotNull String name) {
        return new StringArgument(name)
                .replaceSuggestions(ArgumentSuggestions.strings(info -> {
                    List<String> list = new ArrayList<>();
                    list.add("all");

                    String prefix = info.currentInput().toLowerCase(Locale.ROOT);
                    for (World w : Bukkit.getWorlds()) {
                        if (w.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                            list.add(w.getName());
                        }
                    }
                    return list.toArray(new String[0]);
                }));
    }

    // ============ Статический список ============

    /**
     * {@link StringArgument} с фиксированным списком саггестов.
     * Фильтрация по префиксу — регистронезависимая.
     */
    public static @NotNull StringArgument listArg(@NotNull String name,
                                                  @NotNull String... options) {
        return new StringArgument(name)
                .replaceSuggestions(ArgumentSuggestions.strings(info -> {
                    String prefix = info.currentInput().toLowerCase(Locale.ROOT);
                    List<String> list = new ArrayList<>();
                    for (String opt : options) {
                        if (opt.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                            list.add(opt);
                        }
                    }
                    return list.toArray(new String[0]);
                }));
    }

    // ============ Регистр (для Enchant/SpawnMob — с лимитом) ============

    /**
     * {@link StringArgument} с саггестами из {@link Iterable}.
     * Ограничение — {@code maxSuggestions} (по умолчанию 50).
     */
    public static @NotNull StringArgument registryArg(@NotNull String name,
                                                      @NotNull Iterable<String> all,
                                                      int maxSuggestions) {
        return new StringArgument(name)
                .replaceSuggestions(ArgumentSuggestions.strings(info -> {
                    String prefix = info.currentInput().toLowerCase(Locale.ROOT);
                    List<String> list = new ArrayList<>();
                    int count = 0;
                    for (String opt : all) {
                        if (opt.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                            list.add(opt);
                            if (++count >= maxSuggestions) break;
                        }
                    }
                    return list.toArray(new String[0]);
                }));
    }

    /** Удобный overload с дефолтным лимитом 50. */
    public static @NotNull StringArgument registryArg(@NotNull String name,
                                                      @NotNull Iterable<String> all) {
        return registryArg(name, all, 50);
    }
}