package com.myapp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {
    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DATABASE = "memory_game";
    private static final String USER = "root";
    private static final String PASSWORD = "2004";

    private static final String URL = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", HOST, PORT, DATABASE);

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
