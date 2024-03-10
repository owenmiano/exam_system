package ke.co.skyworld.accessToken;
import com.google.gson.JsonArray;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.KeyManager;
import ke.co.skyworld.Model.ConfigReader;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.queryBuilder.SelectQuery;
import ke.co.skyworld.utils.Response;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

public class VerifyToken {
    public static class TokenExpiredException extends Exception {}
    public static class InvalidTokenException extends Exception {}
    public static class TokenVerificationException extends Exception {
        public TokenVerificationException(String message) {
            super(message);
        }
    }

    // Modified verifyExpectedToken method
    public static User verifyExpectedToken(String expectedToken) throws TokenExpiredException, InvalidTokenException, TokenVerificationException {
        Connection connection = ConnectDB.initializeDatabase();
        User user = new User();
        try {
            String[] columns = {"count(*)"};
            String whereClause = "access_token = ?";
            Object[] params = {expectedToken};
            JsonArray jsonArrayResult = SelectQuery.select(connection, "auth", whereClause, params);
            if (!jsonArrayResult.isEmpty()) {
                String decryptedToken = ConfigReader.decrypt(expectedToken, KeyManager.ENCRYPT_KEY);

                String[] fields = decryptedToken.split("_");
                user.setUsername((fields[0]));
                user.setRole((fields[1]));
                user.setCreationTime(Long.parseLong(fields[2]));
                user.setExpirationTime(Long.parseLong(fields[3]));

                long currentTime = System.currentTimeMillis();
                if (currentTime > user.getExpirationTime()) {
                    user.setValid(false);
                    throw new TokenExpiredException();
                }

                user.setValid(true);
            } else {
                user.setValid(false);
                throw new InvalidTokenException();
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
            throw new TokenVerificationException(e.getMessage());
        } catch (SQLException e) {
            throw new TokenVerificationException(e.getMessage());
        }
        return user;
    }

}
