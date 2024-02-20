package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.*;

import com.google.gson.JsonSyntaxException;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.mindrot.jbcrypt.BCrypt;

public class PupilController {
    private static boolean isValidPhoneNumber(String phoneNumber) {
        String phoneNumberRegex = "^\\d{10}$";
        return phoneNumber != null && phoneNumber.matches(phoneNumberRegex);
    }
    public static void findPupilById(Connection connection, HttpServerExchange exchange,String pupilIdString) {

        try {
            int pupilId = Integer.parseInt(pupilIdString);
            String[] columns = {
                    "p.pupil_name",
                    "p.date_of_birth",
                    "p.guardian_name",
                    "p.guardian_phone",
                    "p.username",
                    "p.reg_no",
                    "cl.class_name"
            };

            String table = "pupils p " +
                    "JOIN class cl ON p.class_id = cl.class_id " ;

            String whereClause = "pupils_id = ?";

            Object[] values = new Object[]{pupilId};

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

    public static void findAll(Connection connection, HttpServerExchange exchange) {
        try {
            String[] columns = {
                    "p.pupil_name",
                    "p.date_of_birth",
                    "p.guardian_name",
                    "p.guardian_phone",
                    "p.username",
                    "p.reg_no",
                    "cl.class_name"
            };

            String table = "pupils p " +
                    "JOIN class cl ON p.class_id = cl.class_id " ;

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
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
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
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
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
