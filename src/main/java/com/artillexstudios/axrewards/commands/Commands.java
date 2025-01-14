package com.artillexstudios.axrewards.commands;

import com.artillexstudios.axrewards.commands.subcommands.ForceOpen;
import com.artillexstudios.axrewards.commands.subcommands.Help;
import com.artillexstudios.axrewards.commands.subcommands.Open;
import com.artillexstudios.axrewards.commands.subcommands.Reload;
import com.artillexstudios.axrewards.commands.subcommands.Reset;
import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.Reward;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;

public class Commands implements OrphanCommand {

    @DefaultFor({"~", "~ help"})
    @CommandPermission("axrewards.help")
    public void help(@NotNull CommandSender sender) {
        Help.INSTANCE.execute(sender);
    }

    @Subcommand("open")
    public void open(@NotNull Player sender, @Optional Menu menu) {
        Open.INSTANCE.execute(sender, menu);
    }

    @Subcommand("reload")
    @CommandPermission("axrewards.reload")
    public void reload(@NotNull CommandSender sender) {
        Reload.INSTANCE.execute(sender);
    }

    @Subcommand("reset")
    @CommandPermission("axrewards.reset")
    public void reset(@NotNull CommandSender sender, @NotNull OfflinePlayer player, @Optional Menu menu, @Optional Reward reward) {
        Reset.INSTANCE.execute(sender, player, menu, reward);
    }

    @Subcommand("forceopen")
    @CommandPermission("axrewards.forceopen")
    public void forceOpen(@NotNull CommandSender sender, Player player, @Optional Menu menu, @Optional Boolean bypass) {
        ForceOpen.INSTANCE.execute(sender, player, menu, bypass);
    }
}
