package ke.co.skyworld.handlers.subjects;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

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
                            System.out.println(errorMessage);
                            exchange.getResponseSender().send(errorMessage);
                            return;
                        }

                        String whereClause = "subject_id = ?";

                        String result = GenericQueries.update(connection, "subject", subjectData, whereClause, subjectId); // Ensure table name matches your database
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange1.getResponseSender().send(result);

                    });

            } else {
                // Handle the case where the subject ID is missing or empty
                String errorMessage = "Subject ID is missing ";
                exchange.setStatusCode(404);
                exchange.getResponseSender().send(errorMessage);
            }
        }catch (Exception e){
            String errorMessage = "An error occurred: " + e.getMessage();
            exchange.setStatusCode(500);
            exchange.getResponseSender().send(errorMessage);
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
