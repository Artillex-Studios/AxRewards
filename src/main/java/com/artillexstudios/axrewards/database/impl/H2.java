package com.artillexstudios.axrewards.database.impl;

import com.artillexstudios.axrewards.AxRewards;
import org.h2.jdbc.JdbcConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class H2 extends Base {
    private H2Connection conn;

    @Override
    public Connection getConnection() {
        return conn;
    }

    @Override
    public String getType() {
        return "H2";
    }

    @Override
    public void setup() {

        try {
            conn = new H2Connection("jdbc:h2:./" + AxRewards.getInstance().getDataFolder() + "/data;mode=MySQL", new Properties(), null, null, false);
            conn.setAutoCommit(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        super.setup();
    }

    @Override
    public void disable() {
        try {
            conn.realClose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static class H2Connection extends JdbcConnection {

        public H2Connection(String s, Properties properties, String s1, Object o, boolean b) throws SQLException {
            super(s, properties, s1, o, b);
        }

        @Override
        public synchronized void close() {
        }

        public synchronized void realClose() throws SQLException {
            super.close();
        }
    }
}
