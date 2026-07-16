package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller;

import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageSortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageUploadCommand;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageContent;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryImageIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVisualAssetIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryDraftId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.assembler.SancaiAssetInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiAssetRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryImageSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiAssetResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "古籍模块-三才图会资产", description = "三才图会资产")
@SysLogger(module = {"古籍", "三才图会资产"})
@RequestMapping("/api/classics/sancai/assets")
@WrappedApiController
public class SancaiAssetAdminController {
    private final SancaiAssetApplicationService service;

    public SancaiAssetAdminController(SancaiAssetApplicationService service) {
        this.service = service;
    }

    @Operation(summary = "更新三才图会草稿", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "更新草稿")
    @PostMapping("drafts/update")
    public SancaiAssetResponse updateDraft(@Valid @RequestBody SancaiAssetRequest request) {
        SancaiEntryDraftId id = service.updateDraft(SancaiAssetInterfaceAssembler.toDraftCommand(request));
        return SancaiAssetResponse.builder().id(id == null ? null : id.value()).build();
    }

    @Operation(summary = "查看三才图会最新草稿", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "最新草稿")
    @GetMapping("drafts/latest/{entryId}")
    public SancaiAssetResponse latestDraft(@PathVariable Long entryId) {
        return SancaiAssetInterfaceAssembler.toDraftResponse(
                service.getLatestDraft(SancaiEntryIdCodec.toDomain(entryId)));
    }

    @Operation(summary = "更新三才图会图片", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "更新图片")
    @PostMapping("images/update")
    public SancaiAssetResponse updateImage(@Valid @RequestBody SancaiAssetRequest request) {
        SancaiEntryImageId id = service.updateImage(SancaiAssetInterfaceAssembler.toImageCommand(request));
        return SancaiAssetResponse.builder().id(id == null ? null : id.value()).build();
    }

    @Operation(summary = "上传三才图会图片", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "图片上传")
    @PostMapping(value = "images/{entryId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SancaiAssetResponse uploadImage(
            @PathVariable Long entryId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "imageType", required = false) String imageType,
            @RequestParam(value = "currentUsed", required = false, defaultValue = "true") Boolean currentUsed,
            @RequestParam(value = "replaceImageId", required = false) Long replaceImageId) {
        try {
            return SancaiAssetInterfaceAssembler.toImageResourceResponse(
                    service.uploadImage(new SancaiEntryImageUploadCommand(
                            entryId,
                            file == null ? null : file.getInputStream(),
                            file == null ? null : file.getOriginalFilename(),
                            file == null ? null : file.getContentType(),
                            file == null ? 0L : file.getSize(),
                            title,
                            StringUtils.isBlank(imageType) ? null : SancaiEntryImageType.from(imageType),
                            Boolean.TRUE.equals(currentUsed),
                            replaceImageId)));
        } catch (IOException exception) {
            throw new BizException("三才图片上传失败：" + exception.getMessage());
        }
    }

    @Operation(summary = "查询三才图会图片", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "图片列表")
    @GetMapping("images/{entryId}")
    public List<SancaiAssetResponse> listImages(@PathVariable Long entryId) {
        return service.listImages(SancaiEntryIdCodec.toDomain(entryId)).stream()
                .map(SancaiAssetInterfaceAssembler::toImageResponse)
                .toList();
    }

    @Operation(summary = "删除三才图会图片", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "图片删除")
    @PostMapping("images/delete")
    public Boolean deleteImage(@Valid @RequestBody SancaiAssetRequest request) {
        Long entryId = requireLong(request == null ? null : request.getEntryId(), "entryId");
        Long imageId = requireLong(request == null ? null : request.getId(), "id");
        SancaiEntryImage image = service.getImage(SancaiEntryImageIdCodec.toDomain(imageId));
        if (image == null
                || image.getEntryId() == null
                || !SancaiEntryIdCodec.toDomain(entryId).equals(image.getEntryId())) {
            throw AdminResponseExceptions.invalidParameter("id");
        }
        service.deleteImage(SancaiEntryImageIdCodec.toDomain(imageId));
        return true;
    }

    @Operation(summary = "切换三才图会当前图片", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "切换当前图片")
    @PostMapping("images/current/change")
    public Boolean changeCurrentImage(@Valid @RequestBody SancaiAssetRequest request) {
        service.useImage(
                SancaiEntryIdCodec.toDomain(requireLong(request == null ? null : request.getEntryId(), "entryId")),
                SancaiEntryImageIdCodec.toDomain(requireLong(request == null ? null : request.getId(), "id")));
        return true;
    }

    @Operation(summary = "排序三才图会图片", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "图片排序")
    @PostMapping("images/sort")
    public Boolean sortImages(@Valid @RequestBody SancaiEntryImageSortRequest request) {
        service.sortImages(new SancaiEntryImageSortCommand(
                SancaiEntryIdCodec.toDomain(requireLong(request == null ? null : request.getEntryId(), "entryId")),
                RequestListHelper.map(
                        RequestListHelper.presentUnique(
                                request == null ? null : request.getOrderedIds(),
                                "orderedIds",
                                AdminResponseExceptions::invalidParameter),
                        SancaiEntryImageIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    @Operation(summary = "读取三才图会图片内容", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "图片读取")
    @GetMapping("images/{entryId}/{imageId}/content")
    public void downloadImage(
            @PathVariable Long entryId,
            @PathVariable Long imageId,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        SancaiEntryImageContent imageContent;
        try {
            imageContent = service.getImageContent(
                    SancaiEntryIdCodec.toDomain(entryId), SancaiEntryImageIdCodec.toDomain(imageId));
        } catch (BizException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        ClassicsStoredContentResult content = imageContent.getContent();
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

    @Operation(summary = "查询三才图会视觉资产", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "视觉资产列表")
    @GetMapping("visual-assets/{entryId}")
    public List<SancaiAssetResponse> listVisualAssets(@PathVariable Long entryId) {
        return service.listVisualAssets(SancaiEntryIdCodec.toDomain(entryId)).stream()
                .map(SancaiAssetInterfaceAssembler::toVisualAssetResponse)
                .toList();
    }

    @Operation(summary = "读取三才图会视觉资产原图", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "视觉资产原图读取")
    @GetMapping("visual-assets/{entryId}/{visualAssetId}/source-content")
    public void downloadVisualAssetSourceContent(
            @PathVariable Long entryId,
            @PathVariable Long visualAssetId,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        downloadVisualAssetContent(entryId, visualAssetId, true, download, response);
    }

    @Operation(summary = "读取三才图会视觉资产生成图", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "视觉资产生成图读取")
    @GetMapping("visual-assets/{entryId}/{visualAssetId}/generated-content")
    public void downloadVisualAssetGeneratedContent(
            @PathVariable Long entryId,
            @PathVariable Long visualAssetId,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        downloadVisualAssetContent(entryId, visualAssetId, false, download, response);
    }

    @Operation(summary = "更新三才图会视觉资产", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "更新视觉资产")
    @PostMapping("visual-assets/update")
    public SancaiAssetResponse updateVisualAsset(@Valid @RequestBody SancaiAssetRequest request) {
        Long visualAssetId = service.updateVisualAsset(SancaiAssetInterfaceAssembler.toVisualAsset(request))
                .value();
        return SancaiAssetResponse.builder()
                .id(visualAssetId)
                .visualAssetId(visualAssetId)
                .build();
    }

    @Operation(summary = "切换三才图会当前视觉资产", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "切换当前视觉资产")
    @PostMapping("visual-assets/current/change")
    public Boolean changeCurrentVisualAsset(@Valid @RequestBody SancaiAssetRequest request) {
        service.useVisualAsset(
                SancaiEntryIdCodec.toDomain(request.getEntryId()),
                SancaiVisualAssetIdCodec.toDomain(request.getVisualAssetId()));
        return true;
    }

    private static Long requireLong(Long value, String fieldName) {
        if (value == null) {
            throw AdminResponseExceptions.invalidParameter(fieldName);
        }
        return value;
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

    private void downloadVisualAssetContent(
            Long entryId, Long visualAssetId, boolean sourceContent, Boolean download, HttpServletResponse response)
            throws IOException {
        ClassicsStoredContentResult content;
        try {
            content = sourceContent
                    ? service.getVisualAssetSourceContent(
                            SancaiEntryIdCodec.toDomain(entryId), SancaiVisualAssetIdCodec.toDomain(visualAssetId))
                    : service.getVisualAssetGeneratedContent(
                            SancaiEntryIdCodec.toDomain(entryId), SancaiVisualAssetIdCodec.toDomain(visualAssetId));
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
}
