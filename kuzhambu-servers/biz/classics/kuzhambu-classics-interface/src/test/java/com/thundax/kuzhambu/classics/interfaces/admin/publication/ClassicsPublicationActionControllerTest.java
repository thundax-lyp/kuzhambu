package com.thundax.kuzhambu.classics.interfaces.admin.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationCreateResult;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.MingCustomsAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationActionRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationBatchActionRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.SancaiAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.WangqiDocumentAdminController;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

class ClassicsPublicationActionControllerTest {

    @Test
    void shouldExposePublicationActionRoutesWithEditPermissionAndAudit() throws Exception {
        assertRoutes(SancaiAdminController.class, "classics:sancai:edit", new String[][] {
            {"publishEntry", "entries/publish"},
            {"offlineEntry", "entries/offline"},
            {"batchPublishEntries", "entries/batch/publish"},
            {"batchOfflineEntries", "entries/batch/offline"}
        });
        assertRoutes(WangqiDocumentAdminController.class, "classics:wangqi:edit", new String[][] {
            {"publish", "publish"},
            {"offline", "offline"},
            {"batchPublish", "batch/publish"},
            {"batchOffline", "batch/offline"}
        });
        assertRoutes(MingCustomsAdminController.class, "classics:mingcustoms:edit", new String[][] {
            {"publish", "publish"},
            {"offline", "offline"},
            {"batchPublish", "batch/publish"},
            {"batchOffline", "batch/offline"}
        });
    }

    @Test
    void shouldDelegateEachContentTypeToPublicationService() {
        ClassicsPublicationApplicationService publicationService = publicationService();
        ClassicsContentApplicationService contentService = mock(ClassicsContentApplicationService.class);

        var sancai = new SancaiAdminController(mock(SancaiApplicationService.class), contentService, publicationService)
                .publishEntry(new ClassicsPublicationActionRequest(11L));
        var wangqi = new WangqiDocumentAdminController(
                        mock(WangqiDocumentApplicationService.class), contentService, publicationService)
                .publish(new ClassicsPublicationActionRequest(12L));
        var ming = new MingCustomsAdminController(
                        mock(MingCustomsApplicationService.class), contentService, publicationService)
                .publish(new ClassicsPublicationActionRequest(13L));

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
            Class<?> requestType = route[0].startsWith("batch")
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
