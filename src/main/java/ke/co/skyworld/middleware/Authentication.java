package ke.co.skyworld.middleware;

import io.undertow.server.HttpHandler;
import ke.co.skyworld.accessToken.User;
import ke.co.skyworld.accessToken.VerifyToken;
import ke.co.skyworld.utils.Response;
import static ke.co.skyworld.accessToken.VerifyToken.verifyExpectedToken;
import io.undertow.util.AttachmentKey;
public class Authentication {
    public static final AttachmentKey<User> USER_ATTACHMENT_KEY = AttachmentKey.create(User.class);
    public HttpHandler authenticateUser(HttpHandler next) {
        return exchange -> {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    User user = verifyExpectedToken(token);
                    if (user.isValid()) {
                        exchange.putAttachment(USER_ATTACHMENT_KEY, user);
                        next.handleRequest(exchange);
                    } else {
                        Response.Message(exchange, 401, "Unauthorized");
                    }
                } catch (VerifyToken.TokenExpiredException e) {
                    Response.Message(exchange, 401, "Token has expired");
                } catch (VerifyToken.InvalidTokenException e) {
                    Response.Message(exchange, 401, "Invalid token");
                } catch (VerifyToken.TokenVerificationException e) {
                    Response.Message(exchange, 500, e.getMessage());
                }
            } else {
                Response.Message(exchange, 401, "Unauthorized");
            }
        };
    }

    public HttpHandler authorizeTeacher(HttpHandler next) {
        return exchange -> {
            authenticateUser(exchange1 -> {
                User user = exchange1.getAttachment(USER_ATTACHMENT_KEY);
                if (user != null && user.getRole().equals("teacher")) {
                    // User is a teacher, proceed to next handler
                    next.handleRequest(exchange1);
                } else {
                    // User is not a teacher, handle unauthorized access
                    Response.Message(exchange1, 401, "Unauthorized: Teacher access required");
                }
            }).handleRequest(exchange);
        };
    }
}
