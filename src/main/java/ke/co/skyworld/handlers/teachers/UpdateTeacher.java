package ke.co.skyworld.handlers.teachers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.UpdateQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;

public class UpdateTeacher implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();
        try {
            // Extracting the teacher ID from the URL path using PathTemplateMatch
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String teacherIdString = pathMatch.getParameters().get("teacherId");

            if (teacherIdString != null && !teacherIdString.trim().isEmpty()) {
                    int teacherId = Integer.parseInt(teacherIdString);

                    exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                        Gson gson = new Gson();
                        JsonObject teacherData = gson.fromJson(requestBody, JsonObject.class);

                        String whereClause = "teacher_id = ?";

                        String updateMessage = UpdateQuery.update(connection, "teachers", teacherData, whereClause, teacherId);
                        if (updateMessage.startsWith("Error")) {
                            Responses.Message(exchange, 500, updateMessage);
                        } else {
                            Responses.Message(exchange, 200, updateMessage);
                        }
                    });

            } else {
                // Handle the case where the teacher ID is missing or empty
                String errorMessage = "Teacher ID is missing .";
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