package ke.co.skyworld.handlers.examSchedules;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;
import java.sql.SQLException;

public class GetExamSchedule implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();
        try {
            // Extracting the exam schedule ID from the URL path using PathTemplateMatch
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String examScheduleIdString = pathMatch.getParameters().get("examScheduleId");
            if (examScheduleIdString == null || examScheduleIdString.isEmpty()) {
                String errorMessage = "Exam Schedule ID must be provided.";
                Responses.Message(exchange, 400, errorMessage);
                return;

            }
            try {
                int examScheduleId = Integer.parseInt(examScheduleIdString);
                String[] columns = {
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
                    JsonArray jsonArrayResult = SelectQuery.select(connection, table, columns, whereClause, values);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    if (jsonArrayResult.size() == 0) {
                        String errorMessage = "Exam schedule not found";
                        Responses.Message(exchange, 404, errorMessage);
                    } else if (jsonArrayResult.size() == 1) {
                        JsonObject jsonObjectResult = jsonArrayResult.get(0).getAsJsonObject();
                        exchange.setStatusCode(200);
                        exchange.getResponseSender().send(jsonObjectResult.toString());
                    } else {
                        exchange.setStatusCode(200);
                        exchange.getResponseSender().send(jsonArrayResult.toString());
                    }

                } catch (SQLException e) {
                    Responses.Message(exchange, 500,  e.getMessage());
                }
            }  catch (Exception e) {
                // Handle any other unexpected exceptions
                Responses.Message(exchange, 500,  e.getMessage());
            }
        }finally {
            if (connection != null) {
                ConnectDB.shutdown();
            }
        }
    }
}

