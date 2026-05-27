package com.thundax.kuzhambu.classics.application.content.service;

import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface ClassicsContentApplicationService {

    List<ClassicsContentTag> listTags(String contentType, Long contentId);

    Long saveTag(ContentTagCommand command);

    void deleteTag(Long id);

    List<ClassicsContentQaPair> listQaPairs(String contentType, Long contentId);

    Long saveQaPair(ContentQaPairCommand command);

    void deleteQaPair(Long id);

    List<ClassicsContentVersion> listVersions(String contentType, Long contentId);

    ClassicsContentVersion getVersion(Long id);

    Long createExportJob(ContentExportCommand command);

    PageResult<ClassicsContentExportJob> pageExportJobs(String contentType, String exportKind, String status, PageQuery page);
}
