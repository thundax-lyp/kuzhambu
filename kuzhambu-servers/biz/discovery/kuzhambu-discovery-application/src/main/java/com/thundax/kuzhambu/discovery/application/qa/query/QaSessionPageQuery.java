package com.thundax.kuzhambu.discovery.application.qa.query;

import java.util.Date;
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
    private Date openedAtStart;
    private Date openedAtEnd;
    private int pageNo;
    private int pageSize;
}
