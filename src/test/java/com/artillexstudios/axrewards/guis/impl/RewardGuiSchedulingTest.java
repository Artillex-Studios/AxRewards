package com.artillexstudios.axrewards.guis.impl;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardGuiSchedulingTest {
    @Test
    void openSchedulesInventoryOpenOnPlayerEntityScheduler() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/artillexstudios/axrewards/guis/impl/RewardGui.java"));
        int openCall = source.indexOf("gui.open(player);");

        assertTrue(openCall >= 0, "RewardGui.open should still open the GUI for the player");
        assertTrue(source.contains("getMethod(\"getScheduler\")"),
                "Folia requires player inventory operations to run on the player's entity scheduler");
        assertTrue(source.contains("getMethod(\"run\", Plugin.class, Consumer.class, Runnable.class)"),
                "The entity scheduler run method should be used when Folia is available");
        assertFalse(source.substring(Math.max(0, openCall - 120), openCall).contains("Scheduler.get().run("),
                "gui.open(player) must not be scheduled through the global/generic scheduler on Folia");
    }
}
