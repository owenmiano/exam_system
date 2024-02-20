package org.example;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;

public class Main {
    private static final int PORT = 2000;
    public static void main(String[] args) {
        File configFile = getConfigFile();
        try {
            DatabaseConnectionManager dbManager = new DatabaseConnectionManager(configFile);
            Connection connection = dbManager.getConnection();
                System.out.println("Database connected successfully");
                dbManager.createTables(connection);

                Undertow server = Undertow.builder()
                        .addHttpListener(PORT, "localhost")
                        .setHandler(exchange -> {
                            // Define routes
                            String requestMethod = exchange.getRequestMethod().toString();

                            switch (exchange.getRequestPath()) {
                                case "/create/class":
                                    if ("POST".equalsIgnoreCase(requestMethod)) {
                                        ClassController.createClass(connection, exchange);
                                    } else {
                                        exchange.getResponseSender().send("POST method required for /create/class.");
                                    }
                                    break;
                                case "/update/class":
                                    if ("PUT".equalsIgnoreCase(requestMethod)) {
                                        ClassController.updateClass(connection, exchange);
                                    } else {
                                        exchange.getResponseSender().send("PUT method required for /update/class.");
                                    }
                                    break;
                                case "/select/class":
                                    if ("GET".equalsIgnoreCase(requestMethod)) {
                                        Deque<String> classIdDeque = exchange.getQueryParameters().get("id");
                                        if (classIdDeque != null && !classIdDeque.isEmpty()) {
                                            // check if id exists call findClassById method
                                            ClassController.findClassById(connection, exchange, classIdDeque.getFirst());
                                        } else {
                                                //fetch all classes
                                            ClassController.findAll(connection, exchange);
                                        }
                                    } else {
                                        exchange.getResponseSender().send("GET method required for /select/class.");
                                    }
                                    break;
                                case "/create/teacher":
                                    if ("POST".equalsIgnoreCase(requestMethod)) {
                                        TeacherController.createTeacher(connection, exchange);
                                    } else {
                                        exchange.getResponseSender().send("POST method required for /create/teacher.");
                                    }
                                    break;
                                case "/update/teacher":
                                    if ("PUT".equalsIgnoreCase(requestMethod)) {
                                        TeacherController.updateTeacher(connection, exchange);
                                    } else {
                                        exchange.getResponseSender().send("PUT method required for /update/teacher.");
                                    }
                                    break;
                                case "/select/teacher":
                                    if ("GET".equalsIgnoreCase(requestMethod)) {
                                        Deque<String> teacherIdDeque = exchange.getQueryParameters().get("id");
                                        if (teacherIdDeque != null && !teacherIdDeque.isEmpty()) {
                                            TeacherController.findTeacherById(connection, exchange, teacherIdDeque.getFirst());
                                        } else {
                                            TeacherController.findAll(connection, exchange);
                                        }
                                    } else {
                                        exchange.getResponseSender().send("GET method required for /select/teacher.");
                                    }
                                    break;
                                case "/create/pupil":
                                    if ("POST".equalsIgnoreCase(requestMethod)) {
                                        PupilController.createPupil(connection, exchange);
                                    } else {
                                        exchange.getResponseSender().send("POST method required for /create/pupil.");
                                    }
                                    break;
                                case "/update/pupil":
                                    if ("PUT".equalsIgnoreCase(requestMethod)) {
                                        PupilController.updatePupil(connection, exchange);
                                    } else {
                                        exchange.getResponseSender().send("PUT method required for /update/pupil.");
                                    }
                                    break;
                                case "/select/pupil":
                                    if ("GET".equalsIgnoreCase(requestMethod)) {
                                        Deque<String> pupilIdDeque = exchange.getQueryParameters().get("id");
                                        if (pupilIdDeque != null && !pupilIdDeque.isEmpty()) {
                                            PupilController.findPupilById(connection, exchange, pupilIdDeque.getFirst());
                                        } else {
                                            PupilController.findAll(connection, exchange);
                                        }
                                    } else {
                                        exchange.getResponseSender().send("GET method required for /select/pupil.");
                                    }
                                    break;
                                case "/create/subject":
                                    SubjectController.createSubject(connection, exchange);
                                    break;
                                case "/update/subject":
                                    SubjectController.updateSubject(connection, exchange);
                                    break;
                                case "/select/subject":
                                    if ("GET".equalsIgnoreCase(requestMethod)) {
                                        Deque<String> subjectIdDeque = exchange.getQueryParameters().get("id");
                                        if (subjectIdDeque != null && !subjectIdDeque.isEmpty()) {
                                            SubjectController.findSubjectById(connection, exchange, subjectIdDeque.getFirst());
                                        } else {
                                            SubjectController.findAll(connection, exchange);
                                        }
                                    } else {
                                        exchange.getResponseSender().send("GET method required for /select/subject.");
                                    }
                                    break;
                                case "/create/exam":
                                    ExamController.createExam(connection, exchange);
                                    break;
                                case "/update/exam":
                                    ExamController.updateExam(connection, exchange);
                                    break;
                                case "/select/exam":
                                    if ("GET".equalsIgnoreCase(requestMethod)) {
                                        Deque<String> examIdDeque = exchange.getQueryParameters().get("id");
                                        if (examIdDeque != null && !examIdDeque.isEmpty()) {
                                            ExamController.findExamById(connection, exchange, examIdDeque.getFirst());
                                        } else {
                                            ExamController.findAll(connection, exchange);
                                        }
                                    } else {
                                        exchange.getResponseSender().send("GET method required for /select/exam.");
                                    }
                                    break;
                                case "/create/exam-schedule":
                                    ExamController.createExamSchedule(connection, exchange);
                                    break;
                                case "/update/exam-schedule":
                                    ExamController.updateExamSchedule(connection, exchange);
                                    break;
                                case "/select/exam-schedule":
                                    if ("GET".equalsIgnoreCase(requestMethod)) {
                                        Deque<String> examScheduleIdDeque = exchange.getQueryParameters().get("id");
                                        if (examScheduleIdDeque != null && !examScheduleIdDeque.isEmpty()) {
                                            ExamController.findExamScheduleById(connection, exchange, examScheduleIdDeque.getFirst());
                                        } else {
                                            ExamController.findAllExamSchedules(connection, exchange);
                                        }
                                    } else {
                                        exchange.getResponseSender().send("GET method required for /select/exam-schedule.");
                                    }
                                    break;
                                case "/create/question":
                                    QuestionController.createQuestion(connection, exchange);
                                    break;
                                case "/update/question":
                                    QuestionController.updateQuestion(connection, exchange);
                                    break;
                                case "/select/question":
                                    if ("GET".equalsIgnoreCase(requestMethod)) {
                                        Deque<String> questionIdDeque = exchange.getQueryParameters().get("id");
                                        if (questionIdDeque != null && !questionIdDeque.isEmpty()) {
                                            QuestionController.findQuestionById(connection, exchange, questionIdDeque.getFirst());
                                        } else {
                                            QuestionController.findAllQuestions(connection, exchange);
                                        }
                                    } else {
                                        exchange.getResponseSender().send("GET method required for /select/question.");
                                    }
                                    break;
                                case "/create/choice":
                                    QuestionController.createChoice(connection, exchange);
                                    break;
//                                case "/select/choice":
//                                    QuestionController.findChoices(connection, exchange);
//                                    break;
                                case "/update/choice":
                                    QuestionController.updateChoice(connection, exchange);
                                    break;
                                case "/create/answer":
                                    AnswersController.createAnswer(connection, exchange);
                                    break;
                                case "/report/exams-by-teacher":
                                    ExamReport.generateExamsByTeacher(connection, exchange);
                                    break;
                                case "/report/generate-answers":
                                    ExamReport.generatePupilsAnswers(connection, exchange);
                                    break;
                                case "/report/top-five-results":
                                    ExamReport.generateTopPupilsByScore(connection, exchange);
                                    break;
                                case "/report/score-report":
                                    ExamReport.generatePupilScoreReport(connection, exchange);
                                    break;
                                default:
                                    defaultHandler(exchange);
                                    break;
                            }
                        }).build();

                server.start();
                System.out.println("⚡ Server is running on port:"+ PORT);
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

