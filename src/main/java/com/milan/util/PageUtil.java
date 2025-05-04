package com.milan.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

 //Utility method to create a Pageable object with sorting.
//@return a Pageable object with specified pagination and sorting
public class PageUtil {

    public static Pageable getPageable(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        return PageRequest.of(pageNo, pageSize, sort);
    }
}

