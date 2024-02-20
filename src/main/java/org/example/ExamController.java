package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class ExamController {
    public static void findExamById(Connection connection, HttpServerExchange exchange,String examIdString) {
            // Extracting the columns parameter from the query string
            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
            String[] columns = null;
            if (columnsDeque != null && !columnsDeque.isEmpty()) {
                String columnsString = columnsDeque.getFirst();
                columns = columnsString.split(",");
            } else {
                // If no columns parameter provided, select all columns
                columns = new String[]{"*"};
            }

            try {
                int examId = Integer.parseInt(examIdString);
                final String[] finalColumns = columns; // Final copy of columns array

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {

                    String whereClause = "exam_id = ?";

                    try {
                        JsonArray jsonArrayResult = GenericQueries.select(connection, "exam", finalColumns, whereClause, examId);
                        exchange1.getResponseSender().send(jsonArrayResult.toString());
                    } catch (SQLException e) {
                        String errorMessage = "SQL Error occurred: " + e.getMessage();
                        System.out.println(errorMessage);
                        exchange1.getResponseSender().send(errorMessage);
                    }
                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid exam ID: " + examIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
    }

    public static void findAll(Connection connection, HttpServerExchange exchange) {
        // Extracting the columns parameter from the query string
        Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
        String[] columns = null;
        if (columnsDeque != null && !columnsDeque.isEmpty()) {
            String columnsString = columnsDeque.getFirst();
            columns = columnsString.split(",");
        } else {
            // If no columns parameter provided, select all columns
            columns = new String[]{"*"};
        }

        final String[] finalColumns = columns;
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {

            try {
                JsonArray jsonArrayResult = GenericQueries.select(connection, "exam", finalColumns);
                exchange1.getResponseSender().send(jsonArrayResult.toString());
            } catch (SQLException e) {
                String errorMessage = "SQL Error occurred: " + e.getMessage();
                System.out.println(errorMessage);
                exchange1.getResponseSender().send(errorMessage);
            }
        });
    }

    public static void createExam(Connection connection, HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
            Gson gson = new Gson();
            JsonObject examData = gson.fromJson(requestBody, JsonObject.class);

            if (examData == null || !examData.has("exam_name") || examData.get("exam_name").getAsString().trim().isEmpty()) {
                String errorMessage = "Exam name is missing.";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
                return;
            }

            String insertionResult = GenericQueries.insertData(connection, "exam", examData);
            System.out.println(insertionResult);
            exchange1.getResponseSender().send(insertionResult);
        });
    }

    public static void updateExam(Connection connection, HttpServerExchange exchange) {
        // Extracting the class ID from the URL path
        Deque<String> examIdDeque = exchange.getQueryParameters().get("id");
        if (examIdDeque != null && !examIdDeque.isEmpty()) {
            String examIdString = examIdDeque.getFirst();

            try {
                int examId = Integer.parseInt(examIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();
                    JsonObject examData = gson.fromJson(requestBody, JsonObject.class);

                    if (examData == null || !examData.has("exam_name") || examData.get("exam_name").getAsString().trim().isEmpty()) {
                        String errorMessage = "Exam name is missing.";
                        System.out.println(errorMessage);
                        exchange.getResponseSender().send(errorMessage);
                        return;
                    }

                    String whereClause = "exam_id = ?";

                    String result = GenericQueries.update(connection, "exam", examData, whereClause, examId);
                    exchange1.getResponseSender().send(result);

                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid exam ID: " + examIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } else {
            // Handle the case where the "id" parameter is missing
            String errorMessage = "Exam ID is missing in the request URL.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }


    public static void createExamSchedule(Connection connection, HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
            Gson gson = new Gson();
            JsonObject examScheduleData = gson.fromJson(requestBody, JsonObject.class);

            if (examScheduleData == null || !examScheduleData.has("exam_id") || examScheduleData.get("exam_id").getAsString().trim().isEmpty()) {
                String errorMessage = "Exam ID is missing.";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
                return;
            }
            if (examScheduleData == null || !examScheduleData.has("subject_id") || examScheduleData.get("subject_id").getAsString().trim().isEmpty()) {
                String errorMessage = "Subject ID is missing.";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
                return;
            }
            if (examScheduleData == null || !examScheduleData.has("teacher_id") || examScheduleData.get("teacher_id").getAsString().trim().isEmpty()) {
                String errorMessage = "Teacher ID is missing.";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
                return;
            }


            String insertionResult = GenericQueries.insertData(connection, "exam_subjects", examScheduleData);
            System.out.println(insertionResult);
            exchange1.getResponseSender().send(insertionResult);
        });
    }


    public static void findExamScheduleById(Connection connection, HttpServerExchange exchange,String examScheduleIdString) {
            try {
                int examScheduleId = Integer.parseInt(examScheduleIdString);
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

                Object[] values = new Object[]{examScheduleId};

                try {
                    JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns, whereClause, values);
                    exchange.getResponseSender().send(jsonArrayResult.toString());
                } catch (SQLException e) {
                    String errorMessage = "SQL Error occurred: " + e.getMessage();
                    System.out.println(errorMessage);
                    exchange.getResponseSender().send(errorMessage);
                }
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid exam schedule ID format.";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            } catch (Exception e) {
                String errorMessage = "An error occurred: " + e.getMessage();
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
    }

    public static void findAllExamSchedules(Connection connection, HttpServerExchange exchange) {
        // Extracting the columns parameter from the query string
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
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {

            try {
                JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns);
                exchange1.getResponseSender().send(jsonArrayResult.toString());
            } catch (SQLException e) {
                String errorMessage = "SQL Error occurred: " + e.getMessage();
                System.out.println(errorMessage);
                exchange1.getResponseSender().send(errorMessage);
            }
        });
    }

    public static void updateExamSchedule(Connection connection, HttpServerExchange exchange) {
        Deque<String> examScheduleIdDeque = exchange.getQueryParameters().get("id");
        if (examScheduleIdDeque != null && !examScheduleIdDeque.isEmpty()) {
            String examScheduleIdString = examScheduleIdDeque.getFirst();

            try {
                int examScheduleId = Integer.parseInt(examScheduleIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();
                    JsonObject examScheduleData = gson.fromJson(requestBody, JsonObject.class);

                    String whereClause = "exam_subject_id = ?";

                    String result = GenericQueries.update(connection, "exam_subjects", examScheduleData, whereClause, examScheduleId);
                    exchange1.getResponseSender().send(result);

                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid exam schedule ID: " + examScheduleIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } else {
            // Handle the case where the "id" parameter is missing
            String errorMessage = "Exam schedule ID is missing in the request URL.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }

}
