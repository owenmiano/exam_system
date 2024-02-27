package ke.co.skyworld.handlers.examSchedules;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class UpdateExamSchedule implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

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

                        String result = GenericQueries.update(connection, "exam_subjects", examScheduleData, whereClause, examScheduleId);
                        exchange1.getResponseSender().send(result);

                    });

            } else {
                // Handle the case where the exam schedule ID is missing or empty
                String errorMessage = "Exam schedule ID is missing or empty in the request URL.";
                exchange.setStatusCode(400);
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
