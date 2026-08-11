package com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.ai.application.invocation.command.ApplyAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.CancelAiBatchJobCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.RejectAiCandidateCommand;
import com.thundax.kuzhambu.ai.application.invocation.query.RequireAiCandidateForApplyQuery;
import com.thundax.kuzhambu.ai.application.invocation.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.service.AiCandidateApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiCandidateStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.CallIdRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.CandidateIdRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.CandidateListRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.CandidateMarkAppliedRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.CandidateRejectRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.InvocationLogPageRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.InvocationSummaryRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.response.AiInvocationResponses.CandidateResponse;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class AiInvocationControllerTest {

    @Test
    void routesShouldKeepCandidateManagementApiPathsAndPermissions() throws Exception {
        assertRequestMapping(AiInvocationController.class, "/api/ai/invocation");
        assertPostMapping(
                AiInvocationController.class,
                "pageInvocationLogs",
                "invocation-log/page",
                "ai:invocation:view",
                InvocationLogPageRequest.class);
        assertPostMapping(
                AiInvocationController.class,
                "summarizeInvocationLogs",
                "invocation-log/summary",
                "ai:invocation:view",
                InvocationSummaryRequest.class);
        assertPostMapping(
                AiInvocationController.class,
                "listCandidates",
                "candidate/list",
                "ai:invocation:view",
                CandidateListRequest.class);
        assertPostMapping(
                AiInvocationController.class,
                "rejectCandidate",
                "candidate/reject",
                "ai:invocation:edit",
                CandidateRejectRequest.class);
        assertPostMapping(
                AiInvocationController.class,
                "markCandidateApplied",
                "candidate/mark-applied",
                "ai:invocation:edit",
                CandidateMarkAppliedRequest.class);
        assertPostMapping(
                AiInvocationController.class,
                "cancelBatch",
                "batch/cancel",
                "ai:invocation:edit",
                com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests
                        .BatchIdRequest.class);
    }

    @Test
    void rejectCandidateShouldMapToDomainService() {
        AiInvocationController controller =
                new AiInvocationController(noRepository(), noBatchService(), rejectCandidateService());
        CandidateRejectRequest request = new CandidateRejectRequest();
        request.setCandidateId(11L);
        request.setErrorType("TIMEOUT");
        request.setErrorMessage("执行超时");

        CandidateResponse response = controller.rejectCandidate(request);

        assertEquals(11L, response.getCandidateId());
        assertEquals("REJECTED", response.getStatus());
        assertEquals("TIMEOUT", response.getErrorType());
    }

    @Test
    void markAppliedShouldDelegateToCandidateApplicationService() {
        AiInvocationController controller = new AiInvocationController(
                currentInvocationRepository(), noBatchService(), markAppliedCandidateService());
        CandidateMarkAppliedRequest request = new CandidateMarkAppliedRequest();
        request.setCandidateId(22L);

        CandidateResponse response = controller.markCandidateApplied(request);

        assertEquals(22L, response.getCandidateId());
        assertEquals("TEXT", response.getResultFormat());
        assertEquals("existing", response.getResultPayload());
        assertEquals("APPLIED", response.getStatus());
    }

    @Test
    void getInvocationLogShouldReturnEmptyResponseWhenLogIsMissing() {
        AiInvocationController controller =
                new AiInvocationController(noRepository(), noBatchService(), noDomainService());
        CallIdRequest request = new CallIdRequest();
        request.setCallId(701L);

        var response = controller.getInvocationLog(request);

        assertNull(response.getCallId());
    }

    @Test
    void getCandidateShouldReturnEmptyResponseWhenCandidateIsMissing() {
        AiInvocationController controller =
                new AiInvocationController(noRepository(), noBatchService(), noDomainService());
        CandidateIdRequest request = new CandidateIdRequest();
        request.setCandidateId(801L);

        var response = controller.getCandidate(request);

        assertNull(response.getCandidateId());
    }

    @Test
    void listCandidatesShouldPassObjectIdFilterToRepository() {
        AiInvocationRepository repository = new FakeRepository() {
            @Override
            public List<AiCandidate> listCandidates(
                    AiContentRef contentRef,
                    AiTargetObjectId targetObjectId,
                    AiBusinessCapability capability,
                    AiCandidateStatus status) {
                assertEquals(AiContentRef.ofNullable("ENTRY", 9001L), contentRef);
                assertEquals(AiTargetObjectIdCodec.toDomain(10001L), targetObjectId);
                assertEquals(AiBusinessCapability.CLASSICS_SUMMARY, capability);
                assertEquals(AiCandidateStatus.PENDING, status);
                return List.of();
            }
        };
        AiInvocationController controller = new AiInvocationController(repository, noBatchService(), noDomainService());

        CandidateListRequest request = new CandidateListRequest();
        request.setContentType("ENTRY");
        request.setContentId(9001L);
        request.setObjectId(10001L);
        request.setCapability("CLASSICS_SUMMARY");
        request.setStatus("PENDING");

        assertTrue(controller.listCandidates(request).isEmpty());
    }

    @Test
    void pageInvocationLogsShouldPassFiltersToRepository() {
        AiInvocationRepository repository = new FakeRepository() {
            @Override
            public PageResult<AiInvocationLog> pageInvocationLogsByFilter(
                    String scope,
                    AiBusinessCapability capability,
                    AiContentRef contentRef,
                    AiInvocationStatus status,
                    String serviceRole,
                    AiModelName modelName,
                    Boolean fallbackUsed,
                    Instant requestedAtStart,
                    Instant requestedAtEnd,
                    int pageNo,
                    int pageSize) {
                assertEquals("classics", scope);
                assertEquals(AiBusinessCapability.CLASSICS_SUMMARY, capability);
                assertEquals(AiContentRef.ofNullable("ENTRY", 1001L), contentRef);
                assertEquals(AiInvocationStatus.FAILED, status);
                assertEquals("PRIMARY", serviceRole);
                assertEquals(AiModelName.of("gpt"), modelName);
                assertEquals(Boolean.TRUE, fallbackUsed);
                assertEquals(Instant.parse("2026-07-01T00:00:00Z"), requestedAtStart);
                assertEquals(Instant.parse("2026-07-02T00:00:00Z"), requestedAtEnd);
                assertEquals(2, pageNo);
                assertEquals(5, pageSize);
                return PageResult.of(2, 5, 1, List.of(failedInvocationLog()));
            }
        };
        AiInvocationController controller = new AiInvocationController(repository, noBatchService(), noDomainService());

        InvocationLogPageRequest request = new InvocationLogPageRequest();
        request.setScope("classics");
        request.setCapability("CLASSICS_SUMMARY");
        request.setContentType("ENTRY");
        request.setContentId(1001L);
        request.setStatus("FAILED");
        request.setServiceRole("PRIMARY");
        request.setModelName("gpt");
        request.setFallbackUsed(Boolean.TRUE);
        request.setRequestedAtStart(Instant.parse("2026-07-01T00:00:00Z"));
        request.setRequestedAtEnd(Instant.parse("2026-07-02T00:00:00Z"));
        request.setPageNo(2);
        request.setPageSize(5);

        var response = controller.pageInvocationLogs(request);

        assertEquals(2, response.getPageNo());
        assertEquals(1, response.getCount());
        assertEquals("FAILED", response.getRecords().get(0).getStatus());
        assertEquals("PRIMARY", response.getRecords().get(0).getServiceRole());
    }

    @Test
    void summarizeInvocationLogsShouldAggregateRepositoryRecords() {
        AiInvocationRepository repository = new FakeRepository() {
            @Override
            public List<AiInvocationLog> listInvocationLogs(
                    String scope,
                    AiBusinessCapability capability,
                    String serviceRole,
                    Instant requestedAtStart,
                    Instant requestedAtEnd) {
                assertEquals("classics", scope);
                assertEquals(AiBusinessCapability.CLASSICS_SUMMARY, capability);
                assertEquals("PRIMARY", serviceRole);
                assertEquals(Instant.parse("2026-07-01T00:00:00Z"), requestedAtStart);
                assertEquals(Instant.parse("2026-07-02T00:00:00Z"), requestedAtEnd);
                return List.of(succeededInvocationLog(), failedInvocationLog());
            }
        };
        AiInvocationController controller = new AiInvocationController(repository, noBatchService(), noDomainService());

        InvocationSummaryRequest request = new InvocationSummaryRequest();
        request.setScope("classics");
        request.setCapability("CLASSICS_SUMMARY");
        request.setServiceRole("PRIMARY");
        request.setPeriodStart(Instant.parse("2026-07-01T00:00:00Z"));
        request.setPeriodEnd(Instant.parse("2026-07-02T00:00:00Z"));

        var response = controller.summarizeInvocationLogs(request);

        assertEquals(2L, response.getInvocationCount());
        assertEquals(1L, response.getSucceededInvocationCount());
        assertEquals(1L, response.getFailedInvocationCount());
        assertEquals(150L, response.getAvgLatencyMs());
        assertEquals(new BigDecimal("0.30"), response.getTotalCostAmount());
        assertEquals(
                AiBusinessCapability.CLASSICS_SUMMARY.value(),
                response.getTopCapabilities().get(0).getCapability());
        assertEquals(2L, response.getTopCapabilities().get(0).getInvocationCount());
    }

    @Test
    void controllerShouldNotExposeApplyEndpoint() {
        assertThrows(
                NoSuchMethodException.class,
                () -> AiInvocationController.class.getDeclaredMethod(
                        "applyAiCandidate", CandidateMarkAppliedRequest.class));
    }

    @Test
    void cancelBatchShouldDelegateToBatchService() {
        AiInvocationController controller =
                new AiInvocationController(noRepository(), cancelBatchService(), noDomainService());
        com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.BatchIdRequest
                request = new com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request
                        .AiInvocationRequests.BatchIdRequest();
        request.setBatchId(8801L);

        var response = controller.cancelBatch(request);

        assertEquals(8801L, response.getBatchId());
        assertEquals("CANCELLED", response.getStatus());
        assertEquals(1, response.getCancelledCount());
    }

    private static AiInvocationRepository noRepository() {
        return fakeRepository();
    }

    private static AiInvocationRepository currentInvocationRepository() {
        return new FakeRepository() {
            @Override
            public AiCandidate getCandidateById(AiCandidateId candidateId) {
                assertEquals(AiCandidateIdCodec.toDomain(22L), candidateId);
                return currentCandidate();
            }
        };
    }

    private static AiCandidate currentCandidate() {
        AiCandidate candidate = new AiCandidate();
        candidate.setId(AiCandidateIdCodec.toDomain(22L));
        candidate.setResultFormat("TEXT");
        candidate.setResultPayload("existing");
        candidate.setStatus(AiCandidateStatus.PENDING);
        return candidate;
    }

    private static AiBatchJobApplicationService noBatchService() {
        return (AiBatchJobApplicationService) Proxy.newProxyInstance(
                AiBatchJobApplicationService.class.getClassLoader(),
                new Class<?>[] {AiBatchJobApplicationService.class},
                noOpInvocationHandler("batch service"));
    }

    private static AiBatchJobApplicationService cancelBatchService() {
        return (AiBatchJobApplicationService) Proxy.newProxyInstance(
                AiBatchJobApplicationService.class.getClassLoader(),
                new Class<?>[] {AiBatchJobApplicationService.class},
                (proxy, method, args) -> {
                    if ("cancel".equals(method.getName())) {
                        assertEquals(new AiBatchJobId(8801L), ((CancelAiBatchJobCommand) args[0]).batchId());
                        return new com.thundax.kuzhambu.ai.application.invocation.result.AiBatchJobResult(
                                new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId(8801L),
                                "classics",
                                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability
                                        .CLASSICS_IMAGE_DESCRIBE,
                                com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef.of(
                                        "SANCAI_ENTRY", 3001L),
                                com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus.CANCELLED,
                                1,
                                0,
                                0,
                                1,
                                null,
                                null,
                                java.time.Instant.parse("2026-07-01T00:00:00Z"),
                                null);
                    }
                    throw new UnsupportedOperationException(
                            "batch service should not be called in this test: " + method.getName());
                });
    }

    private static AiCandidateApplicationService noDomainService() {
        return new AiCandidateApplicationService() {
            @Override
            public AiCandidate requirePendingForApply(RequireAiCandidateForApplyQuery query) {
                throw new UnsupportedOperationException(
                        "candidate application service should not be called in this test");
            }

            @Override
            public AiCandidate markApplied(ApplyAiCandidateCommand command) {
                throw new UnsupportedOperationException(
                        "candidate application service should not be called in this test");
            }

            @Override
            public AiCandidate reject(RejectAiCandidateCommand command) {
                throw new UnsupportedOperationException(
                        "candidate application service should not be called in this test");
            }
        };
    }

    private static AiCandidateApplicationService rejectCandidateService() {
        return new AiCandidateApplicationService() {
            @Override
            public AiCandidate requirePendingForApply(RequireAiCandidateForApplyQuery query) {
                throw new UnsupportedOperationException("requirePendingForApply should not be called in this test");
            }

            @Override
            public AiCandidate markApplied(ApplyAiCandidateCommand command) {
                throw new UnsupportedOperationException("markApplied should not be called in this test");
            }

            @Override
            public AiCandidate reject(RejectAiCandidateCommand command) {
                assertEquals(new AiCandidateId(11L), command.candidateId());
                assertEquals("TIMEOUT", command.errorType());
                assertEquals("执行超时", command.errorMessage());
                return rejectedCandidate();
            }
        };
    }

    private static AiCandidateApplicationService markAppliedCandidateService() {
        return new AiCandidateApplicationService() {
            @Override
            public AiCandidate requirePendingForApply(RequireAiCandidateForApplyQuery query) {
                throw new UnsupportedOperationException("requirePendingForApply should not be called in this test");
            }

            @Override
            public AiCandidate markApplied(ApplyAiCandidateCommand command) {
                assertEquals(new AiCandidateId(22L), command.candidateId());
                assertEquals(null, command.resultFormat());
                assertEquals(null, command.resultPayload());
                assertEquals(null, command.appliedAt());
                return appliedCandidate();
            }

            @Override
            public AiCandidate reject(RejectAiCandidateCommand command) {
                throw new UnsupportedOperationException("reject should not be called in this test");
            }
        };
    }

    private static FakeRepository fakeRepository() {
        return new FakeRepository() {};
    }

    private static AiCandidate rejectedCandidate() {
        AiCandidate candidate = new AiCandidate();
        candidate.setId(AiCandidateIdCodec.toDomain(11L));
        candidate.setStatus(AiCandidateStatus.REJECTED);
        candidate.setErrorType("TIMEOUT");
        candidate.setErrorMessage("执行超时");
        return candidate;
    }

    private static AiCandidate appliedCandidate() {
        AiCandidate candidate = new AiCandidate();
        candidate.setId(AiCandidateIdCodec.toDomain(22L));
        candidate.setResultFormat("TEXT");
        candidate.setResultPayload("existing");
        candidate.setStatus(AiCandidateStatus.APPLIED);
        return candidate;
    }

    private static AiInvocationLog succeededInvocationLog() {
        AiInvocationLog invocationLog = new AiInvocationLog();
        invocationLog.setScope("classics");
        invocationLog.setCapability(AiBusinessCapability.CLASSICS_SUMMARY);
        invocationLog.setServiceRole("PRIMARY");
        invocationLog.setStatus(AiInvocationStatus.SUCCEEDED);
        invocationLog.setUsage(new AiUsageSnapshot(100, 10, 20, new BigDecimal("0.10")));
        return invocationLog;
    }

    private static AiInvocationLog failedInvocationLog() {
        AiInvocationLog invocationLog = new AiInvocationLog();
        invocationLog.setScope("classics");
        invocationLog.setCapability(AiBusinessCapability.CLASSICS_SUMMARY);
        invocationLog.setServiceRole("PRIMARY");
        invocationLog.setStatus(AiInvocationStatus.FAILED);
        invocationLog.setFallbackUsed(true);
        invocationLog.setUsage(new AiUsageSnapshot(200, 30, 40, new BigDecimal("0.20")));
        return invocationLog;
    }

    private static InvocationHandler noOpInvocationHandler(String name) {
        return (proxy, method, args) -> {
            throw new UnsupportedOperationException(name + " should not be called in this test: " + method.getName());
        };
    }

    private static void assertRequestMapping(Class<?> controllerType, String expectedPath) {
        RequestMapping mapping = controllerType.getAnnotation(RequestMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertPostMapping(
            Class<?> controllerType,
            String methodName,
            String expectedPath,
            String expectedPermission,
            Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of(expectedPermission), List.of(permission.value()));
    }

    private static class FakeRepository implements AiInvocationRepository {
        @Override
        public com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog getInvocationLogById(
                AiCallId callId) {
            return null;
        }

        @Override
        public AiCallId insertInvocationLog(
                com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog invocationLog) {
            return null;
        }

        @Override
        public int updateInvocationLog(
                com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog invocationLog) {
            return 0;
        }

        @Override
        public List<com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog> listInvocationLogs(
                java.time.Instant requestedAtStart, java.time.Instant requestedAtEnd) {
            return java.util.Collections.emptyList();
        }

        @Override
        public List<AiInvocationLog> listInvocationLogsByBatch(
                com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId batchId) {
            return java.util.Collections.emptyList();
        }

        @Override
        public PageResult<AiInvocationLog> pageInvocationLogsByFilter(
                String scope,
                AiBusinessCapability capability,
                AiContentRef contentRef,
                AiInvocationStatus status,
                String serviceRole,
                AiModelName modelName,
                Boolean fallbackUsed,
                java.time.Instant requestedAtStart,
                java.time.Instant requestedAtEnd,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, java.util.Collections.emptyList());
        }

        @Override
        public List<AiInvocationLog> listInvocationLogs(
                String scope,
                AiBusinessCapability capability,
                String serviceRole,
                java.time.Instant requestedAtStart,
                java.time.Instant requestedAtEnd) {
            return java.util.Collections.emptyList();
        }

        @Override
        public AiCandidate getCandidateById(AiCandidateId candidateId) {
            return null;
        }

        @Override
        public AiCandidateId insertCandidate(AiCandidate candidate) {
            return null;
        }

        @Override
        public int updateCandidate(AiCandidate candidate) {
            return 1;
        }

        @Override
        public List<com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate> listCandidates(
                AiContentRef contentRef,
                AiTargetObjectId targetObjectId,
                AiBusinessCapability capability,
                AiCandidateStatus status) {
            return java.util.Collections.emptyList();
        }

        @Override
        public List<AiCandidate> listCandidatesByBatch(
                com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId batchId) {
            return java.util.Collections.emptyList();
        }
    }
}
