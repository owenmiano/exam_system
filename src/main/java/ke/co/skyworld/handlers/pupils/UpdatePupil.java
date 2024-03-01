package ke.co.skyworld.handlers.pupils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.UpdateQuery;
import ke.co.skyworld.utils.Response;

import java.sql.Connection;

public class UpdatePupil implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String pupilIdString = pathMatch.getParameters().get("pupilId");

            if (pupilIdString != null && !pupilIdString.trim().isEmpty()) {

                    int pupilId = Integer.parseInt(pupilIdString);

                    exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                        Gson gson = new Gson();
                        JsonObject pupilData = gson.fromJson(requestBody, JsonObject.class);

                        // Optionally, validate pupilData here

                        String whereClause = "pupils_id = ?"; // Ensure the column name matches your database schema

                        String updateMessage = UpdateQuery.update(connection, "pupils", pupilData, whereClause, pupilId);
                        if (updateMessage.startsWith("Error")) {
                            Response.Message(exchange, 500, updateMessage);
                        } else {
                            Response.Message(exchange, 200, updateMessage);
                        }

                    });

            } else {
                // Handle the case where the pupil ID is missing or empty
                String errorMessage = "Pupil ID is missing ";
                Response.Message(exchange, 400, errorMessage);
            }

        }catch (Exception e){
            Response.Message(exchange, 500, e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}