package com.artillexstudios.axrewards.commands;

import com.artillexstudios.axrewards.commands.subcommands.Open;
import com.artillexstudios.axrewards.guis.data.Menu;
import dev.jorel.commandapi.CommandAPICommand;

import java.util.ArrayList;
import java.util.List;

public class OpenCommand extends Command {

    public OpenCommand(List<String> aliases, Menu menu) {
        this.aliases = new ArrayList<>(aliases);
        this.command = new CommandAPICommand(aliases.get(0))
                .withAliases(aliases.subList(1, aliases.size()).toArray(String[]::new))
                .executesPlayer((sender, args) -> {
                    Open.INSTANCE.execute(sender, menu);
                })
                .withSubcommand(new CommandAPICommand("open")
                        .executesPlayer((sender, args) -> {
                            Open.INSTANCE.execute(sender, menu);
                        })
                );
    }
}
