package com.thundax.kuzhambu.classics.infra.sharing.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.*;
import com.thundax.kuzhambu.classics.domain.sharing.repository.ClassicsSharingRepository;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.assembler.ClassicsSharingPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject.*;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.mapper.*;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class ClassicsSharingRepositoryImpl implements ClassicsSharingRepository {
    private final ClassicsShareLinkMapper linkMapper;
    private final ClassicsShareTargetMapper targetMapper;
    private final ClassicsSharingMapper accessMapper;

    public ClassicsSharingRepositoryImpl(ClassicsShareLinkMapper linkMapper, ClassicsShareTargetMapper targetMapper, ClassicsSharingMapper accessMapper) {
        this.linkMapper = linkMapper;
        this.targetMapper = targetMapper;
        this.accessMapper = accessMapper;
    }

    public ClassicsShareLink getLinkById(Long id) { return ClassicsSharingPersistenceAssembler.toLinkDomain(linkMapper.selectById(id)); }
    public ClassicsShareLink getLinkByTokenHash(String tokenHash) { return ClassicsSharingPersistenceAssembler.toLinkDomain(linkMapper.selectOne(new LambdaQueryWrapper<ClassicsShareLinkDO>().eq(ClassicsShareLinkDO::getTokenHash, tokenHash))); }
    public Page<ClassicsShareLink> pageLinks(String status, String visibility, int pageNo, int pageSize) {
        LambdaQueryWrapper<ClassicsShareLinkDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(status), ClassicsShareLinkDO::getStatus, status).eq(StringUtils.isNotBlank(visibility), ClassicsShareLinkDO::getVisibility, visibility).orderByDesc(ClassicsShareLinkDO::getIssuedAt);
        Page<ClassicsShareLinkDO> dataPage = linkMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Page<ClassicsShareLink> entityPage = new Page<>(dataPage.getCurrent(), dataPage.getSize());
        entityPage.setTotal(dataPage.getTotal());
        entityPage.setRecords(ClassicsSharingPersistenceAssembler.toLinkDomainList(dataPage.getRecords()));
        return entityPage;
    }
    public Long insertLink(ClassicsShareLink link) { ClassicsShareLinkDO dataObject = ClassicsSharingPersistenceAssembler.toLinkObject(link); linkMapper.insert(dataObject); return dataObject.getId(); }
    public int updateLink(ClassicsShareLink link) { return linkMapper.updateById(ClassicsSharingPersistenceAssembler.toLinkObject(link)); }
    public int updateLinkStatus(Long id, String status) { return linkMapper.update(null, new LambdaUpdateWrapper<ClassicsShareLinkDO>().eq(ClassicsShareLinkDO::getId, id).set(ClassicsShareLinkDO::getStatus, status)); }
    public int increaseAccessCount(Long id) { return linkMapper.update(null, new LambdaUpdateWrapper<ClassicsShareLinkDO>().eq(ClassicsShareLinkDO::getId, id).setSql("access_count = access_count + 1")); }
    public List<ClassicsShareTarget> listTargetsByLinkId(Long shareLinkId, SortDirection sortDirection) { return ClassicsSharingPersistenceAssembler.toTargetDomainList(targetMapper.selectList(new LambdaQueryWrapper<ClassicsShareTargetDO>().eq(ClassicsShareTargetDO::getShareLinkId, shareLinkId).orderBy(true, sortDirection != SortDirection.DESC, ClassicsShareTargetDO::getPriority))); }
    public Long insertTarget(ClassicsShareTarget target) { ClassicsShareTargetDO dataObject = ClassicsSharingPersistenceAssembler.toTargetObject(target); targetMapper.insert(dataObject); return dataObject.getId(); }
    public int updateTargetStatus(Long id, String targetStatus) { return targetMapper.update(null, new LambdaUpdateWrapper<ClassicsShareTargetDO>().eq(ClassicsShareTargetDO::getId, id).set(ClassicsShareTargetDO::getTargetStatus, targetStatus)); }
    public Long insertAccessRecord(ClassicsShareAccessRecord record) { ClassicsShareAccessRecordDO dataObject = ClassicsSharingPersistenceAssembler.toAccessObject(record); accessMapper.insert(dataObject); return dataObject.getId(); }
    public Page<ClassicsShareAccessRecord> pageAccessRecords(Long shareLinkId, Long shareTargetId, int pageNo, int pageSize) {
        LambdaQueryWrapper<ClassicsShareAccessRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(shareLinkId != null, ClassicsShareAccessRecordDO::getShareLinkId, shareLinkId).eq(shareTargetId != null, ClassicsShareAccessRecordDO::getShareTargetId, shareTargetId).orderByDesc(ClassicsShareAccessRecordDO::getAccessedAt);
        Page<ClassicsShareAccessRecordDO> dataPage = accessMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Page<ClassicsShareAccessRecord> entityPage = new Page<>(dataPage.getCurrent(), dataPage.getSize());
        entityPage.setTotal(dataPage.getTotal());
        entityPage.setRecords(ClassicsSharingPersistenceAssembler.toAccessDomainList(dataPage.getRecords()));
        return entityPage;
    }
}
