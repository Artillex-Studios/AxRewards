package com.artillexstudios.axrewards.utils;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static com.artillexstudios.axrewards.AxRewards.CONFIG;
import static com.artillexstudios.axrewards.AxRewards.LANG;

public class TimeUtils {

    public static @NotNull String fancyTime(long time) {
        if (time < 0) return "---";

        final Duration remainingTime = Duration.ofMillis(time);
        long total = remainingTime.getSeconds();
        long days = total / 86400;
        long hours = (total % 86400) / 3600;
        long minutes = (total % 3600) / 60;
        long seconds = total % 60;

        if (CONFIG.getInt("timer-format", 1) == 1) {
            if (days > 0)
                return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
            if (hours > 0)
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            return String.format("%02d:%02d", minutes, seconds);
        } else if (CONFIG.getInt("timer-format", 1) == 2) {
            if (days > 0)
                return days + LANG.getString("time.day", "d");
            if (hours > 0)
                return hours + LANG.getString("time.hour", "h");
            if (minutes > 0)
                return minutes + LANG.getString("time.minute", "m");
            return seconds + LANG.getString("time.second", "s");
        } else {
            if (days > 0)
                return String.format("%02d" + LANG.getString("time.day", "d") + " %02d" + LANG.getString("time.hour", "h") +" %02d" + LANG.getString("time.minute", "m") + " %02d" + LANG.getString("time.second", "s"), days, hours, minutes, seconds);
            if (hours > 0)
                return String.format("%02d" + LANG.getString("time.hour", "h") +" %02d" + LANG.getString("time.minute", "m") + " %02d" + LANG.getString("time.second", "s"), hours, minutes, seconds);
            return String.format("%02d" + LANG.getString("time.minute", "m") + " %02d" + LANG.getString("time.second", "s"), minutes, seconds);
        }
    }

    public static long timeFromString(@NotNull String time) {
        final String[] tm = time.split("");
        long dateEnd = 0;
        
        String ch = "";
        for (String txt : tm) {
            switch (txt) {
                case "s" -> {
                    dateEnd += getInSeconds(Long.parseLong(ch));
                    ch = "";
                }
                case "m" -> {
                    dateEnd += getInMinutes(Long.parseLong(ch));
                    ch = "";
                }
                case "h" -> {
                    dateEnd += getInHours(Long.parseLong(ch));
                    ch = "";
                }
                case "d" -> {
                    dateEnd += getInDays(Long.parseLong(ch));
                    ch = "";
                }
                case "w" -> {
                    dateEnd += getInDays(Long.parseLong(ch) * 7L);
                    ch = "";
                }
                case "y" -> {
                    dateEnd += getInDays(Long.parseLong(ch) * 365L);
                    ch = "";
                }
                default -> ch += txt;
            }
        }

        return dateEnd;
    }

    private static long getInDays(long time) {
        return time * 86_400_000L;
    }

    private static long getInHours(long time) {
        return time * 3_600_000L;
    }

    private static long getInMinutes(long time) {
        return time * 60_000L;
    }

    private static long getInSeconds(long time) {
        return time * 1_000L;
    }
}
