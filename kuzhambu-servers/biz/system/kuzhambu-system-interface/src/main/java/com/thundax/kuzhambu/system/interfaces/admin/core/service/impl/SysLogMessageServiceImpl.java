package com.thundax.kuzhambu.system.interfaces.admin.core.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.rocketmq.KuzhambuMqMessage;
import com.thundax.kuzhambu.common.rocketmq.KuzhambuMqSender;
import com.thundax.kuzhambu.system.application.core.command.CreateLogCommand;
import com.thundax.kuzhambu.system.application.core.command.DeleteLogCommand;
import com.thundax.kuzhambu.system.application.core.query.LogQuery;
import com.thundax.kuzhambu.system.application.core.service.LogApplicationService;
import com.thundax.kuzhambu.system.domain.core.codec.LogIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Log;
import com.thundax.kuzhambu.system.domain.core.model.enums.LogType;
import com.thundax.kuzhambu.system.interfaces.admin.configure.KuzhambuProperties;
import com.thundax.kuzhambu.system.interfaces.admin.core.service.SysLogMessageService;
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
    private final KuzhambuProperties kuzhambuProperties;
    private final LogApplicationService logService;
    private final ObjectMapper objectMapper;

    @Override
    public void saveLog(Log sysLog) {
        try {
            String payload = objectMapper.writeValueAsString(SysLogDTO.from(sysLog));
            mqSender.send(buildMessage(payload).withHeader("kuzhambu-message-type", "sys-log"));
        } catch (Exception e) {
            log.warn("can not serialize sys-log message", e);
        }
    }

    @Override
    public void consumeLog(String payload) {
        try {
            SysLogDTO sysLog = objectMapper.readValue(payload, SysLogDTO.class);
            if (sysLog != null) {
                logService.create(sysLog.toCreateCommand());

                try {
                    String filename = LOG_FILENAME_FORMAT.format(
                                    sysLog.logDate().toInstant().atZone(ZoneId.systemDefault()))
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
        logService.deleteByCondition(new DeleteLogCommand(query));
    }

    private KuzhambuProperties.LogProperties logProperties() {
        return kuzhambuProperties.getLog();
    }

    private KuzhambuMqMessage buildMessage(String payload) {
        KuzhambuProperties.SysLogProperties sysLogProperties = logProperties().getSys();
        return KuzhambuMqMessage.forTopicWithTag(sysLogProperties.getTopic(), sysLogProperties.getTag(), null, payload);
    }

    private record SysLogDTO(
            Long id,
            String userId,
            String type,
            Date logDate,
            String title,
            String remoteAddr,
            String userAgent,
            String method,
            String requestUri,
            String requestParams,
            String remarks) {

        private static SysLogDTO from(Log log) {
            return log == null
                    ? null
                    : new SysLogDTO(
                            LogIdCodec.toValue(log.getId()),
                            UserIdCodec.toStringValue(log.getUserId()),
                            log.getType() == null ? null : log.getType().value(),
                            log.getLogDate(),
                            log.getTitle(),
                            log.getRemoteAddr(),
                            log.getUserAgent(),
                            log.getMethod(),
                            log.getRequestUri(),
                            log.getRequestParams(),
                            log.getRemarks());
        }

        private CreateLogCommand toCreateCommand() {
            return new CreateLogCommand(
                    LogIdCodec.toDomain(id),
                    userId,
                    type == null ? null : LogType.from(type),
                    logDate,
                    title,
                    remoteAddr,
                    userAgent,
                    method,
                    requestUri,
                    requestParams,
                    remarks);
        }
    }
}
