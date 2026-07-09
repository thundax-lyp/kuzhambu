package com.thundax.kuzhambu.classics.interfaces.admin.sancai;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiCategoryCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryStatusCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVolumeCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.KnowledgeTagId;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
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
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiCategoryRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryPageRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryVersionRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiVolumeRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiCategoryResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiEntryResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiEntryVersionResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiVolumeResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.response.DictResponse;
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
        assertGetMapping(SancaiAdminController.class, "listCategoryTypes", "categories/types");
        assertGetMapping(SancaiAdminController.class, "listVolumeTypes", "volumes/types");
        assertPostMapping(SancaiAdminController.class, "listCategories", "categories/list");
        assertGetMapping(SancaiAdminController.class, "getCategory", "categories/{id}", Long.class);
        assertPostMapping(SancaiAdminController.class, "addCategory", "categories/add", SancaiCategoryRequest.class);
        assertPostMapping(
                SancaiAdminController.class, "updateCategory", "categories/update", SancaiCategoryRequest.class);
        assertPostMapping(
                SancaiAdminController.class, "deleteCategory", "categories/delete", SancaiCategoryRequest.class);
        assertPostMapping(SancaiAdminController.class, "listVolumes", "volumes/list", SancaiEntryPageRequest.class);
        assertGetMapping(SancaiAdminController.class, "getVolume", "volumes/{id}", Long.class);
        assertPostMapping(SancaiAdminController.class, "addVolume", "volumes/add", SancaiVolumeRequest.class);
        assertPostMapping(SancaiAdminController.class, "updateVolume", "volumes/update", SancaiVolumeRequest.class);
        assertPostMapping(SancaiAdminController.class, "deleteVolume", "volumes/delete", SancaiVolumeRequest.class);
        assertPostMapping(SancaiAdminController.class, "pageEntries", "entries/page", SancaiEntryPageRequest.class);
        assertPostMapping(SancaiAdminController.class, "listEntries", "entries/list", SancaiEntryPageRequest.class);
        assertGetMapping(SancaiAdminController.class, "getEntry", "entries/{id}", Long.class);
        assertPostMapping(SancaiAdminController.class, "addEntry", "entries/add", SancaiEntryRequest.class);
        assertPostMapping(SancaiAdminController.class, "updateEntry", "entries/update", SancaiEntryRequest.class);
        assertPostMapping(
                SancaiAdminController.class,
                "changeEntryLifecycle",
                "entries/lifecycle/change",
                "classics:sancai:edit",
                SancaiEntryRequest.class);
        assertPostMapping(
                SancaiAdminController.class,
                "listEntryVersions",
                "entries/versions/list",
                SancaiEntryVersionRequest.class);
        assertPostMapping(
                SancaiAdminController.class,
                "getEntryVersion",
                "entries/versions/get",
                SancaiEntryVersionRequest.class);
        assertPostMapping(
                SancaiAdminController.class,
                "resetEntryVersion",
                "entries/versions/reset",
                SancaiEntryVersionRequest.class);
        assertPostMapping(SancaiAdminController.class, "deleteEntry", "entries/delete", SancaiEntryRequest.class);
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
                  "translationStatus": "READY",
                  "imageStatus": "READY",
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

        SancaiCategoryRequest categoryRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "id": 2,
                  "title": "天文",
                  "categoryType": "FORMAL",
                  "priority": 10
                }
                """,
                SancaiCategoryRequest.class);
        assertEquals(2L, categoryRequest.getId());
        assertJsonFields(categoryRequest, "id", "title", "categoryType", "priority");

        SancaiVolumeRequest volumeRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "id": 101,
                  "categoryId": 2,
                  "title": "天文卷一",
                  "volumeType": "MAIN",
                  "priority": 101
                }
                """,
                SancaiVolumeRequest.class);
        assertEquals(101L, volumeRequest.getId());
        assertJsonFields(volumeRequest, "id", "categoryId", "title", "volumeType", "priority");

        SancaiEntryRequest entryRequest = OBJECT_MAPPER.readValue(
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
                  "translationStatus": "READY",
                  "imageStatus": "READY",
                  "visualAssetStatus": "READY",
                  "refinementStatus": "COMPLETE"
                }
                """,
                SancaiEntryRequest.class);
        assertEquals(3001L, entryRequest.getId());
        assertJsonFields(
                entryRequest,
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

        SancaiEntryRequest lifecycleRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "id": 3001,
                  "lifecycleStatus": "ARCHIVED"
                }
                """,
                SancaiEntryRequest.class);
        assertEquals(3001L, lifecycleRequest.getId());
        assertEquals("ARCHIVED", lifecycleRequest.getLifecycleStatus());
        assertJsonFields(lifecycleRequest, "id", "lifecycleStatus");

        SancaiEntryVersionRequest versionRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "id": 3001,
                  "versionId": 9001
                }
                """,
                SancaiEntryVersionRequest.class);
        assertEquals(3001L, versionRequest.getId());
        assertEquals(9001L, versionRequest.getVersionId());
        assertJsonFields(versionRequest, "id", "versionId");

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
                        .volumeType("MAIN")
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
                        .translationStatus("READY")
                        .imageStatus("READY")
                        .visualAssetStatus("READY")
                        .refinementStatus("COMPLETE")
                        .priority(1)
                        .currentVersionId(9001L)
                        .currentVersionNo(1)
                        .currentVersionedAt(new java.util.Date(1_000L))
                        .contentUpdatedAt(new java.util.Date(1_000L))
                        .versionDirty(false)
                        .tags(List.of(com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response
                                .ClassicsContentResponse.builder()
                                .id(7001L)
                                .contentType("SANCAI_ENTRY")
                                .contentId(3001L)
                                .tagId(8001L)
                                .tagNameSnapshot("天文")
                                .source("MANUAL")
                                .status("ACTIVE")
                                .priority(1)
                                .build()))
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
                "priority",
                "currentVersionId",
                "currentVersionNo",
                "currentVersionedAt",
                "contentUpdatedAt",
                "versionDirty",
                "tags");
        assertJsonFields(
                SancaiEntryVersionResponse.builder()
                        .id(9001L)
                        .contentType("SANCAI_ENTRY")
                        .contentId(3001L)
                        .versionNo(1)
                        .versionedAt(new java.util.Date(1_000L))
                        .snapshotJson("{}")
                        .changeType("MANUAL_SAVE")
                        .changeSummary("手动保存")
                        .build(),
                "id",
                "contentType",
                "contentId",
                "versionNo",
                "versionedAt",
                "snapshotJson",
                "changeType",
                "changeSummary");
    }

    @Test
    void listAndEntryMethodsShouldUseApplicationContracts() {
        SancaiAdminController controller = new SancaiAdminController(sancaiService(), contentService());

        List<DictResponse> categoryTypes = controller.listCategoryTypes();
        assertEquals("SANCAI_CATEGORY_TYPE", categoryTypes.get(0).getType());
        assertEquals("FORMAL", categoryTypes.get(0).getValue());
        assertEquals("正式门类", categoryTypes.get(0).getLabel());

        List<DictResponse> volumeTypes = controller.listVolumeTypes();
        assertEquals("SANCAI_VOLUME_TYPE", volumeTypes.get(0).getType());
        assertEquals("MAIN", volumeTypes.get(0).getValue());
        assertEquals("正式卷目", volumeTypes.get(0).getLabel());

        List<SancaiCategoryResponse> categories = controller.listCategories();
        assertEquals(1, categories.size());
        assertEquals("天文", categories.get(0).getTitle());

        assertEquals("天文", controller.getCategory(2L).getTitle());

        SancaiCategoryRequest categoryRequest = new SancaiCategoryRequest();
        categoryRequest.setId(2L);
        categoryRequest.setTitle("天文");
        categoryRequest.setCategoryType("FORMAL");
        categoryRequest.setPriority(10);
        assertEquals(2L, controller.addCategory(categoryRequest).getId());
        assertEquals(2L, controller.updateCategory(categoryRequest).getId());
        controller.deleteCategory(categoryRequest);

        SancaiEntryPageRequest volumePageRequest = new SancaiEntryPageRequest();
        volumePageRequest.setCategoryId(2L);
        List<SancaiVolumeResponse> volumes = controller.listVolumes(volumePageRequest);
        assertEquals(1, volumes.size());
        assertEquals(2L, volumes.get(0).getCategoryId());
        assertEquals("天文卷一", controller.getVolume(101L).getTitle());

        SancaiVolumeRequest volumeRequest = new SancaiVolumeRequest();
        volumeRequest.setId(101L);
        volumeRequest.setCategoryId(2L);
        volumeRequest.setTitle("天文卷一");
        volumeRequest.setVolumeType("MAIN");
        volumeRequest.setPriority(101);
        assertEquals(101L, controller.addVolume(volumeRequest).getId());
        assertEquals(101L, controller.updateVolume(volumeRequest).getId());
        controller.deleteVolume(volumeRequest);

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
        assertEquals("天地", controller.listEntries(pageRequest).get(0).getTitle());

        SancaiEntryResponse detail = controller.getEntry(3001L);
        assertEquals("天地", detail.getTitle());
        assertEquals("天文", detail.getTags().get(0).getTagNameSnapshot());

        SancaiEntryRequest entryRequest = new SancaiEntryRequest();
        entryRequest.setId(3001L);
        entryRequest.setVolumeId(101L);
        entryRequest.setTitle("天地");
        entryRequest.setLifecycleStatus("PUBLISHED");
        entryRequest.setVisibility("PUBLIC");
        assertEquals(3001L, controller.addEntry(entryRequest).getId());
        assertEquals(3001L, controller.updateEntry(entryRequest).getId());

        SancaiEntryRequest lifecycleRequest = new SancaiEntryRequest();
        lifecycleRequest.setId(3001L);
        lifecycleRequest.setLifecycleStatus("ARCHIVED");
        assertEquals(true, controller.changeEntryLifecycle(lifecycleRequest));

        controller.deleteEntry(entryRequest);
    }

    @Test
    void updateEntryShouldPassTargetVolumeIdToApplication() {
        SancaiAdminController controller = new SancaiAdminController(updateEntryService(), contentService());
        SancaiEntryRequest entryRequest = new SancaiEntryRequest();
        entryRequest.setId(3001L);
        entryRequest.setVolumeId(202L);
        entryRequest.setTitle("迁移条目");
        entryRequest.setLifecycleStatus("PUBLISHED");
        entryRequest.setVisibility("PUBLIC");

        assertEquals(3001L, controller.updateEntry(entryRequest).getId());
    }

    @Test
    void versionMethodsShouldUseContentContracts() {
        SancaiAdminController controller = new SancaiAdminController(sancaiService(), contentService());
        SancaiEntryVersionRequest request = new SancaiEntryVersionRequest();
        request.setId(3001L);
        request.setVersionId(9001L);

        List<SancaiEntryVersionResponse> versions = controller.listEntryVersions(request);
        assertEquals(1, versions.size());
        assertEquals(1, versions.get(0).getVersionNo());
        assertEquals("SANCAI_ENTRY", versions.get(0).getContentType());

        SancaiEntryVersionResponse version = controller.getEntryVersion(request);
        assertEquals(9001L, version.getId());
        assertEquals(3001L, version.getContentId());

        SancaiEntryVersionResponse restoredVersion = controller.resetEntryVersion(request);
        assertEquals(9002L, restoredVersion.getId());
        assertEquals("HISTORY_RESTORED", restoredVersion.getChangeType());
    }

    @Test
    void versionMethodsShouldRejectMismatchedVersionOwnership() {
        SancaiAdminController controller = new SancaiAdminController(sancaiService(), contentService());
        SancaiEntryVersionRequest request = new SancaiEntryVersionRequest();
        request.setId(3002L);
        request.setVersionId(9001L);

        assertThrows(RuntimeException.class, () -> controller.getEntryVersion(request));
        assertThrows(RuntimeException.class, () -> controller.resetEntryVersion(request));
    }

    @Test
    void versionMethodsShouldRejectMissingVersionId() {
        SancaiAdminController controller = new SancaiAdminController(sancaiService(), contentService());
        SancaiEntryVersionRequest request = new SancaiEntryVersionRequest();
        request.setId(3001L);

        assertThrows(RuntimeException.class, () -> controller.getEntryVersion(request));
        assertThrows(RuntimeException.class, () -> controller.resetEntryVersion(request));
    }

    @Test
    void invalidPageEnumShouldBeRejectedBeforeApplicationCall() {
        SancaiAdminController controller = new SancaiAdminController(sancaiService(), contentService());
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
                    if ("getCategory".equals(method.getName())) {
                        assertEquals(SancaiCategoryId.of(2L), args[0]);
                        return new SancaiCategory(SancaiCategoryId.of(2L), "天文", SancaiCategoryType.FORMAL, 10);
                    }
                    if ("addCategory".equals(method.getName())) {
                        SancaiCategoryCommand command = (SancaiCategoryCommand) args[0];
                        assertEquals(2L, command.getId());
                        assertEquals("天文", command.getTitle());
                        assertEquals(SancaiCategoryType.FORMAL, command.getCategoryType());
                        assertEquals(10, command.getPriority());
                        return SancaiCategoryId.of(2L);
                    }
                    if ("updateCategory".equals(method.getName())) {
                        SancaiCategoryCommand command = (SancaiCategoryCommand) args[0];
                        assertEquals(2L, command.getId());
                        assertEquals("天文", command.getTitle());
                        assertEquals(SancaiCategoryType.FORMAL, command.getCategoryType());
                        assertEquals(10, command.getPriority());
                        return SancaiCategoryId.of(2L);
                    }
                    if ("deleteCategory".equals(method.getName())) {
                        assertEquals(SancaiCategoryId.of(2L), args[0]);
                        return null;
                    }
                    if ("listVolumes".equals(method.getName())) {
                        assertEquals(SancaiCategoryId.of(2L), args[0]);
                        return List.of(new SancaiVolume(
                                SancaiVolumeId.of(101L), SancaiCategoryId.of(2L), "天文卷一", SancaiVolumeType.MAIN, 101));
                    }
                    if ("getVolume".equals(method.getName())) {
                        assertEquals(SancaiVolumeId.of(101L), args[0]);
                        return new SancaiVolume(
                                SancaiVolumeId.of(101L), SancaiCategoryId.of(2L), "天文卷一", SancaiVolumeType.MAIN, 101);
                    }
                    if ("addVolume".equals(method.getName()) || "updateVolume".equals(method.getName())) {
                        SancaiVolumeCommand command = (SancaiVolumeCommand) args[0];
                        assertEquals(101L, command.getId());
                        assertEquals(2L, command.getCategoryId());
                        assertEquals("天文卷一", command.getTitle());
                        assertEquals(SancaiVolumeType.MAIN, command.getVolumeType());
                        assertEquals(101, command.getPriority());
                        return SancaiVolumeId.of(101L);
                    }
                    if ("deleteVolume".equals(method.getName())) {
                        assertEquals(SancaiVolumeId.of(101L), args[0]);
                        return null;
                    }
                    if ("pageEntries".equals(method.getName())) {
                        SancaiEntryPageQuery query = (SancaiEntryPageQuery) args[0];
                        PageQuery page = (PageQuery) args[1];
                        assertEquals(101L, query.getVolumeId());
                        assertEquals("天地", query.getKeyword());
                        assertEquals(SancaiEntryLifecycleStatus.PUBLISHED, query.getLifecycleStatus());
                        assertEquals(SancaiEntryVisibility.PUBLIC, query.getVisibility());
                        assertEquals(true, query.getOperatorPermissions() != null);
                        assertEquals(1, page.getPageNo());
                        assertEquals(50, page.getPageSize());
                        return PageResult.of(1, 50, 1, List.of(entry()));
                    }
                    if ("listEntries".equals(method.getName())) {
                        SancaiEntryPageQuery query = (SancaiEntryPageQuery) args[0];
                        assertEquals(101L, query.getVolumeId());
                        assertEquals("天地", query.getKeyword());
                        assertEquals(SancaiEntryLifecycleStatus.PUBLISHED, query.getLifecycleStatus());
                        assertEquals(SancaiEntryVisibility.PUBLIC, query.getVisibility());
                        assertEquals(true, query.getOperatorPermissions() != null);
                        return List.of(entry());
                    }
                    if ("getEntry".equals(method.getName())) {
                        assertEquals(SancaiEntryId.of(3001L), args[0]);
                        return entry();
                    }
                    if ("addEntry".equals(method.getName()) || "updateEntry".equals(method.getName())) {
                        SancaiEntryCommand command = (SancaiEntryCommand) args[0];
                        assertEquals(3001L, command.getId());
                        assertEquals(101L, command.getVolumeId());
                        assertEquals("天地", command.getTitle());
                        assertEquals(SancaiEntryLifecycleStatus.PUBLISHED, command.getLifecycleStatus());
                        assertEquals(SancaiEntryVisibility.PUBLIC, command.getVisibility());
                        return SancaiEntryId.of(3001L);
                    }
                    if ("changeEntryStatus".equals(method.getName())) {
                        SancaiEntryStatusCommand command = (SancaiEntryStatusCommand) args[0];
                        assertEquals(3001L, command.getId());
                        assertEquals(SancaiEntryLifecycleStatus.ARCHIVED, command.getLifecycleStatus());
                        assertEquals(true, command.getOperatorPermissions() != null);
                        return null;
                    }
                    if ("deleteEntry".equals(method.getName())) {
                        assertEquals(SancaiEntryId.of(3001L), args[0]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static SancaiApplicationService updateEntryService() {
        return (SancaiApplicationService) Proxy.newProxyInstance(
                SancaiApplicationService.class.getClassLoader(),
                new Class<?>[] {SancaiApplicationService.class},
                (proxy, method, args) -> {
                    if ("updateEntry".equals(method.getName())) {
                        SancaiEntryCommand command = (SancaiEntryCommand) args[0];
                        assertEquals(3001L, command.getId());
                        assertEquals(202L, command.getVolumeId());
                        assertEquals("迁移条目", command.getTitle());
                        assertEquals(SancaiEntryLifecycleStatus.PUBLISHED, command.getLifecycleStatus());
                        assertEquals(SancaiEntryVisibility.PUBLIC, command.getVisibility());
                        return SancaiEntryId.of(3001L);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static ClassicsContentApplicationService contentService() {
        return (ClassicsContentApplicationService) Proxy.newProxyInstance(
                ClassicsContentApplicationService.class.getClassLoader(),
                new Class<?>[] {ClassicsContentApplicationService.class},
                (proxy, method, args) -> {
                    if ("listVersions".equals(method.getName())) {
                        assertEquals(ClassicsContentType.SANCAI_ENTRY.value(), args[0]);
                        assertEquals(ClassicsContentId.of(3001L), args[1]);
                        return List.of(version(9001L, 3001L, 1, ClassicsContentChangeType.MANUAL_SAVE));
                    }
                    if ("listTags".equals(method.getName())) {
                        assertEquals(ClassicsContentType.SANCAI_ENTRY.value(), args[0]);
                        assertEquals(ClassicsContentId.of(3001L), args[1]);
                        ClassicsContentTag tag = new ClassicsContentTag();
                        tag.setId(ClassicsContentTagId.of(7001L));
                        tag.setContentType(ClassicsContentType.SANCAI_ENTRY);
                        tag.setContentId(ClassicsContentId.of(3001L));
                        tag.setTagId(KnowledgeTagId.of(8001L));
                        tag.setTagNameSnapshot("天文");
                        tag.setSource(ClassicsContentSource.MANUAL);
                        tag.setStatus(ClassicsContentTagStatus.ACTIVE);
                        tag.setPriority(1);
                        return List.of(tag);
                    }
                    if ("getVersion".equals(method.getName())) {
                        assertEquals(ClassicsContentVersionId.of(9001L), args[0]);
                        return version(9001L, 3001L, 1, ClassicsContentChangeType.MANUAL_SAVE);
                    }
                    if ("restoreHistoryVersion".equals(method.getName())) {
                        assertEquals(ClassicsContentVersionId.of(9001L), args[0]);
                        return version(9002L, 3001L, 2, ClassicsContentChangeType.HISTORY_RESTORED);
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
                SancaiEntryTranslationStatus.READY,
                SancaiEntryImageStatus.READY,
                SancaiEntryVisualAssetStatus.READY,
                SancaiEntryRefinementStatus.COMPLETE,
                1);
    }

    private static ClassicsContentVersion version(
            Long versionId, Long contentId, int versionNo, ClassicsContentChangeType changeType) {
        ClassicsContentVersion version = new ClassicsContentVersion();
        version.setId(ClassicsContentVersionId.of(versionId));
        version.setContentType(ClassicsContentType.SANCAI_ENTRY);
        version.setContentId(ClassicsContentId.of(contentId));
        version.setVersionNo(versionNo);
        version.setVersionedAt(new java.util.Date(1_000L));
        version.setSnapshotJson("{}");
        version.setChangeType(changeType);
        version.setChangeSummary(changeType == ClassicsContentChangeType.HISTORY_RESTORED ? "恢复历史版本 v1" : "手动保存");
        return version;
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

    private static void assertPostMapping(
            Class<?> controllerType,
            String methodName,
            String expectedPath,
            String expectedPermission,
            Class<?>... parameterTypes)
            throws Exception {
        assertPostMapping(controllerType, methodName, expectedPath, parameterTypes);
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertArrayEquals(new String[] {expectedPermission}, permission.value());
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
