package com.thundax.kuzhambu.classics.interfaces.portal.sharing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sharing.result.SharePortalResult;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.application.sharing.service.impl.ClassicsSharingApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsSharePortalListItem;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareTargetStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsSharedContentVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.ClassicsSharingPortalController;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.ClassicsSharingPrivatePortalController;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.request.ClassicsSharePortalSearchRequest;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalListResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class ClassicsSharingPortalControllerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void controllerTypeShouldExist() {
        assertNotNull(ClassicsSharingPortalController.class);
    }

    @Test
    void controllerShouldBePublicApi() {
        assertNotNull(ClassicsSharingPortalController.class.getAnnotation(PublicApi.class));
    }

    @Test
    void routesShouldKeepPortalSharingApiPaths() throws Exception {
        RequestMapping mapping = ClassicsSharingPortalController.class.getAnnotation(RequestMapping.class);
        assertEquals("/api/portal/classics/shares", mapping.value()[0]);

        Method list =
                ClassicsSharingPortalController.class.getDeclaredMethod("list", ClassicsSharePortalSearchRequest.class);
        assertEquals("list", list.getAnnotation(PostMapping.class).value()[0]);

        Method get =
                ClassicsSharingPortalController.class.getDeclaredMethod("get", ClassicsSharePortalSearchRequest.class);
        assertEquals("get", get.getAnnotation(PostMapping.class).value()[0]);

        Method content = ClassicsSharingPortalController.class.getDeclaredMethod(
                "content", String.class, Long.class, Boolean.class, HttpServletResponse.class);
        assertEquals(
                "{shareToken}/resources/{storageObjectId}/content",
                content.getAnnotation(GetMapping.class).value()[0]);

        RequestMapping privateMapping =
                ClassicsSharingPrivatePortalController.class.getAnnotation(RequestMapping.class);
        assertEquals("/api/portal/classics/private-shares", privateMapping.value()[0]);
        Method privateGet = ClassicsSharingPrivatePortalController.class.getDeclaredMethod(
                "get", ClassicsSharePortalSearchRequest.class);
        assertEquals("get", privateGet.getAnnotation(PostMapping.class).value()[0]);
    }

    @Test
    void detailResponseShouldUseShareTokenAndExposeSnapshotDto() throws Exception {
        ClassicsSharingPortalController controller = new ClassicsSharingPortalController(sharingService());

        ClassicsSharePortalResponse response = controller.get(shareRequest("share-token"));

        assertEquals("公开分享", response.getTitle());
        assertEquals("SANCAI_ENTRY", response.getTargets().get(0).getContentType());
        assertEquals(sancaiSnapshotJson(), response.getTargets().get(0).getContentSnapshotJson());
        JsonNode json = OBJECT_MAPPER.valueToTree(response);
        assertJsonFields(json, "title", "visibility", "status", "issuedAt", "expiresAt", "loginRequired", "targets");
        assertFalse(json.has("tokenHash"), json::toString);
        assertFalse(json.has("shareToken"), json::toString);
        assertFalse(json.at("/targets/0").has("id"), json::toString);
        assertEquals(
                7001L,
                json.at("/targets/0/images/0/storageObject/storageObjectId").asLong());
        assertEquals(
                "三才图.png",
                json.at("/targets/0/images/0/storageObject/originalFilename").asText());
        assertEquals(
                "/api/portal/classics/shares/share-token/resources/7001/content",
                json.at("/targets/0/images/0/storageObject/previewUrl").asText());
        assertEquals("WANGQI_DOCUMENT", json.at("/targets/1/contentType").asText());
        assertEquals(7002L, json.at("/targets/1/storageObject/storageObjectId").asLong());
        assertEquals(
                "wangqi.pdf",
                json.at("/targets/1/storageObject/originalFilename").asText());
        assertEquals(
                "/api/portal/classics/shares/share-token/resources/7002/content?download=true",
                json.at("/targets/1/storageObject/downloadUrl").asText());
        assertEquals("CONTENT_DELETED", json.at("/targets/2/targetStatus").asText());
        assertEquals("已删标题", json.at("/targets/2/titleSnapshot").asText());
        assertFalse(json.at("/targets/2").has("contentSnapshotJson"), json::toString);
        assertFalse(json.at("/targets/2").has("images"), json::toString);
        assertFalse(json.at("/targets/2").has("storageObject"), json::toString);
    }

    @Test
    void publicDetailShouldReturnLoginRequiredResponseForPrivateShare() {
        ClassicsSharingPortalController controller = new ClassicsSharingPortalController(sharingService());

        ClassicsSharePortalResponse response = controller.get(shareRequest("private-token"));

        assertEquals("PRIVATE", response.getVisibility());
        assertTrue(response.getLoginRequired());
        assertTrue(response.getTargets().isEmpty());
    }

    @Test
    void privateDetailResponseShouldUsePrivateResourcePath() {
        ClassicsSharingPrivatePortalController controller =
                new ClassicsSharingPrivatePortalController(sharingService());

        ClassicsSharePortalResponse response = controller.get(shareRequest("private-token"));

        assertEquals("PRIVATE", response.getVisibility());
        assertFalse(response.getLoginRequired());
        assertEquals(
                "/api/portal/classics/private-shares/private-token/resources/7001/content",
                response.getTargets()
                        .get(0)
                        .getImages()
                        .get(0)
                        .getStorageObject()
                        .getPreviewUrl());
    }

    @Test
    void listResponseShouldFilterByCategoryTimeAndTitleWithoutFullSnapshot() {
        ClassicsSharingPortalController controller = new ClassicsSharingPortalController(sharingService());
        ClassicsSharePortalSearchRequest request = new ClassicsSharePortalSearchRequest();
        request.setContentType("SANCAI_ENTRY");
        request.setTitle("正式");
        request.setIssuedAfter(new Date(1_000L));
        request.setIssuedBefore(new Date(3_000L));
        request.setPageNo(1);
        request.setPageSize(10);

        ClassicsSharePortalListResponse response = controller.list(request);

        assertEquals(1, response.getPageNo());
        assertEquals(1, response.getTotalCount());
        JsonNode json = OBJECT_MAPPER.valueToTree(response);
        assertJsonFields(json, "pageNo", "pageSize", "totalCount", "totalPage", "records");
        assertEquals("share-token", json.at("/records/0/shareToken").asText());
        assertEquals("正式标题", json.at("/records/0/titleSnapshot").asText());
        assertFalse(json.at("/records/0").has("contentSnapshotJson"), json::toString);
        assertFalse(json.at("/records/0").has("tokenHash"), json::toString);
    }

    @Test
    void contentShouldWriteInlineResourceBytesAndHeaders() throws Exception {
        ClassicsSharingPortalController controller = new ClassicsSharingPortalController(sharingService());
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.content("share-token", 7001L, false, response);

        assertEquals("image/png", response.getContentType());
        assertEquals(11, response.getContentLength());
        assertTrue(response.getHeader("Content-Disposition").startsWith("inline;"));
        assertTrue(response.getHeader("Content-Disposition").contains("sancai.png"));
        assertEquals("image-bytes", response.getContentAsString());
    }

    @Test
    void contentShouldSupportWangqiDownloadDisposition() throws Exception {
        ClassicsSharingPortalController controller = new ClassicsSharingPortalController(sharingService());
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.content("share-token", 7002L, true, response);

        assertTrue(response.getHeader("Content-Disposition").startsWith("attachment;"));
        assertTrue(response.getHeader("Content-Disposition").contains("wangqi.pdf"));
    }

    @Test
    void contentShouldReturn404ForRejectedResourceRead() throws Exception {
        ClassicsSharingPortalController controller = new ClassicsSharingPortalController(sharingService());
        MockHttpServletResponse missingResponse = new MockHttpServletResponse();
        MockHttpServletResponse sancaiDownloadResponse = new MockHttpServletResponse();
        MockHttpServletResponse deletedResponse = new MockHttpServletResponse();

        controller.content("share-token", 9999L, false, missingResponse);
        controller.content("share-token", 7001L, true, sancaiDownloadResponse);
        controller.content("share-token", 7010L, false, deletedResponse);

        assertEquals(404, missingResponse.getStatus());
        assertEquals(404, sancaiDownloadResponse.getStatus());
        assertEquals(404, deletedResponse.getStatus());
    }

    private static ClassicsSharingApplicationService sharingService() {
        return (ClassicsSharingApplicationService) Proxy.newProxyInstance(
                ClassicsSharingApplicationService.class.getClassLoader(),
                new Class<?>[] {ClassicsSharingApplicationService.class},
                (proxy, method, args) -> {
                    if ("getPortalShare".equals(method.getName())) {
                        if ("private-token".equals(args[0])) {
                            throw new BizException(
                                    ClassicsSharingApplicationServiceImpl.PRIVATE_SHARE_AUTH_REQUIRED_CODE,
                                    "classics.share.private.auth-required",
                                    "私有分享需要登录后访问");
                        }
                        assertEquals("share-token", args[0]);
                        return new SharePortalResult(
                                "公开分享",
                                ClassicsShareVisibility.PUBLIC,
                                ClassicsShareLinkStatus.ACTIVE,
                                new Date(1_000L),
                                new Date(3_000L),
                                List.of(target(), wangqiTarget(), deletedTarget()));
                    }
                    if ("getPrivatePortalShare".equals(method.getName())) {
                        assertEquals("private-token", args[0]);
                        return new SharePortalResult(
                                "私有分享",
                                ClassicsShareVisibility.PRIVATE,
                                ClassicsShareLinkStatus.ACTIVE,
                                new Date(1_000L),
                                new Date(3_000L),
                                List.of(target()));
                    }
                    if ("pagePortalShares".equals(method.getName())) {
                        assertEquals("SANCAI_ENTRY", args[0]);
                        assertEquals("正式", args[1]);
                        assertEquals(new Date(1_000L), args[2]);
                        assertEquals(new Date(3_000L), args[3]);
                        PageQuery page = (PageQuery) args[4];
                        assertEquals(1, page.getPageNo());
                        assertEquals(10, page.getPageSize());
                        return PageResult.of(1, 10, 1, List.of(listItem()));
                    }
                    if ("getPortalShareResourceContent".equals(method.getName())) {
                        assertEquals("share-token", args[0]);
                        Long storageObjectId = (Long) args[1];
                        boolean download = (Boolean) args[2];
                        if (storageObjectId == 7001L && !download) {
                            return storedContent(7001L, "sancai.png", "image/png");
                        }
                        if (storageObjectId == 7002L && download) {
                            return storedContent(7002L, "wangqi.pdf", "application/pdf");
                        }
                        throw new BizException("分享资源不存在");
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static ClassicsShareTarget target() {
        ClassicsShareTarget target = new ClassicsShareTarget();
        target.setContentType(ClassicsContentType.SANCAI_ENTRY);
        target.setContentId(ClassicsContentId.of(100L));
        target.setContentVersionId(ClassicsContentVersionId.of(30L));
        target.setContentVersionNo(3);
        target.setTitleSnapshot("正式标题");
        target.setContentSnapshotJson(sancaiSnapshotJson());
        target.setContentVisibilitySnapshot(ClassicsSharedContentVisibility.PUBLIC);
        target.setTargetStatus(ClassicsShareTargetStatus.AVAILABLE);
        target.setPriority(1);
        return target;
    }

    private static ClassicsShareTarget wangqiTarget() {
        ClassicsShareTarget target = new ClassicsShareTarget();
        target.setContentType(ClassicsContentType.WANGQI_DOCUMENT);
        target.setContentId(ClassicsContentId.of(200L));
        target.setContentVersionId(ClassicsContentVersionId.of(40L));
        target.setContentVersionNo(1);
        target.setTitleSnapshot("王圻文档");
        target.setContentSnapshotJson("{\"contentType\":\"WANGQI_DOCUMENT\",\"contentId\":200,\"storageObjectId\":7002,"
                + "\"originalFilename\":\"wangqi.pdf\",\"contentType\":\"application/pdf\",\"size\":10}");
        target.setContentVisibilitySnapshot(ClassicsSharedContentVisibility.PUBLIC);
        target.setTargetStatus(ClassicsShareTargetStatus.AVAILABLE);
        target.setPriority(2);
        return target;
    }

    private static ClassicsShareTarget deletedTarget() {
        ClassicsShareTarget target = new ClassicsShareTarget();
        target.setContentType(ClassicsContentType.SANCAI_ENTRY);
        target.setContentId(ClassicsContentId.of(101L));
        target.setContentVersionId(ClassicsContentVersionId.of(31L));
        target.setContentVersionNo(1);
        target.setTitleSnapshot("已删标题");
        target.setContentSnapshotJson("{\"contentType\":\"SANCAI_ENTRY\",\"contentId\":101,"
                + "\"images\":[{\"imageId\":8003,\"storageObjectId\":7010,"
                + "\"originalFilename\":\"deleted.png\",\"contentType\":\"image/png\",\"size\":9}]}");
        target.setContentVisibilitySnapshot(ClassicsSharedContentVisibility.PUBLIC);
        target.setTargetStatus(ClassicsShareTargetStatus.CONTENT_DELETED);
        target.setPriority(3);
        return target;
    }

    private static ClassicsSharePortalListItem listItem() {
        return new ClassicsSharePortalListItem(
                ClassicsShareLinkId.of(10L),
                "share-token",
                "公开分享",
                new Date(1_000L),
                new Date(3_000L),
                ClassicsContentType.SANCAI_ENTRY,
                ClassicsContentId.of(100L),
                ClassicsContentVersionId.of(30L),
                3,
                "正式标题",
                ClassicsSharedContentVisibility.PUBLIC,
                ClassicsShareTargetStatus.AVAILABLE,
                1,
                0L);
    }

    private static ClassicsStoredContentResult storedContent(Long id, String originalFilename, String contentType) {
        return new ClassicsStoredContentResult(
                id, originalFilename, contentType, 11L, new ByteArrayInputStream("image-bytes".getBytes()));
    }

    private static String sancaiSnapshotJson() {
        return "{\"contentType\":\"SANCAI_ENTRY\",\"contentId\":100,\"title\":\"正式标题\","
                + "\"images\":[{\"imageId\":8002,\"storageObjectId\":7001,"
                + "\"originalFilename\":\"三才图.png\",\"contentType\":\"image/png\",\"size\":9,"
                + "\"imageType\":\"ORIGINAL\",\"title\":\"插图\",\"currentUsed\":true,\"priority\":1}]}";
    }

    private static ClassicsSharePortalSearchRequest shareRequest(String shareToken) {
        ClassicsSharePortalSearchRequest request = new ClassicsSharePortalSearchRequest();
        request.setShareToken(shareToken);
        return request;
    }

    private static void assertJsonFields(JsonNode json, String... expectedFields) {
        assertEquals(expectedFields.length, json.size(), json::toString);
        for (String expectedField : expectedFields) {
            assertTrue(json.has(expectedField), json::toString);
        }
    }
}
