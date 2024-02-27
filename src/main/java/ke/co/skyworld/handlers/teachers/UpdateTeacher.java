package ke.co.skyworld.handlers.teachers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;

import java.sql.Connection;

public class UpdateTeacher implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            // Extracting the teacher ID from the URL path using PathTemplateMatch
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String teacherIdString = pathMatch.getParameters().get("teacherId");

            if (teacherIdString != null && !teacherIdString.trim().isEmpty()) {
                try {
                    int teacherId = Integer.parseInt(teacherIdString);

                    exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                        Gson gson = new Gson();
                        JsonObject teacherData = gson.fromJson(requestBody, JsonObject.class);

                        String whereClause = "teacher_id = ?";

                        String result = GenericQueries.update(connection, "teachers", teacherData, whereClause, teacherId);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange1.getResponseSender().send(result);
                    });
                }catch (NumberFormatException e) {
                    String errorMessage = "Invalid Teacher ID: " + teacherIdString;
                    System.out.println(errorMessage);
                    exchange.getResponseSender().send(errorMessage);
                }
            } else {
                // Handle the case where the teacher ID is missing or empty
                String errorMessage = "Teacher ID is missing or empty in the request URL.";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }

        }catch (Exception e){
            throw e;
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}