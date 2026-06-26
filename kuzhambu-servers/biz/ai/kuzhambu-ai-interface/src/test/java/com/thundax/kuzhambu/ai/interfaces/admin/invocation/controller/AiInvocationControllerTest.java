package com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.ai.application.batch.service.AiBatchJobApplicationService;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateDomainService;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.CandidateMarkAppliedRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request.AiInvocationRequests.CandidateRejectRequest;
import com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.response.AiInvocationResponses.CandidateResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class AiInvocationControllerTest {

    @Test
    void routesShouldKeepCandidateManagementApiPathsAndPermissions() throws Exception {
        assertRequestMapping(AiInvocationController.class, "/api/ai/invocation");
        assertPostMapping(
                AiInvocationController.class, "rejectCandidate", "candidate/reject", CandidateRejectRequest.class);
        assertPostMapping(
                AiInvocationController.class,
                "markCandidateApplied",
                "candidate/mark-applied",
                CandidateMarkAppliedRequest.class);
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
    void controllerShouldNotExposeApplyEndpoint() {
        assertThrows(
                NoSuchMethodException.class,
                () -> AiInvocationController.class.getDeclaredMethod(
                        "applyAiCandidate", CandidateMarkAppliedRequest.class));
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
            Class<?> controllerType, String methodName, String expectedPath, Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of("ai:invocation:edit"), List.of(permission.value()));
    }

    private static class FakeRepository implements AiInvocationRepository {
        @Override
        public com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord getCallRecord(Long callId) {
            return null;
        }

        @Override
        public Long saveCallRecord(com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord callRecord) {
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
        public AiCandidate getCandidate(Long candidateId) {
            return null;
        }

        @Override
        public Long saveCandidate(AiCandidate candidate) {
            return null;
        }

        @Override
        public int updateCandidate(AiCandidate candidate) {
            return 1;
        }

        @Override
        public List<com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate> listCandidates(
                String contentType, Long contentId, String capability, String status) {
            return java.util.Collections.emptyList();
        }
    }
}
