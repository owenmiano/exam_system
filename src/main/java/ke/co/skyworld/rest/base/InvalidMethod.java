package ke.co.skyworld.rest.base;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class InvalidMethod implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) {
        exchange.setStatusCode(400);


        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send("Method " + exchange.getRequestMethod() + " not allowed");
    }
}
