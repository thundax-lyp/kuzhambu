package com.thundax.kuzhambu.common.core.page;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageResult<T> {

    private int pageNo = PageRules.firstPageIndex();

    private int pageSize = PageRules.defaultPageSize();

    private long totalCount = 0;

    private int totalPage = 0;

    private List<T> records = new ArrayList<>();

    public static <T> PageResult<T> of(int pageNo, int pageSize, long totalCount, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setPageNo(pageNo);
        result.setPageSize(pageSize);
        result.setTotalCount(totalCount);
        result.setTotalPage(totalPage(totalCount, pageSize));
        result.setRecords(records == null ? new ArrayList<T>() : records);
        return result;
    }

    private static int totalPage(long totalCount, int pageSize) {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) ((totalCount + pageSize - 1) / pageSize);
    }
}
