package com.milan.dto.response;

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
public class PageableResponse<T> {

    private List<T> content;
    private int pageNo;
    private int pageSize;
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
