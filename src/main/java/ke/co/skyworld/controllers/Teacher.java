package ke.co.skyworld.controllers;

import ke.co.skyworld.queryBuilder.GenericQueries;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

public class Teacher {
    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }

    private static boolean isValidIDNumber(String idNumber) {
        String idNumberRegex = "^\\d{8}$";
        return idNumber != null && idNumber.matches(idNumberRegex);
    }
    private static boolean isValidPhoneNumber(String phoneNumber) {
        String phoneNumberRegex = "^\\d{10}$";
        return phoneNumber != null && phoneNumber.matches(phoneNumberRegex);
    }


    public static void findTeacherById(Connection connection, HttpServerExchange exchange) {
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        String teacherIdString = pathMatch.getParameters().get("id");
            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
            String[] columns = null;
            if (columnsDeque != null && !columnsDeque.isEmpty()) {
                String columnsString = columnsDeque.getFirst();
                columns = columnsString.split(",");
            } else {
                // If no columns parameter provided, select all columns
                columns = new String[]{"*"};
            }

                int teacherId = Integer.parseInt(teacherIdString);
                final String[] finalColumns = columns; // Final copy of columns array

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {

                    String whereClause = "teacher_id = ?";

                    try {
                        JsonArray jsonArrayResult = GenericQueries.select(connection, "teachers", finalColumns, whereClause, teacherId);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange1.getResponseSender().send(jsonArrayResult.toString());
                    } catch (SQLException e) {
                        String errorMessage = "SQL Error occurred: " + e.getMessage();
                        System.out.println(errorMessage);
                        exchange1.getResponseSender().send(errorMessage);
                    }
                });
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
                JsonArray jsonArrayResult = GenericQueries.select(connection, "teachers", finalColumns);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange1.getResponseSender().send(jsonArrayResult.toString());
            } catch (SQLException e) {
                String errorMessage = "SQL Error occurred: " + e.getMessage();
                System.out.println(errorMessage);
                exchange1.getResponseSender().send(errorMessage);
            }
        });
    }

    public static void createTeacher(Connection connection, HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
            try {
                Gson gson = new Gson();
                JsonObject teacherData = gson.fromJson(requestBody, JsonObject.class);

                if (teacherData == null) {
                    String errorMessage = "Teacher data is missing or incomplete.";
                    System.out.println(errorMessage);
                    exchange1.getResponseSender().send(errorMessage);
                    return;
                }

                if (!teacherData.has("password")) {
                    String errorMessage = "Password is missing.";
                    System.out.println(errorMessage);
                    exchange1.getResponseSender().send(errorMessage);
                    return;
                }

                String plainPassword = teacherData.get("password").getAsString();
                String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                teacherData.addProperty("password", hashedPassword);

                String emailAddress = teacherData.has("email") ? teacherData.get("email").getAsString() : "";
                String idNumber = teacherData.has("id_number") ? teacherData.get("id_number").getAsString() : "";
                String phone = teacherData.has("phone") ? teacherData.get("phone").getAsString() : "";

                if (!isValidEmail(emailAddress)) {
                    String errorMessage = "Invalid email address.";
                    System.out.println(errorMessage);
                    exchange1.getResponseSender().send(errorMessage);
                    return;
                }

                if (!isValidIDNumber(idNumber)) {
                    String errorMessage = "Invalid ID number.";
                    System.out.println(errorMessage);
                    exchange1.getResponseSender().send(errorMessage);
                    return;
                }

                if (!isValidPhoneNumber(phone)) {
                    String errorMessage = "Invalid phone number.";
                    System.out.println(errorMessage);
                    exchange1.getResponseSender().send(errorMessage);
                    return;
                }
                String insertionResult = GenericQueries.insertData(connection, "teachers", teacherData);

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


    public static void updateTeacher(Connection connection, HttpServerExchange exchange) {
        // Extracting the teacher ID from the URL path using PathTemplateMatch
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        String teacherIdString = pathMatch.getParameters().get("id");

        if (teacherIdString != null && !teacherIdString.trim().isEmpty()) {
            try {
                int teacherId = Integer.parseInt(teacherIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();
                    JsonObject teacherData = gson.fromJson(requestBody, JsonObject.class);

                    String whereClause = "teacher_id = ?";

                    String result = GenericQueries.update(connection, "teachers", teacherData, whereClause, teacherId);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(result);
                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid Teacher ID: " + teacherIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } else {
            // Handle the case where the teacher ID is missing or empty
            String errorMessage = "Teacher ID is missing or empty in the request URL.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }
}
