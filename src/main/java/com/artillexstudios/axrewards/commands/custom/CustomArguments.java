package com.artillexstudios.axrewards.commands.custom;

import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.MenuManager;
import com.artillexstudios.axrewards.guis.data.Reward;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CustomArguments {

    public static Argument<Menu> menu(String nodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            Menu menu = MenuManager.getMenus().get(info.input());

            if (menu == null) {
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(new CustomArgument.MessageBuilder("Unknown menu: ").appendArgInput());
            }
            return menu;
        }).replaceSuggestions(ArgumentSuggestions.stringsAsync(info -> {
            return CompletableFuture.supplyAsync(() -> {
                return MenuManager.getMenus().keySet().toArray(new String[0]);
            });
        }));
    }

    public static Argument<Reward> reward(String nodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            String str = info.input();
            Optional<Object> menuOpt = info.previousArgs().getOptional("menu");
            if (menuOpt.isEmpty())
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(new CustomArgument.MessageBuilder("Unknown reward: ").appendArgInput());
            Menu menu = (Menu) menuOpt.get();
            Optional<Reward> reward = menu.rewards().stream().filter(rw -> rw.name().equals(str)).findAny();
            if (reward.isEmpty()) {
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(new CustomArgument.MessageBuilder("Unknown reward: ").appendArgInput());
            }
            return reward.get();
        }).replaceSuggestions(ArgumentSuggestions.stringsAsync(info -> {
            return CompletableFuture.supplyAsync(() -> {
                var args = info.previousArgs();
                Optional<Object> menuOpt = args.getOptional("menu");
                if (menuOpt.isEmpty()) return new String[0];
                Menu menu = (Menu) menuOpt.get();
                List<String> c = menu.rewards().stream().map(Reward::name).toList();
                return c.toArray(new String[0]);
            });
        }));
    }
}
