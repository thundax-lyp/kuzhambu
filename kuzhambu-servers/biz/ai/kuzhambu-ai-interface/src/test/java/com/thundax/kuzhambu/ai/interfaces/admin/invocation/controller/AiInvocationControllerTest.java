package com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.ai.application.batch.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateDomainService;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.CallRecordPageRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.CallSummaryRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.CandidateListRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.CandidateMarkAppliedRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.CandidateRejectRequest;
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
                "pageCallRecords",
                "call/page",
                "ai:invocation:view",
                CallRecordPageRequest.class);
        assertPostMapping(
                AiInvocationController.class,
                "summarizeCallRecords",
                "call/summary",
                "ai:invocation:view",
                CallSummaryRequest.class);
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
                new AiInvocationController(noRepository(), noBatchService(), rejectDomainService());
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
    void markAppliedShouldFallbackByCurrentCandidateAndMapToDomainService() {
        AiInvocationController controller =
                new AiInvocationController(currentInvocationRepository(), noBatchService(), markAppliedDomainService());
        CandidateMarkAppliedRequest request = new CandidateMarkAppliedRequest();
        request.setCandidateId(22L);

        CandidateResponse response = controller.markCandidateApplied(request);

        assertEquals(22L, response.getCandidateId());
        assertEquals("TEXT", response.getResultFormat());
        assertEquals("existing", response.getResultPayload());
        assertEquals("APPLIED", response.getStatus());
    }

    @Test
    void listCandidatesShouldPassObjectIdFilterToRepository() {
        AiInvocationRepository repository = new FakeRepository() {
            @Override
            public List<AiCandidate> listCandidates(
                    String contentType, Long contentId, Long objectId, String capability, String status) {
                assertEquals("ENTRY", contentType);
                assertEquals(9001L, contentId);
                assertEquals(10001L, objectId);
                assertEquals("summary", capability);
                assertEquals("PENDING", status);
                return List.of();
            }
        };
        AiInvocationController controller = new AiInvocationController(repository, noBatchService(), noDomainService());

        CandidateListRequest request = new CandidateListRequest();
        request.setContentType("ENTRY");
        request.setContentId(9001L);
        request.setObjectId(10001L);
        request.setCapability("summary");
        request.setStatus("PENDING");

        assertTrue(controller.listCandidates(request).isEmpty());
    }

    @Test
    void pageCallRecordsShouldPassFiltersToRepository() {
        AiInvocationRepository repository = new FakeRepository() {
            @Override
            public PageResult<AiCallRecord> pageCallRecords(
                    String scope,
                    String capability,
                    String contentType,
                    Long contentId,
                    String status,
                    String serviceRole,
                    String modelName,
                    Boolean fallbackUsed,
                    Instant requestedAtStart,
                    Instant requestedAtEnd,
                    int pageNo,
                    int pageSize) {
                assertEquals("classics", scope);
                assertEquals("summary", capability);
                assertEquals("ENTRY", contentType);
                assertEquals(1001L, contentId);
                assertEquals("FAILED", status);
                assertEquals("PRIMARY", serviceRole);
                assertEquals("gpt", modelName);
                assertEquals(Boolean.TRUE, fallbackUsed);
                assertEquals(Instant.parse("2026-07-01T00:00:00Z"), requestedAtStart);
                assertEquals(Instant.parse("2026-07-02T00:00:00Z"), requestedAtEnd);
                assertEquals(2, pageNo);
                assertEquals(5, pageSize);
                return PageResult.of(2, 5, 1, List.of(failedCallRecord()));
            }
        };
        AiInvocationController controller = new AiInvocationController(repository, noBatchService(), noDomainService());

        CallRecordPageRequest request = new CallRecordPageRequest();
        request.setScope("classics");
        request.setCapability("summary");
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

        var response = controller.pageCallRecords(request);

        assertEquals(2, response.getPageNo());
        assertEquals(1, response.getCount());
        assertEquals("FAILED", response.getRecords().get(0).getStatus());
        assertEquals("PRIMARY", response.getRecords().get(0).getServiceRole());
    }

    @Test
    void summarizeCallRecordsShouldAggregateRepositoryRecords() {
        AiInvocationRepository repository = new FakeRepository() {
            @Override
            public List<AiCallRecord> listCallRecords(
                    String scope,
                    String capability,
                    String serviceRole,
                    Instant requestedAtStart,
                    Instant requestedAtEnd) {
                assertEquals("classics", scope);
                assertEquals("summary", capability);
                assertEquals("PRIMARY", serviceRole);
                assertEquals(Instant.parse("2026-07-01T00:00:00Z"), requestedAtStart);
                assertEquals(Instant.parse("2026-07-02T00:00:00Z"), requestedAtEnd);
                return List.of(succeededCallRecord(), failedCallRecord());
            }
        };
        AiInvocationController controller = new AiInvocationController(repository, noBatchService(), noDomainService());

        CallSummaryRequest request = new CallSummaryRequest();
        request.setScope("classics");
        request.setCapability("summary");
        request.setServiceRole("PRIMARY");
        request.setPeriodStart(Instant.parse("2026-07-01T00:00:00Z"));
        request.setPeriodEnd(Instant.parse("2026-07-02T00:00:00Z"));

        var response = controller.summarizeCallRecords(request);

        assertEquals(2L, response.getInvocationCount());
        assertEquals(1L, response.getSucceededInvocationCount());
        assertEquals(1L, response.getFailedInvocationCount());
        assertEquals(150L, response.getAvgLatencyMs());
        assertEquals(new BigDecimal("0.30"), response.getTotalCostAmount());
        assertEquals("summary", response.getTopCapabilities().get(0).getCapability());
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
            public AiCandidate getCandidate(Long candidateId) {
                assertEquals(22L, candidateId);
                return currentCandidate();
            }
        };
    }

    private static AiCandidate currentCandidate() {
        AiCandidate candidate = new AiCandidate();
        candidate.setCandidateId(22L);
        candidate.setResultFormat("TEXT");
        candidate.setResultPayload("existing");
        candidate.setStatus("PENDING");
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
                        assertEquals(8801L, args[0]);
                        return new com.thundax.kuzhambu.ai.application.batch.result.AiBatchJobResult(
                                8801L,
                                "classics",
                                "image_analysis",
                                "SANCAI_ENTRY",
                                "CANCELLED",
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

    private static AiCandidateDomainService noDomainService() {
        return new AiCandidateDomainService(fakeRepository()) {
            @Override
            public AiCandidate reject(Long candidateId, String errorType, String errorMessage) {
                throw new UnsupportedOperationException("candidate domain service should not be called in this test");
            }

            @Override
            public AiCandidate markApplied(
                    Long candidateId, String resultFormat, String resultPayload, java.time.Instant appliedAt) {
                throw new UnsupportedOperationException("candidate domain service should not be called in this test");
            }
        };
    }

    private static AiCandidateDomainService rejectDomainService() {
        return new AiCandidateDomainService(fakeRepository()) {
            @Override
            public AiCandidate reject(Long candidateId, String errorType, String errorMessage) {
                assertEquals(11L, candidateId);
                assertEquals("TIMEOUT", errorType);
                assertEquals("执行超时", errorMessage);
                return rejectedCandidate();
            }

            @Override
            public AiCandidate markApplied(
                    Long candidateId, String resultFormat, String resultPayload, java.time.Instant appliedAt) {
                throw new UnsupportedOperationException("markApplied should not be called in this test");
            }
        };
    }

    private static AiCandidateDomainService markAppliedDomainService() {
        return new AiCandidateDomainService(fakeRepository()) {
            @Override
            public AiCandidate markApplied(
                    Long candidateId, String resultFormat, String resultPayload, java.time.Instant appliedAt) {
                assertEquals(22L, candidateId);
                assertEquals("TEXT", resultFormat);
                assertEquals("existing", resultPayload);
                return appliedCandidate();
            }

            @Override
            public AiCandidate reject(Long candidateId, String errorType, String errorMessage) {
                throw new UnsupportedOperationException("reject should not be called in this test");
            }
        };
    }

    private static FakeRepository fakeRepository() {
        return new FakeRepository() {};
    }

    private static AiCandidate rejectedCandidate() {
        AiCandidate candidate = new AiCandidate();
        candidate.setCandidateId(11L);
        candidate.setStatus("REJECTED");
        candidate.setErrorType("TIMEOUT");
        candidate.setErrorMessage("执行超时");
        return candidate;
    }

    private static AiCandidate appliedCandidate() {
        AiCandidate candidate = new AiCandidate();
        candidate.setCandidateId(22L);
        candidate.setResultFormat("TEXT");
        candidate.setResultPayload("existing");
        candidate.setStatus("APPLIED");
        return candidate;
    }

    private static AiCallRecord succeededCallRecord() {
        AiCallRecord record = new AiCallRecord();
        record.setScope("classics");
        record.setCapability("summary");
        record.setServiceRole("PRIMARY");
        record.setStatus("SUCCEEDED");
        record.setUsage(new AiUsageSnapshot(100, 10, 20, new BigDecimal("0.10")));
        return record;
    }

    private static AiCallRecord failedCallRecord() {
        AiCallRecord record = new AiCallRecord();
        record.setScope("classics");
        record.setCapability("summary");
        record.setServiceRole("PRIMARY");
        record.setStatus("FAILED");
        record.setFallbackUsed(true);
        record.setUsage(new AiUsageSnapshot(200, 30, 40, new BigDecimal("0.20")));
        return record;
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
        public com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord getCallRecord(Long callId) {
            return null;
        }

        @Override
        public Long insertCallRecord(com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord callRecord) {
            return null;
        }

        @Override
        public int updateCallRecord(com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord callRecord) {
            return 0;
        }

        @Override
        public List<com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord> listCallRecords(
                java.time.Instant requestedAtStart, java.time.Instant requestedAtEnd) {
            return java.util.Collections.emptyList();
        }

        @Override
        public PageResult<AiCallRecord> pageCallRecords(
                String scope,
                String capability,
                String contentType,
                Long contentId,
                String status,
                String serviceRole,
                String modelName,
                Boolean fallbackUsed,
                java.time.Instant requestedAtStart,
                java.time.Instant requestedAtEnd,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, java.util.Collections.emptyList());
        }

        @Override
        public List<AiCallRecord> listCallRecords(
                String scope,
                String capability,
                String serviceRole,
                java.time.Instant requestedAtStart,
                java.time.Instant requestedAtEnd) {
            return java.util.Collections.emptyList();
        }

        @Override
        public AiCandidate getCandidate(Long candidateId) {
            return null;
        }

        @Override
        public Long insertCandidate(AiCandidate candidate) {
            return null;
        }

        @Override
        public int updateCandidate(AiCandidate candidate) {
            return 1;
        }

        @Override
        public List<com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate> listCandidates(
                String contentType, Long contentId, Long objectId, String capability, String status) {
            return java.util.Collections.emptyList();
        }
    }
}
