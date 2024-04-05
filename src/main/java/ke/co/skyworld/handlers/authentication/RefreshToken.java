package ke.co.skyworld.handlers.authentication;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import ke.co.skyworld.accessToken.GenerateToken;
import ke.co.skyworld.accessToken.UpdateToken;
import ke.co.skyworld.accessToken.User;
import ke.co.skyworld.middleware.AuthenticationMiddleware;

public class RefreshToken implements HttpHandler {
    public static final AttachmentKey<String> USER_NAME_KEY = AttachmentKey.create(String.class);
    public static final AttachmentKey<String> ACCESS_TOKEN_KEY = AttachmentKey.create(String.class);


    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        User user = exchange.getAttachment(AuthenticationMiddleware.USER_ATTACHMENT_KEY);
        if (user != null) {
          String accessToken= GenerateToken.accessToken(user.getUsername(),user.getRole());
            exchange.putAttachment(USER_NAME_KEY, user.getUsername());
            exchange.putAttachment(ACCESS_TOKEN_KEY, accessToken);


            // Invoke the UpdateToken handler
            new UpdateToken().handleRequest(exchange);
        }
    }
}
