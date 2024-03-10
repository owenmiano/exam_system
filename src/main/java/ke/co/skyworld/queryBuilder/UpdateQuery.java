package ke.co.skyworld.queryBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.StringJoiner;

public class UpdateQuery {
    public static String update(Connection conn, String table, JsonObject updates, String where, Object... whereParams) {
        try {
            // Constructing the SET clause
            StringJoiner setClauses = new StringJoiner(", ");
            for (Map.Entry<String, JsonElement> entry : updates.entrySet()) {
                setClauses.add(entry.getKey() + " = ?");
            }

            // Add date_modified to be updated to the current timestamp
            setClauses.add("date_modified = CURRENT_TIMESTAMP");
            // Building the SQL query
            String sql = "UPDATE " + table + " SET " + setClauses;

            // If where clause is provided, add it to the SQL statement
            if (where != null && !where.isEmpty()) {
                sql += " WHERE " + where;
            }

            // Executing the update
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int index = 1;
                // Setting values for the columns to be updated
                for (Map.Entry<String, JsonElement> entry : updates.entrySet()) {
                    JsonElement value = entry.getValue();
                    // Convert JsonElement to appropriate Java object
                    if (value.isJsonPrimitive()) {
                        pstmt.setObject(index++, value.getAsString());
                    } else if (value.isJsonNull()) {
                        pstmt.setNull(index++, java.sql.Types.NULL);
                    } else {
                        pstmt.setString(index++, value.toString());
                    }
                }

                // Setting values for the WHERE clause parameters
                if (whereParams != null) {
                    for (Object param : whereParams) {
                        pstmt.setObject(index++, param);
                    }
                }

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    return "Data Updated successfully";
                } else {
                    return "Failed to update data";
                }
            }
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }
}
