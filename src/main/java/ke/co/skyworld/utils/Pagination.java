package ke.co.skyworld.utils;

import io.undertow.server.HttpServerExchange;
import java.util.Deque;

public class Pagination {
    private final int pageSize;
    private final int page;
    private final int minPageSize = 5; // Set minimum page size to 10
    private final int maxPageSize = 50; // Set maximum page size to 50

    public Pagination(HttpServerExchange exchange) {
        Deque<String> pageDeque = exchange.getQueryParameters().get("page");
        if (pageDeque != null && !pageDeque.isEmpty()) {
            this.page = Integer.parseInt(pageDeque.getFirst()); // Assign to instance variable
        } else {
            this.page = 1; // Default to first page
        }

        int parsedPageSize = 5;
        Deque<String> pageSizeDeque = exchange.getQueryParameters().get("pageSize");
        if (pageSizeDeque != null && !pageSizeDeque.isEmpty()) {
            parsedPageSize = Integer.parseInt(pageSizeDeque.getFirst());
        }
        this.pageSize = Math.max(minPageSize, Math.min(maxPageSize, parsedPageSize)); // Direct assignment with min/max check
    }

    public int getPageSize() {
        return pageSize;
    }

    public int calculateOffset() {
        return (page - 1) * pageSize;
    }
}
