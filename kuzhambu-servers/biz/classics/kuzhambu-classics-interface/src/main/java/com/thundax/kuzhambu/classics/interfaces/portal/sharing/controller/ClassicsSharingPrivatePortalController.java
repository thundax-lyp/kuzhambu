package com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller;

import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.assembler.ClassicsSharingPortalInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/portal/classics/private-shares")
@WrappedApiController
public class ClassicsSharingPrivatePortalController {
    private final ClassicsSharingApplicationService service;

    public ClassicsSharingPrivatePortalController(ClassicsSharingApplicationService service) {
        this.service = service;
    }

    @GetMapping("{shareToken}")
    public ClassicsSharePortalResponse get(@PathVariable("shareToken") String shareToken) {
        return ClassicsSharingPortalInterfaceAssembler.toPrivateResponse(
                service.getPrivatePortalShare(shareToken, currentUserId(), currentAuthorities()), shareToken);
    }

    @GetMapping("{shareToken}/resources/{storageObjectId}/content")
    public void content(
            @PathVariable("shareToken") String shareToken,
            @PathVariable("storageObjectId") Long storageObjectId,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        ClassicsStoredContentResult content;
        try {
            content = service.getPrivatePortalShareResourceContent(
                    shareToken, storageObjectId, Boolean.TRUE.equals(download), currentUserId(), currentAuthorities());
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

    private static Long currentUserId() {
        String subjectId = KuzhambuContextHolder.currentSubjectId();
        if (StringUtils.isBlank(subjectId)) {
            return null;
        }
        try {
            return Long.valueOf(subjectId);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static Set<String> currentAuthorities() {
        return KuzhambuContextHolder.currentAuthorities();
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
