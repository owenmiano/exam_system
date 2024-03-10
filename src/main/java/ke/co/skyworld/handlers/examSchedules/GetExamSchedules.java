package ke.co.skyworld.handlers.examSchedules;

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

public class GetExamSchedules implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
            String[] columns = (columnsDeque != null && !columnsDeque.isEmpty()) ? columnsDeque.getFirst().split(",") : new String[]{"*"};
            // Specify the table and any joins
            String table = "exam_subjects es " +
                    "JOIN exam e ON e.exam_id = es.exam_id " +
                    "JOIN teachers t ON es.teacher_id = t.teacher_id " +
                    "JOIN subject s ON es.subject_id = s.subject_id " +
                    "JOIN class cl ON e.class_id = cl.class_id";

            Response.Results(exchange,connection,table,columns);

        } catch (Exception e) {
            Response.Message(exchange, 500, e.getMessage());
        }
    }
}

