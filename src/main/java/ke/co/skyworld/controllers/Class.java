package ke.co.skyworld.controllers;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.*;
import java.util.*;

public class Class {

    public static void findClassById(Connection connection, HttpServerExchange exchange) {
        // Extracting the class ID from the path parameters
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        String classIdString = pathMatch.getParameters().get("id");

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

        // Ensure classIdString is not null or empty before parsing
        if (classIdString == null || classIdString.isEmpty()) {
            exchange.getResponseSender().send("Class ID must be provided.");
            return;
        }

        int classId = Integer.parseInt(classIdString);
        final String[] finalColumns = columns; // Final copy of columns array

        // No need to receive a full string here since you're fetching data, not posting it
        try {
            JsonArray jsonArrayResult = GenericQueries.select(connection, "class", finalColumns, "class_id = ?", classId);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(jsonArrayResult.toString());
        } catch (SQLException e) {
            String errorMessage = "SQL Error occurred: " + e.getMessage();
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
                        JsonArray jsonArrayResult = GenericQueries.select(connection, "class", finalColumns);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange.getResponseSender().send(jsonArrayResult.toString());
                    } catch (SQLException e) {
                        String errorMessage = "SQL Error occurred: " + e.getMessage();
                        System.out.println(errorMessage);
                        exchange.getResponseSender().send(errorMessage);
                    }
                });
    }


    //insert
    public static void createClass(Connection connection, HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
            Gson gson = new Gson();
            JsonObject classData = gson.fromJson(requestBody, JsonObject.class);

            if (classData == null || !classData.has("class_name") || classData.get("class_name").getAsString().trim().isEmpty()) {
                String errorMessage = "Class name is missing.";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
                return;
            }

            String insertionResult = GenericQueries.insertData(connection, "class", classData);
            System.out.println(insertionResult);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(insertionResult);
        });
    }

    //update class
    public static void updateClass(Connection connection, HttpServerExchange exchange) {
        // Extracting the class ID from the URL path using PathTemplateMatch
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        String classIdString = pathMatch.getParameters().get("id");

        if (classIdString != null && !classIdString.trim().isEmpty()) {
            try {
                int classId = Integer.parseInt(classIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();
                    JsonObject classData = gson.fromJson(requestBody, JsonObject.class);

                    if (classData == null || !classData.has("class_name") || classData.get("class_name").getAsString().trim().isEmpty()) {
                        String errorMessage = "Class name is missing.";
                        System.out.println(errorMessage);
                        exchange.getResponseSender().send(errorMessage);
                        return;
                    }

                    String whereClause = "class_id = ?";

                    String result = GenericQueries.update(connection, "class", classData, whereClause, classId);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(result);

                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid class ID: " + classIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } else {
            // Handle the case where the class ID is missing or empty
            String errorMessage = "Class ID is missing or empty in the request URL.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }
}




