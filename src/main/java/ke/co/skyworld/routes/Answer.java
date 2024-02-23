package ke.co.skyworld.routes;

import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;
import ke.co.skyworld.controllers.Question;

import java.sql.Connection;

public class Answer {
    public static RoutingHandler answerRoutes(Connection connection) {
        return Handlers.routing()
                .post( "/add", exchange -> Question.createChoice(connection, exchange));
    }
}
