package ke.co.skyworld.accessToken;

import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import ke.co.skyworld.KeyManager;
import ke.co.skyworld.Model.ConfigReader;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.handlers.authentication.RefreshToken;
import ke.co.skyworld.queryBuilder.UpdateQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;

public class UpdateToken implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        User user = new User();
        String userName = exchange.getAttachment(RefreshToken.USER_NAME_KEY);
        String accessToken = exchange.getAttachment(RefreshToken.ACCESS_TOKEN_KEY);
        String decryptedToken = ConfigReader.decrypt(accessToken, KeyManager.AES_ENCRYPT_KEY);
        assert decryptedToken != null;
        String[] fields = decryptedToken.split("_");
        user.setRole((fields[1]));
        user.setExpirationTime(Long.parseLong(fields[3]));
        Connection connection = ConnectDB.getConnection();
        JsonObject authData = new JsonObject();
        authData.addProperty("access_token", accessToken);
        String whereClause = "username = ?";
        Object[] params = {userName};
        exchange.getResponseHeaders().put(new HttpString("Content-type"), "application/json")
                .put(new HttpString("Access-Control-Allow-Origin"), "*")
                .put(new HttpString("Access-Control-Allow-Headers"), "*");
        String updateMessage=UpdateQuery.update(connection, "auth",authData,whereClause,params);
        if (updateMessage.startsWith("Error")) {
            Responses.Message(exchange, 500, updateMessage);
        } else {
            JsonObject responseJson = new JsonObject();
            String successMessage = "User session renewed successful";
            responseJson.addProperty("message", successMessage);

            // Create a JsonObject for user info
            JsonObject userInfoJson = new JsonObject();

            userInfoJson.addProperty("username", userName);
            userInfoJson.addProperty("token", accessToken);
            userInfoJson.addProperty("role", user.getRole());
            userInfoJson.addProperty("expirationTime", user.getExpirationTime());
            responseJson.add("userInfo", userInfoJson);
            // Send the response
            exchange.getResponseSender().send(responseJson.toString());
        }

    }
}