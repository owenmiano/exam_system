package ke.co.skyworld.utils;

import io.undertow.server.HttpServerExchange;
import java.util.Deque;

public class Pagination {
    private final int pageSize;
    private final int page;
    private final int totalRecords;
    private final int maxPageSize = 50;

    public Pagination(HttpServerExchange exchange, int totalRecords) {
        this.totalRecords = totalRecords;

        Deque<String> pageDeque = exchange.getQueryParameters().get("page");
        int parsedPage = pageDeque != null && !pageDeque.isEmpty() ? Integer.parseInt(pageDeque.getFirst()) : 1;
        if (parsedPage < 1) {
            Response.Message(exchange, 400, "Page cannot be below 1");
            parsedPage = 1; // Set page to 1 if it's below 1
        }
        this.page = parsedPage;
        int parsedPageSize = 5;
        Deque<String> pageSizeDeque = exchange.getQueryParameters().get("pageSize");
        if (pageSizeDeque != null && !pageSizeDeque.isEmpty()) {
            parsedPageSize = Integer.parseInt(pageSizeDeque.getFirst());
        }

        if (parsedPageSize < 1 || parsedPageSize > maxPageSize) {
            Response.Message(exchange, 400, "Page size must be between 1 and 50");
            parsedPageSize = 5; // Set default page size
        }

        this.pageSize = parsedPageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPage() {
        return page;
    }

    public int calculateOffset() {
        return (page - 1) * pageSize;
    }

    public int calculateTotalPages() {
        return (int) Math.ceil((double) totalRecords / pageSize);
    }
}
