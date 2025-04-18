package com.artillexstudios.axrewards.utils;

import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axrewards.AxRewards;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ItemBuilderUtil {

    @NotNull
    public static ItemBuilder newBuilder(@Nullable Player player, @NotNull Section section) {
        return newBuilder(player, section, Map.of());
    }

    @NotNull
    public static ItemBuilder newBuilder(@Nullable Player player, @NotNull Section section, Map<String, String> replacements) {
        final ItemBuilder builder = new ItemBuilder(section);

        section.getOptionalString("name").ifPresent((name) -> {
            name = AxRewards.getPlaceholderParser().setPlaceholders(player, name);
            builder.setName(name, replacements);
        });

        section.getOptionalStringList("lore").ifPresent((lore) -> {
            lore = AxRewards.getPlaceholderParser().setPlaceholders(player, lore);
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
