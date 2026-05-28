package com.thundax.kuzhambu.classics.domain.content.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public interface ClassicsContentRepository {

    List<ClassicsContentTag> listTags(String contentType, ClassicsContentId contentId, SortDirection sortDirection);

    ClassicsContentTagId insertTag(ClassicsContentTag tag);

    int updateTag(ClassicsContentTag tag);

    int deleteTagById(ClassicsContentTagId id);

    List<ClassicsContentQaPair> listQaPairs(
            String contentType, ClassicsContentId contentId, SortDirection sortDirection);

    ClassicsContentQaPairId insertQaPair(ClassicsContentQaPair qaPair);

    int updateQaPair(ClassicsContentQaPair qaPair);

    int deleteQaPairById(ClassicsContentQaPairId id);

    List<ClassicsContentVersion> listVersions(String contentType, ClassicsContentId contentId);

    ClassicsContentVersionId insertVersion(ClassicsContentVersion version);

    ClassicsContentVersion getVersionById(ClassicsContentVersionId id);

    ClassicsContentExportJobId insertExportJob(ClassicsContentExportJob exportJob);

    int updateExportJob(ClassicsContentExportJob exportJob);

    Page<ClassicsContentExportJob> pageExportJobs(
            String contentType, String exportKind, String status, int pageNo, int pageSize);
}
