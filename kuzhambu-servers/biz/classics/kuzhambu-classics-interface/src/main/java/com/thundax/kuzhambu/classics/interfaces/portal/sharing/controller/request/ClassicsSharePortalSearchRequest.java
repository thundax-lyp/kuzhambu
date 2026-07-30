package com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.request;

import com.thundax.kuzhambu.common.core.page.PageRules;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class ClassicsSharePortalSearchRequest {
    private String shareToken;
    private String contentType;
    private String title;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant issuedAfter;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant issuedBefore;

    private int pageNo = PageRules.firstPageIndex();
    private int pageSize = PageRules.defaultPageSize();
}
