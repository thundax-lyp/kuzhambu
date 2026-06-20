package com.thundax.kuzhambu.classics.interfaces.admin.sharing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.result.ShareLinkCreateResult;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareTargetStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsSharedContentVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareTargetId;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.ClassicsSharingAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request.ClassicsSharingRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.response.ClassicsSharingResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class ClassicsSharingAdminControllerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepAdminSharingApiPaths() throws Exception {
        assertRequestMapping(ClassicsSharingAdminController.class, "/api/classics/shares");
        assertPostMapping(ClassicsSharingAdminController.class, "create", "create", ClassicsSharingRequest.class);
        assertPostMapping(
                ClassicsSharingAdminController.class, "updateStatus", "status/update", ClassicsSharingRequest.class);
        assertGetMapping(ClassicsSharingAdminController.class, "get", "{id}", Long.class);
    }

    @Test
    void createRequestAndResponseJsonFieldsShouldRemainStable() throws Exception {
        ClassicsSharingRequest request = OBJECT_MAPPER.readValue(
                """
                {
                  "title": "公开分享",
                  "visibility": "PUBLIC",
                  "targets": [
                    {
                      "contentType": "SANCAI_ENTRY",
                      "contentId": 100,
                      "titleSnapshot": "不应接收",
                      "contentSnapshotJson": "{}"
                    }
                  ]
                }
                """,
                ClassicsSharingRequest.class);
        JsonNode requestJson = OBJECT_MAPPER.valueToTree(request);
        assertTrue(requestJson.at("/targets/0").has("contentType"), requestJson::toString);
        assertTrue(requestJson.at("/targets/0").has("contentId"), requestJson::toString);
        assertFalse(requestJson.at("/targets/0").has("titleSnapshot"), requestJson::toString);
        assertFalse(requestJson.at("/targets/0").has("contentSnapshotJson"), requestJson::toString);

        ClassicsSharingAdminController controller = new ClassicsSharingAdminController(sharingService());
        ClassicsSharingResponse response = controller.create(request);

        assertEquals("share-token", response.getShareToken());
        assertEquals("https://portal.example/share/share-token", response.getShareUrl());
        JsonNode responseJson = OBJECT_MAPPER.valueToTree(response);
        assertJsonFields(responseJson, "id", "shareToken", "shareUrl", "title", "visibility", "status", "targets");
        assertEquals("正式标题", response.getTargets().get(0).getTitleSnapshot());
        assertFalse(responseJson.at("/targets/0").has("contentSnapshotJson"), responseJson::toString);
    }

    private static ClassicsSharingApplicationService sharingService() {
        return (ClassicsSharingApplicationService) Proxy.newProxyInstance(
                ClassicsSharingApplicationService.class.getClassLoader(),
                new Class<?>[] {ClassicsSharingApplicationService.class},
                (proxy, method, args) -> {
                    if ("createLink".equals(method.getName())) {
                        ShareLinkCreateCommand command = (ShareLinkCreateCommand) args[0];
                        assertEquals("公开分享", command.getTitle());
                        assertEquals(ClassicsShareVisibility.PUBLIC, command.getVisibility());
                        assertEquals(ClassicsShareLinkStatus.ACTIVE, command.getStatus());
                        assertEquals(
                                ClassicsContentType.SANCAI_ENTRY,
                                command.getTargets().get(0).getContentType());
                        assertEquals(
                                ClassicsContentId.of(100L),
                                command.getTargets().get(0).getContentId());
                        return new ShareLinkCreateResult(
                                ClassicsShareLinkId.of(10L),
                                "share-token",
                                "https://portal.example/share/share-token",
                                "公开分享",
                                ClassicsShareVisibility.PUBLIC,
                                ClassicsShareLinkStatus.ACTIVE,
                                null,
                                List.of(target()));
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static ClassicsShareTarget target() {
        ClassicsShareTarget target = new ClassicsShareTarget();
        target.setId(ClassicsShareTargetId.of(20L));
        target.setContentType(ClassicsContentType.SANCAI_ENTRY);
        target.setContentId(ClassicsContentId.of(100L));
        target.setContentVersionId(ClassicsContentVersionId.of(30L));
        target.setContentVersionNo(3);
        target.setTitleSnapshot("正式标题");
        target.setContentSnapshotJson("{\"title\":\"正式标题\"}");
        target.setContentVisibilitySnapshot(ClassicsSharedContentVisibility.PUBLIC);
        target.setTargetStatus(ClassicsShareTargetStatus.AVAILABLE);
        target.setPriority(1);
        return target;
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
    }

    private static void assertGetMapping(
            Class<?> controllerType, String methodName, String expectedPath, Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertJsonFields(JsonNode json, String... expectedFields) {
        assertEquals(expectedFields.length, json.size(), json::toString);
        for (String expectedField : expectedFields) {
            assertTrue(json.has(expectedField), json::toString);
        }
    }
}
