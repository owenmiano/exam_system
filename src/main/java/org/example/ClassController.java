package org.example;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.*;

public class ClassController {

    public static void findClass(Connection connection, HashMap<String, Object> classData, String[] columns) {
        try {
            if (classData == null || classData.isEmpty()) {
                System.out.println("No class data provided.");
                return;
            }

            StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
            ArrayList<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : classData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                whereClauseJoiner.add(key + " = ?");
                values.add(value);
            }

            String whereClause = whereClauseJoiner.toString();
            JsonArray jsonArrayResult = GenericQueries.select(connection, "class");
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }



    //insert
public static void createClass(Connection connection, HashMap<String, Object> classData) {
    if (classData == null || !classData.containsKey("class_name")) {
        System.out.println("Class data is missing or incomplete.");
        return;
    }
    // Check if className is null or empty
    String className = classData.get("class_name") != null ? classData.get("class_name").toString() : "";

    if (className.trim().isEmpty()) {
        System.out.println("Class name cannot be empty.");
        return;
    }


    boolean isInserted = GenericQueries.insertData(connection, "class", classData); // Replace "class" with your actual table name


    if (isInserted) {
        System.out.println("Class added successfully");
    } else {
        System.out.println("Failed to add class");
    }
}


    public static void updateClass(Connection connection, HashMap<String, Object> classData, String classIdString) {
        try {
            if (classData == null || classData.isEmpty()) {
                System.out.println("Class data is missing or empty.");
                return;
            }
            int classId = Integer.parseInt(classIdString);
            String whereClause = "class_id = ?";

            JsonObject result = GenericQueries.update(connection, "class", classData, whereClause,new Object[]{classId});

            if (result.get("success").getAsBoolean()) {
                System.out.println("Class updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
            } else {
                System.out.println("No rows were updated.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}




