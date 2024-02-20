package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class QuestionController {

    public static void findQuestionById(Connection connection, HttpServerExchange exchange,String questionIdString) {
        try {
            int examScheduleId = Integer.parseInt(questionIdString);
            String[] columns = {
                    "q.question_no",
                    "q.description",
                    "q.marks",
                    "s.subject_name"
            };

            String table = "questions q " +
                    "JOIN exam_subjects es ON q.exam_subject_id = es.exam_subject_id " +
                    "JOIN subject s ON es.subject_id = s.subject_id";

            String whereClause = "questions_id = ?";

            Object[] values = new Object[]{examScheduleId};

            try {
                JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns, whereClause, values);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

                exchange.getResponseSender().send(jsonArrayResult.toString());
            } catch (SQLException e) {
                String errorMessage = "SQL Error occurred: " + e.getMessage();
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } catch (NumberFormatException e) {
            String errorMessage = "Invalid question ID format.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        } catch (Exception e) {
            String errorMessage = "An error occurred: " + e.getMessage();
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }

    public static void findAllQuestions(Connection connection, HttpServerExchange exchange) {
        try {
            String[] columns = {
                    "q.question_no",
                    "q.description",
                    "q.marks",
                    "s.subject_name"
            };

            String table = "questions q " +
                    "JOIN exam_subjects es ON q.exam_subject_id = es.exam_subject_id " +
                    "JOIN subject s ON es.subject_id = s.subject_id";

            try {
                JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(jsonArrayResult.toString());
            } catch (SQLException e) {
                String errorMessage = "SQL Error occurred: " + e.getMessage();
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        }  catch (Exception e) {
            String errorMessage = "An error occurred: " + e.getMessage();
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }


    public static void createQuestion(Connection connection, HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
            Gson gson = new Gson();
            JsonObject questionData = gson.fromJson(requestBody, JsonObject.class);

            if (!questionData.has("question_no") || questionData.get("question_no").isJsonNull()) {
                System.out.println("Question number is missing.");
                return;
            }

            if (!questionData.has("description") || questionData.get("description").isJsonNull() || questionData.get("description").getAsString().trim().isEmpty()) {
                System.out.println("Description cannot be empty.");
                return;
            }

            if (!questionData.has("marks") || questionData.get("marks").isJsonNull()) {
                System.out.println("Marks is missing.");
                return;
            }

            String insertionResult = GenericQueries.insertData(connection, "questions", questionData);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            System.out.println(insertionResult);
            exchange.getResponseSender().send(insertionResult);
        });
    }

    public static void updateQuestion(Connection connection, HttpServerExchange exchange) {
        Deque<String> questionIdDeque = exchange.getQueryParameters().get("id");
        if (questionIdDeque != null && !questionIdDeque.isEmpty()) {
            String questionIdString = questionIdDeque.getFirst();

            try {
                int questionId = Integer.parseInt(questionIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();
                    JsonObject questionData = gson.fromJson(requestBody, JsonObject.class);

                    String whereClause = "questions_id = ?";

                    String result = GenericQueries.update(connection, "questions", questionData, whereClause, questionId);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange.getResponseSender().send(result);

                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid question ID: " + questionIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } else {
            // Handle the case where the "id" parameter is missing
            String errorMessage = "Question ID is missing in the request URL.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }

    public static void createChoice(Connection connection, HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
            Gson gson = new Gson();
            JsonObject choiceData = gson.fromJson(requestBody, JsonObject.class);

            if (!choiceData.has("option_label") || choiceData.get("option_label").isJsonNull()) {
                System.out.println("Option label is missing.");
                return;
            }

            if (!choiceData.has("option_value") ||  choiceData.get("option_value").getAsString().trim().isEmpty()) {
                System.out.println("Option value cannot be empty.");
                return;
            }


            String insertionResult = GenericQueries.insertData(connection, "choices", choiceData);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange1.getResponseSender().send(insertionResult);
        });
    }

//    public static void findAllChoices(Connection connection, HttpServerExchange exchange) {
//        Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
//        String[] columns;
//        if (columnsDeque != null && !columnsDeque.isEmpty()) {
//            String columnsString = columnsDeque.getFirst();
//            columns = columnsString.split(",");
//        } else {
//            // If no columns parameter provided, select all columns
//            columns = new String[]{"*"};
//        }
//
//        final String[] finalColumns = columns;
//
//        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
//            Gson gson = new Gson();
//
//            try {
//                JsonArray jsonArrayResult = GenericQueries.select(connection, "choices", finalColumns);
//                exchange1.getResponseSender().send(jsonArrayResult.toString());
//            } catch (SQLException e) {
//                String errorMessage = "SQL Error occurred: " + e.getMessage();
//                System.out.println(errorMessage);
//                exchange1.getResponseSender().send(errorMessage);
//            }
//        });
//    }
//
//    public static void findChoicesById(Connection connection, HttpServerExchange exchange) {
//
//    }

    public static void updateChoice(Connection connection, HttpServerExchange exchange) {
        Deque<String> choiceIdDeque = exchange.getQueryParameters().get("id");
        if (choiceIdDeque != null && !choiceIdDeque.isEmpty()) {
            String choiceIdString = choiceIdDeque.getFirst();

            try {
                int choicesId = Integer.parseInt(choiceIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();
                    JsonObject questionData = gson.fromJson(requestBody, JsonObject.class);

                    String whereClause = "choices_id = ?";

                    String result = GenericQueries.update(connection, "choices", questionData, whereClause, choicesId);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(result);

                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid choices ID: " + choiceIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } else {
            // Handle the case where the "id" parameter is missing
            String errorMessage = "Choices ID is missing in the request URL.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }
}
