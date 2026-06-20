package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordCloudItem;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.MingCustomsAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsKeywordCloudItemResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

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
}
