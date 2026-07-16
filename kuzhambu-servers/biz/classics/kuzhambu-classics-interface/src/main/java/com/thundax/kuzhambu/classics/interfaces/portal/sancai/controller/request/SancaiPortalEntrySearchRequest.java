package com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SancaiPortalEntrySearchRequest {
    private Long id;
    private Long categoryId;
    private Long volumeId;
    private String keyword;
    private Integer pageNo;
    private Integer pageSize;
}
