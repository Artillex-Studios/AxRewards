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

    private static final String CREATE_TABLE = """
                        CREATE TABLE IF NOT EXISTS axrewards_claimed (
                        	uuid VARCHAR(36) NOT NULL,
                        	reward VARCHAR(512) NOT NULL,
                        	time BIGINT NOT NULL
                        );
                """;

    @Override
    public void setup() {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void claimReward(@NotNull UUID uuid, String menu, String reward) {
        String finalReward = menu.equals("default") ? reward : menu + "|" + reward;
        resetReward(uuid, menu, finalReward);
        final String sql = "INSERT INTO axrewards_claimed(uuid, reward, time) VALUES (?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, finalReward);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void resetReward(@NotNull UUID uuid, @Nullable String menu, @Nullable String reward) {
        String sql;
        String rew;

        if (menu == null) {
            sql = "DELETE FROM axrewards_claimed WHERE uuid = ?;";
            executeUpdate(uuid, sql, null);
            return;
        }

        if (menu.equals("default")) {
            if (reward == null) {
                sql = "DELETE FROM axrewards_claimed WHERE uuid = ? AND reward NOT LIKE ?;";
                rew = "%|%";
            } else {
                sql = "DELETE FROM axrewards_claimed WHERE uuid = ? AND reward = ?;";
                rew = reward;
            }
        } else {
            if (reward == null) {
                sql = "DELETE FROM axrewards_claimed WHERE uuid = ? AND reward LIKE ?;";
                rew = menu + "|%";
            } else {
                sql = "DELETE FROM axrewards_claimed WHERE uuid = ? AND reward = ?;";
                rew = menu + "|" + reward;
            }
        }

        executeUpdate(uuid, sql, rew);
    }

    private void executeUpdate(UUID uuid, String sql, @Nullable String param) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            if (param != null) {
                stmt.setString(2, param);
            }
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public long getLastClaimed(@NotNull UUID uuid, String menu, String reward) {
        String finalReward = menu.equals("default") ? reward : menu + "|" + reward;
        final String sql = "SELECT time FROM axrewards_claimed WHERE uuid = ? AND reward = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, finalReward);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public abstract String getType();

    @Override
    public abstract void disable();
}
