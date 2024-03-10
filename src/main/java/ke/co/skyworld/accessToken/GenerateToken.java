package ke.co.skyworld.accessToken;

import ke.co.skyworld.Model.ConfigReader;

import java.util.Date;

public class GenerateToken {
    private static final long EXPIRATION_TIME = 3600000; // One hour
    public static String accessToken(String username, String role) {
        Date now = new Date();
        long expMillis = now.getTime() + EXPIRATION_TIME;
        String payload = username + "_" + role + "_" + now.getTime() + "_" + expMillis;
        return ConfigReader.encrypt(payload);
    }

}
