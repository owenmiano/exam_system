package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Date;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExamReport {


    public static void generateExamsByTeacher(Connection connection, int teacherId) {
        try {
            String[] columns = {
                    "e.exam_name",
                    "es.exam_date",
                    "t.teacher_name",
                    "s.subject_name",
                    "cl.class_name"
            };

            // Adjust the joins to the new format expected by the updated select method
            String[][] joins = {
                    {"INNER", "exam_subjects es", "e.exam_id = es.exam_id"},
                    {"INNER", "teachers t", "es.teacher_id = t.teacher_id"},
                    {"INNER", "subject s", "es.subject_id = s.subject_id"},
                    {"INNER", "class cl", "e.class_id = cl.class_id"}
            };

            String whereClause = "t.teacher_id = ?";
            Object[] values = new Object[]{teacherId};

            // Execute the updated select method with multiple joins and a parameterized where clause
            JsonArray jsonArrayResult = GenericQueries.select(connection, "exam e", joins, columns, whereClause, values);

            // Convert the result to JSON String and print
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void generatePupilsAnswers(Connection connection, int pupilId, int examSubject) {
        String dbType= DatabaseConnectionManager.DatabaseConfig.getDbType();
        try {
            String isCorrectColumn = dbType.equalsIgnoreCase("postgresql") ?
                    "CASE WHEN c.correct = true THEN 'correct' ELSE 'incorrect' END AS is_correct" :
                    "CASE WHEN c.correct = 1 THEN 'correct' ELSE 'incorrect' END AS is_correct";

            String scoreColumn = dbType.equalsIgnoreCase("postgresql") ?
                    "CASE WHEN c.correct = true THEN q.marks ELSE 0 END AS score" :
                    "CASE WHEN c.correct = 1 THEN q.marks ELSE 0 END AS score";

            // Construct the columns array dynamically with the adjusted 'is_correct' and 'score' columns
            String[] columns = {
                    "p.pupil_name AS pupil",
                    "p.reg_no AS registration_number",
                    "cl.class_name AS class",
                    "s.subject_name AS subject",
                    "e.exam_name AS exam",
                    "q.question_no",
                    "q.description AS question",
                    "c.option_value AS chosen_answer",
                    isCorrectColumn, // Use the dynamically determined column definition
                    scoreColumn // Use the dynamically determined column definition
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
            // Execute the updated select method with multiple joins and a parameterized where clause
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

            Gson gson = new Gson();
            String jsonString = gson.toJson(answersReport);
            System.out.println(jsonString);

        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void generateTopPupilsByScore(Connection connection, int examSubject) {
        try {
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

            JsonArray top5Pupils = new JsonArray();
            for (int i = 0; i < Math.min(5, pupilsList.size()); i++) {
                top5Pupils.add(pupilsList.get(i));
            }

            Gson gson = new Gson();
            String jsonString = gson.toJson(top5Pupils);
            System.out.println(jsonString);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public static void generatePupilScoreReport(Connection connection, int examId) {
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

            Object[] params = new Object[]{examId};

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
            }

            workbook.close();

            System.out.println("Pupil Score Report generated successfully.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

}
