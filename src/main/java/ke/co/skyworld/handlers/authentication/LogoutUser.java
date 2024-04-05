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
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;


public class LogoutUser implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                try {
                         Gson gson = new Gson();
                    JsonObject requestData = gson.fromJson(requestBody, JsonObject.class);

                    // Validate request parameters
                    if (!requestData.has("username") || requestData.get("username").getAsString().trim().isEmpty() ||
                            !requestData.has("role") || requestData.get("role").getAsString().trim().isEmpty())
                             {
                        Responses.Message(exchange, 400, "Bad request");
                        return;
                    }

                       String username = requestData.get("username").getAsString();
                       String role = requestData.get("role").getAsString();
                       String accessToken= GenerateToken.accessToken(username,role);
                        String where = "username = ?";
                        Object[]  updateParams = {username};
                        JsonObject authData = new JsonObject();
                        authData.addProperty("access_token", accessToken);
                        String updateMessage = UpdateQuery.update(connection, "auth", authData, where, updateParams);

                        if (updateMessage.startsWith("Error")) {
                            Responses.Message(exchange, 500, updateMessage);
                        } else {
                            Responses.Message(exchange, 200, "Logged out successfully");
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
