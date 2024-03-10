package ke.co.skyworld.handlers.teachers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import ke.co.skyworld.db.ConnectDB;

import ke.co.skyworld.utils.Response;

import java.sql.Connection;

import java.util.Deque;
import java.util.StringJoiner;

public class GetTeachers implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
            String[] columns = (columnsDeque != null && !columnsDeque.isEmpty()) ? columnsDeque.getFirst().split(",") : new String[]{"*"};
            // Specify the table and any joins
             String table = "teachers t " +
                "JOIN class cl ON t.class_id = cl.class_id " ;

          Response.Results(exchange,connection,table,columns);

    } catch (Exception e) {
            Response.Message(exchange, 500, e.getMessage());
        }
    }
}