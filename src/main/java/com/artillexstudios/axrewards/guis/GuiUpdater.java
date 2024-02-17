package com.artillexstudios.axrewards.guis;

import com.artillexstudios.axrewards.guis.impl.MainGui;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GuiUpdater {

    public void start() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> MainGui.getMap().forEach((key, value) -> key.open()), 1, 1, TimeUnit.SECONDS);
    }
}
