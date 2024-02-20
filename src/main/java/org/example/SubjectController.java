package org.example;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.sql.*;
import java.util.*;

public class SubjectController {

    public static void findSubjectById(Connection connection, HttpServerExchange exchange,String subjectIdString) {
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
                int subjectId = Integer.parseInt(subjectIdString);
                final String[] finalColumns = columns;

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {

                    String whereClause = "subject_id = ?";

                    try {
                        JsonArray jsonArrayResult = GenericQueries.select(connection, "subject", finalColumns, whereClause, subjectId);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange1.getResponseSender().send(jsonArrayResult.toString());
                    } catch (SQLException e) {
                        String errorMessage = "SQL Error occurred: " + e.getMessage();
                        System.out.println(errorMessage);
                        exchange1.getResponseSender().send(errorMessage);
                    }
                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid subject ID: " + subjectIdString;
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
                JsonArray jsonArrayResult = GenericQueries.select(connection, "subject", finalColumns);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange1.getResponseSender().send(jsonArrayResult.toString());
            } catch (SQLException e) {
                String errorMessage = "SQL Error occurred: " + e.getMessage();
                System.out.println(errorMessage);
                exchange1.getResponseSender().send(errorMessage);
            }
        });
    }


    //insert
    public static void createSubject(Connection connection, HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
            Gson gson = new Gson();
            JsonObject subjectData = gson.fromJson(requestBody, JsonObject.class);

            if (subjectData == null || !subjectData.has("subject_name") || subjectData.get("subject_name").getAsString().trim().isEmpty()) {
                String errorMessage = "Subject name is missing.";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
                return;
            }

            String insertionResult = GenericQueries.insertData(connection, "subject", subjectData);
            System.out.println(insertionResult);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange1.getResponseSender().send(insertionResult);
        });
    }

    //update class
    public static void updateSubject(Connection connection, HttpServerExchange exchange) {
        // Extracting the class ID from the URL path
        Deque<String> subjectIdDeque = exchange.getQueryParameters().get("id");
        if (subjectIdDeque != null && !subjectIdDeque.isEmpty()) {
            String subjectIdString = subjectIdDeque.getFirst();

            try {
                int subjectId = Integer.parseInt(subjectIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();
                    JsonObject subjectData = gson.fromJson(requestBody, JsonObject.class);

                    if (subjectData == null || !subjectData.has("subject_name") || subjectData.get("subject_name").getAsString().trim().isEmpty()) {
                        String errorMessage = "Subject name is missing.";
                        System.out.println(errorMessage);
                        exchange.getResponseSender().send(errorMessage);
                        return;
                    }

                    String whereClause = "subject_id = ?";

                    String result = GenericQueries.update(connection, "subject", subjectData, whereClause, subjectId);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(result);

                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid subject ID: " + subjectIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } else {
            // Handle the case where the "id" parameter is missing
            String errorMessage = "Subject ID is missing in the request URL.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }
}




