package ke.co.skyworld.handlers.subjects;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.UpdateQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;

public class UpdateSubject implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            // Extracting the subject ID from the URL path using PathTemplateMatch
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String subjectIdString = pathMatch.getParameters().get("subjectId");

            if (subjectIdString != null && !subjectIdString.trim().isEmpty()) {
                    int subjectId = Integer.parseInt(subjectIdString);

                    exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                        Gson gson = new Gson();
                        JsonObject subjectData = gson.fromJson(requestBody, JsonObject.class);

                        if (subjectData == null || !subjectData.has("subject_name") || subjectData.get("subject_name").getAsString().trim().isEmpty()) {
                            String errorMessage = "Subject name is missing.";
                            Responses.Message(exchange, 400, errorMessage);
                            return;
                        }

                        String whereClause = "subject_id = ?";

                        String updateMessage = UpdateQuery.update(connection, "subject", subjectData, whereClause, subjectId);
                        if (updateMessage.startsWith("Error")) {
                            Responses.Message(exchange, 500, updateMessage);
                        } else {
                            Responses.Message(exchange, 200, updateMessage);
                        }
                    });

            } else {
                String errorMessage = "Subject ID is missing ";
                Responses.Message(exchange, 400, errorMessage);
            }
        }catch (Exception e){
            Responses.Message(exchange, 500,  e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
