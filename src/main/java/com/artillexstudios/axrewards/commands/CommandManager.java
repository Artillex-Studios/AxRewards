package com.artillexstudios.axrewards.commands;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrewards.AxRewards;
import com.artillexstudios.axrewards.guis.data.Menu;
import com.artillexstudios.axrewards.guis.data.MenuManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.artillexstudios.axrewards.AxRewards.CONFIG;

public class CommandManager {
    private static final List<Command> commands = new ArrayList<>();
//    private static BukkitCommandHandler handler = null;

    public static void load() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(AxRewards.getInstance())
                .usePluginNamespace()
        );
        CommandAPI.onEnable();
        reload();
//        handler = BukkitCommandHandler.create(AxRewards.getInstance());
//
//        handler.getAutoCompleter().registerSuggestion("rewards", (args, sender, command) -> {
//            String menu = args.get(args.size() - 2);
//            Config cfg;
//            if ((cfg = MenuManager.getMenus().get(menu).settings()) == null) return List.of();
//            return cfg.getBackingDocument().getRoutesAsStrings(false).stream().filter(string -> cfg.getSection(string) != null).toList();
//        });
//        handler.getAutoCompleter().registerSuggestion("menus", (args, sender, command) -> MenuManager.getMenus().keySet());
//
//        handler.registerValueResolver(0, OfflinePlayer.class, context -> {
//            String value = context.pop();
//            if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me")) return ((BukkitCommandActor) context.actor()).requirePlayer();
//            OfflinePlayer player = NMSHandlers.getNmsHandler().getCachedOfflinePlayer(value);
//            if (player == null && !(player = Bukkit.getOfflinePlayer(value)).hasPlayedBefore()) throw new InvalidPlayerException(context.parameter(), value);
//            return player;
//        });
//
//        handler.getAutoCompleter().registerParameterSuggestions(OfflinePlayer.class, (args, sender, command) -> {
//            return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toSet());
//        });
//
//        handler.registerValueResolver(Menu.class, resolver -> {
//            final String str = resolver.popForParameter();
//            Menu menu = MenuManager.getMenus().get(str);
//            if (menu != null) return menu;
//            throw new CommandErrorException("Can't find menu!");
//        });
//
//        handler.registerValueResolver(Reward.class, resolver -> {
//            String str = resolver.popForParameter();
//            String[] spl = str.split("-");
//            String last = spl[0];
//            str = str.replace(last + "-", "");
//            Menu menu = MenuManager.getMenus().get(last);
//            if (menu == null)
//                throw new CommandErrorException("Can't find reward!");
//            String finalStr = str;
//            var reward = menu.rewards().stream().filter(rw -> rw.name().equals(finalStr)).findAny();
//            if (reward.isPresent()) return reward.get();
//            throw new CommandErrorException("Can't find reward!");
//        });
//
//        handler.getAutoCompleter().registerParameterSuggestions(Menu.class, (args, sender, command) -> {
//            return MenuManager.getMenus().keySet();
//        });
//
//        handler.getAutoCompleter().registerParameterSuggestions(Reward.class, (args, sender, command) -> {
//            String last = args.get(args.size() - 2);
//            Menu menu = MenuManager.getMenus().get(last);
//            if (menu == null) return List.of();
//            return menu.rewards().stream().map(reward -> menu.name() + "-" + reward.name()).toList();
//        });
//
//        handler.getTranslator().add(new CommandMessages());
//        handler.setLocale(new Locale("en", "US"));
    }

    public static void reload() {
        for (Command command : commands) {
            command.unregister();
        }
        commands.clear();

        register(new Commands(CONFIG.getStringList("command-aliases")));

        for (Map.Entry<String, Menu> entry : MenuManager.getMenus().entrySet()) {
            try {
                register(new OpenCommand(entry.getValue().settings().getStringList("open-commands"), entry.getValue()));
            } catch (Exception ex) {
                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[" + AxRewards.getInstance().getName() + "] Failed to register the command of menu " + entry.getKey() + ", one of the open-commands is already used by another menu/plugin!"));
            }
        }

//        handler.unregisterAllCommands();
//        for (Map.Entry<String, Menu> entry : MenuManager.getMenus().entrySet()) {
//            try {
//                handler.register(Orphans.path(
//                        entry.getValue().settings().getStringList("open-commands").toArray(String[]::new)
//                ).handler(new OpenCommand(entry.getValue())));
//            } catch (Exception ex) {
//                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxRewards] Failed to register the command of menu " + entry.getKey() + ", one of the open-commands is already used by another menu/plugin!"));
//            }
//        }
//        handler.register(Orphans.path(CONFIG.getStringList("command-aliases").toArray(String[]::new)).handler(new Commands()));
//        handler.registerBrigadier();
    }

    public static void disable() {
        for (Command command : commands) {
            command.unregister();
        }
        CommandAPI.onDisable();
    }

    private static void register(Command command) {
        command.register();
        commands.add(command);
    }
}
