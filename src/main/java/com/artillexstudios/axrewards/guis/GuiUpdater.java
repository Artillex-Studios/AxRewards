package com.artillexstudios.axrewards.guis;

import com.artillexstudios.axrewards.guis.impl.RewardGui;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GuiUpdater {
    private static ScheduledExecutorService service = null;

    public static void start() {
        if (service != null) service.shutdown();

        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            try {
                for (RewardGui gui : RewardGui.getOpenMenus()) {
                    gui.open();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public static void stop() {
        if (service == null) return;
        service.shutdown();
    }
}
