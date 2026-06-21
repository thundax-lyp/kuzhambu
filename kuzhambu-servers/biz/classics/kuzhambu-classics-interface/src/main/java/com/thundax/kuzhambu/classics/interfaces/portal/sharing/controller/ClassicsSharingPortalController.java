package com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller;

import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.assembler.ClassicsSharingPortalInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.request.ClassicsSharePortalSearchRequest;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalListResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@PublicApi
@RequestMapping("/api/portal/classics/shares")
@WrappedApiController
public class ClassicsSharingPortalController {
    private final ClassicsSharingApplicationService service;
    private final StorageApplicationService storageApplicationService;

    public ClassicsSharingPortalController(ClassicsSharingApplicationService service) {
        this(service, null);
    }

    public ClassicsSharingPortalController(
            ClassicsSharingApplicationService service, StorageApplicationService storageApplicationService) {
        this.service = service;
        this.storageApplicationService = storageApplicationService;
    }

    @GetMapping
    public ClassicsSharePortalListResponse list(ClassicsSharePortalSearchRequest request) {
        ClassicsSharePortalSearchRequest effectiveRequest =
                request == null ? new ClassicsSharePortalSearchRequest() : request;
        return ClassicsSharingPortalInterfaceAssembler.toListResponse(service.pagePortalShares(
                effectiveRequest.getContentType(),
                effectiveRequest.getTitle(),
                effectiveRequest.getIssuedAfter(),
                effectiveRequest.getIssuedBefore(),
                new PageQuery(effectiveRequest.getPageNo(), effectiveRequest.getPageSize())));
    }

    @GetMapping("{shareToken}")
    public ClassicsSharePortalResponse get(@PathVariable("shareToken") String shareToken) {
        return ClassicsSharingPortalInterfaceAssembler.toResponse(
                service.getPortalShare(shareToken), shareToken, storageApplicationService);
    }
}
