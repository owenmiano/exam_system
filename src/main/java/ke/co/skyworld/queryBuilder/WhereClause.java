package ke.co.skyworld.queryBuilder;

import io.undertow.server.HttpServerExchange;

import java.util.Deque;
import java.util.StringJoiner;

public class WhereClause {

    public static String generateWhereClause(HttpServerExchange exchange) {
        StringJoiner whereClauseJoiner = new StringJoiner(" AND ");
        Deque<String> filterDeque = exchange.getQueryParameters().get("filter");
        if (filterDeque != null && !filterDeque.isEmpty()) {
            for (String filter : filterDeque) {
                // Splitting each filter into its components: field, operation, and value
                String[] parts = filter.split(":", 3);
                if (parts.length == 3) {
                    String field = parts[0];
                    String operation = parts[1];
                    String value = parts[2];

                    switch (operation) {
                        case "like":
                            whereClauseJoiner.add(field + " LIKE '%" + value + "%'");
                            break;
                        case "eq":
                            whereClauseJoiner.add(field + " = '" + value + "'");
                            break;
                        case "neq":
                            whereClauseJoiner.add(field + " != '" + value + "'");
                            break;
                        case "lt":
                            whereClauseJoiner.add(field + " < '" + value + "'");
                            break;
                        case "lte":
                            whereClauseJoiner.add(field + " <= '" + value + "'");
                            break;
                        case "gt":
                            whereClauseJoiner.add(field + " > '" + value + "'");
                            break;
                        case "gte":
                            whereClauseJoiner.add(field + " >= '" + value + "'");
                            break;
                        case "begins":
                            whereClauseJoiner.add(field + " LIKE '" + value + "%'");
                            break;
                        case "ends":
                            whereClauseJoiner.add(field + " LIKE '%" + value + "'");
                            break;
                    }
                }
            }
        }

        return whereClauseJoiner.toString();
    }
}

