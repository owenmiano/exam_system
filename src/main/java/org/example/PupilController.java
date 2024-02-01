package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import org.mindrot.jbcrypt.BCrypt;

public class PupilController {
    private static boolean isValidPhoneNumber(String phoneNumber) {
        String phoneNumberRegex = "^\\d{10}$";
        return phoneNumber != null && phoneNumber.matches(phoneNumberRegex);
    }
    public static void findPupil(Connection connection, HashMap<String, Object> pupilData, String[] columns) {
        try {
            if (pupilData == null || pupilData.isEmpty()) {
                System.out.println("No Pupil data provided.");
                return;
            }

            StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
            ArrayList<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : pupilData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                whereClauseJoiner.add(key + " = ?");
                values.add(value);
            }

            String whereClause = whereClauseJoiner.toString();
            JsonArray jsonArrayResult = GenericQueries.select(connection, "pupils", columns, whereClause, values.toArray());
            String jsonResult = jsonArrayResult.toString();
            System.out.println(jsonResult);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    //insert
    public static void createPupil(Connection connection, HashMap<String, Object> pupilData) {
        try {
            if (pupilData == null) {
                System.out.println("Class data is missing or incomplete.");
                return;
            }
            // Hashing the password
            if (pupilData.containsKey("password")) {
                String plainPassword = pupilData.get("password").toString();
                String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                pupilData.put("password", hashedPassword);
            } else {
                System.out.println("Password is missing.");
                return;
            }
            String phone = pupilData.get("guardian_phone") != null ? pupilData.get("guardian_phone").toString() : "";

            if (!isValidPhoneNumber(phone)) {
                System.out.println("Invalid phone number.");
                return;
            }
            // Insert the data into the database
            boolean isInserted = GenericQueries.insertData(connection, "pupils", pupilData);

            if (isInserted) {
                System.out.println("Pupil added successfully");
            } else {
                System.out.println("Failed to add Pupil");
            }

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void updatePupil(Connection connection, HashMap<String, Object> pupilData, String pupilIdString) {
        try {
            if (pupilData == null || pupilData.isEmpty()) {
                System.out.println("Pupil data is missing or empty.");
                return;
            }
            int pupilId = Integer.parseInt(pupilIdString);
            String whereClause = "pupil_id = ?";

            JsonObject result = GenericQueries.update(connection, "pupils", pupilData, whereClause,new Object[]{pupilId});


            if (result.get("success").getAsBoolean()) {
                System.out.println("Pupil updated successfully. Rows affected: " + result.get("rowsAffected").getAsInt());
            } else {
                System.out.println("No rows were updated.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
