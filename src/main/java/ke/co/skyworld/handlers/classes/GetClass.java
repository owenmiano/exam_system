package ke.co.skyworld.handlers.classes;

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
import java.util.Deque;

public class GetClass implements HttpHandler {


    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String classIdString = pathMatch.getParameters().get("classId");

            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
            String[] columns = null;
            if (columnsDeque != null && !columnsDeque.isEmpty()) {
                String columnsString = columnsDeque.getFirst();
                columns = columnsString.split(",");
            } else {
                columns = new String[]{"*"};
            }
            if (classIdString != null || !classIdString.isEmpty()) {

            int classId = Integer.parseInt(classIdString);
            final String[] finalColumns = columns;

            JsonArray jsonArrayResult = SelectQuery.select(connection, "class", finalColumns, "class_id = ?", classId);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

            if (jsonArrayResult.size() == 0) {
                String errorMessage = "Class not found";
                Responses.Message(exchange, 404, errorMessage);
            } else if (jsonArrayResult.size() == 1) {
                JsonObject jsonObjectResult = jsonArrayResult.get(0).getAsJsonObject();
                exchange.setStatusCode(200);
                exchange.getResponseSender().send(jsonObjectResult.toString());
            } else {
                exchange.setStatusCode(200);
                exchange.getResponseSender().send(jsonArrayResult.toString());
            }
            }
            else {
                String errorMessage = "Class ID must be provided.";
                Responses.Message(exchange, 400, errorMessage);

            }

        }catch (Exception e){
            Responses.Message(exchange, 500,  e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }

    }
}