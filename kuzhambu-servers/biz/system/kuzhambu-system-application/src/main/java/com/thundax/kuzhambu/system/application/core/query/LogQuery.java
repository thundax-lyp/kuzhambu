package com.thundax.kuzhambu.system.application.core.query;

import com.thundax.kuzhambu.system.domain.core.model.enums.LogType;
import java.time.Instant;

public record LogQuery(
        LogType type,
        String remoteAddr,
        String title,
        String requestUri,
        String userLoginName,
        String userName,
        Instant beginDate,
        Instant endDate) {}
