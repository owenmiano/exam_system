package ke.co.skyworld.handlers.questions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.UpdateQuery;
import ke.co.skyworld.utils.Response;

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

                        String updateMessage = UpdateQuery.update(connection, "questions", questionData, whereClause, questionId);
                        if (updateMessage.startsWith("Error")) {
                            Response.Message(exchange, 500, updateMessage);
                        } else {
                            Response.Message(exchange, 200, updateMessage);
                        }
                    });

            } else {
                // Handle the case where the "id" parameter is missing
                String errorMessage = "Question ID is missing in the request URL.";
                Response.Message(exchange, 400, errorMessage);
            }
        }catch (Exception e){
            Response.Message(exchange, 500, e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
