package com.artillexstudios.axrewards.commands.subcommands;

import com.artillexstudios.axrewards.AxRewards;
import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.Reward;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public enum Reset {
    INSTANCE;

    public void execute(CommandSender sender, OfflinePlayer player, @Nullable Menu menu, @Nullable Reward reward) {
        final Map<String, String> replacements = Map.of(
                "%name%", reward == null ? "---" : reward.name(),
                "%player%", player.getName() == null ? "---" : player.getName()
        );

        AxRewards.getThreadedQueue().submit(() -> {
            if (menu == null) { // reset all rewards for player
                AxRewards.getDatabase().resetReward(player);
                MESSAGEUTILS.sendLang(sender, "reset.all", replacements);
            } else if (reward == null) { // only reset menu rewards
                AxRewards.getDatabase().resetReward(player, menu);
                MESSAGEUTILS.sendLang(sender, "reset.all", replacements);
            } else { // only reset single reward
                AxRewards.getDatabase().resetReward(player, reward);
                MESSAGEUTILS.sendLang(sender, "reset.single", replacements);
            }
        });
    }
}
