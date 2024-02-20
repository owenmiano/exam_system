package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import java.sql.Connection;


public class AnswersController {
    public static void createAnswer(Connection connection, HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
            Gson gson = new Gson();
            JsonObject answerData = gson.fromJson(requestBody, JsonObject.class);

            if (answerData == null || !answerData.has("questions_id") || answerData.get("questions_id").getAsString().trim().isEmpty()) {
                String errorMessage = "Question ID is missing.";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
                return;
            }
            if (answerData == null || !answerData.has("choices_id") || answerData.get("choices_id").getAsString().trim().isEmpty()) {
                String errorMessage = "Choices ID is missing.";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
                return;
            }
            if (answerData == null || !answerData.has("pupils_id") || answerData.get("pupils_id").getAsString().trim().isEmpty()) {
                String errorMessage = "Pupil ID is missing.";
                System.out.println(errorMessage);
                exchange.getResponseSender().send(errorMessage);
                return;
            }


            String insertionResult = GenericQueries.insertData(connection, "answers", answerData);
            System.out.println(insertionResult);
            exchange1.getResponseSender().send(insertionResult);
        });
    }
}
