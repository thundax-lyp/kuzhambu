package com.thundax.kuzhambu.storage.application.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.id.UuidHelper;
import com.thundax.kuzhambu.storage.application.service.MultipartUploadApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.AbortMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.service.command.CompleteMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.service.command.CreateStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.InitMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.service.command.UploadMultipartPartCommand;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.repository.MultipartUploadRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Date;
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
        Date now = new Date();
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
        MultipartUploadSession session = requireActiveMultipartSession(part.getUploadId());
        if (multipartUploadRepository.getMultipartPart(part.getUploadId(), part.getPartNumber()) != null) {
            throw new BizException("Multipart upload part already exists: " + part.getPartNumber());
        }
        part.setPartPath(resolveMultipartPartObjectKey(session, part.getPartNumber()));
        persistMultipartPartContent(part, command.getInputStream());
        part.setId(multipartUploadRepository.insertMultipartPart(part));

        session.setUploadStatus(MultipartUploadStatus.UPLOADING);
        session.setUploadedPartCount(multipartUploadRepository.countMultipartParts(session.getUploadId()));
        multipartUploadRepository.updateMultipartSession(session);
        return part;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoredObject complete(CompleteMultipartUploadCommand command) {
        String uploadId = command == null ? null : command.getUploadId();
        MultipartUploadSession session = requireActiveMultipartSession(uploadId);
        List<MultipartUploadPart> parts = multipartUploadRepository.listMultipartParts(uploadId);
        validateMultipartParts(session, parts);

        StoredObject storage = toCompletedStorage(session, command);
        persistCompletedMultipartStorage(session, parts, storage);
        storage.setId(storageApplicationService.create(toCreateStorageCommand(storage)));

        Date now = new Date();
        session.setUploadStatus(MultipartUploadStatus.COMPLETED);
        session.setUploadedPartCount(parts.size());
        session.setCompletedDate(now);
        multipartUploadRepository.updateMultipartSession(session);
        return storage;
    }

    private void persistMultipartPartContent(MultipartUploadPart part, InputStream inputStream) {
        try (InputStream stream = inputStream) {
            StoredObject partStorage = new StoredObject();
            partStorage.setObjectKey(part.getPartPath());
            storedObjectContentRepository.save(partStorage, stream);
        } catch (IOException exception) {
            throw new BizException("Multipart upload part save failed: " + exception.getMessage());
        }
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
        MultipartUploadSession session = requireActiveMultipartSession(command == null ? null : command.getUploadId());
        Date now = new Date();
        session.setUploadStatus(MultipartUploadStatus.ABORTED);
        session.setAbortedDate(now);
        return multipartUploadRepository.updateMultipartSession(session);
    }

    private MultipartUploadSession requireActiveMultipartSession(String uploadId) {
        if (StringUtils.isBlank(uploadId)) {
            throw new BizException("Multipart upload id can not be empty");
        }
        MultipartUploadSession session = multipartUploadRepository.getMultipartSessionByUploadId(uploadId);
        if (session == null) {
            throw new BizException("Multipart upload session not found: " + uploadId);
        }
        if (MultipartUploadStatus.COMPLETED == session.getUploadStatus()
                || MultipartUploadStatus.ABORTED == session.getUploadStatus()) {
            throw new BizException("Multipart upload session is closed: " + uploadId);
        }
        return session;
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

    private StoredObject toCompletedStorage(MultipartUploadSession session, CompleteMultipartUploadCommand command) {
        StoredObject storage = new StoredObject();
        storage.setName(baseName(session.getOriginalFilename()));
        storage.setExtendName(extension(session.getOriginalFilename()));
        storage.setMimeType(session.getMimeType());
        storage.setOwnerId(session.getOwnerId());
        storage.setOwnerType(session.getOwnerType());
        storage.setBucketName(
                command == null || command.getBucketName() == null ? session.getBucketName() : command.getBucketName());
        storage.setObjectKey(
                command == null || command.getObjectKey() == null ? session.getObjectKey() : command.getObjectKey());
        storage.setSize(command == null || command.getSize() == null ? session.getTotalSize() : command.getSize());
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
        session.setUploadId(command.getUploadId());
        session.setOwnerId(command.getOwnerId());
        session.setOwnerType(command.getOwnerType());
        session.setBusinessType(command.getBusinessType());
        session.setOriginalFilename(command.getOriginalFilename());
        session.setMimeType(command.getMimeType());
        session.setBucketName(command.getBucketName());
        session.setObjectKey(command.getObjectKey());
        session.setProviderUploadId(command.getProviderUploadId());
        session.setTotalSize(command.getTotalSize());
        session.setPartSize(command.getPartSize());
        return session;
    }

    private CreateStorageCommand toCreateStorageCommand(StoredObject storage) {
        CreateStorageCommand command = new CreateStorageCommand();
        command.setId(storage.getId());
        command.setOriginalFilename(storage.getOriginalFilename());
        command.setContentType(storage.getContentType());
        command.setName(storage.getName());
        command.setExtendName(storage.getExtendName());
        command.setMimeType(storage.getMimeType());
        command.setOwnerId(storage.getOwnerId());
        command.setOwnerType(storage.getOwnerType());
        command.setBucketName(storage.getBucketName());
        command.setObjectKey(storage.getObjectKey());
        command.setSize(storage.getSize());
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
        part.setUploadId(command.getUploadId());
        part.setPartNumber(command.getPartNumber());
        part.setEtag(command.getEtag());
        part.setSize(command.getSize());
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
