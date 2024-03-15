package ke.co.skyworld.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.queryBuilder.WhereClause;

import java.sql.Connection;
import java.sql.SQLException;

public class Responses {
    public static void Message(HttpServerExchange exchange, int statusCode, String message) {
        exchange.setStatusCode(statusCode);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send("{\"message\": \"" + message + "\"}");
    }

    public static void Results(HttpServerExchange exchange, Connection connection, String tableName, String[] columns) {
        try {
            String filter= WhereClause.generateWhereClause(exchange);
            if (!filter.isEmpty()) {
                tableName += " WHERE " + filter;
            }
            String[] count = new String[]{"count(*)"};
            JsonArray totalRecords = SelectQuery.select(connection, tableName, count);

            // Extracting total records count from the result
            int totalRecordsCount = 0;
            JsonObject record = totalRecords.get(0).getAsJsonObject();
            totalRecordsCount = record.get("count(*)").getAsInt();


            Pagination pagination = new Pagination(exchange,totalRecordsCount);
            JsonArray jsonArrayResult = SelectQuery.select(connection, tableName, columns, pagination.getPageSize(), pagination.calculateOffset());
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("total records", totalRecordsCount);
            responseJson.addProperty("total Pages", pagination.calculateTotalPages());
            responseJson.addProperty("page", pagination.getPage());
            responseJson.addProperty("page Size", pagination.getPageSize());

            if (jsonArrayResult.size() == 0) {
                String errorMessage = "No record found";
                Responses.Message(exchange, 404, errorMessage);
            } else if (jsonArrayResult.size() == 1) {
                JsonObject jsonObjectResult = jsonArrayResult.get(0).getAsJsonObject();
                responseJson.add("data", jsonObjectResult);
            } else {
                responseJson.add("data", jsonArrayResult);
            }
            exchange.getResponseSender().send(responseJson.toString());


        } catch (SQLException e) {
            Responses.Message(exchange, 500, e.getMessage());
        } catch (Exception e) {
            Responses.Message(exchange, 500, e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
