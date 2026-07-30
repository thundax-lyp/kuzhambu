package com.thundax.kuzhambu.discovery.application.qa.query;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaSessionPageQuery {
    private String title;
    private Instant openedAtStart;
    private Instant openedAtEnd;
    private int pageNo;
    private int pageSize;
}
