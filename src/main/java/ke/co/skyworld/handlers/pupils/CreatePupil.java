package ke.co.skyworld.handlers.pupils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.GenericQueries;
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

                    if (!pupilData.has("password")) {
                        String errorMessage = "Password is missing.";
                        exchange.setStatusCode(404);
                        exchange1.getResponseSender().send(errorMessage);
                        return;
                    }

                    String plainPassword = pupilData.get("password").getAsString();
                    String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                    pupilData.addProperty("password", hashedPassword);

                    String phone = pupilData.has("guardian_phone") ? pupilData.get("guardian_phone").getAsString() : "";

                    if (!isValidPhoneNumber(phone)) {
                        String errorMessage = "Invalid phone number.";
                        exchange.setStatusCode(404);
                        exchange1.getResponseSender().send(errorMessage);
                        return;
                    }
                    String insertionResult = GenericQueries.insertData(connection, "pupils", pupilData);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange1.getResponseSender().send(insertionResult);
                }  catch (Exception e) {
                    String errorMessage = "Error " + e.getMessage();
                    exchange.setStatusCode(500);
                    exchange1.getResponseSender().send(errorMessage);
                }
            });
        } finally {
        if (connection != null) {

            connection.close();
        }
    }
}
}
