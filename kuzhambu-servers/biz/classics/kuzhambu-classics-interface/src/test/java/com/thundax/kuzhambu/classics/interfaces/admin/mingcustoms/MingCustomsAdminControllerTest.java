package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordCloudItem;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsTagCloudItem;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.assembler.MingCustomsInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.MingCustomsAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsVersionRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsKeywordCloudItemResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsTagCloudItemResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsVersionResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.bind.annotation.PostMapping;

class MingCustomsAdminControllerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String V1_JSON =
            """
                {
                  "id": 500000000001,
                  "versionId": 9001
                }
                """;

    @Test
    void controllerTypeShouldExist() {
        assertNotNull(MingCustomsAdminController.class);
    }

    @Test
    void keywordCloudShouldReturnKeywordAndCount() {
        MingCustomsApplicationService service = mock(MingCustomsApplicationService.class);
        when(service.listKeywordCloud("PUBLIC")).thenReturn(List.of(new MingCustomsKeywordCloudItem("礼俗", 3L)));
        MingCustomsAdminController controller = new MingCustomsAdminController(service);
        MingCustomsRequest request = new MingCustomsRequest();
        request.setVisibility("PUBLIC");

        List<MingCustomsKeywordCloudItemResponse> responses = controller.listKeywordCloud(request);

        assertEquals(1, responses.size());
        assertEquals("礼俗", responses.get(0).getKeyword());
        assertEquals(3L, responses.get(0).getCount());
    }

    @Test
    void pageShouldAttachOperatorPermissionsToQuery() {
        MingCustomsApplicationService service = mock(MingCustomsApplicationService.class);
        when(service.page(any(), any())).thenReturn(PageResult.of(1, 20, 0, List.of()));
        MingCustomsAdminController controller = new MingCustomsAdminController(service);
        MingCustomsRequest request = new MingCustomsRequest();
        request.setPageNo(1);
        request.setPageSize(20);
        request.setTagId(7001L);
        request.setTagNameSnapshot("祭祀");

        controller.page(request);

        ArgumentCaptor<MingCustomsPageQuery> queryCaptor = ArgumentCaptor.forClass(MingCustomsPageQuery.class);
        verify(service).page(queryCaptor.capture(), any(PageQuery.class));
        assertNotNull(queryCaptor.getValue().getOperatorPermissions());
        assertEquals(7001L, queryCaptor.getValue().getTagId());
        assertEquals("祭祀", queryCaptor.getValue().getTagNameSnapshot());
    }

    @Test
    void tagCloudShouldReturnStableTagFieldsAndAttachOperatorPermissions() {
        MingCustomsApplicationService service = mock(MingCustomsApplicationService.class);
        when(service.listTagCloud(any())).thenReturn(List.of(new MingCustomsTagCloudItem(7001L, "祭祀", 3L)));
        MingCustomsAdminController controller = new MingCustomsAdminController(service);
        MingCustomsRequest request = new MingCustomsRequest();
        request.setCategory("礼俗");
        request.setKeyword("祭祀");
        request.setVisibility("PUBLIC");

        List<MingCustomsTagCloudItemResponse> responses = controller.listTagCloud(request);

        assertEquals(1, responses.size());
        assertEquals(7001L, responses.get(0).getTagId());
        assertEquals("祭祀", responses.get(0).getTagNameSnapshot());
        assertEquals(3L, responses.get(0).getCount());
        ArgumentCaptor<MingCustomsPageQuery> queryCaptor = ArgumentCaptor.forClass(MingCustomsPageQuery.class);
        verify(service).listTagCloud(queryCaptor.capture());
        assertEquals("礼俗", queryCaptor.getValue().getCategory());
        assertEquals("祭祀", queryCaptor.getValue().getKeyword());
        assertNotNull(queryCaptor.getValue().getOperatorPermissions());
    }

    @Test
    void versionRequestShouldBindVersionIdAndId() throws Exception {
        MingCustomsVersionRequest request = OBJECT_MAPPER.readValue(V1_JSON, MingCustomsVersionRequest.class);

        assertEquals(500000000001L, request.getId());
        assertEquals(9001L, request.getVersionId());
    }

    @Test
    void versionResponseShouldKeepStableFields() {
        MingCustomsAdminController controller =
                new MingCustomsAdminController(mock(MingCustomsApplicationService.class));
        ClassicsContentVersion version = new ClassicsContentVersion(
                ClassicsContentVersionId.of(9001L),
                ClassicsContentType.MING_CUSTOMS,
                ClassicsContentId.of(500000000001L),
                1,
                new Date(1767225600000L),
                "{\"contentType\":\"MING_CUSTOMS\",\"contentId\":500000000001}",
                ClassicsContentChangeType.MANUAL_SAVE,
                "手动保存");
        MingCustomsVersionResponse response = MingCustomsInterfaceAssembler.toVersionResponse(version);

        assertEquals(9001L, response.getId());
        assertEquals("MING_CUSTOMS", response.getContentType());
        assertEquals(500000000001L, response.getContentId());
        assertEquals(1, response.getVersionNo());
        assertEquals("MANUAL_SAVE", response.getChangeType());
        assertEquals("手动保存", response.getChangeSummary());
        assertEquals("{\"contentType\":\"MING_CUSTOMS\",\"contentId\":500000000001}", response.getSnapshotJson());
    }

    @Test
    void versionRoutesShouldKeepExpectedPathsAndPermissions() throws Exception {
        Class<?> controllerType = MingCustomsAdminController.class;
        assertPostMapping(
                controllerType,
                "listKeywordCloud",
                "keyword-cloud/list",
                "classics:mingcustoms:view",
                MingCustomsRequest.class);
        assertPostMapping(
                controllerType,
                "listTagCloud",
                "tag-cloud/list",
                "classics:mingcustoms:view",
                MingCustomsRequest.class);
        assertPostMapping(
                controllerType,
                "listVersions",
                "versions/list",
                "classics:mingcustoms:view",
                MingCustomsVersionRequest.class);
        assertPostMapping(
                controllerType,
                "getVersion",
                "versions/get",
                "classics:mingcustoms:view",
                MingCustomsVersionRequest.class);
        assertPostMapping(
                controllerType,
                "resetVersion",
                "versions/reset",
                "classics:mingcustoms:edit",
                MingCustomsVersionRequest.class);
    }

    @Test
    void listVersionsShouldQueryByMingCustomsTypeAndEntryId() {
        MingCustomsApplicationService service = mock(MingCustomsApplicationService.class);
        ClassicsContentApplicationService contentService = mock(ClassicsContentApplicationService.class);
        ClassicsContentId entryId = ClassicsContentId.of(500000000001L);
        when(contentService.listVersions("MING_CUSTOMS", entryId))
                .thenReturn(List.of(version(9001L, ClassicsContentType.MING_CUSTOMS, entryId)));
        MingCustomsAdminController controller = new MingCustomsAdminController(service, contentService);

        List<MingCustomsVersionResponse> versions = controller.listVersions(versionRequest());

        assertEquals(1, versions.size());
        assertEquals("MING_CUSTOMS", versions.get(0).getContentType());
        assertEquals(1, versions.get(0).getVersionNo());
        verify(contentService).listVersions("MING_CUSTOMS", entryId);
    }

    @Test
    void getVersionShouldValidateOwnershipAndMapResult() {
        MingCustomsApplicationService service = mock(MingCustomsApplicationService.class);
        ClassicsContentApplicationService contentService = mock(ClassicsContentApplicationService.class);
        when(contentService.getVersion(ClassicsContentVersionId.of(9001L)))
                .thenReturn(version(9001L, ClassicsContentType.MING_CUSTOMS, ClassicsContentId.of(500000000001L)));
        MingCustomsAdminController controller = new MingCustomsAdminController(service, contentService);

        MingCustomsVersionResponse response = controller.getVersion(versionRequest());

        assertEquals(9001L, response.getId());
        verify(contentService).getVersion(ClassicsContentVersionId.of(9001L));
    }

    @Test
    void resetVersionShouldValidateOwnershipAndCallRestore() {
        MingCustomsApplicationService service = mock(MingCustomsApplicationService.class);
        ClassicsContentApplicationService contentService = mock(ClassicsContentApplicationService.class);
        when(contentService.getVersion(ClassicsContentVersionId.of(9001L)))
                .thenReturn(version(9001L, ClassicsContentType.MING_CUSTOMS, ClassicsContentId.of(500000000001L)));
        when(contentService.restoreHistoryVersion(ClassicsContentVersionId.of(9001L)))
                .thenReturn(version(9002L, ClassicsContentType.MING_CUSTOMS, ClassicsContentId.of(500000000001L)));
        MingCustomsAdminController controller = new MingCustomsAdminController(service, contentService);

        MingCustomsVersionResponse response = controller.resetVersion(versionRequest());

        assertEquals(9002L, response.getId());
        verify(contentService).restoreHistoryVersion(ClassicsContentVersionId.of(9001L));
    }

    @Test
    void getVersionShouldRejectNotMatchedOwnership() {
        MingCustomsApplicationService service = mock(MingCustomsApplicationService.class);
        ClassicsContentApplicationService contentService = mock(ClassicsContentApplicationService.class);
        when(contentService.getVersion(ClassicsContentVersionId.of(9001L)))
                .thenReturn(version(9001L, ClassicsContentType.WANGQI_DOCUMENT, ClassicsContentId.of(400000000001L)));
        MingCustomsAdminController controller = new MingCustomsAdminController(service, contentService);

        BizException exception = assertThrows(BizException.class, () -> controller.getVersion(versionRequest()));
        assertEquals("历史版本不属于当前明代习俗条目", exception.getMessage());
    }

    @Test
    void getVersionShouldThrowIfHistoryVersionNotFound() {
        MingCustomsApplicationService service = mock(MingCustomsApplicationService.class);
        ClassicsContentApplicationService contentService = mock(ClassicsContentApplicationService.class);
        when(contentService.getVersion(ClassicsContentVersionId.of(9001L))).thenReturn(null);
        MingCustomsAdminController controller = new MingCustomsAdminController(service, contentService);

        BizException exception = assertThrows(BizException.class, () -> controller.getVersion(versionRequest()));
        assertEquals("明代习俗历史版本不存在", exception.getMessage());
    }

    private static void assertPostMapping(
            Class<?> controllerType,
            String methodName,
            String path,
            String expectedPermission,
            Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getMethod(methodName, parameterTypes);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertNotNull(mapping, methodName);
        assertEquals(path, mapping.value()[0], methodName);
        assertNotNull(permission, methodName);
        assertEquals(List.of(expectedPermission), List.of(permission.value()), methodName);
    }

    private static MingCustomsVersionRequest versionRequest() {
        MingCustomsVersionRequest request = new MingCustomsVersionRequest();
        request.setId(500000000001L);
        request.setVersionId(9001L);
        return request;
    }

    private static ClassicsContentVersion version(
            long id, ClassicsContentType contentType, ClassicsContentId contentId) {
        return new ClassicsContentVersion(
                ClassicsContentVersionId.of(id),
                contentType,
                contentId,
                1,
                new Date(1767225600000L),
                "{\"contentType\":\"MING_CUSTOMS\",\"contentId\":500000000001}",
                ClassicsContentChangeType.HISTORY_RESTORED,
                "恢复历史版本 v1");
    }
}
