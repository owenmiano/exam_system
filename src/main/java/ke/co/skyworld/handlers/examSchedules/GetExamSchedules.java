package ke.co.skyworld.handlers.examSchedules;

import com.google.gson.JsonArray;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.sql.SQLException;

public class GetExamSchedules implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            // Extracting the columns parameter from the query string
            String[] columns = {
                    "e.exam_id",
                    "e.exam_name",
                    "es.exam_duration",
                    "es.exam_date",
                    "t.teacher_name",
                    "s.subject_name",
                    "cl.class_name"
            };

            String table = "exam_subjects es " +
                    "JOIN exam e ON e.exam_id = es.exam_id " +
                    "JOIN teachers t ON es.teacher_id = t.teacher_id " +
                    "JOIN subject s ON es.subject_id = s.subject_id " +
                    "JOIN class cl ON e.class_id = cl.class_id";
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {

                try {
                    JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(jsonArrayResult.toString());
                } catch (SQLException e) {
                    exchange.setStatusCode(500);
                    exchange.getResponseSender().send("Error: "+e.getMessage());
                }
            });
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

