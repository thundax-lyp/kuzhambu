package com.thundax.kuzhambu.storage.infra.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.storage.application.dao.MultipartUploadDao;
import com.thundax.kuzhambu.storage.domain.model.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.domain.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.model.valueobject.MultipartUploadPartId;
import com.thundax.kuzhambu.storage.domain.model.valueobject.MultipartUploadSessionId;
import com.thundax.kuzhambu.storage.domain.object.codec.MultipartUploadPartIdCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.MultipartUploadSessionIdCodec;
import com.thundax.kuzhambu.storage.infra.assembler.StoragePersistenceAssembler;
import com.thundax.kuzhambu.storage.infra.dataobject.MultipartUploadPartDO;
import com.thundax.kuzhambu.storage.infra.dataobject.MultipartUploadSessionDO;
import com.thundax.kuzhambu.storage.infra.mapper.MultipartUploadPartMapper;
import com.thundax.kuzhambu.storage.infra.mapper.MultipartUploadSessionMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MultipartUploadDaoImpl implements MultipartUploadDao {

    private final MultipartUploadSessionMapper sessionMapper;
    private final MultipartUploadPartMapper partMapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public MultipartUploadDaoImpl(MultipartUploadSessionMapper sessionMapper, MultipartUploadPartMapper partMapper) {
        this.sessionMapper = sessionMapper;
        this.partMapper = partMapper;
    }

    @Override
    public MultipartUploadSessionId insertMultipartSession(MultipartUploadSession session) {
        MultipartUploadSessionDO dataObject = StoragePersistenceAssembler.toMultipartSessionDataObject(session);
        dataObject.setId(idGenerator.nextId().value());
        sessionMapper.insert(dataObject);
        return MultipartUploadSessionIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public MultipartUploadSession getMultipartSessionByUploadId(String uploadId) {
        LambdaQueryWrapper<MultipartUploadSessionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MultipartUploadSessionDO::getUploadId, uploadId);
        return StoragePersistenceAssembler.toMultipartSessionEntity(sessionMapper.selectOne(wrapper));
    }

    @Override
    public int updateMultipartSession(MultipartUploadSession session) {
        MultipartUploadSessionDO dataObject = StoragePersistenceAssembler.toMultipartSessionDataObject(session);
        return sessionMapper.update(
                null,
                new LambdaUpdateWrapper<MultipartUploadSessionDO>()
                        .eq(MultipartUploadSessionDO::getUploadId, dataObject.getUploadId())
                        .set(MultipartUploadSessionDO::getOwnerId, dataObject.getOwnerId())
                        .set(MultipartUploadSessionDO::getOwnerType, dataObject.getOwnerType())
                        .set(MultipartUploadSessionDO::getBusinessType, dataObject.getBusinessType())
                        .set(MultipartUploadSessionDO::getOriginalFilename, dataObject.getOriginalFilename())
                        .set(MultipartUploadSessionDO::getMimeType, dataObject.getMimeType())
                        .set(MultipartUploadSessionDO::getBucketName, dataObject.getBucketName())
                        .set(MultipartUploadSessionDO::getObjectKey, dataObject.getObjectKey())
                        .set(MultipartUploadSessionDO::getProviderUploadId, dataObject.getProviderUploadId())
                        .set(MultipartUploadSessionDO::getTotalSize, dataObject.getTotalSize())
                        .set(MultipartUploadSessionDO::getPartSize, dataObject.getPartSize())
                        .set(MultipartUploadSessionDO::getUploadedPartCount, dataObject.getUploadedPartCount())
                        .set(MultipartUploadSessionDO::getUploadStatus, dataObject.getUploadStatus())
                        .set(MultipartUploadSessionDO::getCompletedDate, dataObject.getCompletedDate())
                        .set(MultipartUploadSessionDO::getAbortedDate, dataObject.getAbortedDate()));
    }

    @Override
    public MultipartUploadPartId insertMultipartPart(MultipartUploadPart part) {
        MultipartUploadPartDO dataObject = StoragePersistenceAssembler.toMultipartPartDataObject(part);
        dataObject.setId(idGenerator.nextId().value());
        partMapper.insert(dataObject);
        return MultipartUploadPartIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public MultipartUploadPart getMultipartPart(String uploadId, Integer partNumber) {
        LambdaQueryWrapper<MultipartUploadPartDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MultipartUploadPartDO::getUploadId, uploadId);
        wrapper.eq(MultipartUploadPartDO::getPartNumber, partNumber);
        return StoragePersistenceAssembler.toMultipartPartEntity(partMapper.selectOne(wrapper));
    }

    @Override
    public List<MultipartUploadPart> listMultipartParts(String uploadId) {
        LambdaQueryWrapper<MultipartUploadPartDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MultipartUploadPartDO::getUploadId, uploadId);
        wrapper.orderByAsc(MultipartUploadPartDO::getPartNumber);
        List<MultipartUploadPartDO> dataObjects = partMapper.selectList(wrapper);
        List<MultipartUploadPart> parts = new ArrayList<>();
        for (MultipartUploadPartDO dataObject : dataObjects) {
            parts.add(StoragePersistenceAssembler.toMultipartPartEntity(dataObject));
        }
        return parts;
    }

    @Override
    public int countMultipartParts(String uploadId) {
        LambdaQueryWrapper<MultipartUploadPartDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MultipartUploadPartDO::getUploadId, uploadId);
        return partMapper.selectCount(wrapper).intValue();
    }
}
