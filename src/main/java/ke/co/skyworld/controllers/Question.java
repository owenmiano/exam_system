package ke.co.skyworld.controllers;

import ke.co.skyworld.queryBuilder.GenericQueries;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class Question {


    public static void findQuestionById(Connection connection, HttpServerExchange exchange) {
        // Extracting the question ID from the URL path using PathTemplateMatch
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        String examSubjectIdString = pathMatch.getParameters().get("examSubjectId");
        Deque<String> questionIdDeque = exchange.getQueryParameters().get("questionId");

        if (questionIdDeque == null && questionIdDeque.isEmpty()) {
            exchange.getResponseSender().send("Question id is required");
            return;
        }
        String questionIdString=questionIdDeque.getFirst();
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
    }

    public static void findAllQuestions(Connection connection, HttpServerExchange exchange) {
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        String examSubjectIdString = pathMatch.getParameters().get("examSubjectId");
        try {
            int examsubjectId = Integer.parseInt(examSubjectIdString);
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


            String whereClause = "q.exam_subject_id = ?";

            Object[] values = new Object[]{examsubjectId};

            try {
                JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns,whereClause,values);

                // New aggregation logic
                Map<Integer, JsonObject> questionMap = new HashMap<>();
                for (int i = 0; i < jsonArrayResult.size(); i++) {
                    JsonObject row = jsonArrayResult.get(i).getAsJsonObject(); // Adjusted for Gson
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
                String errorMessage = "SQL Error occurred: " + e.getMessage();
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } catch (Exception e) {
            String errorMessage = "An error occurred: " + e.getMessage();
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }

    public static void createQuestion(Connection connection, HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
            Gson gson = new Gson();
            JsonObject questionData = gson.fromJson(requestBody, JsonObject.class);

            if (!questionData.has("question_no") || questionData.get("question_no").isJsonNull()) {
                System.out.println("Question number is missing.");
                return;
            }

            if (!questionData.has("description") || questionData.get("description").isJsonNull() || questionData.get("description").getAsString().trim().isEmpty()) {
                System.out.println("Description cannot be empty.");
                return;
            }

            if (!questionData.has("marks") || questionData.get("marks").isJsonNull()) {
                System.out.println("Marks is missing.");
                return;
            }

            String insertionResult = GenericQueries.insertData(connection, "questions", questionData);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            System.out.println(insertionResult);
            exchange.getResponseSender().send(insertionResult);
        });
    }

    public static void updateQuestion(Connection connection, HttpServerExchange exchange) {
        Deque<String> questionIdDeque = exchange.getQueryParameters().get("id");
        if (questionIdDeque != null && !questionIdDeque.isEmpty()) {
            String questionIdString = questionIdDeque.getFirst();

            try {
                int questionId = Integer.parseInt(questionIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();
                    JsonObject questionData = gson.fromJson(requestBody, JsonObject.class);

                    String whereClause = "questions_id = ?";

                    String result = GenericQueries.update(connection, "questions", questionData, whereClause, questionId);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange.getResponseSender().send(result);

                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid question ID: " + questionIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } else {
            // Handle the case where the "id" parameter is missing
            String errorMessage = "Question ID is missing in the request URL.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }

    public static void createChoice(Connection connection, HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
            Gson gson = new Gson();
            JsonObject choiceData = gson.fromJson(requestBody, JsonObject.class);

            if (!choiceData.has("option_label") || choiceData.get("option_label").isJsonNull()) {
                System.out.println("Option label is missing.");
                return;
            }

            if (!choiceData.has("option_value") ||  choiceData.get("option_value").getAsString().trim().isEmpty()) {
                System.out.println("Option value cannot be empty.");
                return;
            }


            String insertionResult = GenericQueries.insertData(connection, "choices", choiceData);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange1.getResponseSender().send(insertionResult);
        });
    }

    public static void findAllChoices(Connection connection, HttpServerExchange exchange) {
        try {
            String[] columns = {
                    "q.question_no",
                    "q.description",
                    "q.marks",
                    "ch.option_label",
                    "ch.option_value"

            };

            String table = "choices ch " +
                    "JOIN questions q ON ch.questions_id = q.questions_id " ;

            try {
                JsonArray jsonArrayResult = GenericQueries.select(connection, table, columns);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(jsonArrayResult.toString());
            } catch (SQLException e) {
                String errorMessage = "SQL Error occurred: " + e.getMessage();
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        }  catch (Exception e) {
            String errorMessage = "An error occurred: " + e.getMessage();
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }
//
//    public static void findChoicesById(Connection connection, HttpServerExchange exchange) {
//
//    }

    public static void updateChoice(Connection connection, HttpServerExchange exchange) {
        Deque<String> choiceIdDeque = exchange.getQueryParameters().get("id");
        if (choiceIdDeque != null && !choiceIdDeque.isEmpty()) {
            String choiceIdString = choiceIdDeque.getFirst();

            try {
                int choicesId = Integer.parseInt(choiceIdString);

                exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                    Gson gson = new Gson();
                    JsonObject questionData = gson.fromJson(requestBody, JsonObject.class);

                    String whereClause = "choices_id = ?";

                    String result = GenericQueries.update(connection, "choices", questionData, whereClause, choicesId);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(result);

                });
            } catch (NumberFormatException e) {
                String errorMessage = "Invalid choices ID: " + choiceIdString;
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
            }
        } else {
            // Handle the case where the "id" parameter is missing
            String errorMessage = "Choices ID is missing in the request URL.";
            System.out.println(errorMessage);
            exchange.getResponseSender().send(errorMessage);
        }
    }
}
