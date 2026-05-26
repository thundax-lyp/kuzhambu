package com.thundax.kuzhambu.biz.core.service.query;

import com.thundax.kuzhambu.biz.core.entity.enums.LogType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogQuery {
    private LogType type;
    private String remoteAddr;
    private String title;
    private String requestUri;
    private String userLoginName;
    private String userName;
    private Date beginDate;
    private Date endDate;
}
