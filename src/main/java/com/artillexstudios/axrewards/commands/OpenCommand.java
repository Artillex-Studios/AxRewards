package com.artillexstudios.axrewards.commands;

import com.artillexstudios.axrewards.commands.subcommands.Open;
import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.MenuManager;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.orphan.OrphanCommand;

public class OpenCommand implements OrphanCommand {
    private Menu menu;

    public OpenCommand(Menu menu) {
        this.menu = menu;
    }

    @DefaultFor({"~"})
    public void open(@NotNull Player sender) {
        menu = MenuManager.getMenus().get(menu.name());
        if (menu == null) {
            throw new CommandException();
        }
        Open.INSTANCE.execute(sender, menu);
    }
}
