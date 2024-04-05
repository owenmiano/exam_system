package ke.co.skyworld.handlers.authentication;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.queryBuilder.UpdateQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;


public class ChangePassword implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                try {
                    Gson gson = new Gson();
                    JsonObject requestData = gson.fromJson(requestBody, JsonObject.class);

                    // Validate request parameters
//                    if (!requestData.has("currentPassword") || requestData.get("currentPassword").getAsString().trim().isEmpty() ||
//                            !requestData.has("newPassword") || requestData.get("newPassword").getAsString().trim().isEmpty() ||
//                            !requestData.has("confirmNewPassword") || requestData.get("confirmNewPassword").getAsString().trim().isEmpty()) {
//                        String errorMessage = "All fields are required.";
//                        Responses.Message(exchange, 400, errorMessage);
//                        return;
//                    }

                    String currentPassword = requestData.get("currentPassword").getAsString();
                    String newPassword = requestData.get("newPassword").getAsString();

                    // Check if new password matches confirm new password
//                    if (!newPassword.equals(confirmNewPassword)) {
//                        String errorMessage = "New password and confirm new password do not match.";
//                        Responses.Message(exchange, 400, errorMessage);
//                        return;
//                    }

                    // Check if current password matches the one in the database
                    String[] columns = {"password","username"};
                    String whereClause = "password = ?";
                    Object[] params = {currentPassword};

                    JsonArray jsonArrayResult = SelectQuery.select(connection, "auth", columns, whereClause, params);

                    if (!jsonArrayResult.isEmpty()) {
                        JsonObject userData = jsonArrayResult.get(0).getAsJsonObject();
                        String userName = userData.get("username").getAsString();
                            // Update the password in the database
                            String where = "username = ?";
                            Object[]  updateParams = {userName};
                            requestData.remove("currentPassword");
                            JsonObject authData = new JsonObject();
                            authData.addProperty("password", newPassword);
                            String updateMessage = UpdateQuery.update(connection, "auth", authData, where, updateParams);

                            if (updateMessage.startsWith("Error")) {
                                Responses.Message(exchange, 500, updateMessage);
                            } else {
                                Responses.Message(exchange, 200, "Password changed successfully.");
                            }
                    } else {
                        // No matching record found in the database
                        String errorMessage = "Invalid current password.";
                        Responses.Message(exchange, 400, errorMessage);
                    }
                } catch (Exception e) {
                    // Handle any unexpected errors
                    e.printStackTrace();
                    Responses.Message(exchange, 500, "Internal Server Error");
                }
            });
        } finally {
            // Release the database connection
            ConnectDB.shutdown();
        }
    }
}
