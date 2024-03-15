package ke.co.skyworld.handlers.subjects;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.db.ConnectDB;
import ke.co.skyworld.utils.Responses;

import java.sql.Connection;
import java.util.Deque;

public class GetSubjects implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Connection connection = ConnectDB.initializeDatabase();
        try {
            Deque<String> columnsDeque = exchange.getQueryParameters().get("columns");
            String[] columns = (columnsDeque != null && !columnsDeque.isEmpty()) ? columnsDeque.getFirst().split(",") : new String[]{"*"};
            // Specify the table and any joins
            String table = "subject";

            Responses.Results(exchange,connection,table,columns);
        }catch (Exception e){
            Responses.Message(exchange, 500, e.getMessage());
        }
    }
}
