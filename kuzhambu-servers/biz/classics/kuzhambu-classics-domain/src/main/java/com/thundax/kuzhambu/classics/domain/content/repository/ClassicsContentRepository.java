package com.thundax.kuzhambu.classics.domain.content.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public interface ClassicsContentRepository {

    List<ClassicsContentTag> listTags(String contentType, Long contentId, SortDirection sortDirection);

    Long insertTag(ClassicsContentTag tag);

    int updateTag(ClassicsContentTag tag);

    int deleteTagById(Long id);

    List<ClassicsContentQaPair> listQaPairs(String contentType, Long contentId, SortDirection sortDirection);

    Long insertQaPair(ClassicsContentQaPair qaPair);

    int updateQaPair(ClassicsContentQaPair qaPair);

    int deleteQaPairById(Long id);

    List<ClassicsContentVersion> listVersions(String contentType, Long contentId);

    Long insertVersion(ClassicsContentVersion version);

    ClassicsContentVersion getVersionById(Long id);

    Long insertExportJob(ClassicsContentExportJob exportJob);

    int updateExportJob(ClassicsContentExportJob exportJob);

    Page<ClassicsContentExportJob> pageExportJobs(String contentType, String exportKind, String status, int pageNo, int pageSize);
}
