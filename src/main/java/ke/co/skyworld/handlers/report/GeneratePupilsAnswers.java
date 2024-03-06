package ke.co.skyworld.handlers.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.Model.ConfigReader;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.utils.Response;

import java.sql.Connection;
import java.sql.SQLException;

public class GeneratePupilsAnswers implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String examSubjectIdString = pathMatch.getParameters().get("examSubjectId");
            String pupilIdString = pathMatch.getParameters().get("pupilId");

            if (pupilIdString == null || pupilIdString.isEmpty() || examSubjectIdString == null || examSubjectIdString.isEmpty()) {
                Response.Message(exchange, 400, "Pupil ID and Exam Subject ID are required.");
                return;
            }
            try {
                int pupilId = Integer.parseInt(pupilIdString);
                int examSubject = Integer.parseInt(examSubjectIdString);
                String dbType= ConfigReader.getDbType();

                String isCorrectColumn = dbType.equalsIgnoreCase("postgresql") ?
                        "CASE WHEN c.correct = true THEN 'correct' ELSE 'incorrect' END AS is_correct" :
                        "CASE WHEN c.correct = 1 THEN 'correct' ELSE 'incorrect' END AS is_correct";

                String scoreColumn = dbType.equalsIgnoreCase("postgresql") ?
                        "CASE WHEN c.correct = true THEN q.marks ELSE 0 END AS score" :
                        "CASE WHEN c.correct = 1 THEN q.marks ELSE 0 END AS score";

                String[] columns = {
                        "p.pupil_name",
                        "p.reg_no",
                        "cl.class_name",
                        "s.subject_name",
                        "e.exam_name",
                        "q.question_no",
                        "q.description",
                        "c.option_value AS chosen_answer",
                        isCorrectColumn,
                        scoreColumn
                };

                String[][] joins = {
                        {"INNER", "questions q", "a.questions_id = q.questions_id"},
                        {"INNER", "choices c", "a.choices_id = c.choices_id"},
                        {"INNER", "exam_subjects es", "q.exam_subject_id = es.exam_subject_id"},
                        {"INNER", "exam e", "es.exam_id = e.exam_id"},
                        {"INNER", "subject s", "es.subject_id = s.subject_id"},
                        {"INNER", "pupils p", "a.pupils_id = p.pupils_id"},
                        {"INNER", "class cl", "p.class_id = cl.class_id"}
                };


                String whereClause = "p.pupils_id = ? AND es.exam_subject_id = ?";
                Object[] params = new Object[]{pupilId, examSubject};
                JsonArray answersReport = SelectQuery.select(connection, "answers a", joins, columns, whereClause, params);
                JsonObject result = new JsonObject();
                JsonArray questionsArray = new JsonArray();
                int totalScore = 0;

                for (int i = 0; i < answersReport.size(); i++) {
                    JsonObject row = answersReport.get(i).getAsJsonObject();
                    if (i == 0) {
                        result.addProperty("pupil", row.get("pupil_name").getAsString());
                        result.addProperty("reg_no", row.get("reg_no").getAsString());
                        result.addProperty("class", row.get("class_name").getAsString());
                        result.addProperty("subject", row.get("subject_name").getAsString());
                        result.addProperty("exam", row.get("exam_name").getAsString());
                    }
                    JsonObject question = new JsonObject();
                    question.addProperty("question no", row.get("question_no").getAsInt());
                    question.addProperty("question", row.get("description").getAsString());
                    question.addProperty("chosen_answer", row.get("option_value").getAsString());
                    question.addProperty("is_correct", row.get("is_correct").getAsString());
                    question.addProperty("score", row.get("score").getAsInt());
                    questionsArray.add(question);

                    totalScore += row.get("score").getAsInt();
                }

                result.add("questions", questionsArray);
                result.addProperty("total_score", totalScore);

                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(result.toString());

            } catch (SQLException e) {
                Response.Message(exchange, 500,  e.getMessage());
            }
        }catch (Exception e) {
            Response.Message(exchange, 500,  e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}