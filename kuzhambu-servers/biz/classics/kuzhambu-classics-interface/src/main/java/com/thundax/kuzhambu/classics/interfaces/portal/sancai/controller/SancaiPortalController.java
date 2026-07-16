package com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller;

import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiCategoryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.assembler.SancaiPortalInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.request.SancaiPortalEntrySearchRequest;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response.SancaiPortalCategoryResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response.SancaiPortalEntryResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response.SancaiPortalVolumeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@PublicApi
@RequestMapping("/api/portal/classics/sancai")
@WrappedApiController
public class SancaiPortalController {
    private final SancaiApplicationService service;

    public SancaiPortalController(SancaiApplicationService service) {
        this.service = service;
    }

    @PostMapping("categories/list")
    public List<SancaiPortalCategoryResponse> listCategories() {
        return service.listCategories().stream()
                .map(SancaiPortalInterfaceAssembler::toResponse)
                .toList();
    }

    @PostMapping("volumes/list")
    public List<SancaiPortalVolumeResponse> listVolumes(@RequestBody SancaiPortalEntrySearchRequest request) {
        return service
                .listVolumes(SancaiCategoryIdCodec.toDomain(request == null ? null : request.getCategoryId()))
                .stream()
                .map(SancaiPortalInterfaceAssembler::toResponse)
                .toList();
    }

    @PostMapping("entries/page")
    public PageResponse<SancaiPortalEntryResponse> pageEntries(@RequestBody SancaiPortalEntrySearchRequest request) {
        SancaiPortalEntrySearchRequest effectiveRequest =
                request == null ? new SancaiPortalEntrySearchRequest() : request;
        effectiveRequest.setKeyword(SancaiPortalInterfaceAssembler.normalizeKeyword(effectiveRequest.getKeyword()));
        return PageResponseHelper.fromPageResult(
                service.pageEntries(
                        SancaiPortalInterfaceAssembler.toPublicQuery(effectiveRequest),
                        new PageQuery(
                                SancaiPortalInterfaceAssembler.pageNo(effectiveRequest.getPageNo()),
                                SancaiPortalInterfaceAssembler.pageSize(effectiveRequest.getPageSize()))),
                SancaiPortalInterfaceAssembler::toResponse);
    }

    @PostMapping("entries/get")
    public SancaiPortalEntryResponse getEntry(@RequestBody SancaiPortalEntrySearchRequest request) {
        SancaiEntry entry = service.getEntry(SancaiEntryIdCodec.toDomain(request == null ? null : request.getId()));
        if (!SancaiPortalInterfaceAssembler.isPublicPublished(entry)) {
            throw new BizException("三才图会条目不存在或不可公开访问");
        }
        return SancaiPortalInterfaceAssembler.toResponse(entry);
    }
}
