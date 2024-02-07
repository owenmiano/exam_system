package org.example;

import java.sql.*;
import java.util.*;

import com.google.gson.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.lang.reflect.Type;

public class GenericQueries {
    //  insert global method
    public static boolean insertData(Connection connection, String tableName, HashMap<String, Object> data) {
        StringJoiner columnNames = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");

        for (String column : data.keySet()) {
            columnNames.add(column);
            placeholders.add("?");
        }

        String sql = "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int index = 1;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                pstmt.setObject(index++, entry.getValue());
            }

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Insertion error: " + e.getMessage());
            return false;
        }
    }
    // select global method
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return src == null ? null : new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            })
            .create();

    private static JsonArray querySelect(Connection conn, String query, Object... params) throws SQLException {
        JsonArray jsonArray = new JsonArray();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            int index = 1;
            for (Object param : params) {
                pstmt.setObject(index++, param);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    JsonObject obj = new JsonObject();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        if (value instanceof LocalDateTime) {
                            obj.add(columnName, gson.toJsonTree((LocalDateTime) value));
                        } else {
                            obj.add(columnName, gson.toJsonTree(value));
                        }
                    }
                    jsonArray.add(obj);
                }
            }
        }
        return jsonArray;
    }

    // Select all columns from a table
    public static JsonArray select(Connection conn, String table) throws SQLException {
        return querySelect(conn, "SELECT * FROM " + table);
    }

    // Select specific columns from a table
    public static JsonArray select(Connection conn, String table, String[] columns) throws SQLException {
        String columnList = String.join(", ", columns);
        return querySelect(conn, "SELECT " + columnList + " FROM " + table);
    }

    // Select with WHERE clause
    public static JsonArray select(Connection conn, String table, String where, Object... params) throws SQLException {
        return querySelect(conn, "SELECT * FROM " + table + " WHERE " + where, params);
    }

    // Select with WHERE clause and specific columns
    public static JsonArray select(Connection conn, String table, String[] columns, String where, Object... params) throws SQLException {
        String columnList = String.join(", ", columns);
        return querySelect(conn, "SELECT " + columnList + " FROM " + table + " WHERE " + where, params);
    }

    // Select with WHERE clause and GROUP BY
    public static JsonArray select(Connection conn, String table, String where, String groupBy, Object... params) throws SQLException {
        return querySelect(conn, "SELECT * FROM " + table + " WHERE " + where + " GROUP BY " + groupBy, params);
    }

    // Select with WHERE clause, GROUP BY, and specific columns
    public static JsonArray select(Connection conn, String table, String[] columns, String where, String groupBy, Object... params) throws SQLException {
        String columnList = String.join(", ", columns);
        return querySelect(conn, "SELECT " + columnList + " FROM " + table + " WHERE " + where + " GROUP BY " + groupBy, params);
    }

    // Select with LIMIT and OFFSET
    public static JsonArray select(Connection conn, String table, int limit, int offset) throws SQLException {
        return querySelect(conn, "SELECT * FROM " + table + " LIMIT " + limit + " OFFSET " + offset);
    }

    //select with join
    public static JsonArray select(Connection conn, String baseTable, String[][] joins, String[] columns) throws SQLException {
        // Construct the SELECT clause
        String columnList = columns.length > 0 ? String.join(", ", columns) : "*";

        // Begin constructing the JOIN clause(s)
        StringBuilder queryBuilder = new StringBuilder("SELECT " + columnList + " FROM " + baseTable);
        for (String[] join : joins) {
            if (join.length == 3) {
                queryBuilder.append(" ")
                        .append(join[0])
                        .append(" JOIN ")
                        .append(join[1])
                        .append(" ON ")
                        .append(join[2]);
            } else {
                throw new IllegalArgumentException("Each join must include exactly three elements: join type, join table, and join condition.");
            }
        }

        // Call the existing querySelect method to execute the constructed query
        return querySelect(conn, queryBuilder.toString());
    }

    // Select with JOIN also with where
    public static JsonArray select(Connection conn, String baseTable, String[][] joins, String[] columns, String whereClause,String groupByClause, Object... params) throws SQLException {
        // Construct the SELECT clause
        String columnList = columns.length > 0 ? String.join(", ", columns) : "*";

        // Begin constructing the JOIN clause(s)
        StringBuilder queryBuilder = new StringBuilder("SELECT " + columnList + " FROM " + baseTable);
        for (String[] join : joins) {
            if (join.length == 3) { // Ensure each join information array contains exactly three elements
                queryBuilder.append(" ")
                        .append(join[0]) // Join Type (e.g., "INNER JOIN")
                        .append(" JOIN ")
                        .append(join[1]) // Join Table (e.g., "exam_subjects es")
                        .append(" ON ")
                        .append(join[2]); // Join Condition (e.g., "e.exam_id = es.exam_id")
            } else {
                throw new IllegalArgumentException("Each join must include exactly three elements: join type, join table, and join condition.");
            }
        }

        // Add the WHERE clause if it's provided
        if (!whereClause.isEmpty()) {
            queryBuilder.append(" WHERE ").append(whereClause);
        }
        if (groupByClause != null && !groupByClause.isEmpty()) {
            queryBuilder.append(" GROUP BY ").append(groupByClause);
        }
        // Execute the querySelect method, passing the constructed query and parameters for the WHERE clause
        return querySelect(conn, queryBuilder.toString(), params);
    }

    public static JsonArray select(Connection conn, String baseTable, String[][] joins, String[] columns, String whereClause, Object... params) throws SQLException {
        // Construct the SELECT clause
        String columnList = columns.length > 0 ? String.join(", ", columns) : "*";

        // Begin constructing the JOIN clause(s)
        StringBuilder queryBuilder = new StringBuilder("SELECT " + columnList + " FROM " + baseTable);
        for (String[] join : joins) {
            if (join.length == 3) { // Ensure each join information array contains exactly three elements
                queryBuilder.append(" ")
                        .append(join[0]) // Join Type (e.g., "INNER JOIN")
                        .append(" JOIN ")
                        .append(join[1]) // Join Table (e.g., "exam_subjects es")
                        .append(" ON ")
                        .append(join[2]); // Join Condition (e.g., "e.exam_id = es.exam_id")
            } else {
                throw new IllegalArgumentException("Each join must include exactly three elements: join type, join table, and join condition.");
            }
        }

        // Add the WHERE clause if it's provided
        if (!whereClause.isEmpty()) {
            queryBuilder.append(" WHERE ").append(whereClause);
        }

        // Execute the querySelect method, passing the constructed query and parameters for the WHERE clause
        return querySelect(conn, queryBuilder.toString(), params);
    }


    // Combination of WHERE, GROUP BY, ORDER BY, specific columns, and LIMIT
    public static JsonArray select(Connection conn, String baseTable, String[][] joins, String[] columns, String where, String groupBy, String orderBy, Integer limit, Integer offset, Object... params) throws SQLException {
        String columnList = String.join(", ", columns);
        StringJoiner query = new StringJoiner(" ");
        query.add("SELECT").add(columnList).add("FROM").add(baseTable);

        for (String[] join : joins) {
            if (join.length == 3) { // Ensure each join information array contains exactly three elements
                query.add(join[0]) // Join Type (e.g., "INNER JOIN")
                        .add("JOIN")
                        .add(join[1]) // Join Table (e.g., "exam_subjects es")
                        .add("ON")
                        .add(join[2]);
            } else {
                throw new IllegalArgumentException("Each join must include exactly three elements: join type, join table, and join condition.");
            }
        }

        if (where != null && !where.isEmpty()) {
            query.add("WHERE").add(where);
        }

        if (groupBy != null && !groupBy.isEmpty()) {
            query.add("GROUP BY").add(groupBy);
        }

        if (orderBy != null && !orderBy.isEmpty()) {
            query.add("ORDER BY").add(orderBy);
        }

        if (limit != null) {
            query.add("LIMIT").add(limit.toString());
        }

        if (offset != null) {
            query.add("OFFSET").add(offset.toString());
        }

        return querySelect(conn, query.toString(), params);
    }


    // Global update method
    public static JsonObject update(Connection conn, String table, Map<String, Object> updates, String where, Object... whereParams) throws SQLException {
        JsonObject result = new JsonObject();
        if (updates.isEmpty()) {
            result.addProperty("success", false);
            result.addProperty("message", "No updates provided");
            return result;
        }

        // Constructing the SET clause
        StringJoiner setClauses = new StringJoiner(", ");
        for (String column : updates.keySet()) {
            setClauses.add(column + " = ?");
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
            for (Object value : updates.values()) {
                pstmt.setObject(index++, value);
            }

            // Setting values for the WHERE clause parameters
            if (whereParams != null) {
                for (Object param : whereParams) {
                    pstmt.setObject(index++, param);
                }
            }

            int affectedRows = pstmt.executeUpdate();

            // Setting the result properties
            result.addProperty("success", affectedRows > 0);
            result.addProperty("rowsAffected", affectedRows);
            return result;
        }
    }












}
