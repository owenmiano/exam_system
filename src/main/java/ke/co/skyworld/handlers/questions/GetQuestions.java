package ke.co.skyworld.handlers.questions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.utils.Pagination;
import ke.co.skyworld.utils.Response;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class GetQuestions implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {

                Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
                String[] columns = (columnsDeque != null && !columnsDeque.isEmpty()) ? columnsDeque.getFirst().split(",") : new String[]{"*"};

                StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
                Deque<String> filterDeque = exchange.getQueryParameters().get("filter");

                if (filterDeque != null && !filterDeque.isEmpty()) {
                    for (String filter : filterDeque) {
                        // Splitting each filter into its components: field, operation, and value
                        String[] parts = filter.split(":", 3);
                        if (parts.length == 3) {
                            String field = parts[0];
                            String operation = parts[1];
                            String value = parts[2];

                            switch (operation) {
                                case "like":
                                    whereClauseJoiner.add(field + " LIKE '%" + value + "%'");
                                    break;
                                case "eq":
                                    whereClauseJoiner.add(field + " = '" + value + "'");
                                    break;
                                case "begins":
                                    whereClauseJoiner.add(field + " LIKE '" + value + "%'");
                                    break;
                                case "ends":
                                    whereClauseJoiner.add(field + " LIKE '%" + value + "'");
                                    break;

                            }
                        }
                    }
                }

                String whereClause = whereClauseJoiner.toString();

                String table = "questions q " +
                        "JOIN exam_subjects es ON q.exam_subject_id = es.exam_subject_id " +
                        "JOIN subject s ON es.subject_id = s.subject_id " +
                        "JOIN choices ch ON q.questions_id = ch.questions_id ";
                if (!whereClause.isEmpty()) {
                    table += " WHERE " + whereClause;
                }

                try {
                    Pagination pagination = new Pagination(exchange);

                    JsonArray jsonArrayResult = SelectQuery.select(connection, table, columns,pagination.getPageSize(), pagination.calculateOffset());

                    Map<Integer, JsonObject> questionMap = new HashMap<>();
                    for (int i = 0; i < jsonArrayResult.size(); i++) {
                        JsonObject row = jsonArrayResult.get(i).getAsJsonObject();
                        Integer questionId = row.get("questions_id").getAsInt();

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
                    Response.Message(exchange, 500, e.getMessage());
                }
            catch (Exception e) {
                Response.Message(exchange, 500, e.getMessage());
            }
        }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}
