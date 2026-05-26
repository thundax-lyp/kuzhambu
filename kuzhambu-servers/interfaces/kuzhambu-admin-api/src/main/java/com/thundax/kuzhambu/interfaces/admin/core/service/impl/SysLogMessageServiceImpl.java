package com.thundax.kuzhambu.interfaces.admin.core.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.biz.core.entity.Log;
import com.thundax.kuzhambu.biz.core.service.LogService;
import com.thundax.kuzhambu.biz.core.service.command.CreateLogCommand;
import com.thundax.kuzhambu.biz.core.service.query.LogQuery;
import com.thundax.kuzhambu.common.mq.KuzhambuMqMessage;
import com.thundax.kuzhambu.common.mq.KuzhambuMqSender;
import com.thundax.kuzhambu.common.mq.KuzhambuMqType;
import com.thundax.kuzhambu.common.mq.configure.KuzhambuMqProperties;
import com.thundax.kuzhambu.interfaces.admin.configure.KuzhambuProperties;
import com.thundax.kuzhambu.interfaces.admin.core.service.SysLogMessageService;
import java.io.File;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Lazy(false)
@Slf4j
@RequiredArgsConstructor
public class SysLogMessageServiceImpl implements SysLogMessageService {

    private static final DateTimeFormatter LOG_FILENAME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String LOG_EXTEND_NAME = ".log";

    private final KuzhambuMqSender mqSender;
    private final KuzhambuMqProperties mqProperties;
    private final KuzhambuProperties kuzhambuProperties;
    private final LogService logService;
    private final ObjectMapper objectMapper;

    @Override
    public void saveLog(Log sysLog) {
        try {
            String payload = objectMapper.writeValueAsString(sysLog);
            mqSender.send(buildMessage(payload).withHeader("kuzhambu-message-type", "sys-log"));
        } catch (Exception e) {
            log.warn("can not serialize sys-log message", e);
        }
    }

    @Override
    public void consumeLog(String payload) {
        try {
            Log sysLog = objectMapper.readValue(payload, Log.class);
            if (sysLog != null) {
                sysLog.setId(logService.create(toCreateCommand(sysLog)));

                try {
                    String filename = LOG_FILENAME_FORMAT.format(
                                    sysLog.getLogDate().toInstant().atZone(ZoneId.systemDefault()))
                            + LOG_EXTEND_NAME;
                    File logFile = new File(logProperties().getStoragePath(), filename);

                    FileUtils.writeLines(logFile, new ArrayList<>(Collections.singletonList(payload)), true);

                } catch (Exception e) {
                    log.warn("can not save sys-log to {}", logProperties().getStoragePath(), e);
                }
            }

        } catch (Exception e) {
            log.error("can not consume sys-log message", e);
        }
    }

    @Scheduled(cron = "0 0 0/4 * * ?")
    void doTask() {
        LogQuery query = new LogQuery();
        query.setBeginDate(DateUtils.addDays(new Date(), -9999));
        query.setEndDate(DateUtils.addDays(new Date(), -logProperties().getAliveDays()));
        logService.deleteByCondition(query);
    }

    private KuzhambuProperties.LogProperties logProperties() {
        return kuzhambuProperties.getLog();
    }

    private KuzhambuMqMessage buildMessage(String payload) {
        KuzhambuProperties.SysLogProperties sysLogProperties = logProperties().getSys();
        if (KuzhambuMqType.ROCKETMQ == mqProperties.getType()) {
            return KuzhambuMqMessage.forTopicWithTag(
                    sysLogProperties.getTopic(), sysLogProperties.getTag(), null, payload);
        }
        return KuzhambuMqMessage.forQueue(sysLogProperties.getQueue(), null, payload);
    }

    private CreateLogCommand toCreateCommand(Log log) {
        return new CreateLogCommand(
                log.getId(),
                log.getUserId(),
                log.getType(),
                log.getLogDate(),
                log.getTitle(),
                log.getRemoteAddr(),
                log.getUserAgent(),
                log.getMethod(),
                log.getRequestUri(),
                log.getRequestParams(),
                log.getRemarks());
    }
}
