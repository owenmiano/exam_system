package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class QuestionController {

    public static void findQuestion(Connection connection, HashMap<String, Object> questionData, String[] columns) {
        try {
            if (questionData == null || questionData.isEmpty()) {
                System.out.println("No Question data provided.");
                return;
            }

            StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
            ArrayList<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : questionData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                whereClauseJoiner.add(key + " = ?");
                values.add(value);
            }

            String whereClause = whereClauseJoiner.toString();
            JsonArray jsonArrayResult = GenericQueries.select(connection, "questions", columns, whereClause, values.toArray());
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void createQuestion(Connection connection, HashMap<String, Object> questionData) {
        if (questionData == null || !questionData.containsKey("exam_name")) {
            System.out.println("Question data is missing or incomplete.");
            return;
        }
        if (!questionData.containsKey("question_no") || questionData.get("question_no") == null) {
            System.out.println("Question number is missing.");
            return;
        }

        if (!questionData.containsKey("description") || questionData.get("description") == null || questionData.get("description").toString().trim().isEmpty()) {
            System.out.println("Description cannot be empty.");
            return;
        }

        if (!questionData.containsKey("marks") || questionData.get("marks") == null) {
            System.out.println("Marks is missing.");
            return;
        }


        boolean isInserted = GenericQueries.insertData(connection, "questions", questionData); // Replace "class" with your actual table name


        if (isInserted) {
            System.out.println("Question added successfully");
        } else {
            System.out.println("Failed to add question");
        }
    }
    public static void updateQuestion(Connection connection, HashMap<String, Object> questionData, String questionIdString) {
        try {
            if (questionData == null || questionData.isEmpty()) {
                System.out.println("Question data is missing or empty.");
                return;
            }
            int questionId = Integer.parseInt(questionIdString);
            String whereClause = "question_id = ?";

            JsonObject result = GenericQueries.update(connection, "questions", questionData, whereClause,new Object[]{questionId});

            if (result.get("success").getAsBoolean()) {
                System.out.println("Question updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
            } else {
                System.out.println("No rows were updated.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
