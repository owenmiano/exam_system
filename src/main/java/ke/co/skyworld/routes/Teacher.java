package ke.co.skyworld.routes;

import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;

import java.sql.Connection;

public class Teacher {
    public static RoutingHandler teacherRoutes(Connection connection) {
        return Handlers.routing()
                .post( "/add", exchange -> ke.co.skyworld.controllers.Teacher.createTeacher(connection, exchange))
                .put( "/update/{id}",exchange -> ke.co.skyworld.controllers.Teacher.updateTeacher(connection, exchange))
                .get( "/find/{id}", exchange -> ke.co.skyworld.controllers.Teacher.findTeacherById(connection, exchange))
                .get("/all",exchange -> ke.co.skyworld.controllers.Teacher.findAll(connection, exchange));
    }
}
