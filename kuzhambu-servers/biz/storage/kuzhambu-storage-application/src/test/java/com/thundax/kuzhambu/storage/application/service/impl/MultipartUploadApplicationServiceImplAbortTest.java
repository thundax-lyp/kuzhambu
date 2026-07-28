package com.thundax.kuzhambu.storage.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.AbortMultipartUploadCommand;
import com.thundax.kuzhambu.storage.domain.object.codec.MultipartUploadIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;
import com.thundax.kuzhambu.storage.domain.object.repository.MultipartUploadRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MultipartUploadApplicationServiceImplAbortTest {
    private static final String UPLOAD_ID = "upload-1";
    private static final MultipartUploadId UPLOAD_ID_REF = MultipartUploadIdCodec.toDomain(UPLOAD_ID);
    private static final String PART_PATH_1 = "multipart/upload-1/1.part";
    private static final String PART_PATH_2 = "multipart/upload-1/2.part";
    private static final String PART_PATH_3 = "multipart/upload-1/3.part";

    @Test
    void abortShouldCleanupPartsAndMarkSessionAborted() throws IOException {
        MultipartUploadRepository multipartUploadRepository = mock(MultipartUploadRepository.class);
        StoredObjectContentRepository contentRepository = mock(StoredObjectContentRepository.class);
        MultipartUploadSession session = session();
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        MultipartUploadApplicationServiceImpl service = new MultipartUploadApplicationServiceImpl(
                multipartUploadRepository, contentRepository, storageApplicationService);

        when(multipartUploadRepository.getMultipartSessionByUploadId(UPLOAD_ID_REF))
                .thenReturn(session);
        when(multipartUploadRepository.listMultipartParts(UPLOAD_ID_REF)).thenReturn(parts());
        when(multipartUploadRepository.updateMultipartSession(session)).thenReturn(1);

        int updated = service.abort(new AbortMultipartUploadCommand(UPLOAD_ID));

        assertEquals(1, updated);
        verify(contentRepository).delete(argThat(storage -> PART_PATH_1.equals(storage.getObjectKey())));
        verify(contentRepository).delete(argThat(storage -> PART_PATH_2.equals(storage.getObjectKey())));
        verify(contentRepository).delete(argThat(storage -> PART_PATH_3.equals(storage.getObjectKey())));
        verify(contentRepository, times(3)).delete(any());
        verify(multipartUploadRepository).deleteMultipartParts(UPLOAD_ID_REF);
        verify(multipartUploadRepository).updateMultipartSession(session);
    }

    private static MultipartUploadSession session() {
        MultipartUploadSession session = new MultipartUploadSession();
        session.setUploadId(UPLOAD_ID);
        session.setUploadStatus(MultipartUploadStatus.UPLOADING);
        session.setOwnerType(StorageOwnerType.USER);
        session.setOwnerId("u-1");
        session.setOriginalFilename("multipart-file.txt");
        session.setMimeType("text/plain");
        session.setTotalSize(9L);
        session.setPartSize(3L);
        session.setBucketName("local");
        session.setObjectKey("multipart-file.txt");
        return session;
    }

    private static List<MultipartUploadPart> parts() {
        List<MultipartUploadPart> parts = new ArrayList<>();
        parts.add(part(1, PART_PATH_1));
        parts.add(part(2, PART_PATH_2));
        parts.add(part(3, PART_PATH_3));
        return parts;
    }

    private static MultipartUploadPart part(int partNumber, String path) {
        MultipartUploadPart part = new MultipartUploadPart();
        part.setUploadId(UPLOAD_ID);
        part.setPartNumber(partNumber);
        part.setPartPath(path);
        part.setSize(3L);
        part.setEtag("e" + partNumber);
        return part;
    }
}
