package com.milan.handler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
/*
 * Generic response class for paginated data.
 * Allows returning any type of list (Users, Products, Categories) with page info.
 */
@Schema(description = "Paginated response wrapper")

public class PageableResponse<T> {

    @Schema(description = "List of items in current page")
    private List<T> content;
    @Schema(example = "1", description = "Current page number")
    private int pageNo;
    @Schema(example = "10", description = "Number of items per page")
    private int pageSize;
    @Schema(example = "100", description = "Total number of items")
    private long totalElements;
    private int totalPages;
    private Boolean isFirst;
    private Boolean isLast;

    // Factory method for empty response
    public static <T> PageableResponse<T> empty(int pageNo, int pageSize) {
        PageableResponse<T> response = new PageableResponse<>();
        response.setContent(Collections.emptyList());
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setTotalElements(0);
        response.setTotalPages(0);
        response.setIsFirst(true);
        response.setIsLast(true);
        return response;
    }

}
