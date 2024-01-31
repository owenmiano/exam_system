package org.example;
import java.sql.*;
import java.util.HashMap;

public class ClassSubjectController {

    public static void selectAll(Connection connection) {
        String sql = "SELECT * FROM class_subject";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            String rowFormat = "| %-10s | %-20s |%n";

// Calculate the length of a single row and create the border line
            int rowLength = String.format(rowFormat, "", "").length();
            String border = "+" + new String(new char[rowLength - 2]).replace("\0", "-") + "+";

// Print the top border
            System.out.println(border);

// Print the header of the table
            System.out.printf(rowFormat, "Class ID", "Subject ID");

// Print the separator line
            System.out.println(border);

            while (rs.next()) {
                // Extract data from result set
                int id = rs.getInt("class_id");
                String className = rs.getString("subject_id");

                // Display data in table format
                System.out.printf(rowFormat, id, className);

                // Optionally, print a separator line after each row
                System.out.println(border);
            }
// Print the bottom border
            System.out.println(border);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createClassSubject(Connection connection, HashMap<String, Object> classSubjectData) {
        if (classSubjectData == null) {
            System.out.println("classSubjectData data is missing or incomplete.");
            return;
        }
        // Check if className is null or empty
        String classIdStr = classSubjectData.get("class_id").toString();
        String subjectIdStr = classSubjectData.get("subject_id").toString();

        if (classIdStr.trim().isEmpty() && subjectIdStr.trim().isEmpty()) {
            System.out.println("Class ID and Subject ID cannot be empty.");
            return;
        }

        int classId = 0;
        int subjectId = 0;

        if (!classIdStr.trim().isEmpty()) {
            try {
                classId = Integer.parseInt(classIdStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid Class ID format. Please enter a valid integer for Class ID.");
                return;
            }
        }

        if (!subjectIdStr.trim().isEmpty()) {
            try {
                subjectId = Integer.parseInt(subjectIdStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid Subject ID format. Please enter a valid integer for Subject ID.");
                return;
            }
        }


        boolean isInserted = GenericQueries.insertData(connection, "class_subjects", classSubjectData); // Replace "class" with your actual table name

        if (isInserted) {
            System.out.println("Class-Subject relationship added successfully");
        } else {
            System.out.println("Failed to add Class-Subject relationship");
        }
    }



}
