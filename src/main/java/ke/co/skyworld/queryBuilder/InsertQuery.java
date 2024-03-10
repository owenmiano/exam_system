package ke.co.skyworld.queryBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.Map;
import java.util.StringJoiner;

public class InsertQuery {
    public static String insertData(Connection connection, String tableName, JsonObject data) {
        StringJoiner columnNames = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");

        for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
            columnNames.add(entry.getKey());
            placeholders.add("?");
        }

        String sql = "INSERT INTO " + tableName + " (" + columnNames.toString() + ") VALUES (" + placeholders.toString() + ")";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                JsonElement value = entry.getValue();
                // Convert JsonElement to appropriate Java object
                if (value.isJsonPrimitive()) {
                    if (value.getAsJsonPrimitive().isNumber()) {
                        pstmt.setObject(index++, value.getAsInt()); // Use getAsInt() for numeric values
                    } else if (value.getAsJsonPrimitive().isBoolean()) {
                        pstmt.setObject(index++, value.getAsBoolean());
                    } else if (value.getAsJsonPrimitive().isString()) {
                        pstmt.setObject(index++, value.getAsString());
                    } else {
                        pstmt.setObject(index++, null);
                    }
                } else if (value.isJsonNull()) {
                    pstmt.setObject(index++, null);
                } else {
                    // For JsonArray or JsonObject, convert to String or handle differently
                    pstmt.setObject(index++, value.toString());
                }
            }
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int insertId = generatedKeys.getInt(1);
                    return "Data inserted successfully:" + insertId;
                } else {
                    return "Failed to retrieve insert ID";
                }
            } else {
                return "Failed to insert data";
            }
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }
}
