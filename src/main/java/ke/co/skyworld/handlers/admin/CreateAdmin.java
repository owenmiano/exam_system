package ke.co.skyworld.handlers.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.InsertQuery;
import ke.co.skyworld.utils.Responses;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;

public class CreateAdmin implements HttpHandler {


    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection  connection = ConnectDB.initializeDatabase();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                try {
                    Gson gson = new Gson();
                    JsonObject requestData = gson.fromJson(requestBody, JsonObject.class);

                    if (!requestData.has("password")) {
                        String errorMessage = "Password is missing.";
                        Responses.Message(exchange, 400,  errorMessage);
                        return;
                    }
                    if (!requestData.has("username")) {
                        String errorMessage = "Username is missing.";
                        Responses.Message(exchange, 400,  errorMessage);
                        return;
                    }


                    String username = requestData.get("username").getAsString();
                    String plainPassword = requestData.get("password").getAsString();
                    String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

                    // Create JsonObject for "auth" table insertion
                    JsonObject authData = new JsonObject();
                    authData.addProperty("username", username);
                    authData.addProperty("password", hashedPassword);
                    authData.addProperty("role", "admin");


                    String insertAuthMessage = InsertQuery.insertData(connection, "auth", authData);
                    String[] parts = insertAuthMessage.split(":");
                    String insertAuthResult = parts[0];

                    // Check if insertion into the "auth" table was successful
                    if (insertAuthResult.startsWith("Error")) {
                        Responses.Message(exchange, 500, insertAuthResult);
                        return;
                    }

                    Responses.Message(exchange, 200, "Data inserted successfully");
                }  catch (Exception e) {
                    Responses.Message(exchange, 500,  e.getMessage());
                }
            });
        } finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
