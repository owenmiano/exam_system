package ke.co.skyworld.routes;

import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;
import ke.co.skyworld.controllers.Question;

import java.sql.Connection;

public class Choices {
    public static RoutingHandler choicesRoutes(Connection connection) {
        return Handlers.routing()
                .post( "/add", exchange -> Question.createChoice(connection, exchange))
                .put( "/update/{id}",exchange -> Question.updateChoice(connection, exchange))
              //  .get( "/find/{id}", exchange -> org.example.controllers.Question.findQuestionById(connection, exchange))
                .get("/all",exchange -> Question.findAllChoices(connection, exchange));
    }
}
