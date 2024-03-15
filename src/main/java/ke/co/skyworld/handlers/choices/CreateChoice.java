package ke.co.skyworld.handlers.choices;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.InsertQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;

public class CreateChoice implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                Gson gson = new Gson();
                JsonObject choiceData = gson.fromJson(requestBody, JsonObject.class);

                if (!choiceData.has("option_label") || choiceData.get("option_label").isJsonNull()) {
                    Responses.Message(exchange, 400, "Option label is required");
                    return;
                }

                if (!choiceData.has("option_value") ||  choiceData.get("option_value").getAsString().trim().isEmpty()) {
                    Responses.Message(exchange, 400, "Option value is required");
                    return;
                }

                String insertMessage = InsertQuery.insertData(connection, "choices", choiceData);
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