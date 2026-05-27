package com.thundax.kuzhambu.classics.domain.sharing.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareAccessRecord;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public interface ClassicsSharingRepository {

    ClassicsShareLink getLinkById(Long id);

    ClassicsShareLink getLinkByTokenHash(String tokenHash);

    Page<ClassicsShareLink> pageLinks(String status, String visibility, int pageNo, int pageSize);

    Long insertLink(ClassicsShareLink link);

    int updateLink(ClassicsShareLink link);

    int updateLinkStatus(Long id, String status);

    int increaseAccessCount(Long id);

    List<ClassicsShareTarget> listTargetsByLinkId(Long shareLinkId, SortDirection sortDirection);

    Long insertTarget(ClassicsShareTarget target);

    int updateTargetStatus(Long id, String targetStatus);

    Long insertAccessRecord(ClassicsShareAccessRecord record);

    Page<ClassicsShareAccessRecord> pageAccessRecords(Long shareLinkId, Long shareTargetId, int pageNo, int pageSize);
}
