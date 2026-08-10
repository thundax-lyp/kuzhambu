package com.thundax.kuzhambu.system.interfaces.admin.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.rocketmq.KuzhambuMqMessage;
import com.thundax.kuzhambu.common.rocketmq.KuzhambuMqSender;
import com.thundax.kuzhambu.system.application.core.command.CreateLogCommand;
import com.thundax.kuzhambu.system.application.core.command.DeleteLogCommand;
import com.thundax.kuzhambu.system.application.core.query.GetLogQuery;
import com.thundax.kuzhambu.system.application.core.query.LogQuery;
import com.thundax.kuzhambu.system.application.core.service.SystemLogApplicationService;
import com.thundax.kuzhambu.system.domain.core.codec.LogIdCodec;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Log;
import com.thundax.kuzhambu.system.domain.core.model.enums.LogType;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.LogId;
import com.thundax.kuzhambu.system.interfaces.admin.configure.KuzhambuProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SysLogMessageServiceImplTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    @TempDir
    private Path tempDir;

    @Test
    void shouldUseDtoPayloadForMqLogMessages() throws Exception {
        CapturingMqSender mqSender = new CapturingMqSender();
        CapturingSystemLogApplicationService logService = new CapturingSystemLogApplicationService();
        SysLogMessageServiceImpl service =
                new SysLogMessageServiceImpl(mqSender, properties(), logService, OBJECT_MAPPER);
        Instant logDate = Instant.ofEpochMilli(1778513052000L);
        Log log = new Log();
        log.setUserId(UserIdCodec.toDomain(1L));
        log.setType(LogType.ACCESS);
        log.setLogDate(logDate);
        log.setTitle("系统-登录-成功");
        log.setRemoteAddr("127.0.0.1");
        log.setUserAgent("JUnit");
        log.setMethod("POST");
        log.setRequestUri("/api/auth/session/login");
        log.setRequestParams("{}");
        log.setRemarks("ok");

        service.saveLog(log);

        assertNotNull(mqSender.message);
        String payload = String.valueOf(mqSender.message.getPayload());
        JsonNode json = OBJECT_MAPPER.readTree(payload);
        assertEquals("1", json.get("userId").asText());
        assertEquals("ACCESS", json.get("type").asText());
        assertFalse(json.get("userId").isObject(), json::toString);

        service.consumeLog(payload);

        assertNotNull(logService.command);
        assertEquals(UserIdCodec.toDomain(1L), logService.command.userId());
        assertEquals(LogType.ACCESS, logService.command.type());
        assertEquals(logDate, logService.command.logDate());
        assertEquals("系统-登录-成功", logService.command.title());
        assertEquals("/api/auth/session/login", logService.command.requestUri());
    }

    @Test
    void shouldPartitionLogFilesByExplicitGmt8Date() {
        CapturingMqSender mqSender = new CapturingMqSender();
        SysLogMessageServiceImpl service = new SysLogMessageServiceImpl(
                mqSender, properties(), new CapturingSystemLogApplicationService(), OBJECT_MAPPER);
        Log log = new Log();
        log.setType(LogType.ACCESS);
        log.setLogDate(Instant.parse("2026-06-17T16:30:00Z"));

        service.saveLog(log);
        service.consumeLog(String.valueOf(mqSender.message.getPayload()));

        assertTrue(Files.exists(tempDir.resolve("2026-06-18.log")));
    }

    private KuzhambuProperties properties() {
        KuzhambuProperties properties = new KuzhambuProperties();
        KuzhambuProperties.LogProperties logProperties = new KuzhambuProperties.LogProperties();
        logProperties.setStoragePath(tempDir.toString());
        properties.setLog(logProperties);
        return properties;
    }

    private static final class CapturingMqSender implements KuzhambuMqSender {

        private KuzhambuMqMessage message;

        @Override
        public void send(KuzhambuMqMessage message) {
            this.message = message;
        }
    }

    private static final class CapturingSystemLogApplicationService implements SystemLogApplicationService {

        private CreateLogCommand command;

        @Override
        public Log get(GetLogQuery query) {
            return null;
        }

        @Override
        public List<Log> list(LogQuery query) {
            return List.of();
        }

        @Override
        public PageResult<Log> page(LogQuery query, PageQuery page) {
            return PageResult.of(1, 10, 0, List.of());
        }

        @Override
        public LogId create(CreateLogCommand command) {
            this.command = command;
            return LogIdCodec.toDomain(1L);
        }

        @Override
        public int deleteByCondition(DeleteLogCommand command) {
            return 0;
        }
    }
}
