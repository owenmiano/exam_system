package org.example;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;

import java.sql.*;
import java.util.*;

public class ClassController {

    public static void findClass(Connection connection, HttpServerExchange exchange) {
        // Extracting the class ID from the URL path
        Deque<String> classIdDeque = exchange.getQueryParameters().get("id");
        if (classIdDeque != null && !classIdDeque.isEmpty()) {
            String classIdString = classIdDeque.getFirst();

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
                int classId = Integer.parseInt(classIdString);
                final String[] finalColumns = columns; // Final copy of columns array

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();

                    String whereClause = "class_id = ?";

                    try {
                        JsonArray jsonArrayResult = GenericQueries.select(connection, "class", finalColumns, whereClause, classId);
                        exchange1.getResponseSender().send(jsonArrayResult.toString());
                    } catch (SQLException e) {
                        String errorMessage = "SQL Error occurred: " + e.getMessage();
                        System.out.println(errorMessage);
                        exchange1.getResponseSender().send(errorMessage);
                    }
                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid class ID: " + classIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } else {
            // Handle the case where the "id" parameter is missing
            String errorMessage = "Class ID is missing in the request URL.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
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
            exchange1.getResponseSender().send(insertionResult);
        });
    }

    //update class
    public static void updateClass(Connection connection, HttpServerExchange exchange) {
        // Extracting the class ID from the URL path
        Deque<String> classIdDeque = exchange.getQueryParameters().get("id");
        if (classIdDeque != null && !classIdDeque.isEmpty()) {
            String classIdString = classIdDeque.getFirst();

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
                    exchange1.getResponseSender().send(result);

                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid class ID: " + classIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } else {
            // Handle the case where the "id" parameter is missing
            String errorMessage = "Class ID is missing in the request URL.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }
}




