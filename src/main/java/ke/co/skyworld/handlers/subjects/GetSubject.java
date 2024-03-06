package ke.co.skyworld.handlers.subjects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.utils.Response;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;

public class GetSubject implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            // Extracting the columns parameter from the query string
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String subjectIdString = pathMatch.getParameters().get("subjectId");
            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
            String[] columns = null;
            if (columnsDeque != null && !columnsDeque.isEmpty()) {
                String columnsString = columnsDeque.getFirst();
                columns = columnsString.split(",");
            } else {
                // If no columns parameter provided, select all columns
                columns = new String[]{"*"};
            }

                int subjectId = Integer.parseInt(subjectIdString);
                final String[] finalColumns = columns;

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {

                    String whereClause = "subject_id = ?";

                    try {
                        JsonArray jsonArrayResult = SelectQuery.select(connection, "subject", finalColumns, whereClause, subjectId);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

                        if (jsonArrayResult.size() == 0) {
                            String errorMessage = "Subject not found";
                            Response.Message(exchange, 404, errorMessage);
                        } else if (jsonArrayResult.size() == 1) {
                            JsonObject jsonObjectResult = jsonArrayResult.get(0).getAsJsonObject();
                            exchange.setStatusCode(200);
                            exchange.getResponseSender().send(jsonObjectResult.toString());
                        } else {
                            exchange.setStatusCode(200);
                            exchange.getResponseSender().send(jsonArrayResult.toString());
                        }
                    } catch (SQLException e) {
                        Response.Message(exchange, 500,  e.getMessage());
                    }
                });

        }catch (Exception e){
            Response.Message(exchange, 500,  e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
