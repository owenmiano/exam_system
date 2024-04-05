package ke.co.skyworld.accessToken;

import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.handlers.authentication.RefreshToken;
import ke.co.skyworld.queryBuilder.UpdateQuery;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;

public class UpdateToken implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String userName = exchange.getAttachment(RefreshToken.USER_NAME_KEY);
        String accessToken = exchange.getAttachment(RefreshToken.ACCESS_TOKEN_KEY);
        Connection connection = ConnectDB.getConnection();
        JsonObject authData = new JsonObject();
        authData.addProperty("access_token", accessToken);
        String whereClause = "username = ?";
        Object[] params = {userName};
        String updateMessage=UpdateQuery.update(connection, "auth",authData,whereClause,params);
        if (updateMessage.startsWith("Error")) {
            Responses.Message(exchange, 500, updateMessage);
        } else {
            Responses.Message(exchange, 200, "User session successfully renewed");
        }

    }
}