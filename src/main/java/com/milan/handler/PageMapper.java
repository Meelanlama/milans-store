package com.milan.handler;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class PageMapper {

    /**
     * Converts a Page of entities (U) into a custom paginated DTO response (PageableResponse<V>).
     *
     * @param page the Page of entities returned by JPA repository
     * @param type the class type of the DTO to map each entity to
     * @return a paginated response with DTOs and pagination metadata
     */

    // A single shared ModelMapper instance
    private static final ModelMapper mapper = new ModelMapper();

    public static <U, V> PageableResponse<V> getPageableResponse(Page<U> page, Class<V> type) {

        // Extract content (list of entities) from the Page object
        List<U> entityList = page.getContent();

        // Convert each entity to its corresponding DTO using ModelMapper
        List<V> dtoList = entityList.stream()
                .map(object -> mapper.map(object, type))
                .collect(Collectors.toList());

        // Create and populate the custom PageableResponse
        PageableResponse<V> response = new PageableResponse<>();
        response.setContent(dtoList); // Set converted DTOs
        response.setPageNo(page.getNumber()); // Current page number (0-based)
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setIsFirst(page.isFirst());
        response.setIsLast(page.isLast());

        return response;

    }
}

