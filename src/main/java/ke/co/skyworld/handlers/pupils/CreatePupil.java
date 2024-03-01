package ke.co.skyworld.handlers.pupils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.InsertQuery;
import ke.co.skyworld.utils.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;

public class CreatePupil implements HttpHandler {
    private static boolean isValidPhoneNumber(String phoneNumber) {
        String phoneNumberRegex = "^\\d{10}$";
        return phoneNumber != null && phoneNumber.matches(phoneNumberRegex);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      Connection  connection = ConnectDB.initializeDatabase();
        try {
            exchange.getRequestReceiver().receiveFullString((exchange1, requestBody) -> {
                try {
                    Gson gson = new Gson();
                    JsonObject pupilData = gson.fromJson(requestBody, JsonObject.class);

                    if (!pupilData.has("pupil_name")) {
                        String errorMessage = "Pupil name is missing.";
                        Response.Message(exchange, 400,  errorMessage);
                        return;
                    }
                    if (!pupilData.has("password")) {
                        String errorMessage = "Password is missing.";
                        Response.Message(exchange, 400,  errorMessage);
                        return;
                    }


                    String plainPassword = pupilData.get("password").getAsString();
                    String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                    pupilData.addProperty("password", hashedPassword);

                    String phone = pupilData.has("guardian_phone") ? pupilData.get("guardian_phone").getAsString() : "";

                    if (!isValidPhoneNumber(phone)) {
                        String errorMessage = "Invalid phone number.";
                        Response.Message(exchange, 400,  errorMessage);
                        return;
                    }
                    String insertMessage = InsertQuery.insertData(connection, "pupils", pupilData);
                    if (insertMessage.startsWith("Error")) {
                        Response.Message(exchange, 500, insertMessage);
                    } else {
                        Response.Message(exchange, 200, insertMessage);
                    }
                }  catch (Exception e) {
                    Response.Message(exchange, 500,  e.getMessage());
                }
            });
        } finally {
        if (connection != null) {

            connection.close();
        }
    }
}
}
