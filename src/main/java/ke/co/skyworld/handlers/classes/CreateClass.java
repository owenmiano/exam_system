package ke.co.skyworld.handlers.classes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class CreateClass implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                try {
                    Gson gson = new Gson();
                    JsonObject classData = gson.fromJson(requestBody, JsonObject.class);

                    if (classData == null || !classData.has("class_name") || classData.get("class_name").getAsString().trim().isEmpty()) {
                        String errorMessage = "Class name is missing.";
                        exchange.setStatusCode(404);
                        exchange.getResponseSender().send(errorMessage);
                        return;
                    }

                    String insertionResult = GenericQueries.insertData(connection, "class", classData);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(insertionResult);
                } catch (Exception e) {
                    exchange.setStatusCode(500);
                    exchange.getResponseSender().send("Error: "+e.getMessage());
                }
            });
        } finally {
            if (connection != null) {

                    connection.close();
            }
        }
    }
}