package com.myapp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final String URL = "jdbc:mysql://localhost:3306/recipes_db?useSSL=false&serverTimezone=UTC";


    private static final String USERNAME = "root"; 
    private static final String PASSWORD = "p@ssw0rd"; 

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
