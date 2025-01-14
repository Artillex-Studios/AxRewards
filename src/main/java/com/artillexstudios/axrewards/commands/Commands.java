package com.artillexstudios.axrewards.commands;

import com.artillexstudios.axrewards.commands.custom.CustomArguments;
import com.artillexstudios.axrewards.commands.subcommands.ForceOpen;
import com.artillexstudios.axrewards.commands.subcommands.Help;
import com.artillexstudios.axrewards.commands.subcommands.Open;
import com.artillexstudios.axrewards.commands.subcommands.Reload;
import com.artillexstudios.axrewards.commands.subcommands.Reset;
import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.Reward;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Commands extends Command {

    public Commands(List<String> aliases) {
        this.aliases = new ArrayList<>(aliases);
        this.command = new CommandAPICommand(aliases.get(0))
            .withAliases(aliases.subList(1, aliases.size()).toArray(String[]::new))
            .withPermission("axrewards.help")
            .executes((sender, args) -> {
                Help.INSTANCE.execute(sender);
            })
            .withSubcommand(new CommandAPICommand("help")
                    .withPermission("axrewards.help")
                    .executes((sender, args) -> {
                        Help.INSTANCE.execute(sender);
                    })
            )
            .withSubcommand(new CommandAPICommand("open")
                    .withArguments(CustomArguments.menu("menu"))
                    .executesPlayer((sender, args) -> {
                        Open.INSTANCE.execute(sender, args.getByClass("menu", Menu.class));
                    })
            )
            .withSubcommand(new CommandAPICommand("reload")
                    .withPermission("axrewards.reload")
                    .executes((sender, args) -> {
                        Reload.INSTANCE.execute(sender);
                    })
            )
            .withSubcommand(new CommandAPICommand("reset")
                    .withPermission("axrewards.reset")
                    .withArguments(new OfflinePlayerArgument("player"))
                    .withOptionalArguments(CustomArguments.menu("menu"))
                    .withOptionalArguments(CustomArguments.reward("reward"))
                    .executes((sender, args) -> {
                        Reset.INSTANCE.execute(
                                sender,
                                args.getByClass("player", OfflinePlayer.class),
                                args.getByClass("menu", Menu.class),
                                args.getByClass("reward", Reward.class)
                        );
                    })
            )
            .withSubcommand(new CommandAPICommand("forceopen")
                    .withPermission("axrewards.forceopen")
                    .withArguments(new PlayerArgument("player"))
                    .withOptionalArguments(CustomArguments.menu("menu"))
                    .withOptionalArguments(new BooleanArgument("bypass"))
                    .executes((sender, args) -> {
                        ForceOpen.INSTANCE.execute(
                                sender,
                                args.getByClass("player", Player.class),
                                args.getByClass("menu", Menu.class),
                                args.getByClass("bypass", Boolean.class)
                        );
                    })
            );
    }
}
