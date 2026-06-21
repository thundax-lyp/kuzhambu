package com.thundax.kuzhambu.classics.interfaces.admin.sancai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageSortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageUploadCommand;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageContent;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageResource;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryDraft;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryDraftId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiShowcaseId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.SancaiAssetAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiAssetRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryImageSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiAssetResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

class SancaiAssetAdminControllerTest {

    @Test
    void routesShouldKeepAssetAdminApiPathsAndPermissions() throws Exception {
        assertRequestMapping(SancaiAssetAdminController.class, "/api/classics/sancai/assets");
        assertPostMapping(
                SancaiAssetAdminController.class,
                "updateDraft",
                "drafts/update",
                "classics:sancai:edit",
                SancaiAssetRequest.class);
        assertGetMapping(
                SancaiAssetAdminController.class,
                "latestDraft",
                "drafts/latest/{entryId}",
                "classics:sancai:view",
                Long.class);
        assertPostMapping(
                SancaiAssetAdminController.class,
                "updateImage",
                "images/update",
                "classics:sancai:edit",
                SancaiAssetRequest.class);
        assertMultipartPostMapping(
                SancaiAssetAdminController.class,
                "uploadImage",
                "images/{entryId}/upload",
                "classics:sancai:edit",
                Long.class,
                MultipartFile.class,
                String.class,
                String.class,
                Boolean.class,
                Long.class);
        assertGetMapping(
                SancaiAssetAdminController.class,
                "downloadImage",
                "images/{entryId}/{imageId}/content",
                "classics:sancai:view",
                Long.class,
                Long.class,
                Boolean.class,
                HttpServletResponse.class);
        assertGetMapping(
                SancaiAssetAdminController.class, "listImages", "images/{entryId}", "classics:sancai:view", Long.class);
        assertPostMapping(
                SancaiAssetAdminController.class,
                "sortImages",
                "images/sort",
                "classics:sancai:edit",
                SancaiEntryImageSortRequest.class);
        assertPostMapping(
                SancaiAssetAdminController.class,
                "requestShowcase",
                "showcases/request",
                "classics:sancai:edit",
                SancaiAssetRequest.class);
        assertPostMapping(
                SancaiAssetAdminController.class,
                "pageShowcases",
                "showcases/page",
                "classics:sancai:view",
                SancaiAssetRequest.class);
        assertGetMapping(
                SancaiAssetAdminController.class,
                "downloadShowcaseContent",
                "showcases/{id}/content",
                "classics:sancai:view",
                Long.class,
                Boolean.class,
                HttpServletResponse.class);
    }

    @Test
    void uploadImageShouldReturnBusinessResourceUrls() {
        SancaiAssetResponse response = controller()
                .uploadImage(
                        3001L,
                        new InMemoryMultipartFile("三才图.png", "image/png", "image-bin".getBytes()),
                        "山川图",
                        "ORIGINAL",
                        true,
                        8001L);

        assertEquals(8002L, response.getId());
        assertEquals(3001L, response.getEntryId());
        assertEquals(7001L, response.getStorageObjectId());
        assertEquals("三才图.png", response.getOriginalFilename());
        assertEquals("image/png", response.getContentType());
        assertEquals(9L, response.getSize());
        assertEquals("/api/classics/sancai/assets/images/3001/8002/content", response.getPreviewUrl());
        assertEquals("/api/classics/sancai/assets/images/3001/8002/content?download=true", response.getDownloadUrl());
    }

    @Test
    void downloadImageShouldSupportInlineAndAttachment() throws Exception {
        SancaiAssetAdminController controller = controller();
        MockHttpServletResponse inlineResponse = new MockHttpServletResponse();

        controller.downloadImage(3001L, 8002L, false, inlineResponse);

        assertEquals("image/png", inlineResponse.getContentType());
        assertEquals(9, inlineResponse.getContentLength());
        assertTrue(inlineResponse.getHeader("Content-Disposition").startsWith("inline;"));
        assertEquals("image-bin", inlineResponse.getContentAsString());

        MockHttpServletResponse attachmentResponse = new MockHttpServletResponse();
        controller.downloadImage(3001L, 8002L, true, attachmentResponse);

        String disposition = attachmentResponse.getHeader("Content-Disposition");
        assertTrue(disposition.startsWith("attachment;"));
        assertTrue(disposition.contains("filename=\"三才图.png\""));
        assertTrue(disposition.contains("filename*=UTF-8''%E4%B8%89%E6%89%8D%E5%9B%BE.png"));
    }

    @Test
    void pageShowcasesShouldReturnShowcaseContract() {
        SancaiAssetRequest request = new SancaiAssetRequest();
        request.setStatus("REQUESTED");
        request.setPageNo(1);
        request.setPageSize(10);

        var page = controller().pageShowcases(request);

        assertEquals(1, page.getRecords().size());
        assertEquals(1L, page.getRecords().get(0).getId());
        assertEquals("COMPLETED", page.getRecords().get(0).getStatus());
        assertEquals("{}", page.getRecords().get(0).getScopeJson());
        assertEquals(7001L, page.getRecords().get(0).getStorageObjectId());
        assertEquals(3, page.getRecords().get(0).getEntryCount());
        assertEquals("PUBLIC_ONLY", page.getRecords().get(0).getVisibilityRiskStatus());
        assertEquals(
                "/api/classics/sancai/assets/showcases/7001/content",
                page.getRecords().get(0).getContentUrl());
        assertEquals(
                "/api/classics/sancai/assets/showcases/7001/content?download=true",
                page.getRecords().get(0).getDownloadUrl());
    }

    @Test
    void downloadShowcaseContentShouldSupportInlineAndAttachment() throws Exception {
        SancaiAssetAdminController controller = controller();
        MockHttpServletResponse inlineResponse = new MockHttpServletResponse();

        controller.downloadShowcaseContent(7001L, false, inlineResponse);

        assertEquals("application/json", inlineResponse.getContentType());
        assertEquals(8, inlineResponse.getContentLength());
        assertTrue(inlineResponse.getHeader("Content-Disposition").startsWith("inline;"));
        assertEquals("demo-json", inlineResponse.getContentAsString());

        MockHttpServletResponse attachmentResponse = new MockHttpServletResponse();
        controller.downloadShowcaseContent(7001L, true, attachmentResponse);

        String disposition = attachmentResponse.getHeader("Content-Disposition");
        assertTrue(disposition.startsWith("attachment;"));
        assertTrue(disposition.contains("filename=\"showcase.json\""));
        assertTrue(disposition.contains("filename*=UTF-8''showcase.json"));
    }

    private static SancaiAssetAdminController controller() {
        return new SancaiAssetAdminController(service(), storageService());
    }

    private static SancaiAssetApplicationService service() {
        return (SancaiAssetApplicationService) Proxy.newProxyInstance(
                SancaiAssetApplicationService.class.getClassLoader(),
                new Class<?>[] {SancaiAssetApplicationService.class},
                (proxy, method, args) -> {
                    if ("updateDraft".equals(method.getName())) {
                        return SancaiEntryDraftId.of(6001L);
                    }
                    if ("getLatestDraft".equals(method.getName())) {
                        return new SancaiEntryDraft();
                    }
                    if ("updateImage".equals(method.getName())) {
                        return SancaiEntryImageId.of(8001L);
                    }
                    if ("getImage".equals(method.getName())) {
                        return image();
                    }
                    if ("uploadImage".equals(method.getName())) {
                        SancaiEntryImageUploadCommand command = (SancaiEntryImageUploadCommand) args[0];
                        assertEquals(3001L, command.getEntryId());
                        assertEquals("三才图.png", command.getOriginalFilename());
                        assertEquals("image/png", command.getContentType());
                        assertEquals(9L, command.getSize());
                        assertEquals("山川图", command.getTitle());
                        assertEquals(SancaiEntryImageType.ORIGINAL, command.getImageType());
                        assertEquals(8001L, command.getReplaceImageId());
                        return imageResource();
                    }
                    if ("getImageContent".equals(method.getName())) {
                        assertEquals(SancaiEntryId.of(3001L), args[0]);
                        assertEquals(SancaiEntryImageId.of(8002L), args[1]);
                        return new SancaiEntryImageContent(3001L, 8002L, 7001L, storedContent());
                    }
                    if ("sortImages".equals(method.getName())) {
                        SancaiEntryImageSortCommand command = (SancaiEntryImageSortCommand) args[0];
                        assertEquals(List.of(SancaiEntryImageId.of(8002L)), command.getOrderedIds());
                        return null;
                    }
                    if ("deleteImage".equals(method.getName())) {
                        return null;
                    }
                    if ("listImages".equals(method.getName())) {
                        assertEquals(SancaiEntryId.of(3001L), args[0]);
                        return List.of(image());
                    }
                    if ("updateVisualAsset".equals(method.getName())) {
                        return SancaiVisualAssetId.of(5001L);
                    }
                    if ("useVisualAsset".equals(method.getName()) || "listVisualAssets".equals(method.getName())) {
                        return List.of();
                    }
                    if ("requestShowcase".equals(method.getName())) {
                        return SancaiShowcaseId.of(9001L);
                    }
                    if ("pageShowcases".equals(method.getName())) {
                        assertEquals("REQUESTED", args[0]);
                        PageQuery pageQuery = (PageQuery) args[1];
                        assertEquals(1, pageQuery.getPageNo());
                        assertEquals(10, pageQuery.getPageSize());
                        return PageResult.of(1, 10, 1, List.of(showcase()));
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static StorageApplicationService storageService() {
        return (StorageApplicationService) Proxy.newProxyInstance(
                StorageApplicationService.class.getClassLoader(),
                new Class<?>[] {StorageApplicationService.class},
                (proxy, method, args) -> {
                    if ("openReadableContent".equals(method.getName())) {
                        StoredObjectId storageObjectId = (StoredObjectId) args[0];
                        assertEquals(StoredObjectId.of(7001L), storageObjectId);
                        return new StoredObjectContent(
                                showcaseStoredObject(), new ByteArrayInputStream("demo-json".getBytes()));
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static SancaiEntryImage image() {
        SancaiEntryImage image = new SancaiEntryImage();
        image.setId(SancaiEntryImageId.of(8002L));
        image.setEntryId(SancaiEntryId.of(3001L));
        image.setImageType(SancaiEntryImageType.ORIGINAL);
        image.setTitle("山川图");
        image.setCurrentUsed(true);
        image.setPriority(1);
        return image;
    }

    private static SancaiEntryImageResource imageResource() {
        return new SancaiEntryImageResource(
                3001L,
                8002L,
                7001L,
                "三才图.png",
                "image/png",
                9L,
                "/api/classics/sancai/assets/images/3001/8002/content",
                "/api/classics/sancai/assets/images/3001/8002/content?download=true");
    }

    private static StoredObjectContent storedContent() {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectId.of(7001L));
        storage.setOriginalFilename("三才图.png");
        storage.setContentType("image/png");
        storage.setSize(9L);
        return new StoredObjectContent(storage, new ByteArrayInputStream("image-bin".getBytes()));
    }

    private static SancaiShowcase showcase() {
        SancaiShowcase showcase = new SancaiShowcase();
        showcase.setId(SancaiShowcaseId.of(1L));
        showcase.setRequestedAt(new Date(1690000000000L));
        showcase.setStatus(SancaiShowcaseStatus.COMPLETED);
        showcase.setScopeJson("{}");
        showcase.setStorageObjectId(StorageObjectId.of(7001L));
        showcase.setEntryCount(3);
        showcase.setVisibilityRiskStatus(SancaiVisibilityRiskStatus.PUBLIC_ONLY);
        return showcase;
    }

    private static StoredObject showcaseStoredObject() {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectId.of(7001L));
        storage.setOriginalFilename("showcase.json");
        storage.setContentType("application/json");
        storage.setSize(8L);
        return storage;
    }

    private static void assertRequestMapping(Class<?> controllerType, String expectedPath) {
        RequestMapping mapping = controllerType.getAnnotation(RequestMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertPostMapping(
            Class<?> controllerType,
            String methodName,
            String expectedPath,
            String expectedPermission,
            Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of(expectedPermission), List.of(permission.value()));
    }

    private static void assertMultipartPostMapping(
            Class<?> controllerType,
            String methodName,
            String expectedPath,
            String expectedPermission,
            Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        assertEquals(MediaType.MULTIPART_FORM_DATA_VALUE, mapping.consumes()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of(expectedPermission), List.of(permission.value()));
    }

    private static void assertGetMapping(
            Class<?> controllerType,
            String methodName,
            String expectedPath,
            String expectedPermission,
            Class<?>... parameterTypes)
            throws Exception {
        Method method = controllerType.getDeclaredMethod(methodName, parameterTypes);
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        HasPermission permission = method.getAnnotation(HasPermission.class);
        assertEquals(List.of(expectedPermission), List.of(permission.value()));
    }

    private static final class InMemoryMultipartFile implements MultipartFile {

        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        private InMemoryMultipartFile(String originalFilename, String contentType, byte[] content) {
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) {
            throw new UnsupportedOperationException("transferTo");
        }
    }
}
