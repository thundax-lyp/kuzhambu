package com.thundax.kuzhambu.classics.interfaces.portal.sancai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageContent;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.KnowledgeTagId;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategoryOverview;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiCategoryType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVolumeType;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.SancaiPortalController;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.request.SancaiPortalEntrySearchRequest;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.ByteArrayInputStream;
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
        assertNotNull(SancaiPortalController.class.getAnnotation(Tag.class));
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
        for (Method method : SancaiPortalController.class.getDeclaredMethods()) {
            if (method.getAnnotation(PostMapping.class) != null
                    || method.getAnnotation(org.springframework.web.bind.annotation.GetMapping.class) != null) {
                assertNotNull(method.getAnnotation(Operation.class), method.getName());
            }
        }
    }

    @Test
    void pageEntriesShouldForcePublicPublishedFilter() {
        SancaiPortalController controller = controller();
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
    void listCategoriesShouldReturnPortalOverviewMetrics() {
        SancaiPortalController controller = controller();

        var categories = controller.listCategories();

        assertEquals(1, categories.size());
        assertEquals(12L, categories.get(0).getPublicEntryCount());
        assertEquals(8L, categories.get(0).getIllustratedEntryCount());
        assertEquals(
                "/api/portal/classics/sancai/images/3001/8001/content",
                categories.get(0).getThumbnailUrl());
    }

    @Test
    void getEntryShouldRejectNonPublicPublishedEntry() {
        SancaiPortalController controller = controller();

        SancaiPortalEntrySearchRequest request = new SancaiPortalEntrySearchRequest();
        request.setId(3002L);

        assertThrows(BizException.class, () -> controller.getEntry(request));
    }

    @Test
    void getEntryShouldReturnPortalTagsImagesAndCurrentVisualAsset() {
        SancaiPortalController controller = controller();
        SancaiPortalEntrySearchRequest request = new SancaiPortalEntrySearchRequest();
        request.setId(3001L);

        var response = controller.getEntry(request);

        assertEquals("三才", response.getTags().get(0).getTagName());
        assertEquals(
                "/api/portal/classics/sancai/images/3001/8001/content",
                response.getImages().get(0).getPreviewUrl());
        assertEquals(
                "/api/portal/classics/sancai/images/3001/8001/content?download=true",
                response.getImages().get(0).getDownloadUrl());
        assertEquals(5001L, response.getCurrentVisualAsset().getVisualAssetId());
        assertEquals("视觉描述", response.getCurrentVisualAsset().getVisualDescription());
        assertEquals(
                "/api/portal/classics/sancai/visual-assets/3001/5001/generated-content",
                response.getCurrentVisualAsset().getGeneratedPreviewUrl());
    }

    private static SancaiPortalController controller() {
        return new SancaiPortalController(service(), assetService(), contentService());
    }

    private static SancaiApplicationService service() {
        return (SancaiApplicationService) Proxy.newProxyInstance(
                SancaiApplicationService.class.getClassLoader(),
                new Class<?>[] {SancaiApplicationService.class},
                (proxy, method, args) -> {
                    if ("listCategories".equals(method.getName())) {
                        return List.of(new SancaiCategory(SancaiCategoryId.of(2L), "天文", SancaiCategoryType.FORMAL, 1));
                    }
                    if ("listCategoryOverviews".equals(method.getName())) {
                        return List.of(new SancaiCategoryOverview(
                                SancaiCategoryId.of(2L),
                                12L,
                                8L,
                                SancaiEntryId.of(3001L),
                                SancaiEntryImageId.of(8001L),
                                "天图"));
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

    private static SancaiAssetApplicationService assetService() {
        return (SancaiAssetApplicationService) Proxy.newProxyInstance(
                SancaiAssetApplicationService.class.getClassLoader(),
                new Class<?>[] {SancaiAssetApplicationService.class},
                (proxy, method, args) -> {
                    if ("listImages".equals(method.getName())) {
                        return List.of(image());
                    }
                    if ("listVisualAssets".equals(method.getName())) {
                        return List.of(visualAsset());
                    }
                    if ("getImageContent".equals(method.getName())) {
                        return new SancaiEntryImageContent(3001L, 8001L, 7001L, storedContent());
                    }
                    if ("getVisualAssetSourceContent".equals(method.getName())
                            || "getVisualAssetGeneratedContent".equals(method.getName())) {
                        return storedContent();
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static ClassicsContentApplicationService contentService() {
        return (ClassicsContentApplicationService) Proxy.newProxyInstance(
                ClassicsContentApplicationService.class.getClassLoader(),
                new Class<?>[] {ClassicsContentApplicationService.class},
                (proxy, method, args) -> {
                    if ("listTags".equals(method.getName())) {
                        assertEquals("SANCAI_ENTRY", args[0]);
                        assertEquals(ClassicsContentId.of(3001L), args[1]);
                        return List.of(tag());
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

    private static ClassicsContentTag tag() {
        ClassicsContentTag tag = new ClassicsContentTag();
        tag.setId(ClassicsContentTagId.of(6001L));
        tag.setContentType(ClassicsContentType.SANCAI_ENTRY);
        tag.setContentId(ClassicsContentId.of(3001L));
        tag.setTagId(KnowledgeTagId.of(7001L));
        tag.setTagNameSnapshot("三才");
        tag.setSource(ClassicsContentSource.MANUAL);
        tag.setStatus(ClassicsContentTagStatus.ACTIVE);
        tag.setPriority(1);
        return tag;
    }

    private static SancaiEntryImage image() {
        SancaiEntryImage image = new SancaiEntryImage();
        image.setId(SancaiEntryImageId.of(8001L));
        image.setEntryId(SancaiEntryId.of(3001L));
        image.setStorageObjectId(StorageObjectId.of(7001L));
        image.setImageType(SancaiEntryImageType.ORIGINAL);
        image.setTitle("原图");
        image.setCurrentUsed(true);
        image.setPriority(1);
        return image;
    }

    private static SancaiVisualAsset visualAsset() {
        SancaiVisualAsset visualAsset = new SancaiVisualAsset();
        visualAsset.setId(SancaiVisualAssetId.of(5001L));
        visualAsset.setEntryId(SancaiEntryId.of(3001L));
        visualAsset.setVersionNo(1);
        visualAsset.setStatus(SancaiVisualAssetStatus.READY);
        visualAsset.setSourceImageStorageObjectId(StorageObjectId.of(7001L));
        visualAsset.setGeneratedImageStorageObjectId(StorageObjectId.of(7002L));
        visualAsset.setCurrentUsed(true);
        visualAsset.setImageAnalysisMarkdown("图片理解");
        visualAsset.setFusionDescription("融合描述");
        visualAsset.setVisualDescription("视觉描述");
        return visualAsset;
    }

    private static ClassicsStoredContentResult storedContent() {
        return new ClassicsStoredContentResult(
                7001L, "sancai.png", "image/png", 4L, new ByteArrayInputStream(new byte[] {1, 2, 3, 4}));
    }
}
