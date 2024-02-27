package ke.co.skyworld.handlers.exam;

import com.google.gson.JsonArray;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.sql.SQLException;

public class GetExams implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            // Extracting the columns parameter from the query string
            String[] columns = {
                    "e.exam_name",
                    "cl.class_name"
            };

            String table = "exam e " +
                    "JOIN class cl ON e.class_id = cl.class_id " ;
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {

                try {
                    JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(jsonArrayResult.toString());
                } catch (SQLException e) {
                    String errorMessage = "SQL Error occurred: " + e.getMessage();
                    System.out.println(errorMessage);
                    exchange1.getResponseSender().send(errorMessage);
                }
            });

        }catch (Exception e){
            throw e;
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
