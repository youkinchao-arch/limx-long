package com.hongqiao.lims.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

/** Pagination envelope matching the frontend contract: items/total/page/page_size. */
public record PageResponse<T>(
        List<T> items,
        long total,
        int page,
        @JsonProperty("page_size") int pageSize) {

    public static <T> PageResponse<T> of(Page<T> page, int pageNumber, int pageSize) {
        return new PageResponse<>(page.getContent(), page.getTotalElements(), pageNumber, pageSize);
    }
}
