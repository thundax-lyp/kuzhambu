package com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller;

import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.assembler.ClassicsSharingPortalInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.request.ClassicsSharePortalSearchRequest;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalListResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("{shareToken}/resources/{storageObjectId}/content")
    public void content(
            @PathVariable("shareToken") String shareToken,
            @PathVariable("storageObjectId") Long storageObjectId,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        StoredObjectContent content;
        try {
            content = service.getPortalShareResourceContent(shareToken, storageObjectId, Boolean.TRUE.equals(download));
        } catch (BizException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        StoredObject storage = content.getStorage();
        response.setContentType(
                StringUtils.defaultIfBlank(storage.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE));
        if (storage.getSize() != null) {
            response.setContentLengthLong(storage.getSize());
        }
        response.setHeader(
                "Content-Disposition",
                contentDisposition(storage.getOriginalFilename(), Boolean.TRUE.equals(download)));
        try (InputStream inputStream = content.getInputStream()) {
            inputStream.transferTo(response.getOutputStream());
        }
    }

    private static String contentDisposition(String originalFilename, boolean download) {
        String disposition = download ? "attachment" : "inline";
        String filename = StringUtils.defaultIfBlank(FilenameUtils.getName(originalFilename), "file");
        String asciiFilename = filename.replace("\\", "").replace("\"", "");
        String encodedFilename =
                URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return disposition + "; filename=\"" + asciiFilename + "\"; filename*=UTF-8''" + encodedFilename;
    }
}
