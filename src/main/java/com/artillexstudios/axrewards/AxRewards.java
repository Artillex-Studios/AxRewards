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
        instance = this; // todo: proper placeholderapi integration

        int pluginId = 21023;
        new Metrics(this, pluginId);

        CONFIG = new Config(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());
        LANG = new Config(new File(getDataFolder(), "lang.yml"), getResource("lang.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());

        MESSAGEUTILS = new MessageUtils(LANG.getBackingDocument(), "prefix", CONFIG.getBackingDocument());

        threadedQueue = new ThreadedQueue<>("AxRewards-Datastore-thread");

        BUKKITAUDIENCES = BukkitAudiences.create(this);

        if (FileUtils.PLUGIN_DIRECTORY.resolve("menus/").toFile().mkdirs()) {
            if (new File(getDataFolder(), "guis.yml").exists())
                new Converter2();
            else
                FileUtils.copyFromResource("menus");
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

        metrics = new AxMetrics(42);
        metrics.start();

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FFEE00[AxRewards] Loaded plugin! Using &f" + database.getType() + " &#FFEE00database to store data!"));

        if (CONFIG.getBoolean("update-notifier.enabled", true)) new UpdateNotifier(this, 5549);
    }

    public void disable() {
        metrics.cancel();
        GuiUpdater.stop();
        database.disable();
    }

    public void updateFlags(FeatureFlags flags) {
        flags.USE_LEGACY_HEX_FORMATTER.set(true);
    }
}