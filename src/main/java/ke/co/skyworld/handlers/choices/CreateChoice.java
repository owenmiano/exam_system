package ke.co.skyworld.handlers.choices;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

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
                    exchange.setStatusCode(400);
                    exchange.getResponseSender().send("Option label is required");
                    return;
                }

                if (!choiceData.has("option_value") ||  choiceData.get("option_value").getAsString().trim().isEmpty()) {
                    exchange.setStatusCode(400);
                    exchange.getResponseSender().send("Option value is required");
                    return;
                }


                String insertionResult = GenericQueries.insertData(connection, "choices", choiceData);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange1.getResponseSender().send(insertionResult);
            });
        }catch (Exception e){
            e.printStackTrace();
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("Error: "+e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}