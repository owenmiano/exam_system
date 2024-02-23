package ke.co.skyworld.routes;
import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;

import java.sql.Connection;

public class Class {
    public static RoutingHandler classRoutes(Connection connection) {
        return Handlers.routing()
                .post( "/add", exchange -> ke.co.skyworld.controllers.Class.createClass(connection, exchange))
                .put( "/update/{id}",exchange -> ke.co.skyworld.controllers.Class.updateClass(connection, exchange))
                .get( "/find/{id}", exchange -> ke.co.skyworld.controllers.Class.findClassById(connection, exchange))
                .get("/all",exchange -> ke.co.skyworld.controllers.Class.findAll(connection, exchange));
    }
}
