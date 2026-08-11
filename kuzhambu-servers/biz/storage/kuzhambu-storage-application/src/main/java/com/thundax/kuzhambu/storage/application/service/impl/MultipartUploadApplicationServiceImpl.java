package com.thundax.kuzhambu.storage.application.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.id.UuidHelper;
import com.thundax.kuzhambu.storage.application.command.AbortMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.CompleteMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.CreateStorageCommand;
import com.thundax.kuzhambu.storage.application.command.InitMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.UploadMultipartPartCommand;
import com.thundax.kuzhambu.storage.application.service.MultipartUploadApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;
import com.thundax.kuzhambu.storage.domain.object.repository.MultipartUploadRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class MultipartUploadApplicationServiceImpl implements MultipartUploadApplicationService {
    private static final String EXTENSION_SEPARATOR = ".";
    private static final long MAX_MULTIPART_UPLOAD_SIZE = 20L * 1024L * 1024L;

    private final MultipartUploadRepository multipartUploadRepository;
    private final StoredObjectContentRepository storedObjectContentRepository;
    private final StorageApplicationService storageApplicationService;

    public MultipartUploadApplicationServiceImpl(
            MultipartUploadRepository multipartUploadRepository,
            StoredObjectContentRepository storedObjectContentRepository,
            StorageApplicationService storageApplicationService) {
        this.multipartUploadRepository = multipartUploadRepository;
        this.storedObjectContentRepository = storedObjectContentRepository;
        this.storageApplicationService = storageApplicationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MultipartUploadSession init(InitMultipartUploadCommand command) {
        MultipartUploadSession session = toMultipartSession(command);
        if (session == null) {
            throw new BizException("Multipart upload session can not be null");
        }
        if (session.getOwnerType() == null || StringUtils.isBlank(session.getOwnerId())) {
            throw new BizException("Multipart upload owner can not be empty");
        }
        validateMultipartSize(session);
        if (StringUtils.isBlank(session.getUploadId())) {
            session.setUploadId(UuidHelper.compact());
        }
        if (StringUtils.isBlank(session.getObjectKey())) {
            session.setObjectKey(defaultObjectKey(session));
        }
        if (StringUtils.isBlank(session.getProviderUploadId())) {
            session.setProviderUploadId(session.getUploadId());
        }
        session.setUploadStatus(MultipartUploadStatus.INITIATED);
        session.setUploadedPartCount(0);
        session.setId(multipartUploadRepository.insertMultipartSession(session));
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MultipartUploadPart uploadPart(UploadMultipartPartCommand command) {
        MultipartUploadPart part = toMultipartPart(command);
        if (part == null || command == null || command.getInputStream() == null) {
            throw new BizException("Multipart upload part content can not be null");
        }
        if (part == null || StringUtils.isBlank(part.getUploadId())) {
            throw new BizException("Multipart upload part can not be null");
        }
        if (part.getPartNumber() == null || part.getPartNumber() <= 0) {
            throw new BizException("Multipart upload part number must start from 1");
        }
        MultipartUploadSession session = requireActiveMultipartSession(part.getUploadIdRef());
        validateMultipartPart(session, part);
        if (multipartUploadRepository.getMultipartPart(part.getUploadIdRef(), part.getPartNumberRef()) != null) {
            throw new BizException("Multipart upload part already exists: " + part.getPartNumber());
        }
        part.setPartPath(resolveMultipartPartObjectKey(session, part.getPartNumber()));
        persistMultipartPartContent(part, command.getInputStream());
        part.setId(multipartUploadRepository.insertMultipartPart(part));

        session.setUploadStatus(MultipartUploadStatus.UPLOADING);
        session.setUploadedPartCount(multipartUploadRepository.countMultipartParts(session.getUploadIdRef()));
        multipartUploadRepository.updateMultipartSession(session);
        return part;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoredObject complete(CompleteMultipartUploadCommand command) {
        MultipartUploadId uploadId = command == null ? null : command.getUploadId();
        MultipartUploadSession session = requireActiveMultipartSession(uploadId);
        claimCompleting(session);
        List<MultipartUploadPart> parts = multipartUploadRepository.listMultipartParts(uploadId);
        validateMultipartParts(session, parts);

        StoredObject storage = toCompletedStorage(session, command);
        persistCompletedMultipartStorage(session, parts, storage);
        StoredObject createdStorage = storageApplicationService.create(toCreateStorageCommand(storage));
        storage.setId(createdStorage == null ? null : createdStorage.getId());

        Instant now = Instant.now();
        session.setUploadStatus(MultipartUploadStatus.COMPLETED);
        session.setUploadedPartCount(parts.size());
        session.setCompletedDate(now);
        multipartUploadRepository.updateMultipartSession(session);
        cleanupCompletedMultipartParts(session.getUploadIdRef(), parts);
        return storage;
    }

    private void persistMultipartPartContent(MultipartUploadPart part, InputStream inputStream) {
        try (InputStream stream = inputStream) {
            StoredObject partStorage = new StoredObject();
            partStorage.setObjectKey(part.getPartPath());
            StoredObject savedStorage = storedObjectContentRepository.save(
                    partStorage, StorageInputStreamLimiter.limit(stream, part.getSize()));
            if (!part.getSize().equals(savedStorage.getSize())) {
                deleteMultipartPartContent(part);
                throw new BizException("Multipart upload part size mismatch: " + part.getPartNumber());
            }
        } catch (IOException exception) {
            throw new BizException("Multipart upload part save failed: " + exception.getMessage());
        }
    }

    private void deleteMultipartPartContent(MultipartUploadPart part) {
        try {
            StoredObject partStorage = new StoredObject();
            partStorage.setObjectKey(part.getPartPath());
            storedObjectContentRepository.delete(partStorage);
        } catch (IOException ignored) {
            // best-effort cleanup for rejected multipart parts
        }
    }

    private void cleanupCompletedMultipartParts(MultipartUploadId uploadId, List<MultipartUploadPart> parts) {
        for (MultipartUploadPart part : parts) {
            deleteMultipartPartContent(part);
        }
        multipartUploadRepository.deleteByUploadId(uploadId);
    }

    private void persistCompletedMultipartStorage(
            MultipartUploadSession session, List<MultipartUploadPart> parts, StoredObject storage) {
        List<InputStream> streams = new ArrayList<>();
        try (InputStream mergedStream = mergeParts(session, parts, streams)) {
            StoredObject mergedStorage = storedObjectContentRepository.save(storage, mergedStream);
            storage.setObjectKey(mergedStorage.getObjectKey());
            storage.setBucketName(mergedStorage.getBucketName());
            storage.setSize(mergedStorage.getSize());
            storage.setAccessEndpoint(mergedStorage.getAccessEndpoint());
        } catch (IOException exception) {
            throw new BizException("Multipart complete save failed: " + exception.getMessage());
        } finally {
            closePartStreams(streams);
        }
    }

    private void closePartStreams(List<InputStream> streams) {
        for (InputStream stream : streams) {
            if (stream == null) {
                continue;
            }
            try {
                stream.close();
            } catch (IOException ignored) {
                // ignore
            }
        }
    }

    private InputStream mergeParts(
            MultipartUploadSession session, List<MultipartUploadPart> parts, List<InputStream> streams)
            throws IOException {
        if (parts == null || parts.isEmpty()) {
            return InputStream.nullInputStream();
        }
        for (MultipartUploadPart part : parts) {
            if (part == null || StringUtils.isBlank(part.getPartPath())) {
                throw new BizException("Multipart upload part path can not be empty: " + session.getUploadId());
            }
            StoredObject partStorage = new StoredObject();
            partStorage.setObjectKey(part.getPartPath());
            streams.add(storedObjectContentRepository.open(partStorage));
        }
        if (streams.isEmpty()) {
            return InputStream.nullInputStream();
        }
        return new SequenceInputStream(new Vector<>(streams).elements());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int abort(AbortMultipartUploadCommand command) {
        MultipartUploadSession session = requireActiveMultipartSession(command == null ? null : command.uploadId());
        List<MultipartUploadPart> parts = multipartUploadRepository.listMultipartParts(session.getUploadIdRef());
        for (MultipartUploadPart part : parts) {
            StoredObject partStorage = new StoredObject();
            partStorage.setObjectKey(part.getPartPath());
            try {
                storedObjectContentRepository.delete(partStorage);
            } catch (IOException exception) {
                throw new BizException("Multipart upload part delete failed: " + exception.getMessage());
            }
        }
        multipartUploadRepository.deleteByUploadId(session.getUploadIdRef());
        Instant now = Instant.now();
        session.setUploadStatus(MultipartUploadStatus.ABORTED);
        session.setAbortedDate(now);
        return multipartUploadRepository.updateMultipartSession(session);
    }

    private MultipartUploadSession requireActiveMultipartSession(MultipartUploadId uploadId) {
        if (uploadId == null) {
            throw new BizException("Multipart upload id can not be empty");
        }
        MultipartUploadSession session = multipartUploadRepository.getMultipartSessionByUploadId(uploadId);
        if (session == null) {
            throw new BizException("Multipart upload session not found: " + uploadId.value());
        }
        if (MultipartUploadStatus.COMPLETED == session.getUploadStatus()
                || MultipartUploadStatus.ABORTED == session.getUploadStatus()
                || MultipartUploadStatus.COMPLETING == session.getUploadStatus()) {
            throw new BizException("Multipart upload session is closed: " + uploadId.value());
        }
        return session;
    }

    private void claimCompleting(MultipartUploadSession session) {
        int updated = multipartUploadRepository.updateMultipartSessionStatus(
                session.getUploadIdRef(), session.getUploadStatus(), MultipartUploadStatus.COMPLETING);
        if (updated != 1) {
            throw new BizException("Multipart upload session status conflict: " + session.getUploadId());
        }
        session.setUploadStatus(MultipartUploadStatus.COMPLETING);
    }

    private void validateMultipartParts(MultipartUploadSession session, List<MultipartUploadPart> parts) {
        if (parts == null || parts.isEmpty()) {
            throw new BizException("Multipart upload has no parts: " + session.getUploadId());
        }
        int expectedPartCount = expectedPartCount(session);
        if (expectedPartCount > 0 && parts.size() != expectedPartCount) {
            throw new BizException("Multipart upload parts are incomplete: " + session.getUploadId());
        }

        List<Integer> partNumbers = new ArrayList<>();
        for (MultipartUploadPart part : parts) {
            partNumbers.add(part.getPartNumber());
        }
        for (int i = 1; i <= expectedPartCount; i++) {
            if (!partNumbers.contains(i)) {
                throw new BizException("Multipart upload part is missing: " + i);
            }
        }
    }

    private int expectedPartCount(MultipartUploadSession session) {
        Long totalSize = session.getTotalSize();
        Long partSize = session.getPartSize();
        if (totalSize == null || totalSize <= 0 || partSize == null || partSize <= 0) {
            return 0;
        }
        return (int) ((totalSize + partSize - 1) / partSize);
    }

    private void validateMultipartSize(MultipartUploadSession session) {
        if (session.getTotalSize() == null || session.getTotalSize() <= 0L) {
            throw new BizException("Multipart upload total size must be greater than 0");
        }
        if (session.getTotalSize() > MAX_MULTIPART_UPLOAD_SIZE) {
            throw new BizException("Multipart upload total size exceeds limit");
        }
        if (session.getPartSize() == null || session.getPartSize() <= 0L) {
            throw new BizException("Multipart upload part size must be greater than 0");
        }
        if (session.getPartSize() > session.getTotalSize()) {
            throw new BizException("Multipart upload part size can not exceed total size");
        }
    }

    private void validateMultipartPart(MultipartUploadSession session, MultipartUploadPart part) {
        if (part.getSize() == null || part.getSize() <= 0L) {
            throw new BizException("Multipart upload part size must be greater than 0");
        }
        int expectedPartCount = expectedPartCount(session);
        if (expectedPartCount <= 0 || part.getPartNumber() > expectedPartCount) {
            throw new BizException("Multipart upload part number exceeds expected count: " + part.getPartNumber());
        }
        long expectedSize = expectedPartSize(session, part.getPartNumber(), expectedPartCount);
        if (part.getSize() != expectedSize) {
            throw new BizException("Multipart upload part size mismatch: " + part.getPartNumber());
        }
    }

    private long expectedPartSize(MultipartUploadSession session, int partNumber, int expectedPartCount) {
        if (partNumber < expectedPartCount) {
            return session.getPartSize();
        }
        long remaining = session.getTotalSize() % session.getPartSize();
        return remaining == 0L ? session.getPartSize() : remaining;
    }

    private StoredObject toCompletedStorage(MultipartUploadSession session, CompleteMultipartUploadCommand command) {
        StoredObject storage = new StoredObject();
        storage.setName(baseName(session.getOriginalFilename()));
        storage.setExtendName(extension(session.getOriginalFilename()));
        storage.setMimeTypeRef(session.getMimeTypeRef());
        storage.setBucketNameRef(
                command == null || command.getBucketName() == null
                        ? session.getBucketNameRef()
                        : command.getBucketName());
        storage.setObjectKeyRef(
                command == null || command.getObjectKey() == null ? session.getObjectKeyRef() : command.getObjectKey());
        storage.setSizeRef(
                command == null || command.getSize() == null ? session.getTotalSizeRef() : command.getSize());
        storage.setAccessEndpoint(command == null ? null : command.getAccessEndpoint());
        storage.setObjectStatus(StoredObjectStatus.ACTIVE);
        storage.setReferenceStatus(StoredObjectReferenceStatus.UNREFERENCED);
        return storage;
    }

    private MultipartUploadSession toMultipartSession(InitMultipartUploadCommand command) {
        if (command == null) {
            return null;
        }
        MultipartUploadSession session = new MultipartUploadSession();
        session.setUploadIdRef(command.getUploadId());
        session.setOwnerRef(command.getOwnerRef());
        session.setBusinessType(command.getBusinessType());
        session.setOriginalFilename(command.getOriginalFilename());
        session.setMimeTypeRef(command.getMimeType());
        session.setBucketNameRef(command.getBucketName());
        session.setObjectKeyRef(command.getObjectKey());
        session.setProviderUploadId(
                command.getProviderUploadId() == null
                        ? null
                        : command.getProviderUploadId().value());
        session.setTotalSizeRef(command.getTotalSize());
        session.setPartSizeRef(command.getPartSize());
        return session;
    }

    private CreateStorageCommand toCreateStorageCommand(StoredObject storage) {
        CreateStorageCommand command = new CreateStorageCommand();
        command.setId(storage.getId());
        command.setOriginalFilename(storage.getOriginalFilename());
        command.setContentType(storage.getContentType());
        command.setName(storage.getName());
        command.setExtendName(storage.getExtendName());
        command.setMimeType(storage.getMimeTypeRef());
        command.setBucketName(storage.getBucketNameRef());
        command.setObjectKey(storage.getObjectKeyRef());
        command.setSize(storage.getSizeRef());
        command.setAccessEndpoint(storage.getAccessEndpoint());
        command.setObjectStatus(storage.getObjectStatus());
        command.setReferenceStatus(storage.getReferenceStatus());
        command.setRemarks(storage.getRemarks());
        return command;
    }

    private MultipartUploadPart toMultipartPart(UploadMultipartPartCommand command) {
        if (command == null) {
            return null;
        }
        MultipartUploadPart part = new MultipartUploadPart();
        part.setUploadIdRef(command.getUploadId());
        part.setPartNumberRef(command.getPartNumber());
        part.setEtag(command.getEtag());
        part.setSizeRef(command.getSize());
        return part;
    }

    private String resolveMultipartPartObjectKey(MultipartUploadSession session, Integer partNumber) {
        return "multipart/" + session.getUploadId() + "/" + partNumber + ".part";
    }

    private String baseName(String originalFilename) {
        if (StringUtils.isBlank(originalFilename)) {
            return null;
        }
        int index = originalFilename.lastIndexOf(EXTENSION_SEPARATOR);
        return index < 0 ? originalFilename : originalFilename.substring(0, index);
    }

    private String extension(String originalFilename) {
        if (StringUtils.isBlank(originalFilename)) {
            return null;
        }
        int index = originalFilename.lastIndexOf(EXTENSION_SEPARATOR);
        return index < 0 ? null : StringUtils.lowerCase(originalFilename.substring(index + 1));
    }

    private String defaultObjectKey(MultipartUploadSession session) {
        String extension = extension(session.getOriginalFilename());
        return "multipart/"
                + session.getUploadId()
                + "/"
                + UuidHelper.compact()
                + (StringUtils.isBlank(extension) ? "" : EXTENSION_SEPARATOR + extension);
    }
}
