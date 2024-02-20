package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExamReport {

    public static void generateExamsByTeacher(Connection connection, HttpServerExchange exchange) {
        Deque<String> teacherIdDeque = exchange.getQueryParameters().get("teacherId");
        if (teacherIdDeque == null || teacherIdDeque.isEmpty()) {
            exchange.getResponseSender().send("Teacher ID is required.");
            return;
        }

        String teacherIdString = teacherIdDeque.getFirst();
        try {
            int teacherId = Integer.parseInt(teacherIdString);
            String[] columns = {
                    "e.exam_name",
                    "es.exam_date",
                    "t.teacher_name",
                    "s.subject_name",
                    "cl.class_name"
            };

            String[][] joins = {
                    {"INNER", "exam_subjects es", "e.exam_id = es.exam_id"},
                    {"INNER", "teachers t", "es.teacher_id = t.teacher_id"},
                    {"INNER", "subject s", "es.subject_id = s.subject_id"},
                    {"INNER", "class cl", "e.class_id = cl.class_id"}
            };

            String whereClause = "t.teacher_id = ?";
            Object[] values = new Object[]{teacherId};

            JsonArray jsonArrayResult = GenericQueries.select(connection, "exam e", joins, columns, whereClause, values);
            String jsonResult = jsonArrayResult.toString();

            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(jsonResult);

        } catch (NumberFormatException e) {
            exchange.getResponseSender().send("Invalid Teacher ID: " + teacherIdString);
        } catch (SQLException e) {
            exchange.getResponseSender().send("SQL Error occurred: " + e.getMessage());
        } catch (Exception e) {
            exchange.getResponseSender().send("An error occurred: " + e.getMessage());
        }
    }




    public static void generatePupilsAnswers(Connection connection, HttpServerExchange exchange) {
        Deque<String> pupilIdDeque = exchange.getQueryParameters().get("pupil");
        Deque<String> examSubjectDeque = exchange.getQueryParameters().get("examSubject");

        if (pupilIdDeque == null || pupilIdDeque.isEmpty() || examSubjectDeque == null || examSubjectDeque.isEmpty()) {
            exchange.getResponseSender().send("Pupil ID and Exam Subject ID are required.");
            return;
        }
        try {
            int pupilId = Integer.parseInt(pupilIdDeque.getFirst());
            int examSubject = Integer.parseInt(examSubjectDeque.getFirst());
            String dbType= DatabaseConnectionManager.DatabaseConfig.getDbType();

            String isCorrectColumn = dbType.equalsIgnoreCase("postgresql") ?
                    "CASE WHEN c.correct = true THEN 'correct' ELSE 'incorrect' END AS is_correct" :
                    "CASE WHEN c.correct = 1 THEN 'correct' ELSE 'incorrect' END AS is_correct";

            String scoreColumn = dbType.equalsIgnoreCase("postgresql") ?
                    "CASE WHEN c.correct = true THEN q.marks ELSE 0 END AS score" :
                    "CASE WHEN c.correct = 1 THEN q.marks ELSE 0 END AS score";

            String[] columns = {
                    "p.pupil_name AS pupil",
                    "p.reg_no AS registration_number",
                    "cl.class_name AS class",
                    "s.subject_name AS subject",
                    "e.exam_name AS exam",
                    "q.question_no",
                    "q.description AS question",
                    "c.option_value AS chosen_answer",
                    isCorrectColumn,
                    scoreColumn
            };

            String[][] joins = {
                    {"INNER", "choices c", "a.choices_id = c.choices_id"},
                    {"INNER", "questions q", "a.questions_id = q.questions_id"},
                    {"INNER", "exam_subjects es", "q.exam_subject_id = es.exam_subject_id"},
                    {"INNER", "exam e", "es.exam_id = e.exam_id"},
                    {"INNER", "subject s", "es.subject_id = s.subject_id"},
                    {"INNER", "pupils p", "a.pupils_id = p.pupils_id"},
                    {"INNER", "class cl", "p.class_id = cl.class_id"}
            };

            String whereClause = "p.pupils_id = ? AND es.exam_subject_id = ?";
            Object[] params = new Object[]{pupilId, examSubject};
            JsonArray answersReport = GenericQueries.select(connection, "answers a", joins, columns, whereClause, params);

            int totalScore = 0;
            for (int i = 0; i < answersReport.size(); i++) {
                JsonObject answer = answersReport.get(i).getAsJsonObject();
                int score = answer.has("score") ? answer.get("score").getAsInt() : 0;
                totalScore += score;
            }

            JsonObject totalScoreObject = new JsonObject();
            totalScoreObject.addProperty("total_score", totalScore);
            answersReport.add(totalScoreObject);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(answersReport.toString());

        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void generateTopPupilsByScore(Connection connection, HttpServerExchange exchange) {
        Deque<String> examSubjectDeque = exchange.getQueryParameters().get("examSubject");

        if (examSubjectDeque == null) {
            exchange.getResponseSender().send("Exam Subject ID is required.");
            return;
        }
        try {
            int examSubject = Integer.parseInt(examSubjectDeque.getFirst());
            String dbType = DatabaseConnectionManager.DatabaseConfig.getDbType();

            String isCorrectColumn = dbType.equalsIgnoreCase("postgresql") ?
                    "SUM(CASE WHEN c.correct = true THEN q.marks ELSE 0 END) AS total_score" :
                    "SUM(CASE WHEN c.correct = 1 THEN q.marks ELSE 0 END) AS total_score";

            String[] columns = {
                    "p.pupil_name AS pupil",
                    "e.exam_name AS exam",
                    "s.subject_name AS subject",
                    "p.reg_no AS registration_number",
                    "cl.class_name AS class",
                    isCorrectColumn
            };

            String[][] joins = {
                    {"INNER", "choices c", "a.choices_id = c.choices_id"},
                    {"INNER", "questions q", "a.questions_id = q.questions_id"},
                    {"INNER", "pupils p", "a.pupils_id = p.pupils_id"},
                    {"INNER", "exam_subjects es", "q.exam_subject_id = es.exam_subject_id"},
                    {"INNER", "exam e", "es.exam_id = e.exam_id"},
                    {"INNER", "subject s", "es.subject_id = s.subject_id"},
                    {"INNER", "class cl", "p.class_id = cl.class_id"}
            };

            String whereClause = "es.exam_subject_id = ?";
            Object[] values = new Object[]{examSubject};

            String groupBy = "p.pupil_name, e.exam_name, s.subject_name, p.reg_no, cl.class_name"; // Define your GROUP BY clause here
            JsonArray aggregatedResults = GenericQueries.select(connection, "answers a", joins, columns, whereClause,groupBy,values);

            List<JsonObject> pupilsList = new ArrayList<>();
            for (JsonElement element : aggregatedResults) {
                pupilsList.add(element.getAsJsonObject());
            }

            pupilsList.sort((a, b) -> {
                int scoreA = a.get("total_score").getAsInt();
                int scoreB = b.get("total_score").getAsInt();
                return Integer.compare(scoreB, scoreA);
            });

            JsonArray topFivePupils = new JsonArray();
            for (int i = 0; i < Math.min(5, pupilsList.size()); i++) {
                topFivePupils.add(pupilsList.get(i));
            }
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(topFivePupils.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public static void generatePupilScoreReport(Connection connection, HttpServerExchange exchange) {
        Deque<String> examDeque = exchange.getQueryParameters().get("exam");

        if (examDeque == null) {
            exchange.getResponseSender().send("Exam ID is required.");
            return;
        }
        int exam = Integer.parseInt(examDeque.getFirst());
        String dbType= DatabaseConnectionManager.DatabaseConfig.getDbType();
        String scoreColumn = dbType.equalsIgnoreCase("postgresql") ?
                "SUM(CASE WHEN c.correct = true THEN q.marks ELSE 0 END) AS score" :
                "SUM(CASE WHEN c.correct = 1 THEN q.marks ELSE 0 END) AS score";
        try {
            String[] columns = {
                    "p.pupil_name AS pupil_name",
                    "e.exam_name AS exam_name",
                    "cl.class_name AS class_name",
                    "s.subject_name AS subject_name",
                    scoreColumn
            };

            String[][] joins = {
                    {"INNER", "answers a", "p.pupils_id = a.pupils_id"},
                    {"INNER", "choices c", "a.choices_id = c.choices_id"},
                    {"INNER", "questions q", "a.questions_id = q.questions_id"},
                    {"INNER", "exam_subjects es", "q.exam_subject_id = es.exam_subject_id"},
                    {"INNER", "exam e", "es.exam_id = e.exam_id"},
                    {"INNER", "class cl", "e.class_id = cl.class_id"},
                    {"INNER", "subject s", "es.subject_id = s.subject_id"}
            };
            String where = "e.exam_id = ?";
            String groupBy = "p.pupil_name, s.subject_name,cl.class_name,e.exam_name";

            Object[] params = new Object[]{exam};

            JsonArray aggregatedResults = GenericQueries.select(connection, "pupils p", joins, columns, where, groupBy,params);

            Map<String, Map<String, Integer>> pupilScoresMap = new LinkedHashMap<>();
            for (JsonElement element : aggregatedResults) {
                JsonObject jsonObject = element.getAsJsonObject();
                String pupilName = jsonObject.get("pupil_name").getAsString();
                String subjectName = jsonObject.get("subject_name").getAsString();
                BigDecimal subjectScore = new BigDecimal(jsonObject.get("score").getAsString());

                int score = subjectScore.setScale(0, RoundingMode.HALF_UP).intValue();
                pupilScoresMap.computeIfAbsent(pupilName, k -> new LinkedHashMap<>()).put(subjectName, score);
            }
            String className = "";
            if (aggregatedResults.size() > 0) {
                JsonObject jsonObject = aggregatedResults.get(0).getAsJsonObject();
                className = jsonObject.get("class_name").getAsString();
            }
            String examName = "";
            if (aggregatedResults.size() > 0) {
                JsonObject jsonObject = aggregatedResults.get(0).getAsJsonObject();
                examName = jsonObject.get("exam_name").getAsString();
            }

            Map<String, Integer> totalScores = new LinkedHashMap<>();
            Map<String, Double> averageScores = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Integer>> entry : pupilScoresMap.entrySet()) {
                int totalScore = 0;
                for (Integer score : entry.getValue().values()) {
                    totalScore += score;
                }
                totalScores.put(entry.getKey(), totalScore);
                averageScores.put(entry.getKey(), totalScore / (double) entry.getValue().size());
            }

            List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(averageScores.entrySet());
            sortedEntries.sort(Map.Entry.<String, Double>comparingByValue().reversed());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
            String timestamp = dateFormat.format(new Date());
            String newExcelFileName = "reports/" + timestamp+ "_report" + ".xlsx";
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(examName + " - "+className + " - Report");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Position");
            headerRow.createCell(1).setCellValue("Pupil Name");

            if (!pupilScoresMap.isEmpty()) {
                int headerIndex = 2;
                for (String subject : pupilScoresMap.values().iterator().next().keySet()) {
                    headerRow.createCell(headerIndex++).setCellValue(subject);
                }
            }

            int headerIndex = headerRow.getLastCellNum();
            headerRow.createCell(headerIndex++).setCellValue("Total Score");
            headerRow.createCell(headerIndex).setCellValue("Average Score");

            int rowNum = 1;
            int currentPosition = 1;
            for (Map.Entry<String, Double> entry : sortedEntries) {
                String pupilName = entry.getKey();
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(currentPosition++);
                row.createCell(1).setCellValue(pupilName);

                int cellIndex = 2;
                Map<String, Integer> scores = pupilScoresMap.get(pupilName);
                for (Integer score : scores.values()) {
                    row.createCell(cellIndex++).setCellValue(score);
                }

                Integer totalScore = totalScores.get(pupilName);
                Double averageScore = entry.getValue();
                row.createCell(cellIndex++).setCellValue(totalScore);
                row.createCell(cellIndex).setCellValue(Double.parseDouble(String.format("%.1f", averageScore)));
            }

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream outputStream = new FileOutputStream(newExcelFileName)) {
                workbook.write(outputStream);
            } finally {
                if (workbook != null) {
                    workbook.close(); // Ensure the workbook is closed to free resources
                }
            }

            File file = new File(newExcelFileName);
            if (file.exists()) {
                exchange.dispatch(() -> {
                    // Correctly configure the response headers before entering blocking mode.
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/octet-stream");
                    exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");

                    exchange.startBlocking();

                    try (InputStream inputStream = new FileInputStream(file);
                         OutputStream outputStream = exchange.getOutputStream()) {
                        byte[] buf = new byte[8192];
                        int length;
                        while ((length = inputStream.read(buf)) > 0) {
                            outputStream.write(buf, 0, length);
                        }
                        outputStream.flush(); // Make sure all data is sent.
                    } catch (IOException e) {
                        e.printStackTrace();
                        exchange.setStatusCode(500);
                        exchange.getResponseSender().send("Error occurred while sending the report.");
                    }
                });
            } else {
                exchange.setStatusCode(404);
                exchange.getResponseSender().send("Report file not found.");
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            exchange.getResponseSender().send("Error: "+e.getMessage());
        }
    }

}
