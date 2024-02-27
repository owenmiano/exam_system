package ke.co.skyworld.handlers.exam;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class CreateExam implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                Gson gson = new Gson();
                JsonObject examData = gson.fromJson(requestBody, JsonObject.class);

                if (examData == null || !examData.has("exam_name") || examData.get("exam_name").getAsString().trim().isEmpty()) {
                    String errorMessage = "Exam name is missing.";
                    System.out.println(errorMessage);
                    exchange.getResponseSender().send(errorMessage);
                    return;
                }

                String insertionResult = GenericQueries.insertData(connection, "exam", examData);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange1.getResponseSender().send(insertionResult);
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