package com.thundax.kuzhambu.classics.interfaces.admin.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationCreateResult;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationApplicationService;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.ClassicsPublicationActionController;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationActionRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationBatchActionRequest;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

class ClassicsPublicationActionControllerTest {

    @Test
    void shouldExposePublicationActionRoutesWithEditPermissionAndAudit() throws Exception {
        assertRoutes(ClassicsPublicationActionController.class, "classics:sancai:edit", new String[][] {
            {"publishSancai", "sancai/entries/publish"},
            {"offlineSancai", "sancai/entries/offline"},
            {"publishSancaiBatch", "sancai/entries/batch/publish"},
            {"offlineSancaiBatch", "sancai/entries/batch/offline"}
        });
        assertRoutes(ClassicsPublicationActionController.class, "classics:wangqi:edit", new String[][] {
            {"publishWangqi", "wangqi/documents/publish"},
            {"offlineWangqi", "wangqi/documents/offline"},
            {"publishWangqiBatch", "wangqi/documents/batch/publish"},
            {"offlineWangqiBatch", "wangqi/documents/batch/offline"}
        });
        assertRoutes(ClassicsPublicationActionController.class, "classics:mingcustoms:edit", new String[][] {
            {"publishMingCustoms", "ming-customs/publish"},
            {"offlineMingCustoms", "ming-customs/offline"},
            {"publishMingCustomsBatch", "ming-customs/batch/publish"},
            {"offlineMingCustomsBatch", "ming-customs/batch/offline"}
        });
    }

    @Test
    void shouldDelegateEachContentTypeToPublicationService() {
        ClassicsPublicationApplicationService publicationService = publicationService();
        var controller = new ClassicsPublicationActionController(publicationService);

        var sancai = controller.publishSancai(new ClassicsPublicationActionRequest(11L));
        var wangqi = controller.publishWangqi(new ClassicsPublicationActionRequest(12L));
        var ming = controller.publishMingCustoms(new ClassicsPublicationActionRequest(13L));

        assertEquals("SANCAI_ENTRY", sancai.contentType());
        assertEquals("WANGQI_DOCUMENT", wangqi.contentType());
        assertEquals("MING_CUSTOMS", ming.contentType());
        assertEquals("PUBLISHING", ming.transitionStatus());
    }

    private static ClassicsPublicationApplicationService publicationService() {
        ClassicsPublicationApplicationService service = mock(ClassicsPublicationApplicationService.class);
        when(service.create(any())).thenAnswer(invocation -> {
            var command =
                    (com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationCreateCommand)
                            invocation.getArgument(0);
            return ClassicsPublicationCreateResult.success(
                    command.contentType(),
                    command.contentId(),
                    new ClassicsPublicationJobId(command.contentId().value() + 100),
                    ClassicsPublicationLifecycleStatus.DRAFT,
                    ClassicsPublicationTransitionStatus.PUBLISHING);
        });
        return service;
    }

    private static void assertRoutes(Class<?> type, String permission, String[][] routes) throws Exception {
        for (String[] route : routes) {
            Class<?> requestType = route[0].endsWith("Batch")
                    ? ClassicsPublicationBatchActionRequest.class
                    : ClassicsPublicationActionRequest.class;
            Method method = type.getMethod(route[0], requestType);
            assertEquals(route[1], method.getAnnotation(PostMapping.class).value()[0]);
            assertEquals(
                    List.of(permission),
                    List.of(method.getAnnotation(HasPermission.class).value()));
            assertNotNull(method.getAnnotation(SysLogger.class));
        }
    }
}
