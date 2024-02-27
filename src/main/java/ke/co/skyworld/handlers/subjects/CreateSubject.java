package ke.co.skyworld.handlers.subjects;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class CreateSubject implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                Gson gson = new Gson();
                JsonObject subjectData = gson.fromJson(requestBody, JsonObject.class);

                if (subjectData == null || !subjectData.has("subject_name") || subjectData.get("subject_name").getAsString().trim().isEmpty()) {
                    String errorMessage = "Subject name is missing.";
                    exchange.setStatusCode(404);
                    exchange.getResponseSender().send(errorMessage);
                    return;
                }

                String insertionResult = GenericQueries.insertData(connection, "subject", subjectData);
                System.out.println(insertionResult);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange1.getResponseSender().send(insertionResult);
            });
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