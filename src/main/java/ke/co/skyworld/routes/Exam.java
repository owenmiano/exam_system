package ke.co.skyworld.routes;

import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;

import java.sql.Connection;

public class Exam {
    public static RoutingHandler examRoutes(Connection connection) {
        return Handlers.routing()
                .post( "/add", exchange -> ke.co.skyworld.controllers.Exam.createExam(connection, exchange))
                .put( "/update/{id}",exchange -> ke.co.skyworld.controllers.Exam.updateExam(connection, exchange))
                .get( "/find/{id}", exchange -> ke.co.skyworld.controllers.Exam.findExamById(connection, exchange))
                .get("/all",exchange -> ke.co.skyworld.controllers.Exam.findAll(connection, exchange));
    }
}
