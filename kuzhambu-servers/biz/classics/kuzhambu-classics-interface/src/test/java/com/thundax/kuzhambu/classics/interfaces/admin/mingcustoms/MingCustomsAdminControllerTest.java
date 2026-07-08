package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordCloudItem;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.assembler.MingCustomsInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.MingCustomsAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsVersionRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsKeywordCloudItemResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsVersionResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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

        List<MingCustomsKeywordCloudItemResponse> responses = controller.listKeywordCloud("PUBLIC");

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

        controller.page(request);

        ArgumentCaptor<MingCustomsPageQuery> queryCaptor = ArgumentCaptor.forClass(MingCustomsPageQuery.class);
        verify(service).page(queryCaptor.capture(), any(PageQuery.class));
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
}
