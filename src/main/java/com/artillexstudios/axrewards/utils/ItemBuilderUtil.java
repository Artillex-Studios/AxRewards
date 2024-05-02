package com.artillexstudios.axrewards.utils;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.utils.ClassUtils;
import com.artillexstudios.axapi.utils.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ItemBuilderUtil {

    @NotNull
    @Contract("_ -> new")
    public static ItemBuilder newBuilder(@NotNull Section section) {
        return newBuilder(section, Map.of());
    }

    @NotNull
    public static ItemBuilder newBuilder(@NotNull Section section, Map<String, String> replacements) {
        final ItemBuilder builder = new ItemBuilder(section);

        section.getOptionalString("name").ifPresent((name) -> {
            if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                name = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(null, name);
            }
            builder.setName(name, replacements);
        });

        section.getOptionalStringList("lore").ifPresent((lore) -> {
            if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                lore = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(null, lore);
            }
            builder.setLore(lore, replacements);
        });

        return builder;
    }

    @NotNull
    @Contract("_ -> new")
    public static ItemBuilder newBuilder(@NotNull ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }
}
