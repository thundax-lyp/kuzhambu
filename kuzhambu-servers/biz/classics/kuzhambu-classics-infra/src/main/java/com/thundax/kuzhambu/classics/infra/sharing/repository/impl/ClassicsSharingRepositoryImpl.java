package com.thundax.kuzhambu.classics.infra.sharing.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareAccessRecordIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareLinkIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareTargetIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareAccessRecord;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareAccessRecordId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareTargetId;
import com.thundax.kuzhambu.classics.domain.sharing.repository.ClassicsSharingRepository;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.assembler.ClassicsSharingPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject.ClassicsShareAccessRecordDO;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject.ClassicsShareLinkDO;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject.ClassicsShareTargetDO;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.mapper.ClassicsShareLinkMapper;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.mapper.ClassicsShareTargetMapper;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.mapper.ClassicsSharingMapper;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class ClassicsSharingRepositoryImpl implements ClassicsSharingRepository {
    private final ClassicsShareLinkMapper linkMapper;
    private final ClassicsShareTargetMapper targetMapper;
    private final ClassicsSharingMapper accessMapper;

    public ClassicsSharingRepositoryImpl(
            ClassicsShareLinkMapper linkMapper,
            ClassicsShareTargetMapper targetMapper,
            ClassicsSharingMapper accessMapper) {
        this.linkMapper = linkMapper;
        this.targetMapper = targetMapper;
        this.accessMapper = accessMapper;
    }

    public ClassicsShareLink getLinkById(ClassicsShareLinkId id) {
        return ClassicsSharingPersistenceAssembler.toLinkDomain(
                linkMapper.selectById(ClassicsShareLinkIdCodec.toValue(id)));
    }

    public ClassicsShareLink getLinkByTokenHash(String tokenHash) {
        return ClassicsSharingPersistenceAssembler.toLinkDomain(linkMapper.selectOne(
                new LambdaQueryWrapper<ClassicsShareLinkDO>().eq(ClassicsShareLinkDO::getTokenHash, tokenHash)));
    }

    public Page<ClassicsShareLink> pageLinks(String status, String visibility, int pageNo, int pageSize) {
        LambdaQueryWrapper<ClassicsShareLinkDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(status), ClassicsShareLinkDO::getStatus, status)
                .eq(StringUtils.isNotBlank(visibility), ClassicsShareLinkDO::getVisibility, visibility)
                .orderByDesc(ClassicsShareLinkDO::getIssuedAt);
        Page<ClassicsShareLinkDO> dataPage = linkMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Page<ClassicsShareLink> entityPage = new Page<>(dataPage.getCurrent(), dataPage.getSize());
        entityPage.setTotal(dataPage.getTotal());
        entityPage.setRecords(ClassicsSharingPersistenceAssembler.toLinkDomainList(dataPage.getRecords()));
        return entityPage;
    }

    public ClassicsShareLinkId insertLink(ClassicsShareLink link) {
        ClassicsShareLinkDO dataObject = ClassicsSharingPersistenceAssembler.toLinkObject(link);
        linkMapper.insert(dataObject);
        return ClassicsShareLinkIdCodec.toDomain(dataObject.getId());
    }

    public int updateLink(ClassicsShareLink link) {
        return linkMapper.updateById(ClassicsSharingPersistenceAssembler.toLinkObject(link));
    }

    public int updateLinkStatus(ClassicsShareLinkId id, String status) {
        return linkMapper.update(
                null,
                new LambdaUpdateWrapper<ClassicsShareLinkDO>()
                        .eq(ClassicsShareLinkDO::getId, ClassicsShareLinkIdCodec.toValue(id))
                        .set(ClassicsShareLinkDO::getStatus, status));
    }

    public int increaseAccessCount(ClassicsShareLinkId id) {
        return linkMapper.update(
                null,
                new LambdaUpdateWrapper<ClassicsShareLinkDO>()
                        .eq(ClassicsShareLinkDO::getId, ClassicsShareLinkIdCodec.toValue(id))
                        .setSql("access_count = access_count + 1"));
    }

    @Override
    public List<ClassicsShareTarget> listTargets(SortDirection sortDirection) {
        return ClassicsSharingPersistenceAssembler.toTargetDomainList(
                targetMapper.selectList(new LambdaQueryWrapper<ClassicsShareTargetDO>()
                        .orderBy(true, sortDirection != SortDirection.DESC, ClassicsShareTargetDO::getPriority)));
    }

    public List<ClassicsShareTarget> listTargetsByLinkId(ClassicsShareLinkId shareLinkId, SortDirection sortDirection) {
        return ClassicsSharingPersistenceAssembler.toTargetDomainList(
                targetMapper.selectList(new LambdaQueryWrapper<ClassicsShareTargetDO>()
                        .eq(shareLinkId != null, ClassicsShareTargetDO::getShareLinkId, ClassicsShareLinkIdCodec.toValue(shareLinkId))
                        .orderBy(true, sortDirection != SortDirection.DESC, ClassicsShareTargetDO::getPriority)));
    }

    @Override
    public int maxTargetPriority() {
        return maxPriority(targetMapper.selectObjs(new QueryWrapper<ClassicsShareTargetDO>().select("max(priority)")));
    }

    public ClassicsShareTargetId insertTarget(ClassicsShareTarget target) {
        ClassicsShareTargetDO dataObject = ClassicsSharingPersistenceAssembler.toTargetObject(target);
        targetMapper.insert(dataObject);
        return ClassicsShareTargetIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateTargetPriority(ClassicsShareTarget target) {
        ClassicsShareTargetDO dataObject = ClassicsSharingPersistenceAssembler.toTargetObject(target);
        return targetMapper.update(
                null,
                new LambdaUpdateWrapper<ClassicsShareTargetDO>()
                        .eq(ClassicsShareTargetDO::getId, dataObject.getId())
                        .set(ClassicsShareTargetDO::getPriority, dataObject.getPriority()));
    }

    public int updateTargetStatus(ClassicsShareTargetId id, String targetStatus) {
        return targetMapper.update(
                null,
                new LambdaUpdateWrapper<ClassicsShareTargetDO>()
                        .eq(ClassicsShareTargetDO::getId, ClassicsShareTargetIdCodec.toValue(id))
                        .set(ClassicsShareTargetDO::getTargetStatus, targetStatus));
    }

    public ClassicsShareAccessRecordId insertAccessRecord(ClassicsShareAccessRecord record) {
        ClassicsShareAccessRecordDO dataObject = ClassicsSharingPersistenceAssembler.toAccessObject(record);
        accessMapper.insert(dataObject);
        return ClassicsShareAccessRecordIdCodec.toDomain(dataObject.getId());
    }

    public Page<ClassicsShareAccessRecord> pageAccessRecords(
            ClassicsShareLinkId shareLinkId, ClassicsShareTargetId shareTargetId, int pageNo, int pageSize) {
        LambdaQueryWrapper<ClassicsShareAccessRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(
                        shareLinkId != null,
                        ClassicsShareAccessRecordDO::getShareLinkId,
                        ClassicsShareLinkIdCodec.toValue(shareLinkId))
                .eq(
                        shareTargetId != null,
                        ClassicsShareAccessRecordDO::getShareTargetId,
                        ClassicsShareTargetIdCodec.toValue(shareTargetId))
                .orderByDesc(ClassicsShareAccessRecordDO::getAccessedAt);
        Page<ClassicsShareAccessRecordDO> dataPage = accessMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Page<ClassicsShareAccessRecord> entityPage = new Page<>(dataPage.getCurrent(), dataPage.getSize());
        entityPage.setTotal(dataPage.getTotal());
        entityPage.setRecords(ClassicsSharingPersistenceAssembler.toAccessDomainList(dataPage.getRecords()));
        return entityPage;
    }

    private static int maxPriority(List<Object> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        Object max = values.stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (max == null) {
            return 0;
        }
        if (max instanceof Number) {
            return ((Number) max).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(max));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
