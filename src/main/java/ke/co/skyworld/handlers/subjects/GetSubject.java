package ke.co.skyworld.handlers.subjects;

import com.google.gson.JsonArray;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

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
                        JsonArray jsonArrayResult = GenericQueries.select(connection, "subject", finalColumns, whereClause, subjectId);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        if (jsonArrayResult.size() == 0) {
                            exchange.setStatusCode(404); // Not Found
                            exchange.getResponseSender().send("Subject not found.");
                        } else {
                            exchange.getResponseSender().send(jsonArrayResult.toString());
                        }
                    } catch (SQLException e) {
                        String errorMessage = "SQL Error occurred: " + e.getMessage();
                        exchange.setStatusCode(500);
                        exchange1.getResponseSender().send(errorMessage);
                    }
                });

        }catch (Exception e){
            exchange.setStatusCode(500);
            exchange.getResponseSender().send(e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}