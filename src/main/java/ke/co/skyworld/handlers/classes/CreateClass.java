package ke.co.skyworld.handlers.classes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.InsertQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;

public class CreateClass implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();

        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                try {
                    Gson gson = new Gson();
                    JsonObject classData = gson.fromJson(requestBody, JsonObject.class);

                    if (!classData.has("class_name") || classData.get("class_name").getAsString().trim().isEmpty()) {
                        String errorMessage = "Class name field is required.";
                        Responses.Message(exchange, 400, errorMessage);
                        return;
                    }

                    String insertMessage = InsertQuery.insertData(connection, "class", classData);
                    if (insertMessage.startsWith("Error")) {
                        Responses.Message(exchange, 500, insertMessage);
                    } else {
                        Responses.Message(exchange, 200, insertMessage);
                    }
                } catch (Exception e) {
                    Responses.Message(exchange, 500,  e.getMessage());
                }
            });
        } finally {
            if (connection != null) {
                ConnectDB.shutdown();
            }
        }
    }
}