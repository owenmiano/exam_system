package ke.co.skyworld.routes;

import io.undertow.Handlers;
import io.undertow.server.RoutingHandler;

import java.sql.Connection;

public class Report {
    public static RoutingHandler reportRoutes(Connection connection) {
        return Handlers.routing()
                .get( "/exams-by-teacher/{id}", exchange -> ke.co.skyworld.controllers.Report.generateExamsByTeacher(connection, exchange))
                .get( "/generate-answers/{id}",exchange -> ke.co.skyworld.controllers.Report.generatePupilsAnswers(connection, exchange))
                .get( "/top-five-results/{id}", exchange -> ke.co.skyworld.controllers.Report.generateTopFivePupils(connection, exchange))
                .get("/score-report/{exam}",exchange -> ke.co.skyworld.controllers.Report.generatePupilScoreReport(connection, exchange));
    }
}
