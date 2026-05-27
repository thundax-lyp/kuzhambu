package com.thundax.kuzhambu.classics.domain.mingcustoms.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public interface MingCustomsRepository {

    MingCustomsEntry getById(Long id);

    Page<MingCustomsEntry> page(
            String category,
            String keyword,
            String tagName,
            String visibility,
            SortDirection sortDirection,
            int pageNo,
            int pageSize);

    Long insert(MingCustomsEntry entry);

    int update(MingCustomsEntry entry);

    int updateVisibility(Long id, String visibility);

    int deleteById(Long id);

    List<MingCustomsKeyword> listKeywordsByCustomId(Long customId, SortDirection sortDirection);

    Long insertKeyword(MingCustomsKeyword keyword);

    int deleteKeywordById(Long id);

    List<String> listKeywordCloud(String visibility);
}
