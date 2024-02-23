package ke.co.skyworld.db;

import ke.co.skyworld.utils.ConfigFileChecker;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectDB {
    public static Connection initializeDatabase() {
        File configFile = ConfigFileChecker.getConfigFile(); // Ensure this method properly fetches your config file
        try {
            DatabaseConnectionManager dbManager = new DatabaseConnectionManager(configFile);
            Connection connection = dbManager.getConnection();
            System.out.println("Database connected successfully");
            dbManager.createTables(connection);
            return connection;
        } catch (SQLException e) {
            System.err.println("Failed to initialize the database: " + e.getMessage());
            return null;
        }
    }
}
