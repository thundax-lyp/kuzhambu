package com.thundax.kuzhambu.classics.domain.sharing.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareAccessRecord;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareAccessRecordId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareTargetId;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public interface ClassicsSharingRepository {

    ClassicsShareLink getLinkById(ClassicsShareLinkId id);

    ClassicsShareLink getLinkByTokenHash(String tokenHash);

    Page<ClassicsShareLink> pageLinks(String status, String visibility, int pageNo, int pageSize);

    ClassicsShareLinkId insertLink(ClassicsShareLink link);

    int updateLink(ClassicsShareLink link);

    int updateLinkStatus(ClassicsShareLinkId id, String status);

    int increaseAccessCount(ClassicsShareLinkId id);

    List<ClassicsShareTarget> listTargets(SortDirection sortDirection);

    List<ClassicsShareTarget> listTargetsByLinkId(ClassicsShareLinkId shareLinkId, SortDirection sortDirection);

    int maxTargetPriority();

    ClassicsShareTargetId insertTarget(ClassicsShareTarget target);

    int updateTargetPriority(ClassicsShareTarget target);

    int updateTargetStatus(ClassicsShareTargetId id, String targetStatus);

    ClassicsShareAccessRecordId insertAccessRecord(ClassicsShareAccessRecord record);

    Page<ClassicsShareAccessRecord> pageAccessRecords(
            ClassicsShareLinkId shareLinkId, ClassicsShareTargetId shareTargetId, int pageNo, int pageSize);
}
