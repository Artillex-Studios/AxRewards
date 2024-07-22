package com.artillexstudios.axrewards.commands.subcommands;

import com.artillexstudios.axrewards.AxRewards;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.artillexstudios.axrewards.AxRewards.MESSAGEUTILS;

public enum Reset {
    INSTANCE;

    public void execute(CommandSender sender,OfflinePlayer player, @Nullable String menu, @Nullable String name) {
        final Map<String, String> replacements = Map.of(
                "%name%", name == null ? "---" : name,
                "%player%", player.getName() == null ? "---" : player.getName()
        );

        if (name == null)
            MESSAGEUTILS.sendLang(sender, "reset.all", replacements);
        else
            MESSAGEUTILS.sendLang(sender, "reset.single", replacements);

        AxRewards.getThreadedQueue().submit(() -> AxRewards.getDatabase().resetReward(player.getUniqueId(), menu, name));
    }
}
