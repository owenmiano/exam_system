package ke.co.skyworld.handlers.examSchedules;

import com.google.gson.JsonArray;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.sql.SQLException;

public class GetExamSchedule implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            // Extracting the exam schedule ID from the URL path using PathTemplateMatch
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String examScheduleIdString = pathMatch.getParameters().get("examScheduleId");

            try {
                int examScheduleId = Integer.parseInt(examScheduleIdString);
                String[] columns = {
                        "e.exam_id",
                        "e.exam_name",
                        "es.exam_duration",
                        "es.exam_date",
                        "t.teacher_name",
                        "s.subject_name",
                        "cl.class_name"
                };

                String table = "exam_subjects es " +
                        "JOIN exam e ON e.exam_id = es.exam_id " +
                        "JOIN teachers t ON es.teacher_id = t.teacher_id " +
                        "JOIN subject s ON es.subject_id = s.subject_id " +
                        "JOIN class cl ON e.class_id = cl.class_id";

                String whereClause = "es.exam_subject_id = ?";

                Object[] values = new Object[]{examScheduleId};

                try {
                    JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns, whereClause, values);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    if (jsonArrayResult.size() == 0) {
                        exchange.setStatusCode(404); // Not Found
                        exchange.getResponseSender().send("Exam schedule not found.");
                    } else {
                        exchange.getResponseSender().send(jsonArrayResult.toString());
                    }
                } catch (SQLException e) {
                    exchange.setStatusCode(500);
                    exchange.getResponseSender().send("Error: "+e.getMessage());
                }
            }  catch (Exception e) {
                // Handle any other unexpected exceptions
                exchange.setStatusCode(500);
                exchange.getResponseSender().send("Error: "+e.getMessage());
            }
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}

