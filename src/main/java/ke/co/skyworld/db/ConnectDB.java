package ke.co.skyworld.db;


import ke.co.skyworld.KeyManager;
import ke.co.skyworld.Model.ConfigReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import static ke.co.skyworld.Model.ConfigReader.decrypt;

public class ConnectDB {
    private static List<Connection> connectionPool;
    private static List<Connection> usedConnections = new ArrayList<>();
    private static final int INITIAL_POOL_SIZE = 10;
    private static boolean initialized = false;

    private ConnectDB() {
    }

    // Lazily initialize the connection pool
    private static synchronized void initialize() throws SQLException {
        if (!initialized) {
            connectionPool = new ArrayList<>(INITIAL_POOL_SIZE);
            System.out.println("Connection established");
            for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
                try {
                    connectionPool.add(createConnection());
                } catch (SQLException e) {
                    throw new SQLException("Error initializing connection pool", e.getMessage());
                }
            }
            initialized = true;
        }
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        try {
            if (connectionPool.isEmpty()) {
                // If the connection pool is empty, create a new connection
                return createConnection();
            } else {
                // If the connection pool has available connections, return one
                Connection connection = connectionPool.remove(connectionPool.size() - 1);
                usedConnections.add(connection);
                return connection;
            }
        } catch (SQLException e) {
            throw new SQLException("Error getting connection: " + e.getMessage());
        }
    }




    public static boolean releaseConnection(Connection connection) {
        if (connection != null) {
            connectionPool.add(connection);
            return usedConnections.remove(connection);
        }
        return false;
    }

    public static synchronized void shutdown() {
        synchronized (usedConnections) {
            List<Connection> usedConnectionsCopy = new ArrayList<>(usedConnections);
            for (Connection c : usedConnectionsCopy) {
                try {
                    releaseConnection(c);
                } catch (Exception e) {
                    // Log or handle the exception
                    System.out.println("error in releasing connection " + e.getMessage());
                }
            }
            usedConnections.clear();
        }

        synchronized (connectionPool) {
            List<Connection> connectionPoolCopy = new ArrayList<>(connectionPool);
            for (Connection c : connectionPoolCopy) {
                try {
                    c.close();
                } catch (SQLException e) {
                    // Log or handle the exception
                    System.out.println("error in closing connection " + e.getMessage());
                }
                connectionPool.remove(c); // Remove the current connection from the list
            }
        }
    }





    private static Connection createConnection() throws SQLException {
        String connectionUrl = buildConnectionUrl();
        String decryptedUsername = decrypt(ConfigReader.getUsername(), KeyManager.AES_ENCRYPT_KEY);
        String decryptedPassword = decrypt(ConfigReader.getPassword(), KeyManager.AES_ENCRYPT_KEY);

        return DriverManager.getConnection(connectionUrl, decryptedUsername, decryptedPassword);
    }

    private static String buildConnectionUrl() {
        switch (ConfigReader.getDbType().toLowerCase()) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s", ConfigReader.getDbHost(), ConfigReader.getDbPort(), ConfigReader.getDbName());
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s", ConfigReader.getDbHost(), ConfigReader.getDbPort(), ConfigReader.getDbName());
            case "mssql":
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s", ConfigReader.getDbHost(), ConfigReader.getDbPort(), ConfigReader.getDbName());
            default:
                throw new IllegalArgumentException("Unsupported database type: " + ConfigReader.getDbType());
        }
    }

}