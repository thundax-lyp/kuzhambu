package com.thundax.kuzhambu.classics.interfaces.admin.sancai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntrySaveCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiCategoryType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVolumeType;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.SancaiAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryPageRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntrySaveRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiCategoryResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiEntryResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiVolumeResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class SancaiAdminControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepAdminApiPaths() throws Exception {
        assertRequestMapping(SancaiAdminController.class, "/api/classics/sancai");
        assertPostMapping(SancaiAdminController.class, "listCategories", "categories/list");
        assertPostMapping(SancaiAdminController.class, "listVolumes", "volumes/list", SancaiEntryPageRequest.class);
        assertPostMapping(SancaiAdminController.class, "pageEntries", "entries/page", SancaiEntryPageRequest.class);
        assertGetMapping(SancaiAdminController.class, "getEntry", "entries/{id}", Long.class);
        assertPostMapping(SancaiAdminController.class, "saveEntry", "entries/save", SancaiEntrySaveRequest.class);
    }

    @Test
    void requestAndResponseJsonFieldsShouldRemainStable() throws Exception {
        SancaiEntryPageRequest pageRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "categoryId": 2,
                  "volumeId": 101,
                  "keyword": "天地",
                  "lifecycleStatus": "PUBLISHED",
                  "visibility": "PUBLIC",
                  "translationStatus": "TRANSLATED",
                  "imageStatus": "HAS_IMAGE",
                  "visualAssetStatus": "READY",
                  "refinementStatus": "COMPLETE",
                  "sortDirection": "ASC",
                  "pageNo": 1,
                  "pageSize": 50
                }
                """,
                SancaiEntryPageRequest.class);
        assertEquals(2L, pageRequest.getCategoryId());
        assertEquals(101L, pageRequest.getVolumeId());
        assertJsonFields(
                pageRequest,
                "pageNo",
                "pageSize",
                "categoryId",
                "volumeId",
                "keyword",
                "lifecycleStatus",
                "visibility",
                "translationStatus",
                "imageStatus",
                "visualAssetStatus",
                "refinementStatus",
                "sortDirection");

        SancaiEntrySaveRequest saveRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "id": 3001,
                  "volumeId": 101,
                  "title": "天地",
                  "originalText": "原文",
                  "translationText": "译文",
                  "summary": "摘要",
                  "lifecycleStatus": "PUBLISHED",
                  "visibility": "PUBLIC",
                  "translationStatus": "TRANSLATED",
                  "imageStatus": "HAS_IMAGE",
                  "visualAssetStatus": "READY",
                  "refinementStatus": "COMPLETE"
                }
                """,
                SancaiEntrySaveRequest.class);
        assertEquals(3001L, saveRequest.getId());
        assertJsonFields(
                saveRequest,
                "id",
                "volumeId",
                "title",
                "originalText",
                "translationText",
                "summary",
                "lifecycleStatus",
                "visibility",
                "translationStatus",
                "imageStatus",
                "visualAssetStatus",
                "refinementStatus");

        assertJsonFields(
                SancaiCategoryResponse.builder()
                        .id(2L)
                        .title("天文")
                        .categoryType("FORMAL")
                        .priority(10)
                        .build(),
                "id",
                "title",
                "categoryType",
                "priority");
        assertJsonFields(
                SancaiVolumeResponse.builder()
                        .id(101L)
                        .categoryId(2L)
                        .title("天文卷一")
                        .volumeType("FORMAL")
                        .priority(101)
                        .build(),
                "id",
                "categoryId",
                "title",
                "volumeType",
                "priority");
        assertJsonFields(
                SancaiEntryResponse.builder()
                        .id(3001L)
                        .volumeId(101L)
                        .title("天地")
                        .originalText("原文")
                        .translationText("译文")
                        .summary("摘要")
                        .lifecycleStatus("PUBLISHED")
                        .visibility("PUBLIC")
                        .translationStatus("TRANSLATED")
                        .imageStatus("HAS_IMAGE")
                        .visualAssetStatus("READY")
                        .refinementStatus("COMPLETE")
                        .priority(1)
                        .build(),
                "id",
                "volumeId",
                "title",
                "originalText",
                "translationText",
                "summary",
                "lifecycleStatus",
                "visibility",
                "translationStatus",
                "imageStatus",
                "visualAssetStatus",
                "refinementStatus",
                "priority");
    }

    @Test
    void listAndEntryMethodsShouldUseApplicationContracts() {
        SancaiAdminController controller = new SancaiAdminController(sancaiService());

        List<SancaiCategoryResponse> categories = controller.listCategories();
        assertEquals(1, categories.size());
        assertEquals("天文", categories.get(0).getTitle());

        SancaiEntryPageRequest volumeRequest = new SancaiEntryPageRequest();
        volumeRequest.setCategoryId(2L);
        List<SancaiVolumeResponse> volumes = controller.listVolumes(volumeRequest);
        assertEquals(1, volumes.size());
        assertEquals(2L, volumes.get(0).getCategoryId());

        SancaiEntryPageRequest pageRequest = new SancaiEntryPageRequest();
        pageRequest.setVolumeId(101L);
        pageRequest.setKeyword("天地");
        pageRequest.setLifecycleStatus("PUBLISHED");
        pageRequest.setVisibility("PUBLIC");
        pageRequest.setSortDirection("ASC");
        pageRequest.setPageNo(1);
        pageRequest.setPageSize(50);
        assertEquals(
                "天地", controller.pageEntries(pageRequest).getRecords().get(0).getTitle());

        assertEquals("天地", controller.getEntry(3001L).getTitle());

        SancaiEntrySaveRequest saveRequest = new SancaiEntrySaveRequest();
        saveRequest.setId(3001L);
        saveRequest.setVolumeId(101L);
        saveRequest.setTitle("天地");
        saveRequest.setLifecycleStatus("PUBLISHED");
        saveRequest.setVisibility("PUBLIC");
        assertEquals(3001L, controller.saveEntry(saveRequest).getId());
    }

    @Test
    void invalidPageEnumShouldBeRejectedBeforeApplicationCall() {
        SancaiAdminController controller = new SancaiAdminController(sancaiService());
        SancaiEntryPageRequest request = new SancaiEntryPageRequest();
        request.setLifecycleStatus("UNKNOWN");

        assertThrows(RuntimeException.class, () -> controller.pageEntries(request));
    }

    private static SancaiApplicationService sancaiService() {
        return (SancaiApplicationService) Proxy.newProxyInstance(
                SancaiApplicationService.class.getClassLoader(),
                new Class<?>[] {SancaiApplicationService.class},
                (proxy, method, args) -> {
                    if ("listCategories".equals(method.getName())) {
                        return List.of(
                                new SancaiCategory(SancaiCategoryId.of(2L), "天文", SancaiCategoryType.FORMAL, 10));
                    }
                    if ("listVolumes".equals(method.getName())) {
                        assertEquals(SancaiCategoryId.of(2L), args[0]);
                        return List.of(new SancaiVolume(
                                SancaiVolumeId.of(101L),
                                SancaiCategoryId.of(2L),
                                "天文卷一",
                                SancaiVolumeType.FORMAL,
                                101));
                    }
                    if ("pageEntries".equals(method.getName())) {
                        SancaiEntryPageQuery query = (SancaiEntryPageQuery) args[0];
                        PageQuery page = (PageQuery) args[1];
                        assertEquals(101L, query.getVolumeId());
                        assertEquals("天地", query.getKeyword());
                        assertEquals(SancaiEntryLifecycleStatus.PUBLISHED, query.getLifecycleStatus());
                        assertEquals(SancaiEntryVisibility.PUBLIC, query.getVisibility());
                        assertEquals(1, page.getPageNo());
                        assertEquals(50, page.getPageSize());
                        return PageResult.of(1, 50, 1, List.of(entry()));
                    }
                    if ("getEntry".equals(method.getName())) {
                        assertEquals(SancaiEntryId.of(3001L), args[0]);
                        return entry();
                    }
                    if ("saveEntry".equals(method.getName())) {
                        SancaiEntrySaveCommand command = (SancaiEntrySaveCommand) args[0];
                        assertEquals(3001L, command.getId());
                        assertEquals(101L, command.getVolumeId());
                        assertEquals("天地", command.getTitle());
                        assertEquals(SancaiEntryLifecycleStatus.PUBLISHED, command.getLifecycleStatus());
                        assertEquals(SancaiEntryVisibility.PUBLIC, command.getVisibility());
                        return SancaiEntryId.of(3001L);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static SancaiEntry entry() {
        return new SancaiEntry(
                SancaiEntryId.of(3001L),
                SancaiVolumeId.of(101L),
                "天地",
                "原文",
                "译文",
                "摘要",
                SancaiEntryLifecycleStatus.PUBLISHED,
                SancaiEntryVisibility.PUBLIC,
                SancaiEntryTranslationStatus.TRANSLATED,
                SancaiEntryImageStatus.HAS_IMAGE,
                SancaiEntryVisualAssetStatus.READY,
                SancaiEntryRefinementStatus.COMPLETE,
                1);
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

    private static void assertGetMapping(
            Class<?> controllerType, String methodName, String expectedPath, Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertJsonFields(Object value, String... expectedFields) {
        JsonNode json = OBJECT_MAPPER.valueToTree(value);
        assertEquals(expectedFields.length, json.size(), json::toString);
        for (String expectedField : expectedFields) {
            assertTrue(json.has(expectedField), json::toString);
        }
    }
}
