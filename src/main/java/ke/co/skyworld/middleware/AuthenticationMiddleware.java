package ke.co.skyworld.middleware;

import io.undertow.server.HttpHandler;
import ke.co.skyworld.accessToken.User;
import ke.co.skyworld.accessToken.VerifyToken;
import ke.co.skyworld.utils.Responses;
import static ke.co.skyworld.accessToken.VerifyToken.verifyExpectedToken;
import io.undertow.util.AttachmentKey;

import java.util.Arrays;

public class AuthenticationMiddleware {
    public static final AttachmentKey<User> USER_ATTACHMENT_KEY = AttachmentKey.create(User.class);
    public HttpHandler authenticateUser(HttpHandler next, String... allowedRoles) {
        return exchange -> {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    User user = verifyExpectedToken(token);
                    if (user.isValid()) {
                        if (isAuthorized(user, allowedRoles)) {
                            exchange.putAttachment(USER_ATTACHMENT_KEY, user);
                            next.handleRequest(exchange);
                        } else {
                            Responses.Message(exchange, 403, "Forbidden: You don't have sufficient permissions to access this resource.");
                        }
                    } else {
                        Responses.Message(exchange, 401, "Unauthorized");
                    }
                } catch (VerifyToken.TokenExpiredException e) {
                    Responses.Message(exchange, 401, "Token has expired");
                } catch (VerifyToken.InvalidTokenException e) {
                    Responses.Message(exchange, 401, "Invalid token");
                } catch (VerifyToken.TokenVerificationException e) {
                    Responses.Message(exchange, 500, e.getMessage());
                }
            } else {
                Responses.Message(exchange, 401, "Unauthorized: Please log in to access this resource.");
            }
        };
    }

    public boolean isAuthorized(User user, String... allowedRoles) {
        return user != null && Arrays.asList(allowedRoles).contains(user.getRole());
    }


}
