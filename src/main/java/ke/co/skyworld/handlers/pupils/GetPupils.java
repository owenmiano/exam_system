package ke.co.skyworld.handlers.pupils;

import com.google.gson.JsonArray;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.sql.SQLException;

public class GetPupils implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            String[] columns = {
                    "p.pupil_name",
                    "p.date_of_birth",
                    "p.guardian_name",
                    "p.guardian_phone",
                    "p.username",
                    "p.reg_no",
                    "cl.class_name"
            };

            String table = "pupils p " +
                    "JOIN class cl ON p.class_id = cl.class_id " ;

            try {
                JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns);
                if (jsonArrayResult.size() == 0) {
                    exchange.setStatusCode(404);
                    exchange.getResponseSender().send("Pupils not found.");
                } else {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange.getResponseSender().send(jsonArrayResult.toString());
                }

            } catch (SQLException e) {
                String errorMessage = "SQL Error occurred: " + e.getMessage();
                exchange.setStatusCode(500);
                exchange.getResponseSender().send(errorMessage);
            }
        }  catch (Exception e) {
            String errorMessage = "An error occurred: " + e.getMessage();
            exchange.setStatusCode(500);
            exchange.getResponseSender().send(errorMessage);
        }
        finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}