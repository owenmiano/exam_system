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
                // Encode the filter string with both |AND| and |OR| operators
                String encodedFilter = encodeLogicalOperators(filter);

                // Split the filter based on OR operator
                String[] orParts = encodedFilter.split("%7COR%7C");

                // Process each part separately
                StringJoiner orJoiner = new StringJoiner(" OR ");
                for (String orPart : orParts) {
                    // Split the part based on AND operator
                    String[] andParts = orPart.split("%7CAND%7C");
                    StringJoiner andJoiner = new StringJoiner(" AND ");
                    for (String andPart : andParts) {
                        String processedFilter = processSubFilter(decodeLogicalOperators(andPart));
                        if (!processedFilter.isEmpty()) {
                            andJoiner.add(processedFilter);
                        }
                    }
                    if (andJoiner.length() > 0) {
                        orJoiner.add(andJoiner.toString());
                    }
                }
                if (orJoiner.length() > 0) {
                    whereClauseJoiner.add("(" + orJoiner.toString() + ")");
                }
            }
        }
        return whereClauseJoiner.toString();
    }

    private static String processSubFilter(String subFilter) {
        // Splitting each subfilter into its components: field, operation, and value
        String[] parts = subFilter.split(":", 3);
        if (parts.length == 3) {
            String field = parts[0];
            String operation = parts[1];
            String value = parts[2];

            switch (operation) {
                case "contains":
                    return field + " LIKE '%" + value + "%'";
                case "eq":
                    return field + " = '" + value + "'";
                case "neq":
                    return field + " != '" + value + "'";
                case "lt":
                    return field + " < '" + value + "'";
                case "lte":
                    return field + " <= '" + value + "'";
                case "gt":
                    return field + " > '" + value + "'";
                case "gte":
                    return field + " >= '" + value + "'";
                case "begins":
                    return field + " LIKE '" + value + "%'";
                case "ends":
                    return field + " LIKE '%" + value + "'";
                case "between":
                    String[] rangeValues = value.split(",");
                    if (rangeValues.length == 2) {
                        return field + " BETWEEN '" + rangeValues[0] + "' AND '" + rangeValues[1] + "'";
                    }
                    break;
            }
        }
        return ""; // Return an empty string if the subfilter is invalid
    }

    private static String encodeLogicalOperators(String filter) {
        return filter.replace("|AND|", "%7CAND%7C").replace("|OR|", "%7COR%7C");
    }

    private static String decodeLogicalOperators(String filter) {
        return filter.replace("%7CAND%7C", "|AND|").replace("%7COR%7C", "|OR|");
    }
}
