package com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller;

import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.application.sharing.service.impl.ClassicsSharingApplicationServiceImpl;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.assembler.ClassicsSharingPortalInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.request.ClassicsSharePortalSearchRequest;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalListResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.PostJsonApiExempt;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@PublicApi
@RequestMapping("/api/portal/classics/shares")
@WrappedApiController
public class ClassicsSharingPortalController {
    private final ClassicsSharingApplicationService service;

    public ClassicsSharingPortalController(ClassicsSharingApplicationService service) {
        this.service = service;
    }

    @PostMapping("list")
    public ClassicsSharePortalListResponse list(@Valid @RequestBody ClassicsSharePortalSearchRequest request) {
        ClassicsSharePortalSearchRequest effectiveRequest =
                request == null ? new ClassicsSharePortalSearchRequest() : request;
        return ClassicsSharingPortalInterfaceAssembler.toListResponse(service.pagePortalShares(
                effectiveRequest.getContentType(),
                effectiveRequest.getTitle(),
                effectiveRequest.getIssuedAfter(),
                effectiveRequest.getIssuedBefore(),
                new PageQuery(effectiveRequest.getPageNo(), effectiveRequest.getPageSize())));
    }

    @PostMapping("get")
    public ClassicsSharePortalResponse get(@Valid @RequestBody ClassicsSharePortalSearchRequest request) {
        String shareToken = request == null ? null : request.getShareToken();
        try {
            return ClassicsSharingPortalInterfaceAssembler.toResponse(service.getPortalShare(shareToken), shareToken);
        } catch (BizException exception) {
            if (ClassicsSharingApplicationServiceImpl.PRIVATE_SHARE_AUTH_REQUIRED_CODE.equals(exception.getCode())) {
                return ClassicsSharingPortalInterfaceAssembler.privateAuthRequiredResponse();
            }
            throw exception;
        }
    }

    @PostJsonApiExempt(reason = "文件内容需要浏览器直链预览或下载")
    @GetMapping("{shareToken}/resources/{storageObjectId}/content")
    public void content(
            @PathVariable("shareToken") String shareToken,
            @PathVariable("storageObjectId") Long storageObjectId,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        ClassicsStoredContentResult content;
        try {
            content = service.getPortalShareResourceContent(shareToken, storageObjectId, Boolean.TRUE.equals(download));
        } catch (BizException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(
                StringUtils.defaultIfBlank(content.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE));
        if (content.getSize() != null) {
            response.setContentLengthLong(content.getSize());
        }
        response.setHeader(
                "Content-Disposition",
                contentDisposition(content.getOriginalFilename(), Boolean.TRUE.equals(download)));
        try (InputStream inputStream = content.getInputStream()) {
            inputStream.transferTo(response.getOutputStream());
        }
    }

    private static String contentDisposition(String originalFilename, boolean download) {
        String disposition = download ? "attachment" : "inline";
        String filename = StringUtils.defaultIfBlank(fileName(originalFilename), "file");
        String asciiFilename = filename.replace("\\", "").replace("\"", "");
        String encodedFilename =
                URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return disposition + "; filename=\"" + asciiFilename + "\"; filename*=UTF-8''" + encodedFilename;
    }

    private static String fileName(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        String normalized = path.replace('\\', '/');
        int index = normalized.lastIndexOf('/');
        return index >= 0 ? normalized.substring(index + 1) : normalized;
    }
}
