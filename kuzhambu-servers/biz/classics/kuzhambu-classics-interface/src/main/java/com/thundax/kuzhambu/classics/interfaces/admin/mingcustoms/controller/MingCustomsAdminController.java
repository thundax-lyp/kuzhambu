package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordSortCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsKeywordIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.assembler.MingCustomsInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsKeywordSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsVersionRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsKeywordCloudItemResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsTagCloudItemResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsVersionResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "古籍模块-明代习俗", description = "明代习俗")
@SysLogger(module = {"古籍", "明代习俗"})
@RequestMapping("/api/classics/ming-customs")
@WrappedApiController
public class MingCustomsAdminController {
    private final MingCustomsApplicationService service;
    private final ClassicsContentApplicationService contentService;

    public MingCustomsAdminController(MingCustomsApplicationService service) {
        this(service, null);
    }

    @Autowired
    public MingCustomsAdminController(
            MingCustomsApplicationService service, ClassicsContentApplicationService contentService) {
        this.service = service;
        this.contentService = contentService;
    }

    @Operation(summary = "分页查询明代习俗", description = "classics:mingcustoms:view")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:view")
    @SysLogger(value = "分页查询")
    @PostMapping("page")
    public PageResponse<MingCustomsResponse> page(@Valid @RequestBody MingCustomsRequest request) {
        MingCustomsPageQuery query = MingCustomsInterfaceAssembler.toQuery(request);
        query.setOperatorPermissions(KuzhambuContextHolder.currentAuthorities());
        return PageResponseHelper.fromPageResult(
                service.page(query, PageInterfaceAssembler.toPageQuery(request)),
                MingCustomsInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查看明代习俗", description = "classics:mingcustoms:view")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:view")
    @SysLogger(value = "详情")
    @PostMapping("get")
    public MingCustomsResponse get(@Valid @RequestBody MingCustomsRequest request) {
        Long id = requireParameter(request == null ? null : request.getId(), "id");
        return MingCustomsInterfaceAssembler.toResponse(service.get(MingCustomsEntryIdCodec.toDomain(id)));
    }

    @Operation(summary = "新增明代习俗", description = "classics:mingcustoms:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:edit")
    @SysLogger(value = "新增")
    @PostMapping("add")
    public MingCustomsResponse add(@Valid @RequestBody MingCustomsRequest request) {
        MingCustomsEntryId id = service.add(MingCustomsInterfaceAssembler.toCommand(request));
        return MingCustomsResponse.builder().id(id == null ? null : id.value()).build();
    }

    @Operation(summary = "更新明代习俗", description = "classics:mingcustoms:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:edit")
    @SysLogger(value = "更新")
    @PostMapping("update")
    public MingCustomsResponse update(@Valid @RequestBody MingCustomsRequest request) {
        MingCustomsEntryId id = service.update(MingCustomsInterfaceAssembler.toCommand(request));
        return MingCustomsResponse.builder().id(id == null ? null : id.value()).build();
    }

    @Operation(summary = "新增明代习俗关键词", description = "classics:mingcustoms:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:edit")
    @SysLogger(value = "新增关键词")
    @PostMapping("keywords/add")
    public MingCustomsResponse addKeyword(@Valid @RequestBody MingCustomsRequest request) {
        return MingCustomsResponse.builder()
                .id(MingCustomsKeywordIdCodec.toValue(
                        service.addKeyword(MingCustomsInterfaceAssembler.toKeywordCommand(request.getId(), request))))
                .build();
    }

    @Operation(summary = "排序明代习俗关键词", description = "classics:mingcustoms:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:edit")
    @SysLogger(value = "关键词排序")
    @PostMapping("keywords/sort")
    public Boolean sortKeywords(@Valid @RequestBody MingCustomsKeywordSortRequest request) {
        service.sortKeywords(new MingCustomsKeywordSortCommand(
                RequestListHelper.map(
                        RequestListHelper.presentUnique(
                                request == null ? null : request.getOrderedIds(),
                                "orderedIds",
                                AdminResponseExceptions::invalidParameter),
                        MingCustomsKeywordIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    @Operation(summary = "查询明代习俗关键词云", description = "classics:mingcustoms:view")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:view")
    @SysLogger(value = "关键词云")
    @PostMapping("keyword-cloud/list")
    public List<MingCustomsKeywordCloudItemResponse> listKeywordCloud(@Valid @RequestBody MingCustomsRequest request) {
        return service.listKeywordCloud(request == null ? null : request.getVisibility()).stream()
                .map(MingCustomsInterfaceAssembler::toKeywordCloudResponse)
                .toList();
    }

    @Operation(summary = "查询明代习俗标签云", description = "classics:mingcustoms:view")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:view")
    @SysLogger(value = "标签云")
    @PostMapping("tag-cloud/list")
    public List<MingCustomsTagCloudItemResponse> listTagCloud(@Valid @RequestBody MingCustomsRequest request) {
        MingCustomsPageQuery query = MingCustomsInterfaceAssembler.toTagCloudQuery(
                request == null ? null : request.getCategory(),
                request == null ? null : request.getKeyword(),
                request == null ? null : request.getVisibility());
        query.setOperatorPermissions(KuzhambuContextHolder.currentAuthorities());
        return service.listTagCloud(query).stream()
                .map(MingCustomsInterfaceAssembler::toTagCloudResponse)
                .toList();
    }

    @Operation(summary = "删除明代习俗", description = "classics:mingcustoms:delete")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:delete")
    @SysLogger(value = "删除")
    @PostMapping("delete")
    public void delete(@Valid @RequestBody MingCustomsRequest request) {
        service.delete(MingCustomsEntryIdCodec.toDomain(request.getId()));
    }

    @Operation(summary = "查询明代习俗版本", description = "classics:mingcustoms:view")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:view")
    @SysLogger(value = "版本列表")
    @PostMapping("versions/list")
    public List<MingCustomsVersionResponse> listVersions(@Valid @RequestBody MingCustomsVersionRequest request) {
        return contentService
                .listVersions(
                        ClassicsContentType.MING_CUSTOMS.value(), ClassicsContentIdCodec.toDomain(request.getId()))
                .stream()
                .map(MingCustomsInterfaceAssembler::toVersionResponse)
                .toList();
    }

    @Operation(summary = "查看明代习俗版本", description = "classics:mingcustoms:view")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:view")
    @SysLogger(value = "版本详情")
    @PostMapping("versions/get")
    public MingCustomsVersionResponse getVersion(@Valid @RequestBody MingCustomsVersionRequest request) {
        return MingCustomsInterfaceAssembler.toVersionResponse(ownedVersion(request.getId(), request.getVersionId()));
    }

    @Operation(summary = "恢复明代习俗版本", description = "classics:mingcustoms:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:edit")
    @SysLogger(value = "版本恢复")
    @PostMapping("versions/reset")
    public MingCustomsVersionResponse resetVersion(@Valid @RequestBody MingCustomsVersionRequest request) {
        ownedVersion(request.getId(), request.getVersionId());
        return MingCustomsInterfaceAssembler.toVersionResponse(
                contentService.restoreHistoryVersion(ClassicsContentVersionIdCodec.toDomain(request.getVersionId())));
    }

    private ClassicsContentVersion ownedVersion(Long entryId, Long versionId) {
        ClassicsContentVersion version = contentService.getVersion(ClassicsContentVersionIdCodec.toDomain(versionId));
        if (version == null) {
            throw new BizException("明代习俗历史版本不存在");
        }
        if (version.getContentType() != ClassicsContentType.MING_CUSTOMS
                || !ClassicsContentIdCodec.toDomain(entryId).equals(version.getContentId())) {
            throw new BizException("历史版本不属于当前明代习俗条目");
        }
        return version;
    }

    private static Long requireParameter(Long value, String fieldName) {
        if (value == null) {
            throw AdminResponseExceptions.invalidParameter(fieldName);
        }
        return value;
    }
}
