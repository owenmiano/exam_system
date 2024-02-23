package ke.co.skyworld.routes;

import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;

import java.sql.Connection;

public class Question {
    public static RoutingHandler questionRoutes(Connection connection) {
        return Handlers.routing()
                .post( "/add", exchange -> ke.co.skyworld.controllers.Question.createQuestion(connection, exchange))
                .put( "/update/{id}",exchange -> ke.co.skyworld.controllers.Question.updateQuestion(connection, exchange))
                .get( "/find/{examSubjectId}", exchange -> ke.co.skyworld.controllers.Question.findQuestionById(connection, exchange))
                .get("/all/{examSubjectId}",exchange -> ke.co.skyworld.controllers.Question.findAllQuestions(connection, exchange));
    }
}
