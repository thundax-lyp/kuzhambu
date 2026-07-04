package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.discovery.application.qa.command.DeleteQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.ExportQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionExportResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSessionCsvExporter;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSourceAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.QaTraceAssembler;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSessionExport;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaMessageRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaRetrievalTraceRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionExportRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSourceRepository;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class QaApplicationServiceImplTest {

    private QaSessionExportRepository exportRepository;
    private StorageFacade storageFacade;

    @Test
    void openSessionShouldPersistAndReturnSessionResult() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        QaSourceRepository sourceRepository = mock(QaSourceRepository.class);
        QaRetrievalTraceRepository traceRepository = mock(QaRetrievalTraceRepository.class);
        QaApplicationServiceImpl service = new QaApplicationServiceImpl(
                sessionRepository,
                messageRepository,
                sourceRepository,
                traceRepository,
                mock(QaSessionExportRepository.class),
                mock(StorageFacade.class),
                new QaSessionCsvExporter(),
                new QaSourceAssembler(),
                new QaTraceAssembler());
        when(sessionRepository.save(any(QaSession.class))).thenReturn(9001L);

        var result = service.openSession(new OpenQaSessionCommand(
                1001L, "黄帝问答", "GLOBAL", "SEARCH", "SANCAI_ENTRY", 10001L, "req-1", "trace-1"));

        assertEquals(9001L, result.getSessionId());
        assertEquals("黄帝问答", result.getTitle());
        assertEquals(1001L, result.getOwnerUserId());
        verify(sessionRepository).save(any(QaSession.class));
    }

    @Test
    void getSessionDetailShouldAssembleMessages() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        QaSourceRepository sourceRepository = mock(QaSourceRepository.class);
        QaRetrievalTraceRepository traceRepository = mock(QaRetrievalTraceRepository.class);
        QaApplicationServiceImpl service = new QaApplicationServiceImpl(
                sessionRepository,
                messageRepository,
                sourceRepository,
                traceRepository,
                mock(QaSessionExportRepository.class),
                mock(StorageFacade.class),
                new QaSessionCsvExporter(),
                new QaSourceAssembler(),
                new QaTraceAssembler());

        QaSession session = new QaSession(
                1L,
                5001L,
                "USER",
                "1001",
                "kuzhambu-qa",
                "黄帝问答",
                "GLOBAL",
                "GENERAL",
                "SANCAI_ENTRY",
                10001L,
                "OPEN",
                new Date(),
                new Date(),
                null);
        when(sessionRepository.getBySessionId(5001L)).thenReturn(session);
        when(messageRepository.listBySessionId(5001L))
                .thenReturn(List.of(new QaMessage(
                        1L, 1L, 5001L, "USER", "黄帝是谁", "SENT", "kuzhambu-qa", 0, null, null, null, new Date(), null)));
        when(sourceRepository.listByMessageId(any())).thenReturn(List.of());
        when(traceRepository.getByTraceId(any())).thenReturn(new QaRetrievalTrace());

        QaSessionDetailResult result = service.getSessionDetail(5001L);

        assertEquals(5001L, result.getSessionId());
        assertEquals("黄帝问答", result.getTitle());
        assertEquals(1, result.getMessages().size());
        verify(messageRepository).listBySessionId(5001L);
    }

    @Test
    void deleteSessionShouldMarkOwnedSessionRemoved() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaApplicationServiceImpl service = service(sessionRepository);
        when(sessionRepository.getBySessionId(5001L)).thenReturn(openSession());
        when(sessionRepository.markRemoved(eq(5001L), any(Date.class))).thenReturn(1);

        service.deleteSession(new DeleteQaSessionCommand(5001L, "USER", "1001", false));

        verify(sessionRepository).markRemoved(eq(5001L), any(Date.class));
    }

    @Test
    void deleteSessionShouldSkipOwnerCheckForAdminOperation() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaApplicationServiceImpl service = service(sessionRepository);
        when(sessionRepository.getBySessionId(5001L)).thenReturn(openSession());
        when(sessionRepository.markRemoved(eq(5001L), any(Date.class))).thenReturn(1);

        service.deleteSession(new DeleteQaSessionCommand(5001L, null, null, true));

        verify(sessionRepository).markRemoved(eq(5001L), any(Date.class));
    }

    @Test
    void deleteSessionShouldRejectRepeatedDeletion() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaApplicationServiceImpl service = service(sessionRepository);
        QaSession session = openSession();
        session.markRemoved(new Date());
        when(sessionRepository.getBySessionId(5001L)).thenReturn(session);

        BizException exception = assertThrows(
                BizException.class,
                () -> service.deleteSession(new DeleteQaSessionCommand(5001L, "USER", "1001", false)));

        assertEquals("QA_SESSION_ALREADY_REMOVED", exception.getCode());
        verify(sessionRepository, never()).markRemoved(eq(5001L), any(Date.class));
    }

    @Test
    void deleteSessionShouldRejectWhenConcurrentDeletionWins() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaApplicationServiceImpl service = service(sessionRepository);
        QaSession latest = openSession();
        latest.markRemoved(new Date());
        when(sessionRepository.getBySessionId(5001L)).thenReturn(openSession(), latest);
        when(sessionRepository.markRemoved(eq(5001L), any(Date.class))).thenReturn(0);

        BizException exception = assertThrows(
                BizException.class,
                () -> service.deleteSession(new DeleteQaSessionCommand(5001L, "USER", "1001", false)));

        assertEquals("QA_SESSION_ALREADY_REMOVED", exception.getCode());
        verify(sessionRepository).markRemoved(eq(5001L), any(Date.class));
    }

    @Test
    void deleteSessionShouldRejectOwnerMismatch() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaApplicationServiceImpl service = service(sessionRepository);
        when(sessionRepository.getBySessionId(5001L)).thenReturn(openSession());

        BizException exception = assertThrows(
                BizException.class,
                () -> service.deleteSession(new DeleteQaSessionCommand(5001L, "USER", "2002", false)));

        assertEquals("DISCOVERY-30009", exception.getCode());
        verify(sessionRepository, never()).markRemoved(eq(5001L), any(Date.class));
    }

    @Test
    void getPortalSessionDetailShouldRejectRemovedSession() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaApplicationServiceImpl service = service(sessionRepository);
        QaSession session = openSession();
        session.markRemoved(new Date());
        when(sessionRepository.getBySessionId(5001L)).thenReturn(session);

        BizException exception =
                assertThrows(BizException.class, () -> service.getPortalSessionDetail(5001L, "USER", "1001"));

        assertEquals("QA_SESSION_ALREADY_REMOVED", exception.getCode());
    }

    @Test
    void listPortalSessionsShouldReturnOwnedSessions() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaApplicationServiceImpl service = service(sessionRepository);
        when(sessionRepository.listByOwnerUserId("USER", "1001", 10)).thenReturn(List.of(openSession()));

        List<QaSessionResult> results = service.listPortalSessions("USER", "1001", 10);

        assertEquals(1, results.size());
        assertEquals(5001L, results.get(0).getSessionId());
    }

    @Test
    void exportSessionShouldUploadEscapedCsvAndReturnSucceededResult() throws Exception {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        QaSourceRepository sourceRepository = mock(QaSourceRepository.class);
        QaRetrievalTraceRepository traceRepository = mock(QaRetrievalTraceRepository.class);
        QaApplicationServiceImpl service =
                service(sessionRepository, messageRepository, sourceRepository, traceRepository);
        when(sessionRepository.getBySessionId(5001L)).thenReturn(openSession());
        when(messageRepository.listBySessionId(5001L)).thenReturn(List.of(answerMessage()));
        when(sourceRepository.listByMessageId(6001L)).thenReturn(List.of(source()));
        when(traceRepository.getByMessageId(6001L)).thenReturn(trace());
        when(exportRepository.save(any(QaSessionExport.class))).thenReturn(7001L);
        when(storageFacade.upload(any(UploadStorageFacadeRequest.class)))
                .thenReturn(UploadStorageFacadeResponse.builder()
                        .storageObjectId(8001L)
                        .originalFilename("discovery-qa-session-5001-7001.csv")
                        .contentType("text/csv; charset=UTF-8")
                        .build());

        QaSessionExportResult result =
                service.exportSession(new ExportQaSessionCommand(5001L, 1001L, "USER", "1001", false, "csv"));

        assertEquals(7001L, result.getExportId());
        assertEquals(8001L, result.getStorageObjectId());
        assertEquals("SUCCEEDED", result.getExportStatus());
        assertEquals("discovery-qa-session-5001-7001.csv", result.getFilename());
        ArgumentCaptor<UploadStorageFacadeRequest> uploadCaptor =
                ArgumentCaptor.forClass(UploadStorageFacadeRequest.class);
        verify(storageFacade).upload(uploadCaptor.capture());
        UploadStorageFacadeRequest uploadRequest = uploadCaptor.getValue();
        assertEquals("DISCOVERY_QA_SESSION_EXPORT", uploadRequest.getOwnerType());
        assertEquals("session:5001:export:7001", uploadRequest.getOwnerId());
        assertEquals("discovery-qa-session-5001-7001.csv", uploadRequest.getOriginalFilename());
        assertEquals("text/csv; charset=UTF-8", uploadRequest.getContentType());
        assertEquals(List.of("csv"), uploadRequest.getAllowedSuffixes());
        String csv = new String(uploadRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("\"rowType\",\"sessionId\",\"title\""));
        assertTrue(csv.contains("\"他说\"\"引号\"\"，并换行\n第二行\""));
        assertTrue(csv.contains("\"SOURCE\""));
        assertTrue(csv.contains("\"TRACE\""));
        ArgumentCaptor<QaSessionExport> exportCaptor = ArgumentCaptor.forClass(QaSessionExport.class);
        verify(exportRepository).update(exportCaptor.capture());
        assertEquals("SUCCEEDED", exportCaptor.getValue().getExportStatus());
    }

    @Test
    void exportSessionShouldRejectRemovedPortalSession() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaApplicationServiceImpl service = service(sessionRepository);
        QaSession session = openSession();
        session.markRemoved(new Date());
        when(sessionRepository.getBySessionId(5001L)).thenReturn(session);

        BizException exception = assertThrows(
                BizException.class,
                () -> service.exportSession(new ExportQaSessionCommand(5001L, 1001L, "USER", "1001", false, "CSV")));

        assertEquals("QA_SESSION_ALREADY_REMOVED", exception.getCode());
        verify(exportRepository, never()).save(any(QaSessionExport.class));
        verify(storageFacade, never()).upload(any(UploadStorageFacadeRequest.class));
    }

    @Test
    void exportSessionShouldAllowRemovedAdminSession() throws Exception {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        QaSourceRepository sourceRepository = mock(QaSourceRepository.class);
        QaRetrievalTraceRepository traceRepository = mock(QaRetrievalTraceRepository.class);
        QaApplicationServiceImpl service =
                service(sessionRepository, messageRepository, sourceRepository, traceRepository);
        QaSession session = openSession();
        session.markRemoved(new Date());
        when(sessionRepository.getBySessionId(5001L)).thenReturn(session);
        when(messageRepository.listBySessionId(5001L)).thenReturn(List.of(answerMessage()));
        when(sourceRepository.listByMessageId(6001L)).thenReturn(List.of(source()));
        when(traceRepository.getByMessageId(6001L)).thenReturn(trace());
        when(exportRepository.save(any(QaSessionExport.class))).thenReturn(7001L);
        when(storageFacade.upload(any(UploadStorageFacadeRequest.class)))
                .thenReturn(UploadStorageFacadeResponse.builder()
                        .storageObjectId(8001L)
                        .build());

        QaSessionExportResult result =
                service.exportSession(new ExportQaSessionCommand(5001L, 1001L, null, null, true, "CSV"));

        assertEquals("SUCCEEDED", result.getExportStatus());
        ArgumentCaptor<UploadStorageFacadeRequest> uploadCaptor =
                ArgumentCaptor.forClass(UploadStorageFacadeRequest.class);
        verify(storageFacade).upload(uploadCaptor.capture());
        String csv = new String(uploadCaptor.getValue().getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(csv.contains("\"SESSION\""));
        assertTrue(csv.contains("\"REMOVED\""));
    }

    @Test
    void exportSessionShouldMarkFailedWhenStorageUploadFails() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaApplicationServiceImpl service = service(sessionRepository);
        when(sessionRepository.getBySessionId(5001L)).thenReturn(openSession());
        when(exportRepository.save(any(QaSessionExport.class))).thenReturn(7001L);
        doThrow(new IllegalStateException("storage down"))
                .when(storageFacade)
                .upload(any(UploadStorageFacadeRequest.class));

        QaSessionExportResult result =
                service.exportSession(new ExportQaSessionCommand(5001L, 1001L, "USER", "1001", false, "CSV"));

        assertEquals("FAILED", result.getExportStatus());
        assertEquals("storage down", result.getFailureReason());
        ArgumentCaptor<QaSessionExport> exportCaptor = ArgumentCaptor.forClass(QaSessionExport.class);
        verify(exportRepository).update(exportCaptor.capture());
        assertEquals("FAILED", exportCaptor.getValue().getExportStatus());
        assertEquals("storage down", exportCaptor.getValue().getFailureReason());
    }

    @BeforeEach
    void setUp() {
        exportRepository = mock(QaSessionExportRepository.class);
        storageFacade = mock(StorageFacade.class);
    }

    private QaApplicationServiceImpl service(QaSessionRepository sessionRepository) {
        return new QaApplicationServiceImpl(
                sessionRepository,
                mock(QaMessageRepository.class),
                mock(QaSourceRepository.class),
                mock(QaRetrievalTraceRepository.class),
                exportRepository,
                storageFacade,
                new QaSessionCsvExporter(),
                new QaSourceAssembler(),
                new QaTraceAssembler());
    }

    private QaApplicationServiceImpl service(
            QaSessionRepository sessionRepository,
            QaMessageRepository messageRepository,
            QaSourceRepository sourceRepository,
            QaRetrievalTraceRepository traceRepository) {
        return new QaApplicationServiceImpl(
                sessionRepository,
                messageRepository,
                sourceRepository,
                traceRepository,
                exportRepository,
                storageFacade,
                new QaSessionCsvExporter(),
                new QaSourceAssembler(),
                new QaTraceAssembler());
    }

    private static QaSession openSession() {
        return new QaSession(
                1L,
                5001L,
                "USER",
                "1001",
                "kuzhambu-qa",
                "黄帝问答",
                "GLOBAL",
                "GENERAL",
                "SANCAI_ENTRY",
                10001L,
                "OPEN",
                new Date(),
                new Date(),
                null);
    }

    private static QaMessage answerMessage() {
        return new QaMessage(
                1L,
                6001L,
                5001L,
                "ASSISTANT",
                "他说\"引号\"，并换行\n第二行",
                "ANSWERED",
                "deepseek",
                1,
                null,
                "chat-1",
                "STOP",
                new Date(1000L),
                new Date(2000L));
    }

    private static QaSource source() {
        return new QaSource(
                1L,
                6101L,
                "SANCAI_ENTRY:1",
                6001L,
                "SANCAI_ENTRY",
                1L,
                "kuzhambu-qa",
                "素问, \"上古天真论\"",
                "卷一",
                "片段",
                "/sancai/1",
                1,
                null,
                "ACTIVE",
                new Date(3000L));
    }

    private static QaRetrievalTrace trace() {
        return new QaRetrievalTrace(
                1L,
                6201L,
                6001L,
                "黄帝是谁",
                "dify",
                "kb-1",
                "item-1",
                "chat-1",
                "provider-request-1",
                100L,
                null,
                "{}",
                new Date(4000L));
    }
}
