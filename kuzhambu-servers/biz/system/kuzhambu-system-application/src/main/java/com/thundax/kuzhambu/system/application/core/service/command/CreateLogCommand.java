package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.application.core.entity.enums.LogType;
import com.thundax.kuzhambu.system.domain.core.valueobject.LogId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateLogCommand {
    private LogId id;
    private String userId;
    private LogType type;
    private Date logDate;
    private String title;
    private String remoteAddr;
    private String userAgent;
    private String method;
    private String requestUri;
    private String requestParams;
    private String remarks;
}
