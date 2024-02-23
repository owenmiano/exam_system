package ke.co.skyworld.rest;

import ke.co.skyworld.routes.*;
import ke.co.skyworld.routes.Class;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.PathHandler;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.db.DatabaseConnectionManager;
import ke.co.skyworld.rest.base.CORSHandler;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;


public class RestAPIServer {
    public static void start() {
        try {
            // Initialize the database and obtain the connection
            Connection connection = ConnectDB.initializeDatabase();
            if (connection == null) {
                System.err.println("Failed to establish database connection.");
                return;
            }
            String HOST = DatabaseConnectionManager.ConfigDetails.getServerHost();
            int PORT = DatabaseConnectionManager.ConfigDetails.getServerPort();
            String BASE_REST_API_URL = "/api/rest";

            PathHandler pathHandler = Handlers.path()
                    .addPrefixPath(BASE_REST_API_URL + "/class", Class.classRoutes(connection))
                    .addPrefixPath(BASE_REST_API_URL + "/pupil", Pupil.pupilRoutes(connection))
                    .addPrefixPath(BASE_REST_API_URL + "/teacher", Teacher.teacherRoutes(connection))
                    .addPrefixPath(BASE_REST_API_URL + "/subject", Subject.subjectRoutes(connection))
                    .addPrefixPath(BASE_REST_API_URL + "/exam", Exam.examRoutes(connection))
                    .addPrefixPath(BASE_REST_API_URL + "/exam-schedule", ExamSchedule.examScheduleRoutes(connection))
                    .addPrefixPath(BASE_REST_API_URL + "/question", Question.questionRoutes(connection))
                    .addPrefixPath(BASE_REST_API_URL + "/choices", Choices.choicesRoutes(connection))
                    .addPrefixPath(BASE_REST_API_URL + "/answer", Answer.answerRoutes(connection))
                    .addPrefixPath(BASE_REST_API_URL + "/report", Report.reportRoutes(connection));

            Undertow server = Undertow.builder()
                    .setServerOption(UndertowOptions.DECODE_URL, true)
                    .setServerOption(UndertowOptions.URL_CHARSET, StandardCharsets.UTF_8.name())
                    .setIoThreads(10) //TODO: put in config
                    .setWorkerThreads(100) //TODO: put in config
                    .addHttpListener(PORT, HOST) //TODO: put in config
                    .setHandler(new CORSHandler(pathHandler))
                    .build();

            server.start();
            System.out.println("Rest API Server started at:"+HOST+":"+PORT);
        } catch (Exception e) {
            System.err.println("Error starting RestAPIServer: (" + e.getMessage() + ")");
            System.exit(-1);
        }
    }
}

