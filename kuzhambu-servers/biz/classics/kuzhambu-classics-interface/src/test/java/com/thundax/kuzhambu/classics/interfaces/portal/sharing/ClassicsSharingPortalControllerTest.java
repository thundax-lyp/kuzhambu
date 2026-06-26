package com.thundax.kuzhambu.classics.interfaces.portal.sharing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.sharing.result.SharePortalResult;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
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
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.request.ClassicsSharePortalSearchRequest;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalListResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
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
        assertEquals(0, list.getAnnotation(GetMapping.class).value().length);

        Method get = ClassicsSharingPortalController.class.getDeclaredMethod("get", String.class);
        assertEquals("{shareToken}", get.getAnnotation(GetMapping.class).value()[0]);

        Method content = ClassicsSharingPortalController.class.getDeclaredMethod(
                "content", String.class, Long.class, Boolean.class, HttpServletResponse.class);
        assertEquals(
                "{shareToken}/resources/{storageObjectId}/content",
                content.getAnnotation(GetMapping.class).value()[0]);
    }

    @Test
    void detailResponseShouldUseShareTokenAndExposeSnapshotDto() throws Exception {
        ClassicsSharingPortalController controller =
                new ClassicsSharingPortalController(sharingService(), storageService());

        ClassicsSharePortalResponse response = controller.get("share-token");

        assertEquals("公开分享", response.getTitle());
        assertEquals("SANCAI_ENTRY", response.getTargets().get(0).getContentType());
        assertEquals(sancaiSnapshotJson(), response.getTargets().get(0).getContentSnapshotJson());
        JsonNode json = OBJECT_MAPPER.valueToTree(response);
        assertJsonFields(json, "title", "visibility", "status", "issuedAt", "expiresAt", "targets");
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

        controller.content("share-token", 9999L, false, missingResponse);
        controller.content("share-token", 7001L, true, sancaiDownloadResponse);

        assertEquals(404, missingResponse.getStatus());
        assertEquals(404, sancaiDownloadResponse.getStatus());
    }

    private static ClassicsSharingApplicationService sharingService() {
        return (ClassicsSharingApplicationService) Proxy.newProxyInstance(
                ClassicsSharingApplicationService.class.getClassLoader(),
                new Class<?>[] {ClassicsSharingApplicationService.class},
                (proxy, method, args) -> {
                    if ("getPortalShare".equals(method.getName())) {
                        assertEquals("share-token", args[0]);
                        return new SharePortalResult(
                                "公开分享",
                                ClassicsShareVisibility.PUBLIC,
                                ClassicsShareLinkStatus.ACTIVE,
                                new Date(1_000L),
                                new Date(3_000L),
                                List.of(target(), wangqiTarget()));
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
        target.setContentSnapshotJson(
                "{\"contentType\":\"WANGQI_DOCUMENT\",\"contentId\":200,\"storageObjectId\":7002}");
        target.setContentVisibilitySnapshot(ClassicsSharedContentVisibility.PUBLIC);
        target.setTargetStatus(ClassicsShareTargetStatus.AVAILABLE);
        target.setPriority(2);
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

    private static StorageApplicationService storageService() {
        return (StorageApplicationService) Proxy.newProxyInstance(
                StorageApplicationService.class.getClassLoader(),
                new Class<?>[] {StorageApplicationService.class},
                (proxy, method, args) -> {
                    if ("get".equals(method.getName())) {
                        StoredObjectId id = (StoredObjectId) args[0];
                        if (StoredObjectId.of(7001L).equals(id)) {
                            return storage(7001L, "三才图.png", "image/png", 9L);
                        }
                        if (StoredObjectId.of(7002L).equals(id)) {
                            return storage(7002L, "wangqi.pdf", "application/pdf", 10L);
                        }
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static StoredObject storage(Long id, String originalFilename, String contentType, Long size) {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectId.of(id));
        storage.setOriginalFilename(originalFilename);
        storage.setContentType(contentType);
        storage.setSize(size);
        return storage;
    }

    private static StoredObjectContent storedContent(Long id, String originalFilename, String contentType) {
        StoredObject storage = storage(id, originalFilename, contentType, 11L);
        return new StoredObjectContent(storage, new ByteArrayInputStream("image-bytes".getBytes()));
    }

    private static String sancaiSnapshotJson() {
        return "{\"contentType\":\"SANCAI_ENTRY\",\"contentId\":100,\"title\":\"正式标题\","
                + "\"images\":[{\"imageId\":8002,\"storageObjectId\":7001,"
                + "\"originalFilename\":\"三才图.png\",\"contentType\":\"image/png\",\"size\":9,"
                + "\"imageType\":\"ORIGINAL\",\"title\":\"插图\",\"currentUsed\":true,\"priority\":1}]}";
    }

    private static void assertJsonFields(JsonNode json, String... expectedFields) {
        assertEquals(expectedFields.length, json.size(), json::toString);
        for (String expectedField : expectedFields) {
            assertTrue(json.has(expectedField), json::toString);
        }
    }
}
