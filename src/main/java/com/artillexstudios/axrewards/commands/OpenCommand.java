package com.artillexstudios.axrewards.commands;

import com.artillexstudios.axrewards.commands.subcommands.Open;
import com.artillexstudios.axrewards.guis.data.Menu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.orphan.OrphanCommand;

public class OpenCommand implements OrphanCommand {
    private final Menu menu;

    public OpenCommand(Menu menu) {
        this.menu = menu;
    }

    @DefaultFor({"~", "~ open"})
    public void open(@NotNull Player sender) {
        Open.INSTANCE.execute(sender, menu);
    }
}
