package com.artillexstudios.axrewards;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.dependencies.DependencyManagerWrapper;
import com.artillexstudios.axapi.executor.ThreadedQueue;
import com.artillexstudios.axapi.libs.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.metrics.AxMetrics;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axrewards.commands.CommandManager;
import com.artillexstudios.axrewards.database.Database;
import com.artillexstudios.axrewards.database.impl.H2;
import com.artillexstudios.axrewards.database.impl.MySQL;
import com.artillexstudios.axrewards.database.impl.PostgreSQL;
import com.artillexstudios.axrewards.guis.GuiUpdater;
import com.artillexstudios.axrewards.guis.data.MenuManager;
import com.artillexstudios.axrewards.hooks.PlaceholderAPIHook;
import com.artillexstudios.axrewards.hooks.PlaceholderAPIParser;
import com.artillexstudios.axrewards.hooks.Placeholders;
import com.artillexstudios.axrewards.libraries.Libraries;
import com.artillexstudios.axrewards.utils.FileUtils;
import com.artillexstudios.axrewards.utils.UpdateNotifier;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.relocation.Relocation;

import java.io.File;

public final class AxRewards extends AxPlugin {
    public static Config CONFIG;
    public static Config LANG;
    public static MessageUtils MESSAGEUTILS;
    private static AxPlugin instance;
    private static ThreadedQueue<Runnable> threadedQueue;
    private static Database database;
    private static Placeholders placeholderParser;
    private static AxMetrics metrics;

    public static ThreadedQueue<Runnable> getThreadedQueue() {
        return threadedQueue;
    }

    public static Database getDatabase() {
        return database;
    }

    public static AxPlugin getInstance() {
        return instance;
    }

    public static Placeholders getPlaceholderParser() {
        return placeholderParser;
    }

    @Override
    public void dependencies(DependencyManagerWrapper manager) {
        instance = this;
        manager.repository("https://jitpack.io/");
        manager.repository("https://repo.codemc.org/repository/maven-public/");
        manager.repository("https://repo.papermc.io/repository/maven-public/");
        manager.repository("https://repo.artillex-studios.com/releases/");

        DependencyManager dependencyManager = manager.wrapped();
        for (Libraries lib : Libraries.values()) {
            dependencyManager.dependency(lib.fetchLibrary());
            for (Relocation relocation : lib.relocations()) {
                dependencyManager.relocate(relocation);
            }
        }
    }

    public void enable() {
        new Metrics(this, 21023);

        CONFIG = new Config(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());
        LANG = new Config(new File(getDataFolder(), "lang.yml"), getResource("lang.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());

        MESSAGEUTILS = new MessageUtils(LANG.getBackingDocument(), "prefix", CONFIG.getBackingDocument());

        threadedQueue = new ThreadedQueue<>("AxRewards-Datastore-thread");

        if (FileUtils.PLUGIN_DIRECTORY.resolve("menus/").toFile().mkdirs()) {
            if (new File(getDataFolder(), "guis.yml").exists()) {
                new Converter2();
            } else {
                FileUtils.copyFromResource("menus");
            }
        }

        MenuManager.reload();

        switch (CONFIG.getString("database.type").toLowerCase()) {
//            case "sqlite" -> database = new SQLite();
            case "mysql" -> database = new MySQL();
            case "postgresql" -> database = new PostgreSQL();
            default -> database = new H2();
        }

        database.setup();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook().register();
            placeholderParser = new PlaceholderAPIParser();
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00[AxRewards] Hooked into PlaceholderAPI!"));
        } else {
            placeholderParser = new Placeholders() {};
        }

        GuiUpdater.start();

        CommandManager.load();

        metrics = new AxMetrics(this, 42);
        metrics.start();

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00[AxRewards] Loaded plugin! Using &f" + database.getType() + " &#FFEE00database to store data!"));

        if (CONFIG.getBoolean("update-notifier.enabled", true)) new UpdateNotifier(this, 5549);
    }

    public void disable() {
        if (metrics != null) metrics.cancel();
        GuiUpdater.stop();
        database.disable();
    }

    public void updateFlags() {
        FeatureFlags.USE_LEGACY_HEX_FORMATTER.set(true);
    }
}