package com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.request;

import com.thundax.kuzhambu.common.core.page.PageRules;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class ClassicsSharePortalSearchRequest {
    private String contentType;
    private String title;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date issuedAfter;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date issuedBefore;

    private int pageNo = PageRules.firstPageIndex();
    private int pageSize = PageRules.defaultPageSize();
}
