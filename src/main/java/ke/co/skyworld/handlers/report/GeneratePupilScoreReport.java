package ke.co.skyworld.handlers.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import ke.co.skyworld.Model.ConfigReader;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.utils.Responses;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GeneratePupilScoreReport implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.getConnection();
        try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String examIdString = pathMatch.getParameters().get("examId");

            if (examIdString == null) {
                Responses.Message(exchange, 400,"Exam ID is required.");
                return;
            }
            int exam = Integer.parseInt(examIdString);
            String dbType= ConfigReader.getDbType();
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

                JsonArray aggregatedResults = SelectQuery.select(connection, "pupils p", joins, columns, where, groupBy,params);

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
                if (aggregatedResults.isEmpty()) {
                    JsonObject jsonObject = aggregatedResults.get(0).getAsJsonObject();
                    className = jsonObject.get("class_name").getAsString();
                }
                String examName = "";
                if (aggregatedResults.isEmpty()) {
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
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
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
                            Responses.Message(exchange, 500,  e.getMessage());
                        }
                    });
                } else {
                    Responses.Message(exchange, 400,"Report file not found.");
                }

            } catch (SQLException | IOException e) {
                Responses.Message(exchange, 500,  e.getMessage());
            }
        }
        finally {
            if (connection != null) {
                ConnectDB.shutdown();
            }
        }
    }
}





