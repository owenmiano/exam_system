package ke.co.skyworld.handlers.exam;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.UpdateQuery;
import ke.co.skyworld.utils.Response;

import java.sql.Connection;

public class UpdateExam implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            // Extracting the exam ID from the URL path using PathTemplateMatch
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String examIdString = pathMatch.getParameters().get("examId");

            if (examIdString != null && !examIdString.trim().isEmpty()) {

                    int examId = Integer.parseInt(examIdString);

                    exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                        Gson gson = new Gson();
                        JsonObject examData = gson.fromJson(requestBody, JsonObject.class);

                        if (examData == null || !examData.has("exam_name") || examData.get("exam_name").getAsString().trim().isEmpty()) {
                            String errorMessage = "Exam name is missing.";
                            System.out.println(errorMessage);
                            exchange.getResponseSender().send(errorMessage);
                            return;
                        }

                        String whereClause = "exam_id = ?";

                        String updateMessage = UpdateQuery.update(connection, "exam", examData, whereClause, examId);
                        if (updateMessage.startsWith("Error")) {
                            Response.Message(exchange, 500, updateMessage);
                        } else {
                            Response.Message(exchange, 200, updateMessage);
                        }

                    });
                }else {
                String errorMessage = "Exam ID is missing ";
                Response.Message(exchange, 400, errorMessage);
            }
        }catch (Exception e){
            Response.Message(exchange, 500, e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
