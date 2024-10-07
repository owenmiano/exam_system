package ke.co.skyworld.queryBuilder;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

public class SelectQuery {
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
            System.out.println(pstmt);
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
    public static JsonArray select(Connection conn, String table,String[] columns, int limit, int offset) throws SQLException {
        String columnList = String.join(", ", columns);
        return querySelect(conn, "SELECT " + columnList +  " FROM " + table + " LIMIT " + limit + " OFFSET " + offset);
    }
    public static JsonArray select(Connection conn, String baseTable, String[] columns, String[][] joins, int limit, int offset) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder("SELECT ");
        String columnList = String.join(", ", columns);
        queryBuilder.append(columnList).append(" FROM ").append(baseTable);

        for (String[] join : joins) {
            if (join.length >= 3) { // Ensure each join information array contains exactly three elements
                queryBuilder.append(" ").append(join[0])
                        .append(" JOIN ")
                        .append(join[1])
                        .append(" ON ")
                        .append(join[2]);
                // Add additional ON conditions if present
                for (int i = 3; i < join.length; i++) {
                    queryBuilder.append(" AND ").append(join[i]);
                }
            } else {
                throw new IllegalArgumentException("Each join must include exactly three elements: join type, join table, and join condition.");
            }
        }

        queryBuilder.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);

        return querySelect(conn, queryBuilder.toString());
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

}
