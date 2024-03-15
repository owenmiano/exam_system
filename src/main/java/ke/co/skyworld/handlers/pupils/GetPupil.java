package ke.co.skyworld.handlers.pupils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;
import java.sql.SQLException;

public class GetPupil implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String pupilIdString = pathMatch.getParameters().get("pupilId");

            try {
                   if (pupilIdString == null || pupilIdString.isEmpty()) {
                    String errorMessage = "Pupil ID must be provided.";
                    Responses.Message(exchange, 400, errorMessage);
                    return;
                    }

                int pupilId = Integer.parseInt(pupilIdString);
                String[] columns = {
                        "p.pupil_name",
                        "p.date_of_birth",
                        "p.guardian_name",
                        "p.guardian_phone",
                        "p.username",
                        "p.reg_no",
                        "cl.class_name"
                };

                // Specify the table and any joins
                String table = "pupils p JOIN class cl ON p.class_id = cl.class_id";

                String whereClause = "p.pupils_id = ?";

                Object[] values = new Object[]{pupilId};

                try {
                    // Execute the SELECT query
                    JsonArray jsonArrayResult = SelectQuery.select(connection, table, columns, whereClause, values);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

                    if (jsonArrayResult.size() == 0) {
                        String errorMessage = "Pupil not found";
                        Responses.Message(exchange, 404, errorMessage);
                    } else if (jsonArrayResult.size() == 1) {
                        JsonObject jsonObjectResult = jsonArrayResult.get(0).getAsJsonObject();
                        exchange.setStatusCode(200);
                        exchange.getResponseSender().send(jsonObjectResult.toString());
                    } else {
                        exchange.setStatusCode(200);
                        exchange.getResponseSender().send(jsonArrayResult.toString());
                    }
                } catch (SQLException e) {
                    Responses.Message(exchange, 500,  e.getMessage());
                }
            }catch (Exception e){
                Responses.Message(exchange, 500,  e.getMessage());
            }
            finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
