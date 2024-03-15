package ke.co.skyworld.handlers.subjects;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.InsertQuery;
import ke.co.skyworld.utils.Responses;

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
                    Responses.Message(exchange, 400, errorMessage);
                    return;
                }

                String insertMessage = InsertQuery.insertData(connection, "subject", subjectData);
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

                connection.close();
            }
        }
    }
}