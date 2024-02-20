package org.example;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        File configFile = getConfigFile(); // Ensure this method exists and returns a valid File object
        try {
            DatabaseConnectionManager dbManager = new DatabaseConnectionManager(configFile); // Assuming constructor and method signatures
            Connection connection = dbManager.getConnection();
                System.out.println("Database connected successfully");
                dbManager.createTables(connection); // Assuming this method exists

                Undertow server = Undertow.builder()
                        .addHttpListener(2000, "localhost")
                        .setHandler(exchange -> {
                            // Define routes
                            switch (exchange.getRequestPath()) {
                                case "/create/class":
                                    ClassController.createClass(connection, exchange);
                                    break;
                                case "/update/class":
                                    ClassController.updateClass(connection, exchange);
                                    break;
                                case "/select/class":
                                    ClassController.findClass(connection, exchange);
                                    break;
                                case "/create/teacher":
                                    TeacherController.createTeacher(connection, exchange);
                                    break;
                                case "/update/teacher":
                                    TeacherController.updateTeacher(connection, exchange);
                                    break;
                                case "/select/teacher":
                                    TeacherController.findTeacher(connection, exchange);
                                    break;
                                case "/create/pupil":
                                    PupilController.createPupil(connection, exchange);
                                    break;
                                case "/update/pupil":
                                    PupilController.updatePupil(connection, exchange);
                                    break;
                                case "/select/pupil":
                                    PupilController.findPupil(connection, exchange);
                                    break;
                                case "/create/subject":
                                    SubjectController.createSubject(connection, exchange);
                                    break;
                                case "/update/subject":
                                    SubjectController.updateSubject(connection, exchange);
                                    break;
                                case "/select/subject":
                                    SubjectController.findSubject(connection, exchange);
                                    break;
                                case "/create/exam":
                                    ExamController.createExam(connection, exchange);
                                    break;
                                case "/update/exam":
                                    ExamController.updateExam(connection, exchange);
                                    break;
                                case "/select/exam":
                                    ExamController.findExam(connection, exchange);
                                    break;
                                case "/create/exam-schedule":
                                    ExamController.createExamSchedule(connection, exchange);
                                    break;
                                case "/update/exam-schedule":
                                    ExamController.updateExamSchedule(connection, exchange);
                                    break;
                                case "/select/exam-schedule":
                                    ExamController.findExamSchedule(connection, exchange);
                                    break;
                                case "/create/question":
                                    QuestionController.createQuestion(connection, exchange);
                                    break;
                                case "/update/question":
                                    QuestionController.updateQuestion(connection, exchange);
                                    break;
                                case "/select/question":
                                    QuestionController.findQuestion(connection, exchange);
                                    break;
                                case "/create/choice":
                                    QuestionController.createChoice(connection, exchange);
                                    break;
                                case "/select/choice":
                                    QuestionController.findChoice(connection, exchange);
                                    break;
                                case "/update/choice":
                                    QuestionController.updateChoice(connection, exchange);
                                    break;
                                case "/create/answer":
                                    AnswersController.createAnswer(connection, exchange);
                                    break;
                                default:
                                    defaultHandler(exchange);
                                    break;
                            }
                        }).build();

                server.start();
                System.out.println("Server started at http://localhost:2000");
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Failed to initialize the database: " + e.getMessage());
            }
        }



    private static File getConfigFile() {
        Path folderPath = Paths.get("config", "config.xml");
        if (!Files.exists(folderPath)) {
            throw new IllegalArgumentException("Configuration file not found at " + folderPath);
        }
        return folderPath.toFile();
    }
    private static void defaultHandler(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Route not found");
    }


////Report Section
//    //Generate a report on the answers provided by a pupil for an exam and their percentage score in that exam.
//private static void pupilAnswers(Connection connection) throws SQLException {
//
//    int pupilId = 1; // Example pupilId
//    int examSubject = 2; // Example examId
//
//    ExamReport.generatePupilsAnswers(connection, pupilId, examSubject);
//
// }
//
// //Display all the exams set by a teacher
//    private static void displayExams(Connection connection) throws SQLException {
//
//        int teacherId = 1;
//
//        ExamReport.generateExamsByTeacher(connection, teacherId);
//    }
//
//    //Generate a report on the top 5 pupils with the highest scores in a certain exam.
//    private static void topFivePupils(Connection connection) throws SQLException {
//
//        int examSubject = 1;
//
//        ExamReport.generateTopPupilsByScore(connection, examSubject);
//    }
//
//    //Generate a report sheet of the scores for all pupils in each of the exams done and rank them from the highest average score to lowest.
//    private static void allPupilsScores(Connection connection) throws SQLException {
//
//        int examId = 1;
//
//        ExamReport.generatePupilScoreReport(connection, examId);
//    }
}

