package ke.co.skyworld.handlers.questions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.util.Deque;

public class UpdateQuestion implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            Deque<String> questionIdDeque = exchange.getQueryParameters().get("questionId");
            if (questionIdDeque != null && !questionIdDeque.isEmpty()) {
                String questionIdString = questionIdDeque.getFirst();

                    int questionId = Integer.parseInt(questionIdString);

                    exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                        Gson gson = new Gson();
                        JsonObject questionData = gson.fromJson(requestBody, JsonObject.class);

                        String whereClause = "questions_id = ?";

                        String result = GenericQueries.update(connection, "questions", questionData, whereClause, questionId);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange.getResponseSender().send(result);

                    });

            } else {
                // Handle the case where the "id" parameter is missing
                String errorMessage = "Question ID is missing in the request URL.";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        }catch (Exception e){
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("Error: "+e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
