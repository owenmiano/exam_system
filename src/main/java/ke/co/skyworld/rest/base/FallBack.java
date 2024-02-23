package ke.co.skyworld.rest.base;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

public class FallBack implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) {

        exchange.setStatusCode(StatusCodes.OK);

        //TODO: use JSON instead of plain text

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("URI "+exchange.getRequestURI()+" not found on server");
    }
}