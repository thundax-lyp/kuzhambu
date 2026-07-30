package com.thundax.kuzhambu.system.application.core.query;

import com.thundax.kuzhambu.system.domain.core.model.enums.LogType;
import java.time.Instant;
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
    private Instant beginDate;
    private Instant endDate;
}
