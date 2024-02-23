package ke.co.skyworld.routes;

import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;
import ke.co.skyworld.controllers.Exam;

import java.sql.Connection;

public class ExamSchedule {
    public static RoutingHandler examScheduleRoutes(Connection connection) {
        return Handlers.routing()
                .post( "/add", exchange -> Exam.createExamSchedule(connection, exchange))
                .put( "/update/{id}",exchange -> Exam.updateExamSchedule(connection, exchange))
                .get( "/find/{id}", exchange -> Exam.findExamScheduleById(connection, exchange))
                .get("/all",exchange -> Exam.findAllExamSchedules(connection, exchange));
    }
}
