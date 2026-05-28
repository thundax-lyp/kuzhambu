package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller;

import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiCategorySortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntrySortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVolumeSortCommand;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiCategoryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVolumeIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.assembler.SancaiInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiCategorySortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntrySortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryPageRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiVolumeSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntrySaveRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiEntryResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    public SancaiAdminController(SancaiApplicationService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询三才图会条目", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "分页查询")
    @PostMapping("entries/page")
    public PageResponse<SancaiEntryResponse> pageEntries(@Valid @RequestBody SancaiEntryPageRequest request) {
        return PageResponseHelper.fromPageResult(
                service.pageEntries(
                        SancaiInterfaceAssembler.toQuery(request), PageInterfaceAssembler.toPageQuery(request)),
                SancaiInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查看三才图会条目", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "详情")
    @GetMapping("entries/{id}")
    public SancaiEntryResponse getEntry(@PathVariable Long id) {
        return SancaiInterfaceAssembler.toResponse(service.getEntry(SancaiEntryIdCodec.toDomain(id)));
    }

    @Operation(summary = "保存三才图会条目", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "保存")
    @PostMapping("entries/save")
    public SancaiEntryResponse saveEntry(@Valid @RequestBody SancaiEntrySaveRequest request) {
        SancaiEntryId id = service.saveEntry(SancaiInterfaceAssembler.toCommand(request));
        return SancaiEntryResponse.builder().id(id == null ? null : id.value()).build();
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
    public void deleteEntry(@Valid @RequestBody SancaiEntrySaveRequest request) {
        service.deleteEntry(SancaiEntryIdCodec.toDomain(request.getId()));
    }

}
