package ke.co.skyworld.handlers.teachers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.utils.Response;

import java.sql.Connection;
import java.sql.SQLException;

public class GetTeacher implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();

        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String teacherIdString = pathMatch.getParameters().get("teacherId");
            if (teacherIdString == null || teacherIdString.isEmpty()) {
                String errorMessage = "Teacher ID must be provided.";
                Response.Message(exchange, 400, errorMessage);
                return;
            }

            String[] columns = {
                    "t.teacher_name",
                    "t.tsc_number",
                    "t.id_number",
                    "t.kra_pin",
                    "t.phone",
                    "t.email",
                    "t.date_of_birth",
                    "t.username",
                    "cl.class_name",
            };

            // Specify the table and any joins
            String table = "teachers t JOIN class cl ON t.class_id = cl.class_id";
            int teacherId = Integer.parseInt(teacherIdString);

            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {

                String whereClause = "teacher_id = ?";

                try {
                    JsonArray jsonArrayResult = SelectQuery.select(connection, table, columns, whereClause, teacherId);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

                    if (jsonArrayResult.size() == 0) {
                        String errorMessage = "Teacher not found";
                        Response.Message(exchange, 404, errorMessage);
                    } else if (jsonArrayResult.size() == 1) {
                        JsonObject jsonObjectResult = jsonArrayResult.get(0).getAsJsonObject();
                        exchange.setStatusCode(200);
                        exchange.getResponseSender().send(jsonObjectResult.toString());
                    } else {
                        exchange.setStatusCode(200);
                        exchange.getResponseSender().send(jsonArrayResult.toString());
                    }
                } catch (SQLException e) {
                    Response.Message(exchange, 500,  e.getMessage());
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