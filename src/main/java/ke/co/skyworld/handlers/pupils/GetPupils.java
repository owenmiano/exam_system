package ke.co.skyworld.handlers.pupils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.utils.Pagination;
import ke.co.skyworld.utils.Response;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.StringJoiner;

public class GetPupils implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
            String[] columns = (columnsDeque != null && !columnsDeque.isEmpty()) ? columnsDeque.getFirst().split(",") : new String[]{"*"};

            StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
            Deque<String> filterDeque = exchange.getQueryParameters().get("filter");

            if (filterDeque != null && !filterDeque.isEmpty()) {
                for (String filter : filterDeque) {
                    // Splitting each filter into its components: field, operation, and value
                    String[] parts = filter.split(":", 3);
                    if (parts.length == 3) {
                        String field = parts[0];
                        String operation = parts[1];
                        String value = parts[2];

                        switch (operation) {
                            case "like":
                                whereClauseJoiner.add(field + " LIKE '%" + value + "%'");
                                break;
                            case "eq":
                                whereClauseJoiner.add(field + " = '" + value + "'");
                                break;
                            case "begins":
                                whereClauseJoiner.add(field + " LIKE '" + value + "%'");
                                break;
                            case "ends":
                                whereClauseJoiner.add(field + " LIKE '%" + value + "'");
                                break;

                        }
                    }
                }
            }

            String whereClause = whereClauseJoiner.toString();

            String table = "pupils p " +
                    "JOIN class cl ON p.class_id = cl.class_id " ;
            if (!whereClause.isEmpty()) {
                table += " WHERE " + whereClause;
            }

            try {
                Pagination pagination = new Pagination(exchange);


                JsonArray jsonArrayResult = SelectQuery.select(connection, table, columns, pagination.getPageSize(), pagination.calculateOffset());
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

                if (jsonArrayResult.size() == 0) {
                    String errorMessage = "No pupils";
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
                Response.Message(exchange, 500, e.getMessage());
            }
        }  catch (Exception e) {
            Response.Message(exchange, 500, e.getMessage());
        }
        finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}