package ke.co.skyworld.handlers.examSchedules;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.InsertQuery;
import ke.co.skyworld.utils.Response;

import java.sql.Connection;

public class CreateExamSchedules implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                Gson gson = new Gson();
                JsonObject examScheduleData = gson.fromJson(requestBody, JsonObject.class);

                if (!examScheduleData.has("exam_id") || examScheduleData.get("exam_id").getAsInt() == 0) {
                    String errorMessage = "Exam ID is missing.";
                    Response.Message(exchange, 400, errorMessage);
                    return;
                }
                if (!examScheduleData.has("subject_id") || examScheduleData.get("subject_id").getAsInt() == 0) {
                    String errorMessage = "Subject ID is missing.";
                    Response.Message(exchange, 400, errorMessage);
                    return;
                }
                if (!examScheduleData.has("teacher_id") || examScheduleData.get("teacher_id").getAsInt() == 0) {
                    String errorMessage = "Teacher ID is missing.";
                    Response.Message(exchange, 400, errorMessage);
                    return;
                }

                String insertMessage = InsertQuery.insertData(connection, "exam_subjects", examScheduleData);
                if (insertMessage.startsWith("Error")) {
                    Response.Message(exchange, 500, insertMessage);
                } else {
                    Response.Message(exchange, 200, insertMessage);
                }
            });

        }catch (Exception e){
            Response.Message(exchange, 500,  e.getMessage());
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
