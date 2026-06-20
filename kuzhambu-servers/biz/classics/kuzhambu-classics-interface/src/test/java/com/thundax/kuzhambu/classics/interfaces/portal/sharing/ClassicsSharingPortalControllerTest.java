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
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
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
    }

    @Test
    void detailResponseShouldUseShareTokenAndExposeSnapshotDto() throws Exception {
        ClassicsSharingPortalController controller = new ClassicsSharingPortalController(sharingService());

        ClassicsSharePortalResponse response = controller.get("share-token");

        assertEquals("公开分享", response.getTitle());
        assertEquals("SANCAI_ENTRY", response.getTargets().get(0).getContentType());
        assertEquals("{\"title\":\"正式标题\"}", response.getTargets().get(0).getContentSnapshotJson());
        JsonNode json = OBJECT_MAPPER.valueToTree(response);
        assertJsonFields(json, "title", "visibility", "status", "issuedAt", "expiresAt", "targets");
        assertFalse(json.has("tokenHash"), json::toString);
        assertFalse(json.has("shareToken"), json::toString);
        assertFalse(json.at("/targets/0").has("id"), json::toString);
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
        target.setContentSnapshotJson("{\"title\":\"正式标题\"}");
        target.setContentVisibilitySnapshot(ClassicsSharedContentVisibility.PUBLIC);
        target.setTargetStatus(ClassicsShareTargetStatus.AVAILABLE);
        target.setPriority(1);
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
                1);
    }

    private static void assertJsonFields(JsonNode json, String... expectedFields) {
        assertEquals(expectedFields.length, json.size(), json::toString);
        for (String expectedField : expectedFields) {
            assertTrue(json.has(expectedField), json::toString);
        }
    }
}
