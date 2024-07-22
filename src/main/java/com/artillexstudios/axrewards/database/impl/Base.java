package com.artillexstudios.axrewards.database.impl;

import com.artillexstudios.axrewards.database.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public abstract class Base implements Database {

    public abstract Connection getConnection();

    @Override
    public abstract String getType();

    @Override
    public void setup() {
        final String CREATE_TABLE = """
                        CREATE TABLE IF NOT EXISTS axrewards_claimed (
                        	uuid VARCHAR(36) NOT NULL,
                        	reward VARCHAR(512) NOT NULL,
                        	time BIGINT NOT NULL
                        );
                """;

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void claimReward(@NotNull UUID uuid, String menu, String reward) {
        if (!menu.equals("default")) reward = menu + "|" + reward;
        resetReward(uuid, menu, reward);
        final String sql = "INSERT INTO axrewards_claimed(uuid, reward, time) VALUES (?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, reward);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void resetReward(@NotNull UUID uuid, @Nullable String menu, @Nullable String reward) {
        // menu null
        if (menu == null) {
            final String sql = "DELETE FROM axrewards_claimed WHERE uuid = ?;";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        final String sql;
        String rew;
        if (menu.equals("default")) { // default menu (for backwards compatibility)
            if (reward == null) {
                // delete all
                sql = "DELETE FROM axrewards_claimed WHERE uuid = ? AND reward NOT LIKE ?;";
                rew = "%|%";
            } else {
                // delete one
                sql = "DELETE FROM axrewards_claimed WHERE uuid = ? AND reward = ?;";
                rew = reward;
            }
        } else { // other menus
            if (reward == null) {
                // delete all
                sql = "DELETE FROM axrewards_claimed WHERE uuid = ? AND reward LIKE ?;";
                rew = menu + "|%";
            } else {
                // delete one
                sql = "DELETE FROM axrewards_claimed WHERE uuid = ? AND reward = ?;";
                rew = menu + "|" + reward;
            }
        }
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, rew);
            stmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public long getLastClaimed(@NotNull UUID uuid, String menu, String reward) {
        if (!menu.equals("default")) reward = menu + "|" + reward;
        final String sql = "SELECT time FROM axrewards_claimed WHERE uuid = ? AND reward = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, reward);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public abstract void disable();
}
