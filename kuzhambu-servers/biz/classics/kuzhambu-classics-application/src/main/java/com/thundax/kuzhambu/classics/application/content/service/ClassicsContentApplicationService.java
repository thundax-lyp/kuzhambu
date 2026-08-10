package com.thundax.kuzhambu.classics.application.content.service;

import com.thundax.kuzhambu.classics.application.content.command.AiCandidateApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchRejectContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairSortCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagSortCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentVersionCommand;
import com.thundax.kuzhambu.classics.application.content.query.ContentExportJobQuery;
import com.thundax.kuzhambu.classics.application.content.query.ContentObjectQuery;
import com.thundax.kuzhambu.classics.application.content.result.AiCandidateApplyContentResult;
import com.thundax.kuzhambu.classics.application.content.result.ClassicsExportJobResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface ClassicsContentApplicationService {

    List<ClassicsContentTag> listTags(ContentObjectQuery query);

    ClassicsContentTagId addTag(ContentTagCommand command);

    ClassicsContentTagId updateTag(ContentTagCommand command);

    void sortTags(ContentTagSortCommand command);

    void deleteTag(ClassicsContentTagId id);

    List<ClassicsContentQaPair> listQaPairs(ContentObjectQuery query);

    ClassicsContentQaPairId addQaPair(ContentQaPairCommand command);

    ClassicsContentQaPairId updateQaPair(ContentQaPairCommand command);

    void sortQaPairs(ContentQaPairSortCommand command);

    void deleteQaPair(ClassicsContentQaPairId id);

    List<ClassicsContentVersion> listVersions(ContentObjectQuery query);

    ClassicsContentVersion getVersion(ClassicsContentVersionId id);

    int deleteVersions(ContentObjectQuery query);

    ClassicsContentVersion ensureVersioned(ContentVersionCommand command);

    AiCandidateApplyContentResult applyAiCandidate(AiCandidateApplyContentCommand command);

    ClassicsBatchOperationResult applyAiCandidates(AiCandidateBatchApplyContentCommand command);

    ClassicsBatchOperationResult rejectAiCandidates(AiCandidateBatchRejectContentCommand command);

    ClassicsContentVersion restoreHistoryVersion(ClassicsContentVersionId versionId);

    ClassicsExportJobResult createExportJob(ContentExportCommand command);

    ClassicsContentExportJob getExportJob(ClassicsContentExportJobId id);

    ClassicsStoredContentResult getExportJobContent(ClassicsContentExportJobId id);

    void deleteExportJob(ClassicsContentExportJobId id);

    PageResult<ClassicsContentExportJob> pageExportJobs(ContentExportJobQuery query, PageQuery page);
}
