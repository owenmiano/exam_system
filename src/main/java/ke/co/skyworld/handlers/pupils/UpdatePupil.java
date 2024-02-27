package ke.co.skyworld.handlers.pupils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class UpdatePupil implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String pupilIdString = pathMatch.getParameters().get("pupilId");

            if (pupilIdString != null && !pupilIdString.trim().isEmpty()) {
                try {
                    int pupilId = Integer.parseInt(pupilIdString);

                    exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                        Gson gson = new Gson();
                        JsonObject pupilData = gson.fromJson(requestBody, JsonObject.class);

                        // Optionally, validate pupilData here

                        String whereClause = "pupils_id = ?"; // Ensure the column name matches your database schema

                        String result = GenericQueries.update(connection, "pupils", pupilData, whereClause, pupilId);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange1.getResponseSender().send(result);

                    });
                } catch (NumberFormatException e) {
                    String errorMessage = "Invalid Pupil ID: " + pupilIdString;
                    System.out.println(errorMessage);
                    exchange.getResponseSender().send(errorMessage);
                }
            } else {
                // Handle the case where the pupil ID is missing or empty
                String errorMessage = "Pupil ID is missing or empty in the request URL.";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }

        }catch (Exception e){
            throw e;
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}