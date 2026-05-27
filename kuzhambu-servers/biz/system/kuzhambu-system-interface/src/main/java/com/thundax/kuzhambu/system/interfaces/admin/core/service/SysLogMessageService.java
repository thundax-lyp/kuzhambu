package com.thundax.kuzhambu.system.interfaces.admin.core.service;

import com.thundax.kuzhambu.system.domain.core.model.entity.Log;

public interface SysLogMessageService {

    String TOPIC_SAVE_LOG = "kuzhambu_save_log";

    void saveLog(Log sysLog);

    void consumeLog(String payload);
}
