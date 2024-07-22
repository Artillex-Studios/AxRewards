package com.artillexstudios.axrewards.commands;

import com.artillexstudios.axrewards.commands.subcommands.Open;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.orphan.OrphanCommand;

public class OpenCommand implements OrphanCommand {
    private final String menu;

    public OpenCommand(String menu) {
        this.menu = menu;
    }

    @DefaultFor({"~", "~ open"})
    public void open(@NotNull Player sender) {
        Open.INSTANCE.execute(sender, menu);
    }
}
