package ke.co.skyworld.handlers.choices;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.UpdateQuery;
import ke.co.skyworld.utils.Response;

import java.sql.Connection;

public class UpdateChoice implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String choiceIdString = pathMatch.getParameters().get("choiceId");
            if (choiceIdString != null && !choiceIdString.isEmpty()) {

                    int choicesId = Integer.parseInt(choiceIdString);

                    exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                        Gson gson = new Gson();
                        JsonObject choicesData = gson.fromJson(requestBody, JsonObject.class);

                        String whereClause = "choices_id = ?";

                        String updateMessage = UpdateQuery.update(connection, "choices", choicesData, whereClause, choicesId);
                        if (updateMessage.startsWith("Error")) {
                            Response.Message(exchange, 500, updateMessage);
                        } else {
                            Response.Message(exchange, 200, updateMessage);
                        }

                    });

            } else {
                // Handle the case where the "id" parameter is missing
                String errorMessage = "Choices ID is missing ";
                Response.Message(exchange, 400, errorMessage);
            }
        }catch (Exception e){
            Response.Message(exchange, 400, e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}