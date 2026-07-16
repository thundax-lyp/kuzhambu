package com.thundax.kuzhambu.classics.interfaces.portal.sancai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiCategoryType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVolumeType;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.SancaiPortalController;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.request.SancaiPortalEntrySearchRequest;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class SancaiPortalControllerTest {

    @Test
    void routesShouldKeepPortalSancaiApiPaths() throws Exception {
        assertNotNull(SancaiPortalController.class.getAnnotation(PublicApi.class));
        RequestMapping mapping = SancaiPortalController.class.getAnnotation(RequestMapping.class);
        assertEquals("/api/portal/classics/sancai", mapping.value()[0]);

        assertEquals(
                "categories/list",
                SancaiPortalController.class
                        .getDeclaredMethod("listCategories")
                        .getAnnotation(PostMapping.class)
                        .value()[0]);
        assertEquals(
                "volumes/list",
                SancaiPortalController.class
                        .getDeclaredMethod("listVolumes", SancaiPortalEntrySearchRequest.class)
                        .getAnnotation(PostMapping.class)
                        .value()[0]);
        Method entries =
                SancaiPortalController.class.getDeclaredMethod("pageEntries", SancaiPortalEntrySearchRequest.class);
        assertEquals("entries/page", entries.getAnnotation(PostMapping.class).value()[0]);
        assertEquals(
                "entries/get",
                SancaiPortalController.class
                        .getDeclaredMethod("getEntry", SancaiPortalEntrySearchRequest.class)
                        .getAnnotation(PostMapping.class)
                        .value()[0]);
    }

    @Test
    void pageEntriesShouldForcePublicPublishedFilter() {
        SancaiPortalController controller = new SancaiPortalController(service());
        SancaiPortalEntrySearchRequest request = new SancaiPortalEntrySearchRequest();
        request.setCategoryId(2L);
        request.setVolumeId(101L);
        request.setKeyword(" 天地 ");
        request.setPageNo(0);
        request.setPageSize(200);

        var page = controller.pageEntries(request);

        assertEquals(1, page.getRecords().size());
        assertEquals("天地", page.getRecords().get(0).getTitle());
        assertEquals("PUBLISHED", page.getRecords().get(0).getLifecycleStatus());
        assertEquals("PUBLIC", page.getRecords().get(0).getVisibility());
    }

    @Test
    void getEntryShouldRejectNonPublicPublishedEntry() {
        SancaiPortalController controller = new SancaiPortalController(service());

        SancaiPortalEntrySearchRequest request = new SancaiPortalEntrySearchRequest();
        request.setId(3002L);

        assertThrows(BizException.class, () -> controller.getEntry(request));
    }

    private static SancaiApplicationService service() {
        return (SancaiApplicationService) Proxy.newProxyInstance(
                SancaiApplicationService.class.getClassLoader(),
                new Class<?>[] {SancaiApplicationService.class},
                (proxy, method, args) -> {
                    if ("listCategories".equals(method.getName())) {
                        return List.of(new SancaiCategory(SancaiCategoryId.of(2L), "天文", SancaiCategoryType.FORMAL, 1));
                    }
                    if ("listVolumes".equals(method.getName())) {
                        assertEquals(SancaiCategoryId.of(2L), args[0]);
                        return List.of(new SancaiVolume(
                                SancaiVolumeId.of(101L), SancaiCategoryId.of(2L), "天文卷一", SancaiVolumeType.MAIN, 1));
                    }
                    if ("pageEntries".equals(method.getName())) {
                        SancaiEntryPageQuery query = (SancaiEntryPageQuery) args[0];
                        PageQuery pageQuery = (PageQuery) args[1];
                        assertEquals(2L, query.getCategoryId());
                        assertEquals(101L, query.getVolumeId());
                        assertEquals("天地", query.getKeyword());
                        assertEquals(SancaiEntryLifecycleStatus.PUBLISHED, query.getLifecycleStatus());
                        assertEquals(SancaiEntryVisibility.PUBLIC, query.getVisibility());
                        assertEquals(SortDirection.ASC, query.getSortDirection());
                        assertEquals(1, pageQuery.getPageNo());
                        assertEquals(100, pageQuery.getPageSize());
                        return PageResult.of(1, 100, 1, List.of(publicEntry()));
                    }
                    if ("getEntry".equals(method.getName())) {
                        if (SancaiEntryId.of(3002L).equals(args[0])) {
                            SancaiEntry entry = publicEntry();
                            entry.setVisibility(SancaiEntryVisibility.PRIVATE);
                            return entry;
                        }
                        return publicEntry();
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static SancaiEntry publicEntry() {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(3001L));
        entry.setVolumeId(SancaiVolumeId.of(101L));
        entry.setTitle("天地");
        entry.setOriginalText("天地玄黄");
        entry.setTranslationText("天地的译文");
        entry.setSummary("公开摘要");
        entry.setLifecycleStatus(SancaiEntryLifecycleStatus.PUBLISHED);
        entry.setVisibility(SancaiEntryVisibility.PUBLIC);
        return entry;
    }
}
