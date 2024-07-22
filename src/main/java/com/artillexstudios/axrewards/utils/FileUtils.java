package com.artillexstudios.axrewards.utils;

import com.artillexstudios.axrewards.AxRewards;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtils {
    public static final Path PLUGIN_DIRECTORY = AxRewards.getInstance().getDataFolder().toPath();

    public static void copyFromResource(@NotNull String path) {
        try (ZipFile zip = new ZipFile(Paths.get(AxRewards.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toFile())) {
            for (Iterator<? extends ZipEntry> it = zip.entries().asIterator(); it.hasNext(); ) {
                ZipEntry entry = it.next();
                if (entry.getName().startsWith(path + "/")) {
                    if (!entry.getName().endsWith(".yaml") && !entry.getName().endsWith(".yml")) continue;
                    InputStream resource = AxRewards.getInstance().getResource(entry.getName());
                    if (resource == null)   continue;

                    Files.copy(resource, PLUGIN_DIRECTORY.resolve(entry.getName()));
                }
            }
        } catch (IOException | URISyntaxException ex) {
            ex.printStackTrace();
        }
    }
}
