package ke.co.skyworld.handlers.pupils;

import com.google.gson.JsonArray;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;
import java.sql.Connection;
import java.sql.SQLException;

public class GetPupil implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String pupilIdString = pathMatch.getParameters().get("pupilId");

            try {
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
                    JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns, whereClause, values);

                    if (jsonArrayResult.size() == 0) {
                        exchange.setStatusCode(404); // Not Found
                        exchange.getResponseSender().send("Pupil not found.");
                    } else {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange.getResponseSender().send(jsonArrayResult.toString());
                    }
                } catch (SQLException e) {
                    String errorMessage = "Error " + e.getMessage();
                    exchange.setStatusCode(500);
                    exchange.getResponseSender().send(errorMessage);
                }
            }
            finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
