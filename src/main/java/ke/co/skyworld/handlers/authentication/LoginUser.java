package ke.co.skyworld.handlers.authentication;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.accessToken.GenerateToken;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.queryBuilder.UpdateQuery;
import ke.co.skyworld.utils.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;

public class LoginUser implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                try {
                    Gson gson = new Gson();
                    JsonObject requestData = gson.fromJson(requestBody, JsonObject.class);
                    if (!requestData.has("username") || requestData.get("username").getAsString().trim().isEmpty() ||
                            !requestData.has("password") || requestData.get("password").getAsString().trim().isEmpty()) {
                        String errorMessage = "Username and password fields are required.";
                        Response.Message(exchange, 400, errorMessage);
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
                            Response.Message(exchange, 403, errorMessage);
                            return;
                        }

                        // Check if the provided password matches the stored password
                        if (!BCrypt.checkpw(password, storedPassword)) {
                            // Increment login attempts
                            loginAttempts++;
                            if (loginAttempts >= 3) {
                                // Update account status to locked
                                updateAccountStatus(connection, username);
                                String errorMessage = "Maximum login attempts reached. Account locked.";
                                Response.Message(exchange, 403, errorMessage);
                            } else {
                                // Update login attempts count
                                updateLoginAttempts(connection, username, loginAttempts);
                                String errorMessage = "Incorrect password. Please try again.";
                                Response.Message(exchange, 401, errorMessage);
                            }
                            return;
                        }

                        // Reset login attempts count upon successful login
                        updateLoginAttempts(connection, username, 0);
                        String accessToken=GenerateToken.accessToken(username,userRole);
                        updateAccessToken(connection, username, accessToken);
                        String successMessage = "Login successful.";
                        Response.Message(exchange, 200, successMessage);
                    } else {
                        // User not found
                        String errorMessage = "Invalid username or password.";
                        Response.Message(exchange, 401, errorMessage);
                    }
                } catch (Exception e) {
                    Response.Message(exchange, 500,  e.getMessage());
                }
            });
        } finally {
            if (connection != null) {

                connection.close();
            }
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
