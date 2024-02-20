package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.*;

import com.google.gson.JsonSyntaxException;
import io.undertow.server.HttpServerExchange;
import org.mindrot.jbcrypt.BCrypt;

public class PupilController {
    private static boolean isValidPhoneNumber(String phoneNumber) {
        String phoneNumberRegex = "^\\d{10}$";
        return phoneNumber != null && phoneNumber.matches(phoneNumberRegex);
    }
    public static void findPupil(Connection connection, HttpServerExchange exchange) {
        Deque<String> pupilIdDeque = exchange.getQueryParameters().get("id");
        if (pupilIdDeque != null && !pupilIdDeque.isEmpty()) {
            String pupilIdString = pupilIdDeque.getFirst();

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
                int pupilId = Integer.parseInt(pupilIdString);
                final String[] finalColumns = columns; // Final copy of columns array

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();

                    String whereClause = "pupils_id = ?";

                    try {
                        JsonArray jsonArrayResult = GenericQueries.select(connection, "pupils", finalColumns, whereClause, pupilId);
                        exchange1.getResponseSender().send(jsonArrayResult.toString());
                    } catch (SQLException e) {
                        String errorMessage = "SQL Error occurred: " + e.getMessage();
                        System.out.println(errorMessage);
                        exchange1.getResponseSender().send(errorMessage);
                    }
                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid pupil ID: " + pupilIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } else {
            // Handle the case where the "id" parameter is missing
            String errorMessage = "Pupil ID is missing in the request URL.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }


    //insert
    public static void createPupil(Connection connection, HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
            try {
                Gson gson = new Gson();
                JsonObject pupilData = gson.fromJson(requestBody, JsonObject.class);

                if (!pupilData.has("password")) {
                    String errorMessage = "Password is missing.";
                    System.out.println(errorMessage);
                    exchange1.getResponseSender().send(errorMessage);
                    return;
                }

                String plainPassword = pupilData.get("password").getAsString();
                String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                pupilData.addProperty("password", hashedPassword);

                String phone = pupilData.has("guardian_phone") ? pupilData.get("guardian_phone").getAsString() : "";

                if (!isValidPhoneNumber(phone)) {
                    String errorMessage = "Invalid phone number.";
                    System.out.println(errorMessage);
                    exchange1.getResponseSender().send(errorMessage);
                    return;
                }
                String insertionResult = GenericQueries.insertData(connection, "pupils", pupilData);

                System.out.println(insertionResult);
                exchange1.getResponseSender().send(insertionResult);
            } catch (JsonSyntaxException e) {
                String errorMessage = "Error parsing JSON data: " + e.getMessage();
                System.out.println(errorMessage);
                exchange1.getResponseSender().send(errorMessage);
            } catch (Exception e) {
                String errorMessage = "Error processing request: " + e.getMessage();
                System.out.println(errorMessage);
                exchange1.getResponseSender().send(errorMessage);
            }
        });
    }

    public static void updatePupil(Connection connection, HttpServerExchange exchange) {
        // Extracting the class ID from the URL path
        Deque<String> pupilIdDeque = exchange.getQueryParameters().get("id");
        if (pupilIdDeque != null && !pupilIdDeque.isEmpty()) {
            String pupilIdString = pupilIdDeque.getFirst();

            try {
                int pupilId = Integer.parseInt(pupilIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();
                    JsonObject pupilData = gson.fromJson(requestBody, JsonObject.class);


                    String whereClause = "pupils_id = ?";

                    String result = GenericQueries.update(connection, "pupils", pupilData, whereClause, pupilId);
                    exchange1.getResponseSender().send(result);

                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid Pupil ID: " + pupilIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } else {
            // Handle the case where the "id" parameter is missing
            String errorMessage = "Teacher ID is missing in the request URL.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }

}
