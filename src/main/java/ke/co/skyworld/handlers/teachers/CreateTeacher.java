package ke.co.skyworld.handlers.teachers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.InsertQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;
import java.util.regex.Pattern;

import static ke.co.skyworld.utils.PasswordEncryption.hashPassword;

public class CreateTeacher implements HttpHandler {
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

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
    try{
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
            try {
                Gson gson = new Gson();
                JsonObject requestData = gson.fromJson(requestBody, JsonObject.class);

                // Extract username and password for insertion into the "auth" table
                String username = requestData.get("username").getAsString();
                String password = requestData.get("password").getAsString();

                // Hash the password using SHA-256
//                String hashedPassword = hashPassword(password);


                // Create JsonObject for "auth" table insertion
                JsonObject authData = new JsonObject();
                authData.addProperty("username", username);
                authData.addProperty("password", password);
                authData.addProperty("role", "teacher");

                // Remove username and password from the original data for insertion into the "teachers" table
                requestData.remove("username");
                requestData.remove("password");

                if (!requestData.has("teacher_name")) {
                    String errorMessage = "Teacher name is missing.";
                    Responses.Message(exchange, 400,  errorMessage);
                    return;
                }

                // Validate other fields
                String emailAddress = requestData.has("email") ? requestData.get("email").getAsString() : "";
                String idNumber = requestData.has("id_number") ? requestData.get("id_number").getAsString() : "";
                String phone = requestData.has("phone") ? requestData.get("phone").getAsString() : "";

                if (!isValidEmail(emailAddress)) {
                    String errorMessage = "Invalid email address.";
                    Responses.Message(exchange, 400,  errorMessage);
                    return;
                }

                if (!isValidIDNumber(idNumber)) {
                    String errorMessage = "Invalid ID number.";
                    Responses.Message(exchange, 400,  errorMessage);
                    return;
                }

                if (!isValidPhoneNumber(phone)) {
                    String errorMessage = "Invalid phone number.";
                    Responses.Message(exchange, 400,  errorMessage);
                    return;
                }
                connection.setAutoCommit(false);
                // Insert data into the "auth" table
                String insertAuthMessage = InsertQuery.insertData(connection, "auth", authData);
                String[] parts = insertAuthMessage.split(":");
                String insertAuthResult = parts[0];
                int authId = Integer.parseInt(parts[1]);

                // Check if insertion into the "auth" table was successful
                if (insertAuthResult.startsWith("Error")) {
                    connection.rollback();
                    Responses.Message(exchange, 500, insertAuthResult);
                    return;
                }
                requestData.addProperty("auth_id", authId);

                // Insert data into the "teachers" table
                String insertTeacherMessage = InsertQuery.insertData(connection, "teachers", requestData);

                // Check if insertion into the "teachers" table was successful
                if (insertTeacherMessage.startsWith("Error")) {
                    connection.rollback();
                    Responses.Message(exchange, 500, insertTeacherMessage);
                    return;
                }

                // Commit transaction
                connection.commit();
                Responses.Message(exchange, 200, "Data inserted successfully");
            } catch (Exception e) {
                Responses.Message(exchange, 500,  e.getMessage());
            }
        });
    }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}