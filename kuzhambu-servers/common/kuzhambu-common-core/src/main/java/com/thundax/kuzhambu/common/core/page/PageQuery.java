package com.thundax.kuzhambu.common.core.page;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageQuery {

    private int pageNo = PageRules.firstPageIndex();

    private int pageSize = PageRules.defaultPageSize();

    public PageQuery() {}

    public PageQuery(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        normalize();
    }

    public void normalize() {
        if (pageNo < PageRules.firstPageIndex()) {
            pageNo = PageRules.firstPageIndex();
        }
        if (pageSize <= 0) {
            pageSize = PageRules.defaultPageSize();
        }
    }
}
