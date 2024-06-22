package com.artillexstudios.axrewards.utils;

import com.artillexstudios.axapi.utils.StringUtils;
import revxrsal.commands.locales.LocaleReader;

import java.util.Locale;

import static com.artillexstudios.axrewards.AxRewards.CONFIG;
import static com.artillexstudios.axrewards.AxRewards.LANG;

public class CommandMessages implements LocaleReader {
    @Override
    public boolean containsKey(String s) {
        return true;
    }

    @Override
    public String get(String s) {
        String res;
        switch (s) {
            case "invalid-enum", "invalid-number", "invalid-uuid", "invalid-url", "invalid-boolean": {
                res = LANG.getString("commands.invalid-value")
                        .replace("%value%", "{0}");
                break;
            }
            case "missing-argument": {
                res = LANG.getString("commands.missing-argument")
                        .replace("%value%", "{0}");
                break;
            }
            case "no-permission": {
                res = LANG.getString("commands.no-permission");
                break;
            }
            case "number-not-in-range": {
                res = LANG.getString("commands.out-of-range")
                        .replace("%number%", "{0}")
                        .replace("%min%", "{1}")
                        .replace("%max%", "{2}");
                break;
            }
            case "must-be-player": {
                res = LANG.getString("commands.player-only");
                break;
            }
            case "must-be-console": {
                res = LANG.getString("commands.console-only");
                break;
            }
            case "invalid-player": {
                res = LANG.getString("commands.invalid-player")
                        .replace("%player%", "{0}");
                break;
            }
            case "invalid-selector": {
                res = LANG.getString("commands.invalid-selector");
                break;
            }
            default:  {
                res = LANG.getString("commands.invalid-command");
                break;
            }
        }
        return StringUtils.formatToString(CONFIG.getString("prefix", "") + res);
    }

    private final Locale locale = new Locale("en", "US");

    @Override
    public Locale getLocale() {
        return locale;
    }
}
