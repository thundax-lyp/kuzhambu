package com.thundax.kuzhambu.common.web.response;

import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PageResponseHelper {

    private PageResponseHelper() {}

    public static <T, R> PageResponse<R> fromPageResult(PageResult<T> page, Function<T, R> mappingFunction) {
        PageResponse<R> pageResponse = new PageResponse<>();

        pageResponse.setPageNo(page.getPageNo());
        pageResponse.setPageSize(page.getPageSize());
        pageResponse.setTotalPage(page.getTotalPage());
        pageResponse.setCount(page.getTotalCount());

        pageResponse.setRecords(Optional.ofNullable(page.getRecords())
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(mappingFunction)
                .collect(Collectors.toList()));

        return pageResponse;
    }
}
