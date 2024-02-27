package ke.co.skyworld.handlers.teachers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.util.regex.Pattern;
import ke.co.skyworld.queryBuilder.GenericQueries;

public class CreateTeacher implements HttpHandler {
    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }

    private static boolean isValidIDNumber(String idNumber) {
        String idNumberRegex = "^\\d{8}$";
        return idNumber != null && idNumber.matches(idNumberRegex);
    }
    private static boolean isValidPhoneNumber(String phoneNumber) {
        String phoneNumberRegex = "^\\d{10}$";
        return phoneNumber != null && phoneNumber.matches(phoneNumberRegex);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
    try{
        exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
            try {
                Gson gson = new Gson();
                JsonObject teacherData = gson.fromJson(requestBody, JsonObject.class);

                if (teacherData == null) {
                    String errorMessage = "Teacher data is missing or incomplete.";
                    System.out.println(errorMessage);
                    exchange1.getResponseSender().send(errorMessage);
                    return;
                }

                if (!teacherData.has("password")) {
                    String errorMessage = "Password is missing.";
                    System.out.println(errorMessage);
                    exchange1.getResponseSender().send(errorMessage);
                    return;
                }

                String plainPassword = teacherData.get("password").getAsString();
                String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                teacherData.addProperty("password", hashedPassword);

                String emailAddress = teacherData.has("email") ? teacherData.get("email").getAsString() : "";
                String idNumber = teacherData.has("id_number") ? teacherData.get("id_number").getAsString() : "";
                String phone = teacherData.has("phone") ? teacherData.get("phone").getAsString() : "";

                if (!isValidEmail(emailAddress)) {
                    String errorMessage = "Invalid email address.";
                    System.out.println(errorMessage);
                    exchange1.getResponseSender().send(errorMessage);
                    return;
                }

                if (!isValidIDNumber(idNumber)) {
                    String errorMessage = "Invalid ID number.";
                    System.out.println(errorMessage);
                    exchange1.getResponseSender().send(errorMessage);
                    return;
                }

                if (!isValidPhoneNumber(phone)) {
                    String errorMessage = "Invalid phone number.";
                    System.out.println(errorMessage);
                    exchange1.getResponseSender().send(errorMessage);
                    return;
                }
                String insertionResult = GenericQueries.insertData(connection, "teachers", teacherData);

                System.out.println(insertionResult);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange1.getResponseSender().send(insertionResult);
            } catch (Exception e) {
                String errorMessage = "Error processing request: " + e.getMessage();
                System.out.println(errorMessage);
                exchange1.getResponseSender().send(errorMessage);
            }
        });
    }finally {
            if (connection != null) {

                connection.close();
            }
        }
    }
}