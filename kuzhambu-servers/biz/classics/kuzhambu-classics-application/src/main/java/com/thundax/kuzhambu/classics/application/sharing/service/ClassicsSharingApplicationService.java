package com.thundax.kuzhambu.classics.application.sharing.service;

import com.thundax.kuzhambu.classics.application.sharing.command.ClassicsShareTargetSortCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkStatusCommand;
import com.thundax.kuzhambu.classics.application.sharing.query.ShareAccessQuery;
import com.thundax.kuzhambu.classics.application.sharing.result.ShareLinkCreateResult;
import com.thundax.kuzhambu.classics.application.sharing.result.SharePortalResult;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareAccessRecord;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsSharePortalListItem;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.Date;
import java.util.List;

public interface ClassicsSharingApplicationService {

    ClassicsShareLink getLink(ClassicsShareLinkId id);

    ClassicsShareLink getLinkByTokenHash(String tokenHash);

    PageResult<ClassicsShareLink> pageLinks(String status, String visibility, PageQuery page);

    PageResult<ClassicsSharePortalListItem> pagePortalShares(
            String contentType, String title, Date issuedAfter, Date issuedBefore, PageQuery page);

    ShareLinkCreateResult createLink(ShareLinkCreateCommand command);

    SharePortalResult getPortalShare(String shareToken);

    void changeStatus(ShareLinkStatusCommand command);

    void sortTargets(ClassicsShareTargetSortCommand command);

    List<ClassicsShareTarget> listTargets(ClassicsShareLinkId shareLinkId);

    void recordAccess(ClassicsShareAccessRecord record);

    PageResult<ClassicsShareAccessRecord> pageAccessRecords(ShareAccessQuery query, PageQuery page);
}
