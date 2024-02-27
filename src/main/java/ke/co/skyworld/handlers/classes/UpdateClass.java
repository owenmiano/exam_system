package ke.co.skyworld.handlers.classes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class UpdateClass implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

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
                            String errorMessage = "Class name is missing.";
                            System.out.println(errorMessage);
                            exchange.getResponseSender().send(errorMessage);
                            return;
                        }

                        String whereClause = "class_id = ?";

                        String result = GenericQueries.update(connection, "class", classData, whereClause, classId);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange1.getResponseSender().send(result);

                    });

            } else {
                // Handle the case where the class ID is missing or empty
                String errorMessage = "Class ID is missing or empty in the request URL.";
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