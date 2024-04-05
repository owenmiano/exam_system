package ke.co.skyworld.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.queryBuilder.WhereClause;

import java.sql.Connection;

public class Responses {
    public static void Message(HttpServerExchange exchange, int statusCode, String message) {
        exchange.setStatusCode(statusCode);

//        exchange.getResponseHeaders().put(new HttpString("Content-type"), "application/json");

        exchange.getResponseHeaders().put(new HttpString("Content-type"), "application/json")
                .put(new HttpString("Access-Control-Allow-Origin"), "*")
                .put(new HttpString("Access-Control-Allow-Headers"), "*");

        exchange.getResponseSender().send("{\"message\": \"" + message + "\"}");
    }

    public static void Results(HttpServerExchange exchange, Connection connection, String tableName, String[] columns) {
        try {
            String filter = WhereClause.generateWhereClause(exchange);
            if (!filter.isEmpty()) {
                tableName += " WHERE " + filter;
            }
            String[] count = new String[]{"count(*)"};
            JsonArray totalRecords = SelectQuery.select(connection, tableName, count);

            // Extracting total records count from the result
            int totalRecordsCount = 0;
            JsonObject record = totalRecords.get(0).getAsJsonObject();
            totalRecordsCount = record.get("count(*)").getAsInt();


            Pagination pagination = new Pagination(exchange, totalRecordsCount);
            JsonArray jsonArrayResult = SelectQuery.select(connection, tableName, columns, pagination.getPageSize(), pagination.calculateOffset());
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("total_records", totalRecordsCount);
            responseJson.addProperty("total_Pages", pagination.calculateTotalPages());
            responseJson.addProperty("page", pagination.getPage());
            responseJson.addProperty("page_Size", pagination.getPageSize());

            if (jsonArrayResult.size() == 0) {
                String errorMessage = "No record found";
                Responses.Message(exchange, 404, errorMessage);
            } else if (jsonArrayResult.size() == 1) {
                JsonObject jsonObjectResult = jsonArrayResult.get(0).getAsJsonObject();
                responseJson.add("records", jsonObjectResult);
            } else {
                responseJson.add("data", jsonArrayResult);
            }
            exchange.getResponseSender().send(responseJson.toString());


        } catch (Exception e) {
            Responses.Message(exchange, 500, e.getMessage());
        } finally {
                ConnectDB.shutdown();
        }
    }
}
