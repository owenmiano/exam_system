package ke.co.skyworld.handlers.questions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.utils.Pagination;
import ke.co.skyworld.utils.Response;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class GetQuestions implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
            String[] columns = (columnsDeque != null && !columnsDeque.isEmpty()) ? columnsDeque.getFirst().split(",") : new String[]{"*"};
            // Specify the table and any joins
            String table = "questions q " +
                    "JOIN exam_subjects es ON q.exam_subject_id = es.exam_subject_id " +
                    "JOIN subject s ON es.subject_id = s.subject_id " +
                    "JOIN choices ch ON q.questions_id = ch.questions_id ";

            Response.Results(exchange,connection,table,columns);

        } catch (Exception e) {
            Response.Message(exchange, 500, e.getMessage());
        }
    }
}
