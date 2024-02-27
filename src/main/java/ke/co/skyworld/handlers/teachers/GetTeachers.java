package ke.co.skyworld.handlers.teachers;

import com.google.gson.JsonArray;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;

public class GetTeachers implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            String[] columns = {
                    "t.teacher_name",
                    "t.tsc_number",
                    "t.id_number",
                    "t.kra_pin",
                    "t.phone",
                    "t.email",
                    "t.date_of_birth",
                    "t.username",
                    "cl.class_name",
            };

            // Specify the table and any joins
            String table = "teachers t JOIN class cl ON t.class_id = cl.class_id";

            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {

                try {
                    JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns);
                    if (jsonArrayResult.size() == 0) {
                        exchange.setStatusCode(404); // Not Found
                        exchange.getResponseSender().send("Teachers not found.");
                    } else {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange.getResponseSender().send(jsonArrayResult.toString());
                    }
                } catch (Exception e) {
                    String errorMessage = "SQL Error occurred: " + e.getMessage();
                    System.out.println(errorMessage);
                    exchange.getResponseSender().send(errorMessage);
                }
            });
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}