package ke.co.skyworld.handlers.teachers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;

public class GetTeachers implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws SQLException {
        Connection connection = ConnectDB.getConnection();

            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
          //  String[] columns = (columnsDeque != null && !columnsDeque.isEmpty()) ? columnsDeque.getFirst().split(",") : new String[]{"*"};
            // Specify the table and any joins
        String table = "teachers t " +
                "JOIN class cl ON t.class_id = cl.class_id";
        String columns = "t.*, cl.class_name";
        String[] columnsArray = columns.split(",\\s*");

        Responses.Results(exchange, connection, table, columnsArray);

    }
    }
