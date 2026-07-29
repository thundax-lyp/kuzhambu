package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.enums.LogType;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.LogId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
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
    private UserId userId;
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
