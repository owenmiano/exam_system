package ke.co.skyworld.routes;

import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;

import java.sql.Connection;

public class Subject {
    public static RoutingHandler subjectRoutes(Connection connection) {
        return Handlers.routing()
                .post( "/add", exchange -> ke.co.skyworld.controllers.Subject.createSubject(connection, exchange))
                .put( "/update/{id}",exchange -> ke.co.skyworld.controllers.Subject.updateSubject(connection, exchange))
                .get( "/find/{id}", exchange -> ke.co.skyworld.controllers.Subject.findSubjectById(connection, exchange))
                .get("/all",exchange -> ke.co.skyworld.controllers.Subject.findAll(connection, exchange));
    }
}
