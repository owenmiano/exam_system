package ke.co.skyworld.utils;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class Response {
    public static void Message(HttpServerExchange exchange, int statusCode, String message) {
        exchange.setStatusCode(statusCode);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send("{\"message\": \"" + message + "\"}");
    }
}
