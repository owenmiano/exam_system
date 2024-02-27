package ke.co.skyworld.handlers.questions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class GetQuestion implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            // Extracting the question ID from the URL path using PathTemplateMatch
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String examSubjectIdString = pathMatch.getParameters().get("examSubjectId");
            String questionIdString = pathMatch.getParameters().get("questionId");

            if (examSubjectIdString == null || examSubjectIdString.isEmpty() || questionIdString == null || questionIdString.isEmpty()) {
                exchange.setStatusCode(400);
                exchange.getResponseSender().send("Both examSubjectId and questionId are required.");
                return;
            }
            try {
                int examsubjectId = Integer.parseInt(examSubjectIdString);
                int questionId = Integer.parseInt(questionIdString);
                String[] columns = {
                        "q.question_no",
                        "q.questions_id",
                        "q.description",
                        "q.marks",
                        "s.subject_name",
                        "ch.option_label",
                        "ch.option_value",
                        "ch.correct"
                };

                String table = "questions q " +
                        "JOIN exam_subjects es ON q.exam_subject_id = es.exam_subject_id " +
                        "JOIN subject s ON es.subject_id = s.subject_id " +
                        "JOIN choices ch ON q.questions_id = ch.questions_id ";


                String whereClause = "q.exam_subject_id = ? AND q.questions_id = ?";

                Object[] values = new Object[]{examsubjectId,questionId};

                try {
                    JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns,whereClause,values);

                    // New aggregation logic
                    Map<Integer, JsonObject> questionMap = new HashMap<>();
                    for (int i = 0; i < jsonArrayResult.size(); i++) {
                        JsonObject row = jsonArrayResult.get(i).getAsJsonObject(); // Adjusted for Gson
                        JsonObject question;
                        if (questionMap.containsKey(questionId)) {
                            question = questionMap.get(questionId);
                            JsonArray options = question.getAsJsonArray("options");
                            JsonObject option = new JsonObject();
                            option.addProperty("option_label", row.get("option_label").getAsString());
                            option.addProperty("option_value", row.get("option_value").getAsString());
                            option.addProperty("correct", row.get("correct").getAsBoolean());
                            options.add(option);
                        } else {
                            question = new JsonObject();
                            question.addProperty("question number", row.get("question_no").getAsInt());
                            question.addProperty("description", row.get("description").getAsString());
                            question.addProperty("marks", row.get("marks").getAsInt());
                            question.addProperty("subject", row.get("subject_name").getAsString());

                            JsonArray options = new JsonArray();
                            JsonObject option = new JsonObject();
                            option.addProperty("option_label", row.get("option_label").getAsString());
                            option.addProperty("option_value", row.get("option_value").getAsString());
                            option.addProperty("correct", row.get("correct").getAsBoolean());
                            options.add(option);

                            question.add("options", options);
                            questionMap.put(questionId, question);
                        }
                    }


                    JsonArray aggregatedQuestions = new JsonArray();
                    questionMap.values().forEach(aggregatedQuestions::add);

                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange.getResponseSender().send(aggregatedQuestions.toString());
                } catch (SQLException e) {
                    String errorMessage = "SQL Error occurred: " + e.getMessage();
                    System.out.println(errorMessage);
                    exchange.getResponseSender().send(errorMessage);
                }
            } catch (Exception e) {
                String errorMessage = "An error occurred: " + e.getMessage();
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        }catch (Exception e){
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("Error: "+e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
