package com.artillexstudios.axrewards.utils;

import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.utils.StringUtils;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.artillexstudios.axrewards.AxRewards.BUKKITAUDIENCES;

public class SoundUtils {

    public static void playSound(@NotNull Player player, @Nullable Section section) {
        if (section == null) return;
        if (section.getString("sound", "").isBlank()) return;

        try {
            final Sound sound = Sound.sound().type(Key.key(section.getString("sound"))).build();
            BUKKITAUDIENCES.player(player).playSound(sound);
        } catch (InvalidKeyException ex) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFFFAA[AxRewards] The sound %sound% does not exist, section: %section%!".replace("%sound%", section.getString("sound")).replace("%section%", section.getNameAsString())));
        }
    }
}
