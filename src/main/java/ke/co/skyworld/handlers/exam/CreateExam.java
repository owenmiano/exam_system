package ke.co.skyworld.handlers.exam;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.InsertQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;

public class CreateExam implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                Gson gson = new Gson();
                JsonObject examData = gson.fromJson(requestBody, JsonObject.class);

                if (examData == null || !examData.has("exam_name") || examData.get("exam_name").getAsString().trim().isEmpty()) {
                    String errorMessage = "Exam name is missing.";
                    Responses.Message(exchange, 400, errorMessage);
                    return;
                }

                String insertMessage = InsertQuery.insertData(connection, "exam", examData);
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