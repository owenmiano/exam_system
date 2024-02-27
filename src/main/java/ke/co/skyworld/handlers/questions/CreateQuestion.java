package ke.co.skyworld.handlers.questions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class CreateQuestion implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                Gson gson = new Gson();
                JsonObject questionData = gson.fromJson(requestBody, JsonObject.class);

                if (!questionData.has("question_no") || questionData.get("question_no").isJsonNull()) {
                    System.out.println("Question number is missing.");
                    return;
                }

                if (!questionData.has("description") || questionData.get("description").isJsonNull() || questionData.get("description").getAsString().trim().isEmpty()) {
                    System.out.println("Description cannot be empty.");
                    return;
                }

                if (!questionData.has("marks") || questionData.get("marks").isJsonNull()) {
                    System.out.println("Marks is missing.");
                    return;
                }

                String insertionResult = GenericQueries.insertData(connection, "questions", questionData);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                System.out.println(insertionResult);
                exchange.getResponseSender().send(insertionResult);
            });
        }catch (Exception e){
            throw e;
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}