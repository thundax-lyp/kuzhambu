package com.thundax.kuzhambu.discovery.application.qa.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSyncItemPageQuery {
    private String contentType;
    private String syncStatus;
    private int pageNo;
    private int pageSize;
}
