package ke.co.skyworld.handlers.examSchedules;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.UpdateQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;

public class UpdateExamSchedule implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();

        try {
            // Extracting the exam schedule ID from the URL path using PathTemplateMatch
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String examScheduleIdString = pathMatch.getParameters().get("examScheduleId");

            if (examScheduleIdString != null && !examScheduleIdString.trim().isEmpty()) {
                    int examScheduleId = Integer.parseInt(examScheduleIdString);

                    exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                        Gson gson = new Gson();
                        JsonObject examScheduleData = gson.fromJson(requestBody, JsonObject.class);

                        String whereClause = "exam_subject_id = ?";

                        String updateMessage = UpdateQuery.update(connection, "exam_subjects", examScheduleData, whereClause, examScheduleId);
                        if (updateMessage.startsWith("Error")) {
                            Responses.Message(exchange, 500, updateMessage);
                        } else {
                            Responses.Message(exchange, 200, updateMessage);
                        }

                    });

            } else {
                // Handle the case where the exam schedule ID is missing or empty
                String errorMessage = "Exam schedule ID is missing ";
                Responses.Message(exchange, 400, errorMessage);
            }
        }catch (Exception e){
            Responses.Message(exchange, 500, e.getMessage());
        }finally {
            if (connection != null) {
                ConnectDB.shutdown();
            }
        }
    }
}
