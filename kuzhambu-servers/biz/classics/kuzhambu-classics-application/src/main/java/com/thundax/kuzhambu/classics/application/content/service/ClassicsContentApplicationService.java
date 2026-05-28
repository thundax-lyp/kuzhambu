package com.thundax.kuzhambu.classics.application.content.service;

import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairSortCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagSortCommand;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface ClassicsContentApplicationService {

    List<ClassicsContentTag> listTags(String contentType, ClassicsContentId contentId);

    ClassicsContentTagId saveTag(ContentTagCommand command);

    void sortTags(ContentTagSortCommand command);

    void deleteTag(ClassicsContentTagId id);

    List<ClassicsContentQaPair> listQaPairs(String contentType, ClassicsContentId contentId);

    ClassicsContentQaPairId saveQaPair(ContentQaPairCommand command);

    void sortQaPairs(ContentQaPairSortCommand command);

    void deleteQaPair(ClassicsContentQaPairId id);

    List<ClassicsContentVersion> listVersions(String contentType, ClassicsContentId contentId);

    ClassicsContentVersion getVersion(ClassicsContentVersionId id);

    ClassicsContentExportJobId createExportJob(ContentExportCommand command);

    PageResult<ClassicsContentExportJob> pageExportJobs(
            String contentType, String exportKind, String status, PageQuery page);
}
