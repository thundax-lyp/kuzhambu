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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@PublicApi
@RequestMapping("/api/portal/classics/sancai")
@WrappedApiController
public class SancaiPortalController {
    private final SancaiApplicationService service;

    public SancaiPortalController(SancaiApplicationService service) {
        this.service = service;
    }

    @GetMapping("categories")
    public List<SancaiPortalCategoryResponse> listCategories() {
        return service.listCategories().stream()
                .map(SancaiPortalInterfaceAssembler::toResponse)
                .toList();
    }

    @GetMapping("volumes")
    public List<SancaiPortalVolumeResponse> listVolumes(
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        return service.listVolumes(SancaiCategoryIdCodec.toDomain(categoryId)).stream()
                .map(SancaiPortalInterfaceAssembler::toResponse)
                .toList();
    }

    @GetMapping("entries")
    public PageResponse<SancaiPortalEntryResponse> pageEntries(@ModelAttribute SancaiPortalEntrySearchRequest request) {
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

    @GetMapping("entries/{id}")
    public SancaiPortalEntryResponse getEntry(@PathVariable("id") Long id) {
        SancaiEntry entry = service.getEntry(SancaiEntryIdCodec.toDomain(id));
        if (!SancaiPortalInterfaceAssembler.isPublicPublished(entry)) {
            throw new BizException("三才图会条目不存在或不可公开访问");
        }
        return SancaiPortalInterfaceAssembler.toResponse(entry);
    }
}
