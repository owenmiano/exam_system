package ke.co.skyworld.handlers.authentication;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import ke.co.skyworld.KeyManager;
import ke.co.skyworld.Model.ConfigReader;
import ke.co.skyworld.accessToken.GenerateToken;
import ke.co.skyworld.accessToken.User;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.queryBuilder.UpdateQuery;
import ke.co.skyworld.utils.Responses;
import java.sql.Connection;

import static ke.co.skyworld.utils.PasswordEncryption.hashPassword;

public class LoginUser implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                try {
                    Gson gson = new Gson();
                    JsonObject requestData = gson.fromJson(requestBody, JsonObject.class);
                    if (!requestData.has("username") || requestData.get("username").getAsString().trim().isEmpty() ||
                            !requestData.has("password") || requestData.get("password").getAsString().trim().isEmpty()) {
                        String errorMessage = "Username and password fields are required.";
                        Responses.Message(exchange, 400, errorMessage);
                        return;
                    }

                    String username = requestData.get("username").getAsString();
                    String password = requestData.get("password").getAsString();

                    String[] columns = {"password", "login_attempts", "account_status","role"};
                    // Construct WHERE clause to find the user by username
                    String whereClause = "username = ?";
                    Object[] params = {username};
                    JsonArray jsonArrayResult = SelectQuery.select(connection, "auth",columns,whereClause,params);
                    if (!jsonArrayResult.isEmpty()) {
                        JsonObject userData = jsonArrayResult.get(0).getAsJsonObject();
                        String storedPassword = userData.get("password").getAsString();
                        int loginAttempts = userData.get("login_attempts").getAsInt();
                        String accountStatus = userData.get("account_status").getAsString();
                        String userRole = userData.get("role").getAsString();

                        // Check if the account is locked
                        if (accountStatus.equalsIgnoreCase("locked")) {
                            String errorMessage = "Your Account is locked.";
                            Responses.Message(exchange, 403, errorMessage);
                            return;
                        }

                        // Check if the provided password matches the stored password
                        if (!storedPassword.equals(password)) {
                            // Increment login attempts
                            loginAttempts++;
                            if (loginAttempts >= 3) {
                                // Update account status to locked
                                updateAccountStatus(connection, username);
                                String errorMessage = "Maximum login attempts reached. Account locked.";
                                Responses.Message(exchange, 403, errorMessage);
                            } else {
                                // Update login attempts count
                                updateLoginAttempts(connection, username, loginAttempts);
                                String errorMessage = "Incorrect password. Please try again.";
                                Responses.Message(exchange, 401, errorMessage);
                            }
                            return;
                        }

                        // Reset login attempts count upon successful login
                        exchange.getResponseHeaders().put(new HttpString("Content-type"), "application/json")
                                .put(new HttpString("Access-Control-Allow-Origin"), "*")
                                .put(new HttpString("Access-Control-Allow-Headers"), "*");
                        updateLoginAttempts(connection, username, 0);
                        String accessToken=GenerateToken.accessToken(username,userRole);
                        updateAccessToken(connection, username, accessToken);
                        User user = new User();
                        String decryptedToken = ConfigReader.decrypt(accessToken, KeyManager.AES_ENCRYPT_KEY);
                        String[] fields = decryptedToken.split("_");
                        user.setRole((fields[1]));
                        user.setExpirationTime(Long.parseLong(fields[3]));
                        JsonObject responseJson = new JsonObject();

                        // Set the success message
                        String successMessage = "Login successful";
                        responseJson.addProperty("message", successMessage);

                        // Create a JsonObject for user info
                        JsonObject userInfoJson = new JsonObject();

                        userInfoJson.addProperty("username", username);
                        userInfoJson.addProperty("token", accessToken);
                        userInfoJson.addProperty("role", user.getRole());
                        userInfoJson.addProperty("expirationTime", user.getExpirationTime());
                        responseJson.add("userInfo", userInfoJson);
                        // Send the response
                        exchange.getResponseSender().send(responseJson.toString());
                    } else {
                        // User not found
                        String errorMessage = "Invalid username or password.";
                        Responses.Message(exchange, 401, errorMessage);
                    }
                } catch (Exception e) {
                    Responses.Message(exchange, 500,  e.getMessage());
                }
            });
        } finally {
            ConnectDB.shutdown();
        }
    }

    private void updateLoginAttempts(Connection connection, String username, int loginAttempts) {
        JsonObject authData = new JsonObject();
        authData.addProperty("login_attempts", loginAttempts);
        String whereClause = "username = ?";
        Object[] params = {username};
        UpdateQuery.update(connection, "auth",authData,whereClause,params);
    }

    private void updateAccountStatus(Connection connection, String username) {
        JsonObject authData = new JsonObject();
        authData.addProperty("account_status", "locked");
        String whereClause = "username = ?";
        Object[] params = {username};
        UpdateQuery.update(connection, "auth",authData,whereClause,params);
    }

    private void updateAccessToken(Connection connection, String username, String token) {
        JsonObject authData = new JsonObject();
        authData.addProperty("access_token", token);
        String whereClause = "username = ?";
        Object[] params = {username};
        UpdateQuery.update(connection, "auth",authData,whereClause,params);
    }
}
