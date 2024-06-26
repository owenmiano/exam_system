package ke.co.skyworld.handlers.report;

import com.google.gson.JsonArray;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;
import java.sql.SQLException;

public class GenerateExamsByTeacher implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();
        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String teacherIdString = pathMatch.getParameters().get("teacherId");

            if (teacherIdString == null || teacherIdString.isEmpty()) {
                Responses.Message(exchange, 400, "Teacher ID is required.");
                return;
            }


            try {
                int teacherId = Integer.parseInt(teacherIdString);
                String[] columns = {
                        "e.exam_name",
                        "es.exam_date",
                        "t.teacher_name",
                        "s.subject_name",
                        "cl.class_name"
                };

                String[][] joins = {
                        {"INNER", "exam_subjects es", "e.exam_id = es.exam_id"},
                        {"INNER", "teachers t", "es.teacher_id = t.teacher_id"},
                        {"INNER", "subject s", "es.subject_id = s.subject_id"},
                        {"INNER", "class cl", "e.class_id = cl.class_id"}
                };

                String whereClause = "t.teacher_id = ?";
                Object[] values = new Object[]{teacherId};

                JsonArray jsonArrayResult = SelectQuery.select(connection, "exam e", joins, columns, whereClause, values);
                String jsonResult = jsonArrayResult.toString();

                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(jsonResult);

            }  catch (SQLException e) {
                Responses.Message(exchange, 500,  e.getMessage());
            }
        }catch (Exception e) {
            Responses.Message(exchange, 500,  e.getMessage());
        }finally {
            if (connection != null) {
                ConnectDB.shutdown();
            }
        }
    }
}
