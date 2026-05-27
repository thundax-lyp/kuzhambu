package com.thundax.kuzhambu.classics.domain.wangqi.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public interface WangqiDocumentRepository {

    WangqiDocument getById(Long id);

    Page<WangqiDocument> page(String keyword, String visibility, SortDirection sortDirection, int pageNo, int pageSize);

    List<WangqiDocument> listTimeline(String visibility, SortDirection sortDirection);

    Long insert(WangqiDocument document);

    int update(WangqiDocument document);

    int updateStorageObjectId(Long id, Long storageObjectId);

    int updateVisibility(Long id, String visibility);

    int deleteById(Long id);
}
