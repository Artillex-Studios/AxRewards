package com.artillexstudios.axrewards.guis;

import com.artillexstudios.axrewards.guis.impl.MainGui;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GuiUpdater {

    private static ScheduledFuture<?> future = null;

    public static void start() {
        if (future != null) future.cancel(true);

        future = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                MainGui.getMap().forEach((key, value) -> key.open());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public static void stop() {
        future.cancel(true);
    }
}
