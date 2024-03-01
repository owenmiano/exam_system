package ke.co.skyworld.handlers.answers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.InsertQuery;
import ke.co.skyworld.utils.Response;

import java.sql.Connection;

public class CreateAnswer implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                try {
                    Gson gson = new Gson();
                    JsonObject answerData = gson.fromJson(requestBody, JsonObject.class);

                    if (!answerData.has("questions_id") || answerData.get("questions_id").getAsInt() == 0) {
                        String errorMessage = "Question ID is missing.";
                        Response.Message(exchange, 400, errorMessage);
                        return;
                    }
                    if (!answerData.has("choices_id") || answerData.get("choices_id").getAsInt() == 0) {
                        String errorMessage = "Choices ID is missing.";
                        Response.Message(exchange, 400, errorMessage);
                        return;
                    }
                    if (!answerData.has("pupils_id") || answerData.get("pupils_id").getAsInt() == 0) {
                        String errorMessage = "Pupil ID is missing.";
                        Response.Message(exchange, 400, errorMessage);
                        return;
                    }

                    String insertMessage = InsertQuery.insertData(connection, "answers", answerData);
                    if (insertMessage.startsWith("Error")) {
                        Response.Message(exchange, 500, insertMessage);
                    } else {
                        Response.Message(exchange, 200, insertMessage);
                    }
                } catch (Exception e) {
                    Response.Message(exchange, 500,  e.getMessage());
                }
            });
        } finally {
            if (connection != null) {
                connection.close(); // Close the connection
            }
        }
    }
}

