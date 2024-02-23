package ke.co.skyworld.routes;

import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;

import java.sql.Connection;

public class Pupil {
    public static RoutingHandler pupilRoutes(Connection connection) {
        return Handlers.routing()
                .post( "/add", exchange -> ke.co.skyworld.controllers.Pupil.createPupil(connection, exchange))
                .put( "/update/{id}",exchange -> ke.co.skyworld.controllers.Pupil.updatePupil(connection, exchange))
                .get( "/find/{id}", exchange -> ke.co.skyworld.controllers.Pupil.findPupilById(connection, exchange))
                .get("/all",exchange -> ke.co.skyworld.controllers.Pupil.findAll(connection, exchange));
    }
}
