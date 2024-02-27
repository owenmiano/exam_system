package ke.co.skyworld.handlers.choices;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class UpdateChoice implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String choiceIdString = pathMatch.getParameters().get("choiceId");
            if (choiceIdString != null && !choiceIdString.isEmpty()) {


                try {
                    int choicesId = Integer.parseInt(choiceIdString);

                    exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                        Gson gson = new Gson();
                        JsonObject questionData = gson.fromJson(requestBody, JsonObject.class);

                        String whereClause = "choices_id = ?";

                        String result = GenericQueries.update(connection, "choices", questionData, whereClause, choicesId);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange1.getResponseSender().send(result);

                    });
                } catch (NumberFormatException e) {
                    String errorMessage = "Invalid choices ID: " + choiceIdString;
                    System.out.println(errorMessage);
                    exchange.getResponseSender().send(errorMessage);
                }
            } else {
                // Handle the case where the "id" parameter is missing
                String errorMessage = "Choices ID is missing in the request URL.";
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