package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class SubjectController {
    public static void findSubject(Connection connection, HashMap<String, Object> subjectData, String[] columns) {
        try {
            if (subjectData == null || subjectData.isEmpty()) {
                System.out.println("No Subject data provided.");
                return;
            }

            StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
            ArrayList<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : subjectData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                whereClauseJoiner.add(key + " = ?");
                values.add(value);
            }

            String whereClause = whereClauseJoiner.toString();
            JsonArray jsonArrayResult = GenericQueries.select(connection, "subject", columns, whereClause, values.toArray());
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //insert
    public static void createSubject(Connection connection, HashMap<String, Object> subjectData) {
        if (subjectData == null) {
            System.out.println("Subject data is missing or incomplete.");
            return;
        }
        // Check if className is null or empty
        String className = subjectData.get("subject_name").toString() ;

        if (className.trim().isEmpty()) {
            System.out.println("Subject cannot be empty.");
            return;
        }

        boolean isInserted = GenericQueries.insertData(connection, "subject", subjectData); // Replace "class" with your actual table name

        if (isInserted) {
            System.out.println("Subject added successfully");
        } else {
            System.out.println("Failed to add subject");
        }
    }


    public static void updateSubject(Connection connection, HashMap<String, Object> subjectData, String subjectIdString) {
        try {
            // Check if classData is not null or empty
            if (subjectData == null || subjectData.isEmpty()) {
                System.out.println("Subject data is missing or empty.");
                return;
            }
            int subjectId = Integer.parseInt(subjectIdString);
            // WHERE clause to identify the specific class to update
            String whereClause = "subject_id = ?"; // Assuming the identifier column is 'class_id'

            // Call the updateWithJoin method with a null joinClause
            JsonObject result = GenericQueries.update(connection, "subject", subjectData, whereClause,new Object[]{subjectId});

            // Optionally handle the result
            if (result.get("success").getAsBoolean()) {
                System.out.println("Subject updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
            } else {
                System.out.println("No rows were updated.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
