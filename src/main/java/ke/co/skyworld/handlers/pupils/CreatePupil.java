package ke.co.skyworld.handlers.pupils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.InsertQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;

public class CreatePupil implements HttpHandler {
    private static boolean isValidPhoneNumber(String phoneNumber) {
        String phoneNumberRegex = "^\\d{10}$";
        return phoneNumber != null && phoneNumber.matches(phoneNumberRegex);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                try {
                    Gson gson = new Gson();
                    JsonObject requestData = gson.fromJson(requestBody, JsonObject.class);

                    if (!requestData.has("pupil_name")) {
                        String errorMessage = "Pupil name is missing.";
                        Responses.Message(exchange, 400,  errorMessage);
                        return;
                    }
                    if (!requestData.has("password")) {
                        String errorMessage = "Password is missing.";
                        Responses.Message(exchange, 400,  errorMessage);
                        return;
                    }

                    String username = requestData.get("username").getAsString();
                    String password = requestData.get("password").getAsString();
//                    String hashedPassword = hashPassword(password);

                    // Create JsonObject for "auth" table insertion
                    JsonObject authData = new JsonObject();
                    authData.addProperty("username", username);
                    authData.addProperty("password", password);
                    authData.addProperty("role", "pupil");

                    requestData.remove("username");
                    requestData.remove("password");

                    String phone = requestData.has("guardian_phone") ? requestData.get("guardian_phone").getAsString() : "";

                    if (!isValidPhoneNumber(phone)) {
                        String errorMessage = "Invalid phone number.";
                        Responses.Message(exchange, 400,  errorMessage);
                        return;
                    }
                    connection.setAutoCommit(false);

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
                    String insertPupilMessage = InsertQuery.insertData(connection, "pupils", requestData);

                    // Check if insertion into the "teachers" table was successful
                    if (insertPupilMessage.startsWith("Error")) {
                        connection.rollback();
                        Responses.Message(exchange, 500, insertPupilMessage);
                        return;
                    }

                     // Commit transaction
                    connection.commit();
                    Responses.Message(exchange, 200, "Data inserted successfully");
                }  catch (Exception e) {
                    Responses.Message(exchange, 500,  e.getMessage());
                }
            });
        } finally {
        if (connection != null) {
            connection.setAutoCommit(true);
            ConnectDB.shutdown();
        }
    }
}
}
