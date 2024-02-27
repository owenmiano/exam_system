package ke.co.skyworld.handlers.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.Model.ConfigReader;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenerateTopFivePupils implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String examsubjectIdString = pathMatch.getParameters().get("examSubjectId");

            if (examsubjectIdString == null) {
                exchange.setStatusCode(400);
                exchange.getResponseSender().send("Exam Subject ID is required.");
                return;
            }
            try {
                int examSubject = Integer.parseInt(examsubjectIdString);
                String dbType= ConfigReader.getDbType();

                String isCorrectColumn = dbType.equalsIgnoreCase("postgresql") ?
                        "SUM(CASE WHEN c.correct = true THEN q.marks ELSE 0 END) AS total_score" :
                        "SUM(CASE WHEN c.correct = 1 THEN q.marks ELSE 0 END) AS total_score";

                String[] columns = {
                        "p.pupil_name AS pupil",
                        "e.exam_name AS exam",
                        "s.subject_name AS subject",
                        "p.reg_no AS registration_number",
                        "cl.class_name AS class",
                        isCorrectColumn
                };

                String[][] joins = {
                        {"INNER", "choices c", "a.choices_id = c.choices_id"},
                        {"INNER", "questions q", "a.questions_id = q.questions_id"},
                        {"INNER", "pupils p", "a.pupils_id = p.pupils_id"},
                        {"INNER", "exam_subjects es", "q.exam_subject_id = es.exam_subject_id"},
                        {"INNER", "exam e", "es.exam_id = e.exam_id"},
                        {"INNER", "subject s", "es.subject_id = s.subject_id"},
                        {"INNER", "class cl", "p.class_id = cl.class_id"}
                };

                String whereClause = "es.exam_subject_id = ?";
                Object[] values = new Object[]{examSubject};

                String groupBy = "p.pupil_name, e.exam_name, s.subject_name, p.reg_no, cl.class_name"; // Define your GROUP BY clause here
                JsonArray aggregatedResults = GenericQueries.select(connection, "answers a", joins, columns, whereClause,groupBy,values);

                List<JsonObject> pupilsList = new ArrayList<>();
                for (JsonElement element : aggregatedResults) {
                    pupilsList.add(element.getAsJsonObject());
                }

                pupilsList.sort((a, b) -> {
                    int scoreA = a.get("total_score").getAsInt();
                    int scoreB = b.get("total_score").getAsInt();
                    return Integer.compare(scoreB, scoreA);
                });

                JsonArray topFivePupils = new JsonArray();
                for (int i = 0; i < Math.min(5, pupilsList.size()); i++) {
                    topFivePupils.add(pupilsList.get(i));
                }
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(topFivePupils.toString());

            } catch (SQLException e) {
                e.printStackTrace();
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


