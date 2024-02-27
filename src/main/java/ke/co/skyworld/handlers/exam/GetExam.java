package ke.co.skyworld.handlers.exam;

import com.google.gson.JsonArray;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.sql.SQLException;

public class GetExam implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

        try {
            // Extracting the exam ID from the URL path using PathTemplateMatch
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String examIdString = pathMatch.getParameters().get("examId");

            String[] columns = {
                    "e.exam_name",
                    "cl.class_name"
            };

            String table = "exam e " +
                    "JOIN class cl ON e.class_id = cl.class_id " ;

                int examId = Integer.parseInt(examIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    String whereClause = "exam_id = ?";

                    try {
                        JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns, whereClause, examId);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        if (jsonArrayResult.size() == 0) {
                            exchange.setStatusCode(404); // Not Found
                            exchange.getResponseSender().send("Exam not found.");
                        } else {
                            exchange.getResponseSender().send(jsonArrayResult.toString());
                        }
                    } catch (SQLException e) {
                        String errorMessage = "SQL Error occurred: " + e.getMessage();
                        System.out.println(errorMessage);
                        exchange1.getResponseSender().send(errorMessage);
                    }
                });

        }finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}