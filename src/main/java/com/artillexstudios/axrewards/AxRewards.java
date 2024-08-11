package com.artillexstudios.axrewards;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.data.ThreadedQueue;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.libs.libby.BukkitLibraryManager;
import com.artillexstudios.axapi.utils.FeatureFlags;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrewards.commands.Commands;
import com.artillexstudios.axrewards.database.Database;
import com.artillexstudios.axrewards.database.impl.H2;
import com.artillexstudios.axrewards.database.impl.MySQL;
import com.artillexstudios.axrewards.database.impl.PostgreSQL;
import com.artillexstudios.axrewards.database.impl.SQLite;
import com.artillexstudios.axrewards.guis.GuiUpdater;
import com.artillexstudios.axrewards.guis.impl.GuiManager;
import com.artillexstudios.axrewards.hooks.PlaceholderAPIHook;
import com.artillexstudios.axrewards.libraries.Libraries;
import com.artillexstudios.axrewards.utils.FileUtils;
import com.artillexstudios.axrewards.utils.UpdateNotifier;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;

import java.io.File;

public final class AxRewards extends AxPlugin {
    public static Config CONFIG;
    public static Config LANG;
    public static MessageUtils MESSAGEUTILS;
    private static AxPlugin instance;
    private static ThreadedQueue<Runnable> threadedQueue;
    private static Database database;
    public static BukkitAudiences BUKKITAUDIENCES;

    public static ThreadedQueue<Runnable> getThreadedQueue() {
        return threadedQueue;
    }

    public static Database getDatabase() {
        return database;
    }

    public static AxPlugin getInstance() {
        return instance;
    }

    public void load() {
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this, "lib");
        libraryManager.addMavenCentral();
        libraryManager.addJitPack();
        libraryManager.addRepository("https://repo.codemc.org/repository/maven-public/");
        libraryManager.addRepository("https://repo.papermc.io/repository/maven-public/");

        for (Libraries lib : Libraries.values()) {
            libraryManager.loadLibrary(lib.getLibrary());
        }
    }

    public void enable() {
        instance = this;

        int pluginId = 21023;
        new Metrics(this, pluginId);

        CONFIG = new Config(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());
        LANG = new Config(new File(getDataFolder(), "lang.yml"), getResource("lang.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());

        MESSAGEUTILS = new MessageUtils(LANG.getBackingDocument(), "prefix", CONFIG.getBackingDocument());

        threadedQueue = new ThreadedQueue<>("AxRewards-Datastore-thread");

        BUKKITAUDIENCES = BukkitAudiences.create(this);

        if (FileUtils.PLUGIN_DIRECTORY.resolve("menus/").toFile().mkdirs()) {
            if (new File(getDataFolder(), "guis.yml").exists())
                new ConverterV2();
            else
                FileUtils.copyFromResource("menus");
        }

        GuiManager.reload();

        switch (CONFIG.getString("database.type").toLowerCase()) {
            case "sqlite" -> database = new SQLite();
            case "mysql" -> database = new MySQL();
            case "postgresql" -> database = new PostgreSQL();
            default -> database = new H2();
        }

        database.setup();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook().register();
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00[AxRewards] Hooked into PlaceholderAPI!"));
        }

        GuiUpdater.start();

        Commands.registerCommand();

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00[AxRewards] Loaded plugin! Using &f" + database.getType() + " &#FFEE00database to store data!"));

        if (CONFIG.getBoolean("update-notifier.enabled", true)) new UpdateNotifier(this, 5549);
    }

    public void disable() {
        GuiUpdater.stop();
        database.disable();
    }

    public void updateFlags() {
        FeatureFlags.USE_LEGACY_HEX_FORMATTER.set(true);
    }
}