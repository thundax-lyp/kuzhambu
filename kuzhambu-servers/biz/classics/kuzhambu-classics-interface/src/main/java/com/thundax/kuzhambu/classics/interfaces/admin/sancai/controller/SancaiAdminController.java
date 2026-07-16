package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiCategorySortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntrySortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVolumeSortCommand;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiCategoryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVolumeIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.assembler.SancaiInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiCategoryRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiCategorySortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryPageRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntrySortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryVersionRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiVolumeRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiVolumeSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiCategoryResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiEntryResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiEntryVersionResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiVolumeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.web.annotation.PostJsonApiExempt;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import com.thundax.kuzhambu.common.web.response.DictResponse;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "古籍模块-三才图会", description = "三才图会")
@SysLogger(module = {"古籍", "三才图会"})
@RequestMapping("/api/classics/sancai")
@WrappedApiController
public class SancaiAdminController {
    private final SancaiApplicationService service;
    private final ClassicsContentApplicationService contentService;

    public SancaiAdminController(SancaiApplicationService service, ClassicsContentApplicationService contentService) {
        this.service = service;
        this.contentService = contentService;
    }

    @Operation(summary = "查询三才图会门类类型", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "门类类型")
    @PostJsonApiExempt(reason = "存量 GET JSON 数据接口，待迁移为 POST JSON")
    @GetMapping("categories/types")
    public List<DictResponse> listCategoryTypes() {
        return SancaiInterfaceAssembler.toCategoryTypes();
    }

    @Operation(summary = "查询三才图会卷目类型", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "卷目类型")
    @PostJsonApiExempt(reason = "存量 GET JSON 数据接口，待迁移为 POST JSON")
    @GetMapping("volumes/types")
    public List<DictResponse> listVolumeTypes() {
        return SancaiInterfaceAssembler.toVolumeTypes();
    }

    @Operation(summary = "查询三才图会门类", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "门类列表")
    @PostMapping("categories/list")
    public List<SancaiCategoryResponse> listCategories() {
        return service.listCategories().stream()
                .map(SancaiInterfaceAssembler::toResponse)
                .toList();
    }

    @Operation(summary = "查看三才图会门类", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "门类详情")
    @PostJsonApiExempt(reason = "存量 GET JSON 数据接口，待迁移为 POST JSON")
    @GetMapping("categories/{id}")
    public SancaiCategoryResponse getCategory(@PathVariable("id") Long id) {
        return SancaiInterfaceAssembler.toResponse(service.getCategory(SancaiCategoryIdCodec.toDomain(id)));
    }

    @Operation(summary = "新增三才图会门类", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "门类新增")
    @PostMapping("categories/add")
    public SancaiCategoryResponse addCategory(@Valid @RequestBody SancaiCategoryRequest request) {
        SancaiCategoryId id = service.addCategory(SancaiInterfaceAssembler.toCommand(request));
        return SancaiCategoryResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "更新三才图会门类", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "门类更新")
    @PostMapping("categories/update")
    public SancaiCategoryResponse updateCategory(@Valid @RequestBody SancaiCategoryRequest request) {
        SancaiCategoryId id = service.updateCategory(SancaiInterfaceAssembler.toCommand(request));
        return SancaiCategoryResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "删除三才图会门类", description = "classics:sancai:delete")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:delete")
    @SysLogger(value = "门类删除")
    @PostMapping("categories/delete")
    public void deleteCategory(@Valid @RequestBody SancaiCategoryRequest request) {
        service.deleteCategory(SancaiCategoryIdCodec.toDomain(request.getId()));
    }

    @Operation(summary = "查询三才图会卷", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "卷列表")
    @PostMapping("volumes/list")
    public List<SancaiVolumeResponse> listVolumes(@Valid @RequestBody SancaiEntryPageRequest request) {
        return service
                .listVolumes(SancaiCategoryIdCodec.toDomain(request == null ? null : request.getCategoryId()))
                .stream()
                .map(SancaiInterfaceAssembler::toResponse)
                .toList();
    }

    @Operation(summary = "查看三才图会卷", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "卷详情")
    @PostJsonApiExempt(reason = "存量 GET JSON 数据接口，待迁移为 POST JSON")
    @GetMapping("volumes/{id}")
    public SancaiVolumeResponse getVolume(@PathVariable("id") Long id) {
        return SancaiInterfaceAssembler.toResponse(service.getVolume(SancaiVolumeIdCodec.toDomain(id)));
    }

    @Operation(summary = "新增三才图会卷", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "卷新增")
    @PostMapping("volumes/add")
    public SancaiVolumeResponse addVolume(@Valid @RequestBody SancaiVolumeRequest request) {
        SancaiVolumeId id = service.addVolume(SancaiInterfaceAssembler.toCommand(request));
        return SancaiVolumeResponse.builder().id(id == null ? null : id.value()).build();
    }

    @Operation(summary = "更新三才图会卷", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "卷更新")
    @PostMapping("volumes/update")
    public SancaiVolumeResponse updateVolume(@Valid @RequestBody SancaiVolumeRequest request) {
        SancaiVolumeId id = service.updateVolume(SancaiInterfaceAssembler.toCommand(request));
        return SancaiVolumeResponse.builder().id(id == null ? null : id.value()).build();
    }

    @Operation(summary = "删除三才图会卷", description = "classics:sancai:delete")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:delete")
    @SysLogger(value = "卷删除")
    @PostMapping("volumes/delete")
    public void deleteVolume(@Valid @RequestBody SancaiVolumeRequest request) {
        service.deleteVolume(SancaiVolumeIdCodec.toDomain(request.getId()));
    }

    @Operation(summary = "分页查询三才图会条目", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "分页查询")
    @PostMapping("entries/page")
    public PageResponse<SancaiEntryResponse> pageEntries(@Valid @RequestBody SancaiEntryPageRequest request) {
        var query = SancaiInterfaceAssembler.toQuery(request);
        query.setOperatorPermissions(KuzhambuContextHolder.currentAuthorities());
        return PageResponseHelper.fromPageResult(
                service.pageEntries(query, PageInterfaceAssembler.toPageQuery(request)),
                SancaiInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查询三才图会条目", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "条目列表")
    @PostMapping("entries/list")
    public List<SancaiEntryResponse> listEntries(@Valid @RequestBody SancaiEntryPageRequest request) {
        var query = SancaiInterfaceAssembler.toQuery(request);
        query.setOperatorPermissions(KuzhambuContextHolder.currentAuthorities());
        return service.listEntries(query).stream()
                .map(SancaiInterfaceAssembler::toResponse)
                .toList();
    }

    @Operation(summary = "查看三才图会条目", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "详情")
    @PostJsonApiExempt(reason = "存量 GET JSON 数据接口，待迁移为 POST JSON")
    @GetMapping("entries/{id}")
    public SancaiEntryResponse getEntry(@PathVariable("id") Long id) {
        Long entryId = requireParameter(id, "id");
        return SancaiInterfaceAssembler.toResponse(
                service.getEntry(SancaiEntryIdCodec.toDomain(entryId)),
                contentService.listTags(
                        ClassicsContentType.SANCAI_ENTRY.value(), ClassicsContentIdCodec.toDomain(entryId)));
    }

    @Operation(summary = "新增三才图会条目", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "新增")
    @PostMapping("entries/add")
    public SancaiEntryResponse addEntry(@Valid @RequestBody SancaiEntryRequest request) {
        SancaiEntryId id = service.addEntry(SancaiInterfaceAssembler.toCommand(request));
        return SancaiEntryResponse.builder().id(id == null ? null : id.value()).build();
    }

    @Operation(summary = "更新三才图会条目", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "更新")
    @PostMapping("entries/update")
    public SancaiEntryResponse updateEntry(@Valid @RequestBody SancaiEntryRequest request) {
        SancaiEntryId id = service.updateEntry(SancaiInterfaceAssembler.toCommand(request));
        return SancaiEntryResponse.builder().id(id == null ? null : id.value()).build();
    }

    @Operation(summary = "变更三才图会条目生命周期", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "生命周期变更")
    @PostMapping("entries/lifecycle/change")
    public Boolean changeEntryLifecycle(@Valid @RequestBody SancaiEntryRequest request) {
        service.changeEntryStatus(
                SancaiInterfaceAssembler.toStatusCommand(request, KuzhambuContextHolder.currentAuthorities()));
        return true;
    }

    @Operation(summary = "查询三才图会条目版本", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "版本列表")
    @PostMapping("entries/versions/list")
    public List<SancaiEntryVersionResponse> listEntryVersions(@Valid @RequestBody SancaiEntryVersionRequest request) {
        Long entryId = requireParameter(request == null ? null : request.getId(), "id");
        return contentService
                .listVersions(ClassicsContentType.SANCAI_ENTRY.value(), ClassicsContentIdCodec.toDomain(entryId))
                .stream()
                .map(SancaiInterfaceAssembler::toVersionResponse)
                .toList();
    }

    @Operation(summary = "查看三才图会条目版本", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "版本详情")
    @PostMapping("entries/versions/get")
    public SancaiEntryVersionResponse getEntryVersion(@Valid @RequestBody SancaiEntryVersionRequest request) {
        Long entryId = requireParameter(request == null ? null : request.getId(), "id");
        Long versionId = requireParameter(request == null ? null : request.getVersionId(), "versionId");
        return SancaiInterfaceAssembler.toVersionResponse(ownedEntryVersion(entryId, versionId));
    }

    @Operation(summary = "恢复三才图会条目版本", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "版本恢复")
    @PostMapping("entries/versions/reset")
    public SancaiEntryVersionResponse resetEntryVersion(@Valid @RequestBody SancaiEntryVersionRequest request) {
        Long entryId = requireParameter(request == null ? null : request.getId(), "id");
        Long versionId = requireParameter(request == null ? null : request.getVersionId(), "versionId");
        ownedEntryVersion(entryId, versionId);
        return SancaiInterfaceAssembler.toVersionResponse(
                contentService.restoreHistoryVersion(ClassicsContentVersionIdCodec.toDomain(versionId)));
    }

    @Operation(summary = "排序三才图会门类", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "门类排序")
    @PostMapping("categories/sort")
    public Boolean sortCategories(@Valid @RequestBody SancaiCategorySortRequest request) {
        service.sortCategories(new SancaiCategorySortCommand(
                RequestListHelper.map(
                        RequestListHelper.presentUnique(
                                request == null ? null : request.getOrderedIds(),
                                "orderedIds",
                                AdminResponseExceptions::invalidParameter),
                        SancaiCategoryIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    @Operation(summary = "排序三才图会卷", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "卷排序")
    @PostMapping("volumes/sort")
    public Boolean sortVolumes(@Valid @RequestBody SancaiVolumeSortRequest request) {
        service.sortVolumes(new SancaiVolumeSortCommand(
                RequestListHelper.map(
                        RequestListHelper.presentUnique(
                                request == null ? null : request.getOrderedIds(),
                                "orderedIds",
                                AdminResponseExceptions::invalidParameter),
                        SancaiVolumeIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    @Operation(summary = "排序三才图会条目", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "条目排序")
    @PostMapping("entries/sort")
    public Boolean sortEntries(@Valid @RequestBody SancaiEntrySortRequest request) {
        service.sortEntries(new SancaiEntrySortCommand(
                RequestListHelper.map(
                        RequestListHelper.presentUnique(
                                request == null ? null : request.getOrderedIds(),
                                "orderedIds",
                                AdminResponseExceptions::invalidParameter),
                        SancaiEntryIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    @Operation(summary = "删除三才图会条目", description = "classics:sancai:delete")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:delete")
    @SysLogger(value = "删除")
    @PostMapping("entries/delete")
    public void deleteEntry(@Valid @RequestBody SancaiEntryRequest request) {
        service.deleteEntry(SancaiEntryIdCodec.toDomain(request.getId()));
    }

    private ClassicsContentVersion ownedEntryVersion(Long entryId, Long versionId) {
        ClassicsContentVersion version = contentService.getVersion(ClassicsContentVersionIdCodec.toDomain(versionId));
        if (version == null
                || version.getContentType() != ClassicsContentType.SANCAI_ENTRY
                || !ClassicsContentIdCodec.toDomain(entryId).equals(version.getContentId())) {
            throw new BizException("三才图会版本不属于当前条目");
        }
        return version;
    }

    private static Long requireParameter(Long value, String name) {
        if (value == null) {
            throw AdminResponseExceptions.invalidParameter(name);
        }
        return value;
    }
}
