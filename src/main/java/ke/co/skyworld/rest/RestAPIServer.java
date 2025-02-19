package ke.co.skyworld.rest;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.PathHandler;
import ke.co.skyworld.Model.ConfigReader;
import ke.co.skyworld.rest.base.CORSHandler;
import ke.co.skyworld.utils.ConfigFileChecker;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class RestAPIServer {
    public static void start() {
        try {
            File configFile = ConfigFileChecker.getConfigFile();
            ConfigReader config = new ConfigReader(configFile);

            String HOST = ConfigReader.getServerHost();
            int PORT = ConfigReader.getServerPort();

            String BASE_REST_API_URL = "/api/rest";

            PathHandler pathHandler = Handlers.path()
                .addPrefixPath(BASE_REST_API_URL + "/classes",Routes.Class())
                .addPrefixPath(BASE_REST_API_URL + "/pupils", Routes.Pupil())
                .addPrefixPath(BASE_REST_API_URL + "/teachers", Routes.Teacher())
                .addPrefixPath(BASE_REST_API_URL + "/auth", Routes.Auth())
                .addPrefixPath(BASE_REST_API_URL + "/subjects", Routes.Subject())
                .addPrefixPath(BASE_REST_API_URL + "/exams", Routes.Exam())
                .addPrefixPath(BASE_REST_API_URL + "/exam-schedules", Routes.ExamSchedule())
                .addPrefixPath(BASE_REST_API_URL + "/questions", Routes.Question())
                .addPrefixPath(BASE_REST_API_URL + "/choices", Routes.Choice())
                .addPrefixPath(BASE_REST_API_URL + "/answers", Routes.Answers())
                .addPrefixPath(BASE_REST_API_URL + "/admins", Routes.Admin())
                .addPrefixPath(BASE_REST_API_URL + "/reports", Routes.Report());

                 Undertow server = Undertow.builder()
                    .setServerOption(UndertowOptions.DECODE_URL, true)
                    .setServerOption(UndertowOptions.URL_CHARSET, StandardCharsets.UTF_8.name())
    //                    .setIoThreads(10) //TODO: put in config
                    //.setWorkerThreads(100) //TODO: put in config
                    .addHttpListener(PORT, HOST)
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

