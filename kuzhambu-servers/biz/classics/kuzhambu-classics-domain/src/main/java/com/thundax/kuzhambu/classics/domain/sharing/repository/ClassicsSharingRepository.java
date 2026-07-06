package com.thundax.kuzhambu.classics.domain.sharing.repository;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareAccessRecord;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsSharePortalListItem;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareAccessRecordId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareTargetId;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.Date;
import java.util.List;

public interface ClassicsSharingRepository {

    ClassicsShareLink getLinkById(ClassicsShareLinkId id);

    ClassicsShareLink getLinkByTokenHash(String tokenHash);

    PageResult<ClassicsShareLink> pageLinks(String status, String visibility, int pageNo, int pageSize);

    default List<ClassicsShareLinkId> listExpiredShareLinkIds(Date now, int limit) {
        return List.of();
    }

    PageResult<ClassicsSharePortalListItem> pagePortalShares(
            String contentType, String title, Date issuedAfter, Date issuedBefore, int pageNo, int pageSize);

    List<ClassicsSharePortalListItem> listTopPortalShares(String visibility, int limit);

    ClassicsShareLinkId insertLink(ClassicsShareLink link);

    int updateLink(ClassicsShareLink link);

    int updateLinkStatus(ClassicsShareLinkId id, String status);

    default int markShareLinkExpired(ClassicsShareLinkId id) {
        return updateLinkStatus(id, "EXPIRED");
    }

    int increaseAccessCount(ClassicsShareLinkId id);

    List<ClassicsShareTarget> listTargets(SortDirection sortDirection);

    List<ClassicsShareTarget> listTargetsByLinkId(ClassicsShareLinkId shareLinkId, SortDirection sortDirection);

    List<ClassicsShareTarget> listTargetsByContent(ClassicsContentType contentType, Long contentId);

    int markTargetsContentDeleted(ClassicsContentType contentType, Long contentId);

    int updateLinkVisibilityRiskStatus(ClassicsShareLinkId id, SancaiVisibilityRiskStatus visibilityRiskStatus);

    int maxTargetPriority();

    ClassicsShareTargetId insertTarget(ClassicsShareTarget target);

    int updateTargetPriority(ClassicsShareTarget target);

    int updateTargetStatus(ClassicsShareTargetId id, String targetStatus);

    ClassicsShareAccessRecordId insertAccessRecord(ClassicsShareAccessRecord record);

    PageResult<ClassicsShareAccessRecord> pageAccessRecords(
            ClassicsShareLinkId shareLinkId, ClassicsShareTargetId shareTargetId, int pageNo, int pageSize);
}
