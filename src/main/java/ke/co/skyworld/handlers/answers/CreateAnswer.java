package ke.co.skyworld.handlers.answers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;
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

                    if (answerData == null || !answerData.has("questions_id") || answerData.get("questions_id").getAsString().trim().isEmpty()) {
                        String errorMessage = "Question ID is missing.";
                        exchange.setStatusCode(404); // Not Found
                        exchange.getResponseSender().send(errorMessage);
                        return;
                    }
                    if (answerData == null || !answerData.has("choices_id") || answerData.get("choices_id").getAsString().trim().isEmpty()) {
                        String errorMessage = "Choices ID is missing.";
                        exchange.setStatusCode(404);
                        exchange.getResponseSender().send(errorMessage);
                        return;
                    }
                    if (answerData == null || !answerData.has("pupils_id") || answerData.get("pupils_id").getAsString().trim().isEmpty()) {
                        String errorMessage = "Pupil ID is missing.";
                        exchange.setStatusCode(404);
                        exchange.getResponseSender().send(errorMessage);
                        return;
                    }

                    String insertionResult = GenericQueries.insertData(connection, "answers", answerData);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(insertionResult);
                } catch (Exception e) {
                    exchange.setStatusCode(500);
                    exchange.getResponseSender().send("Error: "+e.getMessage());
                }
            });
        } finally {
            if (connection != null) {
                connection.close(); // Close the connection
            }
        }
    }
}

