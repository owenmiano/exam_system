package ke.co.skyworld.handlers.examSchedules;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;
import java.util.Deque;

public class GetExamSchedules implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();
        try {
            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
            String[] columns = (columnsDeque != null && !columnsDeque.isEmpty()) ? columnsDeque.getFirst().split(",") : new String[]{"*"};
            // Specify the table and any joins
            String table = "exam_subjects es " +
                    "JOIN exam e ON e.exam_id = es.exam_id " +
                    "JOIN teachers t ON es.teacher_id = t.teacher_id " +
                    "JOIN subject s ON es.subject_id = s.subject_id " +
                    "JOIN class cl ON e.class_id = cl.class_id";

            Responses.Results(exchange,connection,table,columns);

        } catch (Exception e) {
            Responses.Message(exchange, 500, e.getMessage());
        }
    }
}

