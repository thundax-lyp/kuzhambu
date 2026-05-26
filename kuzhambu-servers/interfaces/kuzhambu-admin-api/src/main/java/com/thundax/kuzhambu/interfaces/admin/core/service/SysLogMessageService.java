package com.thundax.kuzhambu.interfaces.admin.core.service;

import com.thundax.kuzhambu.biz.core.entity.Log;

public interface SysLogMessageService {

    String QUEUE_SAVE_LOG = "kuzhambu.save-log";
    String TOPIC_SAVE_LOG = "kuzhambu_save_log";

    void saveLog(Log sysLog);

    void consumeLog(String payload);
}
