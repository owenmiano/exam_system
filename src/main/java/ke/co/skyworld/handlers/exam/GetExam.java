package ke.co.skyworld.handlers.exam;

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

public class GetExam implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

        try {
            // Extracting the exam ID from the URL path using PathTemplateMatch
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String examIdString = pathMatch.getParameters().get("examId");

            if (examIdString == null || examIdString.isEmpty()) {
                String errorMessage = "Exam ID must be provided.";
                Responses.Message(exchange, 400, errorMessage);
                return;

            }
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
                        JsonArray jsonArrayResult = SelectQuery.select(connection, table, columns, whereClause, examId);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

                        if (jsonArrayResult.size() == 0) {
                            String errorMessage = "Exam not found";
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
                });

        }catch (Exception e){
            Responses.Message(exchange, 500,  e.getMessage());
        }finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}