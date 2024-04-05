package ke.co.skyworld.handlers.questions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.InsertQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;

public class CreateQuestion implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                Gson gson = new Gson();
                JsonObject questionData = gson.fromJson(requestBody, JsonObject.class);

                if (!questionData.has("exam_subject_id") || questionData.get("exam_subject_id").getAsInt() == 0) {
                    String errorMessage = "Exam schedule is missing.";
                    Responses.Message(exchange, 400, errorMessage);
                    return;
                }

                if (!questionData.has("question_no") || questionData.get("question_no").isJsonNull()) {
                    Responses.Message(exchange, 400, "Question number is missing.");
                    return;
                }

                if (!questionData.has("description") || questionData.get("description").isJsonNull() || questionData.get("description").getAsString().trim().isEmpty()) {
                    Responses.Message(exchange, 400, "Description cannot be empty.");
                    return;
                }

                if (!questionData.has("marks") || questionData.get("marks").isJsonNull()) {
                    Responses.Message(exchange, 400, "Marks is missing.");
                    return;
                }

                String insertMessage = InsertQuery.insertData(connection, "questions", questionData);
                if (insertMessage.startsWith("Error")) {
                    Responses.Message(exchange, 500, insertMessage);
                } else {
                    Responses.Message(exchange, 200, insertMessage);
                }
            });
        }catch (Exception e){
            Responses.Message(exchange, 500,  e.getMessage());
        }finally {
            if (connection != null) {
                ConnectDB.shutdown();
            }
        }
    }
}