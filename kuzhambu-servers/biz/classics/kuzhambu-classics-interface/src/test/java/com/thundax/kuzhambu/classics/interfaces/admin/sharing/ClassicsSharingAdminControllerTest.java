package com.thundax.kuzhambu.classics.interfaces.admin.sharing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationItemResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.sharing.command.BatchShareCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.query.ShareAccessQuery;
import com.thundax.kuzhambu.classics.application.sharing.result.ShareLinkCreateResult;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareAccessRecordIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareLinkIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareTargetIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareAccessRecord;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareAccessResult;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareTargetStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsSharedContentVisibility;
import com.thundax.kuzhambu.classics.interfaces.admin.common.response.ClassicsBatchOperationResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.ClassicsSharingAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request.ClassicsBatchShareCreateRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request.ClassicsSharingRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.response.ClassicsSharingResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.page.PageRules;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class ClassicsSharingAdminControllerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepAdminSharingApiPaths() throws Exception {
        assertRequestMapping(ClassicsSharingAdminController.class, "/api/classics/shares");
        assertPostMapping(ClassicsSharingAdminController.class, "create", "create", ClassicsSharingRequest.class);
        assertPostMapping(
                ClassicsSharingAdminController.class,
                "createBatch",
                "batch/create",
                ClassicsBatchShareCreateRequest.class);
        assertPostMapping(ClassicsSharingAdminController.class, "page", "page", ClassicsSharingRequest.class);
        assertPostMapping(
                ClassicsSharingAdminController.class, "updateStatus", "status/update", ClassicsSharingRequest.class);
        assertPostMapping(ClassicsSharingAdminController.class, "get", "get", ClassicsSharingRequest.class);
        assertPostMapping(
                ClassicsSharingAdminController.class,
                "pageAccessRecords",
                "access-records/page",
                ClassicsSharingRequest.class);
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
        assertEquals("AVAILABLE", responseJson.at("/targets/0/targetStatus").asText());
        assertFalse(responseJson.at("/targets/0").has("contentSnapshotJson"), responseJson::toString);
    }

    @Test
    void detailResponseShouldExposeDeletedTargetStatusAndTitleSnapshot() {
        ClassicsSharingAdminController controller = new ClassicsSharingAdminController(sharingService());
        ClassicsSharingRequest request = new ClassicsSharingRequest();
        request.setId(10L);

        ClassicsSharingResponse response = controller.get(request);

        assertEquals("公开分享", response.getTitle());
        JsonNode json = OBJECT_MAPPER.valueToTree(response);
        assertEquals("已删标题", json.at("/targets/0/titleSnapshot").asText());
        assertEquals("CONTENT_DELETED", json.at("/targets/0/targetStatus").asText());
    }

    @Test
    void batchCreateRequestAndResponseJsonFieldsShouldRemainStable() throws Exception {
        ClassicsBatchShareCreateRequest request = OBJECT_MAPPER.readValue(
                """
                {
                  "titlePrefix": "批量-",
                  "visibility": "PUBLIC",
                  "status": "ACTIVE",
                  "visibilityRiskStatus": "PUBLIC_ONLY",
                  "privateContentConfirmed": true,
                  "targets": [
                    {
                      "contentType": "SANCAI_ENTRY",
                      "contentId": 100,
                      "titleSnapshot": "不应接收"
                    }
                  ]
                }
                """,
                ClassicsBatchShareCreateRequest.class);
        JsonNode requestJson = OBJECT_MAPPER.valueToTree(request);
        assertTrue(requestJson.has("titlePrefix"), requestJson::toString);
        assertTrue(requestJson.has("privateContentConfirmed"), requestJson::toString);
        assertFalse(requestJson.at("/targets/0").has("titleSnapshot"), requestJson::toString);

        ClassicsSharingAdminController controller = new ClassicsSharingAdminController(sharingService());
        ClassicsBatchOperationResponse response = controller.createBatch(request);

        assertEquals(1, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        JsonNode responseJson = OBJECT_MAPPER.valueToTree(response);
        assertJsonFields(responseJson, "successCount", "failureCount", "successes", "failures");
        assertEquals(10L, responseJson.at("/successes/0/resultId").asLong());
        assertEquals("ACTIVE", responseJson.at("/successes/0/status").asText());
    }

    @Test
    void pageAndAccessRecordsShouldMapListResults() {
        ClassicsSharingRequest request = new ClassicsSharingRequest();
        request.setStatus("ACTIVE");
        request.setVisibility("PUBLIC");
        request.setShareLinkId(10L);
        request.setPageNo(1);
        request.setPageSize(10);

        ClassicsSharingAdminController controller = new ClassicsSharingAdminController(sharingService());

        JsonNode page =
                OBJECT_MAPPER.valueToTree(controller.page(request).getRecords().get(0));
        assertEquals("ACTIVE", page.get("status").asText());
        assertEquals("公开分享", page.get("title").asText());
        assertEquals(1L, page.get("accessCount").asLong());

        JsonNode accessRecord = OBJECT_MAPPER.valueToTree(
                controller.pageAccessRecords(request).getRecords().get(0));
        assertEquals(100L, accessRecord.get("id").asLong());
        assertEquals(10L, accessRecord.get("shareLinkId").asLong());
        assertEquals("ALLOWED", accessRecord.get("accessResult").asText());
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
                                ClassicsContentIdCodec.toDomain(100L),
                                command.getTargets().get(0).getContentId());
                        return new ShareLinkCreateResult(
                                ClassicsShareLinkIdCodec.toDomain(10L),
                                "share-token",
                                "https://portal.example/share/share-token",
                                "公开分享",
                                ClassicsShareVisibility.PUBLIC,
                                ClassicsShareLinkStatus.ACTIVE,
                                null,
                                List.of(target()));
                    }
                    if ("batchCreateLinks".equals(method.getName())) {
                        BatchShareCreateCommand command = (BatchShareCreateCommand) args[0];
                        assertEquals("批量-", command.getTitlePrefix());
                        assertEquals(ClassicsShareVisibility.PUBLIC, command.getVisibility());
                        assertEquals(ClassicsShareLinkStatus.ACTIVE, command.getStatus());
                        assertTrue(command.isPrivateContentConfirmed());
                        assertEquals(
                                ClassicsContentType.SANCAI_ENTRY,
                                command.getTargets().get(0).getContentType());
                        assertEquals(
                                ClassicsContentIdCodec.toDomain(100L),
                                command.getTargets().get(0).getContentId());
                        return ClassicsBatchOperationResult.of(
                                List.of(ClassicsBatchOperationItemResult.success(
                                        ClassicsContentType.SANCAI_ENTRY.value(), 100L, 10L, "ACTIVE")),
                                List.of());
                    }
                    if ("pageLinks".equals(method.getName())) {
                        assertEquals("ACTIVE", args[0]);
                        assertEquals("PUBLIC", args[1]);
                        PageQuery page = (PageQuery) args[2];
                        assertEquals(1, page.getPageNo());
                        assertEquals(10, page.getPageSize());
                        return PageResult.of(1, 10, 1, List.of(link()));
                    }
                    if ("pageAccessRecords".equals(method.getName())) {
                        ShareAccessQuery query = (ShareAccessQuery) args[0];
                        assertEquals(ClassicsShareLinkIdCodec.toDomain(10L), query.getShareLinkId());
                        PageQuery page = (PageQuery) args[1];
                        assertEquals(1, page.getPageNo());
                        assertEquals(10, page.getPageSize());
                        return PageResult.of(
                                PageRules.firstPageIndex(), page.getPageSize(), 1, List.of(accessRecord()));
                    }
                    if ("getLink".equals(method.getName())) {
                        assertEquals(ClassicsShareLinkIdCodec.toDomain(10L), args[0]);
                        return link();
                    }
                    if ("listTargets".equals(method.getName())) {
                        assertEquals(ClassicsShareLinkIdCodec.toDomain(10L), args[0]);
                        return List.of(deletedTarget());
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static ClassicsShareLink link() {
        ClassicsShareLink shareLink = new ClassicsShareLink();
        shareLink.setId(ClassicsShareLinkIdCodec.toDomain(10L));
        shareLink.setTitle("公开分享");
        shareLink.setVisibility(ClassicsShareVisibility.PUBLIC);
        shareLink.setStatus(ClassicsShareLinkStatus.ACTIVE);
        shareLink.setIssuedAt(new Date());
        shareLink.setExpiresAt(new Date(System.currentTimeMillis() + 86_400_000L));
        shareLink.setAccessCount(1L);
        return shareLink;
    }

    private static ClassicsShareAccessRecord accessRecord() {
        ClassicsShareAccessRecord record = new ClassicsShareAccessRecord();
        record.setId(ClassicsShareAccessRecordIdCodec.toDomain(100L));
        record.setShareLinkId(ClassicsShareLinkIdCodec.toDomain(10L));
        record.setShareTargetId(ClassicsShareTargetIdCodec.toDomain(20L));
        record.setAccessedAt(new Date(System.currentTimeMillis() - 3_600_000L));
        record.setAccessResult(ClassicsShareAccessResult.ALLOWED);
        record.setClientSnapshot("resourceStorageObjectId=101");
        return record;
    }

    private static ClassicsShareTarget target() {
        ClassicsShareTarget target = new ClassicsShareTarget();
        target.setId(ClassicsShareTargetIdCodec.toDomain(20L));
        target.setContentType(ClassicsContentType.SANCAI_ENTRY);
        target.setContentId(ClassicsContentIdCodec.toDomain(100L));
        target.setContentVersionId(ClassicsContentVersionIdCodec.toDomain(30L));
        target.setContentVersionNo(3);
        target.setTitleSnapshot("正式标题");
        target.setContentSnapshotJson("{\"title\":\"正式标题\"}");
        target.setContentVisibilitySnapshot(ClassicsSharedContentVisibility.PUBLIC);
        target.setTargetStatus(ClassicsShareTargetStatus.AVAILABLE);
        target.setPriority(1);
        return target;
    }

    private static ClassicsShareTarget deletedTarget() {
        ClassicsShareTarget target = target();
        target.setTitleSnapshot("已删标题");
        target.setTargetStatus(ClassicsShareTargetStatus.CONTENT_DELETED);
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

    private static void assertJsonFields(JsonNode json, String... expectedFields) {
        assertEquals(expectedFields.length, json.size(), json::toString);
        for (String expectedField : expectedFields) {
            assertTrue(json.has(expectedField), json::toString);
        }
    }
}
