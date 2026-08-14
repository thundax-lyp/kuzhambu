package com.thundax.kuzhambu.system.application.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.system.application.audit.command.CreateAuditLogCommand;
import com.thundax.kuzhambu.system.application.audit.query.GetAuditLogQuery;
import com.thundax.kuzhambu.system.application.audit.service.AuditTrailApplicationService;
import com.thundax.kuzhambu.system.application.facade.assembler.SystemAuditFacadeAssembler;
import com.thundax.kuzhambu.system.domain.audit.codec.AuditLogIdCodec;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
import com.thundax.kuzhambu.system.facade.request.SystemAuditFacadeRequest;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SystemAuditFacadeImplTest {

    @Test
    void recordShouldMapFacadeProtocolToAuditCommand() {
        AuditTrailApplicationService auditTrailApplicationService = mock(AuditTrailApplicationService.class);
        when(auditTrailApplicationService.record(any(CreateAuditLogCommand.class)))
                .thenReturn(AuditLogIdCodec.toDomain(1001L));
        SystemAuditFacadeImpl facade =
                new SystemAuditFacadeImpl(auditTrailApplicationService, new SystemAuditFacadeAssembler());

        Long auditLogId = facade.record(new SystemAuditFacadeRequest(
                "GRAPH_NODE",
                "node-1",
                "UPDATE",
                "graph-node:node-1:update:7",
                "USER",
                "9001",
                "张三",
                "ADMIN_WEB",
                "request-1",
                "trace-1",
                "127.0.0.1",
                "更新图谱节点",
                "{\"objectType\":\"GRAPH_NODE\",\"objectId\":\"node-1\",\"displayName\":\"旧节点\"}",
                "{\"objectType\":\"GRAPH_NODE\",\"objectId\":\"node-1\",\"displayName\":\"新节点\"}",
                true));

        ArgumentCaptor<CreateAuditLogCommand> captor = ArgumentCaptor.forClass(CreateAuditLogCommand.class);
        verify(auditTrailApplicationService).record(captor.capture());
        CreateAuditLogCommand command = captor.getValue();
        assertEquals(1001L, auditLogId);
        assertEquals(AuditObjectRef.of("GRAPH_NODE", "node-1"), command.objectRef());
        assertEquals(AuditAction.UPDATE, command.action());
        assertEquals(AuditOperatorRef.of(AuditOperatorType.USER, "9001"), command.operatorRef());
        assertEquals("旧节点", command.beforeSnapshot().getDisplayName());
        assertEquals("新节点", command.afterSnapshot().getDisplayName());
    }

    @Test
    void getShouldExposeOperatorAndOccurredAt() {
        AuditTrailApplicationService auditTrailApplicationService = mock(AuditTrailApplicationService.class);
        AuditLogId auditLogId = AuditLogIdCodec.toDomain(1001L);
        AuditLog log = new AuditLog();
        log.setId(auditLogId);
        log.setObjectRef(AuditObjectRef.of("GRAPH_NODE", "node-1"));
        log.setAction(AuditAction.UPDATE);
        log.setOperatorRef(AuditOperatorRef.of(AuditOperatorType.USER, "9001"));
        log.setOperatorName("张三");
        log.setSource("ADMIN_WEB");
        log.setRequestId("request-1");
        log.setTraceId("trace-1");
        log.setRemoteAddr("127.0.0.1");
        log.setSummary("更新图谱节点");
        log.setOccurredAt(Instant.parse("2026-08-14T00:00:00Z"));
        when(auditTrailApplicationService.getLog(new GetAuditLogQuery(auditLogId)))
                .thenReturn(log);
        SystemAuditFacadeImpl facade =
                new SystemAuditFacadeImpl(auditTrailApplicationService, new SystemAuditFacadeAssembler());

        var response = facade.get(1001L);

        assertEquals(1001L, response.auditLogId());
        assertEquals("9001", response.operatorId());
        assertEquals("张三", response.operatorName());
        assertEquals(Instant.parse("2026-08-14T00:00:00Z"), response.occurredAt());
    }
}
