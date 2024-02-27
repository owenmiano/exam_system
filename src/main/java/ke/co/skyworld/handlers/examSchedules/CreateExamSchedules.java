package ke.co.skyworld.handlers.examSchedules;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class CreateExamSchedules implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                Gson gson = new Gson();
                JsonObject examScheduleData = gson.fromJson(requestBody, JsonObject.class);

                if (examScheduleData == null || !examScheduleData.has("exam_id") || examScheduleData.get("exam_id").getAsString().trim().isEmpty()) {
                    String errorMessage = "Exam ID is missing.";
                    exchange.setStatusCode(404);
                    exchange.getResponseSender().send(errorMessage);
                    return;
                }
                if (examScheduleData == null || !examScheduleData.has("subject_id") || examScheduleData.get("subject_id").getAsString().trim().isEmpty()) {
                    String errorMessage = "Subject ID is missing.";
                    exchange.setStatusCode(404); // Not Found
                    exchange.getResponseSender().send(errorMessage);
                    return;
                }
                if (examScheduleData == null || !examScheduleData.has("teacher_id") || examScheduleData.get("teacher_id").getAsString().trim().isEmpty()) {
                    String errorMessage = "Teacher ID is missing.";
                    exchange.getResponseSender().send(errorMessage);
                    return;
                }


                String insertionResult = GenericQueries.insertData(connection, "exam_subjects", examScheduleData);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(insertionResult);
            });

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
