package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class ExamController {
    public static void findExam(Connection connection, HashMap<String, Object> examData, String[] columns) {
        try {
            if (examData == null || examData.isEmpty()) {
                System.out.println("No Exam data provided.");
                return;
            }

            StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
            ArrayList<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : examData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                whereClauseJoiner.add(key + " = ?");
                values.add(value);
            }

            String whereClause = whereClauseJoiner.toString();
            JsonArray jsonArrayResult = GenericQueries.select(connection, "exam", columns, whereClause, values.toArray());
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void createExam(Connection connection, HashMap<String, Object> examData) {
        if (examData == null || !examData.containsKey("exam_name")) {
            System.out.println("Exam data is missing or incomplete.");
            return;
        }
        String examName = examData.get("exam_name") != null ? examData.get("exam_name").toString() : "";

        if (examName.trim().isEmpty()) {
            System.out.println("Exam name cannot be empty.");
            return;
        }


        boolean isInserted = GenericQueries.insertData(connection, "exam", examData); // Replace "class" with your actual table name


        if (isInserted) {
            System.out.println("Exam added successfully");
        } else {
            System.out.println("Failed to add class");
        }
    }

    public static void updateExam(Connection connection, HashMap<String, Object> examData, String examIdString) {
        try {
            if (examData == null || examData.isEmpty()) {
                System.out.println("Exam data is missing or empty.");
                return;
            }
            int examId = Integer.parseInt(examIdString);
            String whereClause = "exam_id = ?";

            JsonObject result = GenericQueries.update(connection, "exam", examData, whereClause,new Object[]{examId});

            if (result.get("success").getAsBoolean()) {
                System.out.println("Exam updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
            } else {
                System.out.println("No rows were updated.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void createExamSchedule(Connection connection, HashMap<String, Object> examScheduleData) {

        boolean isInserted = GenericQueries.insertData(connection, "exam_subjects", examScheduleData); // Replace "class" with your actual table name


        if (isInserted) {
            System.out.println("Exam Schedule added successfully");
        } else {
            System.out.println("Failed to add Exam Schedule");
        }
    }

    public static void findExamSchedule(Connection connection, int examSubjectId) {
        try {
            String[] columns = {
                    "e.exam_id",
                    "e.exam_name",
                    "es.exam_duration",
                    "es.exam_date",
                    "t.teacher_name",
                    "s.subject_name",
                    "cl.class_name"
            };

            String table = "exam_subjects es " +
                    "JOIN exam e ON e.exam_id = es.exam_id " +
                    "JOIN teachers t ON es.teacher_id = t.teacher_id " +
                    "JOIN subject s ON es.subject_id = s.subject_id " +
                    "JOIN class cl ON e.class_id = cl.class_id";

            String whereClause = "es.exam_subject_id = ?";

            Object[] values = new Object[]{examSubjectId};

            JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns, whereClause, values);

            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void updateExamSchedule(Connection connection, HashMap<String, Object> examScheduleData, int examSubjectId) {
        try {
            if (examScheduleData == null || examScheduleData.isEmpty()) {
                System.out.println("Exam Schedule data is missing or empty.");
                return;
            }

            String whereClause = "exam_subject_id = ?";

            JsonObject result = GenericQueries.update(connection, "exam_subjects", examScheduleData, whereClause,new Object[]{examSubjectId});

            if (result.get("success").getAsBoolean()) {
                System.out.println("Exam Schedule updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
            } else {
                System.out.println("No rows were updated.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
