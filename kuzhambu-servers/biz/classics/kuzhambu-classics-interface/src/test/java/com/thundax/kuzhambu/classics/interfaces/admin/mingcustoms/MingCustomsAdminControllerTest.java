package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordCloudItem;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.MingCustomsAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsKeywordCloudItemResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MingCustomsAdminControllerTest {
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
}
