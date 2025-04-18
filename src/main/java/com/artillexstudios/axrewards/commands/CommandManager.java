package com.artillexstudios.axrewards.commands;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrewards.AxRewards;
import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.MenuManager;
import com.artillexstudios.axrewards.guis.data.Reward;
import com.artillexstudios.axrewards.utils.CommandMessages;
import org.bukkit.Bukkit;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.core.CommandPath;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.orphan.OrphanRegistry;
import revxrsal.commands.orphan.Orphans;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.artillexstudios.axrewards.AxRewards.CONFIG;

public class CommandManager {
    private static BukkitCommandHandler handler = null;

    public static void load() {
        handler = BukkitCommandHandler.create(AxRewards.getInstance());

        handler.getAutoCompleter().registerSuggestion("rewards", (args, sender, command) -> {
            String menu = args.get(args.size() - 2);
            Config cfg;
            if ((cfg = MenuManager.getMenus().get(menu).settings()) == null) return List.of();
            return cfg.getBackingDocument().getRoutesAsStrings(false).stream().filter(string -> cfg.getSection(string) != null).toList();
        });
        handler.getAutoCompleter().registerSuggestion("menus", (args, sender, command) -> MenuManager.getMenus().keySet());

        handler.registerValueResolver(Menu.class, resolver -> {
            final String str = resolver.popForParameter();
            Menu menu = MenuManager.getMenus().get(str);
            if (menu != null) return menu;
            throw new CommandErrorException("Can't find menu!");
        });

        handler.registerValueResolver(Reward.class, resolver -> {

            String str = resolver.popForParameter();
            String[] spl = str.split("-");
            String last = spl[0];
            str = str.replace(last + "-", "");
            Menu menu = MenuManager.getMenus().get(last);
            if (menu == null)
                throw new CommandErrorException("Can't find reward!");
            String finalStr = str;
            var reward = menu.rewards().stream().filter(rw -> rw.name().equals(finalStr)).findAny();
            if (reward.isPresent()) return reward.get();
            throw new CommandErrorException("Can't find reward!");
        });

        handler.getAutoCompleter().registerParameterSuggestions(Menu.class, (args, sender, command) -> {
            return MenuManager.getMenus().keySet();
        });

        handler.getAutoCompleter().registerParameterSuggestions(Reward.class, (args, sender, command) -> {
            String last = args.get(args.size() - 2);
            Menu menu = MenuManager.getMenus().get(last);
            if (menu == null) return List.of();
            return menu.rewards().stream().map(reward -> menu.name() + "-" + reward.name()).toList();
        });

        handler.getTranslator().add(new CommandMessages());
        handler.setLocale(new Locale("en", "US"));

        handler.register(Orphans.path(CONFIG.getStringList("command-aliases").toArray(String[]::new)).handler(new Commands()));
        handler.registerBrigadier();

        reload();
    }

    private static final List<CommandPath> registered = new ArrayList<>();
    public static void reload() {
        for (CommandPath path : registered) {
            handler.unregister(path);
        }

        registered.clear();
        for (Map.Entry<String, Menu> entry : MenuManager.getMenus().entrySet()) {
            try {
                OrphanRegistry registry = Orphans.path(
                        entry.getValue().settings().getStringList("open-commands").toArray(String[]::new)
                ).handler(new OpenCommand(entry.getValue()));
                registered.addAll(registry.getParentPaths());
                handler.register(registry);
            } catch (Exception ex) {
                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxRewards] Failed to register the command of menu " + entry.getKey() + ", one of the open-commands is already used by another menu/plugin!"));
            }
        }
    }
}
