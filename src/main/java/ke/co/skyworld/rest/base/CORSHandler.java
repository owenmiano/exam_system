package ke.co.skyworld.rest.base;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

public class CORSHandler implements HttpHandler {

    private final HttpHandler httpHandler;

    public CORSHandler(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // Handle preflight requests (OPTIONS)
        if (exchange.getRequestMethod().equalToString("OPTIONS")) {
            exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
            exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Methods"), "POST, GET, OPTIONS, PUT, PATCH, DELETE");
            exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Headers"), "*");
            exchange.setStatusCode(200); // Return HTTP OK status
            exchange.endExchange(); // End the exchange
            return;
        }

        // For other requests, pass them to the next handler in the chain
        if (httpHandler != null) {
            httpHandler.handleRequest(exchange);
        }
    }
}
