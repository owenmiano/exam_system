package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class QuestionController {

    public static void findQuestion(Connection connection, HttpServerExchange exchange) {
        Deque<String> questionIdDeque = exchange.getQueryParameters().get("id");
        if (questionIdDeque != null && !questionIdDeque.isEmpty()) {
            String questionIdString = questionIdDeque.getFirst();

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
                int questionId = Integer.parseInt(questionIdString);
                final String[] finalColumns = columns; // Final copy of columns array

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();

                    String whereClause = "questions_id = ?";

                    try {
                        JsonArray jsonArrayResult = GenericQueries.select(connection, "questions", finalColumns, whereClause, questionId);
                        exchange1.getResponseSender().send(jsonArrayResult.toString());
                    } catch (SQLException e) {
                        String errorMessage = "SQL Error occurred: " + e.getMessage();
                        System.out.println(errorMessage);
                        exchange1.getResponseSender().send(errorMessage);
                    }
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
            System.out.println(insertionResult);
            exchange1.getResponseSender().send(insertionResult);
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
                    exchange1.getResponseSender().send(result);

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
            System.out.println(insertionResult);
            exchange1.getResponseSender().send(insertionResult);
        });
    }

    public static void findChoice(Connection connection, HttpServerExchange exchange) {
        Deque<String> choiceIdDeque = exchange.getQueryParameters().get("id");
        if (choiceIdDeque != null && !choiceIdDeque.isEmpty()) {
            String choiceIdString = choiceIdDeque.getFirst();

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
                int choicesId = Integer.parseInt(choiceIdString);
                final String[] finalColumns = columns; // Final copy of columns array

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();

                    String whereClause = "choices_id = ?";

                    try {
                        JsonArray jsonArrayResult = GenericQueries.select(connection, "choices", finalColumns, whereClause, choicesId);
                        exchange1.getResponseSender().send(jsonArrayResult.toString());
                    } catch (SQLException e) {
                        String errorMessage = "SQL Error occurred: " + e.getMessage();
                        System.out.println(errorMessage);
                        exchange1.getResponseSender().send(errorMessage);
                    }
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
