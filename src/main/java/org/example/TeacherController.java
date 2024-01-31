package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public class TeacherController {
    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }

    private static boolean isValidIDNumber(String idNumber) {
        String idNumberRegex = "^\\d{8}$"; // Regular expression for exactly 8 digits
        return idNumber != null && idNumber.matches(idNumberRegex);
    }
    private static boolean isValidPhoneNumber(String phoneNumber) {
        String phoneNumberRegex = "^\\d{10}$"; // Regular expression for exactly 10 digits
        return phoneNumber != null && phoneNumber.matches(phoneNumberRegex);
    }


    public static void findTeacher(Connection connection, HashMap<String, Object> teacherData, String[] columns) {
        try {
            if (teacherData == null || teacherData.isEmpty()) {
                System.out.println("No teacher data provided.");
                return;
            }

            StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
            ArrayList<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : teacherData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                whereClauseJoiner.add(key + " = ?");
                values.add(value);
            }

            String whereClause = whereClauseJoiner.toString();
            JsonArray jsonArrayResult = GenericQueries.select(connection, "teachers", columns, whereClause, values.toArray());
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void createTeacher(Connection connection, HashMap<String, Object> teacherData) {
        try {
            if (teacherData == null) {
                System.out.println("Class data is missing or incomplete.");
                return;
            }
            if (teacherData.containsKey("password")) {
                String plainPassword = teacherData.get("password").toString();
                String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                teacherData.put("password", hashedPassword);
            } else {
                System.out.println("Password is missing.");
                return;
            }
            String emailAddress = teacherData.get("email") != null ? teacherData.get("email").toString() : "";
            String idNumber = teacherData.get("id_number") != null ? teacherData.get("id_number").toString() : "";
            String phone = teacherData.get("phone") != null ? teacherData.get("phone").toString() : "";

            // Validate email, ID number, and phone
            if (!isValidEmail(emailAddress)) {
                System.out.println("Invalid email address.");
                return;
            }

            if (!isValidIDNumber(idNumber)) {
                System.out.println("Invalid ID number.");
                return;
            }

            if (!isValidPhoneNumber(phone)) {
                System.out.println("Invalid phone number.");
                return;
            }
            // Insert the data into the database
            boolean isInserted = GenericQueries.insertData(connection, "teachers", teacherData);

            if (isInserted) {
                System.out.println("Teacher added successfully");
            } else {
                System.out.println("Failed to add teacher");
            }

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }




    public static void updateTeacher(Connection connection, HashMap<String, Object> teacherData, String teacherIdString) {
        try {
            if (teacherData == null || teacherData.isEmpty()) {
                System.out.println("Teacher data is missing or empty.");
                return;
            }
            int teacherId = Integer.parseInt(teacherIdString);
            String whereClause = "teacher_id = ?";

            // Call the updateWithJoin method with a null joinClause
            JsonObject result = GenericQueries.update(connection, "teachers", teacherData, whereClause,new Object[]{teacherId});

            // Optionally handle the result
            if (result.get("success").getAsBoolean()) {
                System.out.println("Teacher updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
            } else {
                System.out.println("No rows were updated.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
