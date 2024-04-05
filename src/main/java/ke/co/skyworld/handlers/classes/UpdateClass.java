package ke.co.skyworld.handlers.classes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.UpdateQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;

public class UpdateClass implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();

        try {
            // Extracting the class ID from the URL path using PathTemplateMatch
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String classIdString = pathMatch.getParameters().get("classId");

            if (classIdString != null && !classIdString.trim().isEmpty()) {

                    int classId = Integer.parseInt(classIdString);

                    exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                        Gson gson = new Gson();
                        JsonObject classData = gson.fromJson(requestBody, JsonObject.class);

                        if (classData == null || !classData.has("class_name") || classData.get("class_name").getAsString().trim().isEmpty()) {
                            String errorMessage = "Class name field is required.";
                            Responses.Message(exchange, 400, errorMessage);
                            return;
                        }

                        String whereClause = "class_id = ?";

                        String updateMessage = UpdateQuery.update(connection, "class", classData, whereClause, classId);
                        if (updateMessage.startsWith("Error")) {
                            Responses.Message(exchange, 500, updateMessage);
                        } else {
                            Responses.Message(exchange, 200, updateMessage);
                        }

                    });
            } else {
                String errorMessage = "Class ID must be provided.";
                Responses.Message(exchange, 400, errorMessage);

            }
        }catch (Exception e){
            Responses.Message(exchange, 500,  e.getMessage());

        }finally {
            if (connection != null) {
                ConnectDB.shutdown();
            }
        }
    }
}