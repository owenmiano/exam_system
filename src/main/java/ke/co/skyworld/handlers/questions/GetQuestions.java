package ke.co.skyworld.handlers.questions;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;
import java.util.Deque;

public class GetQuestions implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();
        try {
            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
            String[] columns = (columnsDeque != null && !columnsDeque.isEmpty()) ? columnsDeque.getFirst().split(",") : new String[]{"*"};
            // Specify the table and any joins
            String table = "questions q " +
                    "JOIN exam_subjects es ON q.exam_subject_id = es.exam_subject_id " +
                    "JOIN subject s ON es.subject_id = s.subject_id " +
                    "JOIN choices ch ON q.questions_id = ch.questions_id ";

            Responses.Results(exchange,connection,table,columns);

        } catch (Exception e) {
            Responses.Message(exchange, 500, e.getMessage());
        }
    }
}
