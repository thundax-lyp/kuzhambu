package com.thundax.kuzhambu.classics.application.sharing.service;

import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkStatusCommand;
import com.thundax.kuzhambu.classics.application.sharing.query.ShareAccessQuery;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareAccessRecord;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface ClassicsSharingApplicationService {

    ClassicsShareLink getLink(ClassicsShareLinkId id);

    ClassicsShareLink getLinkByTokenHash(String tokenHash);

    PageResult<ClassicsShareLink> pageLinks(String status, String visibility, PageQuery page);

    ClassicsShareLinkId createLink(ShareLinkCreateCommand command);

    void changeStatus(ShareLinkStatusCommand command);

    List<ClassicsShareTarget> listTargets(ClassicsShareLinkId shareLinkId);

    void recordAccess(ClassicsShareAccessRecord record);

    PageResult<ClassicsShareAccessRecord> pageAccessRecords(ShareAccessQuery query, PageQuery page);
}
