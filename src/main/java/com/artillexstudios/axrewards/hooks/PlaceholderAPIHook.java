package com.artillexstudios.axrewards.hooks;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axrewards.AxRewards;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axrewards.AxRewards.GUIS;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    @NotNull
    @Override
    public String getAuthor() {
        return "ArtillexStudios";
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "axrewards";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {

        if (offlinePlayer == null) return "---";
        final Player player = offlinePlayer.getPlayer();
        if (player == null) return "---";

        if (params.equalsIgnoreCase("collectable")) {
            int am = 0;
            for (String str : GUIS.getBackingDocument().getRoutesAsStrings(false)) {
                final Section section = GUIS.getSection(str);
                if (section == null) continue;

                final String permission = section.getString("permission", null);
                if (permission != null && !player.hasPermission(permission)) continue;

                final long lastClaim = AxRewards.getDatabase().getLastClaimed(player.getUniqueId(), str);
                final long claimCooldown = section.getLong("cooldown") * 1_000L;
                if (lastClaim > System.currentTimeMillis() - claimCooldown) continue;
                am++;
            }
            return "" + am;
        }

        return null;
    }
}
