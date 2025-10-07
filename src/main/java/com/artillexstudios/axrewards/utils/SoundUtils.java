package com.artillexstudios.axrewards.utils;

import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoundUtils {

    public static void playSound(@NotNull Player player, @Nullable Section section) {
        if (section == null) return;
        if (section.getString("sound", "").isBlank()) return;

        try {
            player.playSound(player, section.getString("sound"), 1, 1);
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFFFAA[AxRewards] The sound %sound% does not exist, section: %section%!".replace("%sound%", section.getString("sound")).replace("%section%", section.getNameAsString())));
        }
    }
}
