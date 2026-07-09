package com.thundax.kuzhambu.classics.domain.mingcustoms.repository;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordCloudItem;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsTagCloudItem;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;

public interface MingCustomsRepository {

    MingCustomsEntry getById(MingCustomsEntryId id);

    PageResult<MingCustomsEntry> page(
            String category,
            String keyword,
            String tagName,
            String visibility,
            SortDirection sortDirection,
            int pageNo,
            int pageSize);

    List<MingCustomsEntry> list(
            String category, String keyword, String tagName, String visibility, SortDirection sortDirection);

    MingCustomsEntryId insert(MingCustomsEntry entry);

    int update(MingCustomsEntry entry);

    int updateVisibility(MingCustomsEntryId id, String visibility);

    int deleteById(MingCustomsEntryId id);

    List<MingCustomsKeyword> listKeywordsByCustomId(MingCustomsEntryId customId, SortDirection sortDirection);

    List<MingCustomsKeyword> listKeywords(SortDirection sortDirection);

    int maxPriority();

    MingCustomsKeywordId insertKeyword(MingCustomsKeyword keyword);

    int updateKeywordPriority(MingCustomsKeyword keyword);

    int deleteKeywordById(MingCustomsKeywordId id);

    List<MingCustomsKeywordCloudItem> listKeywordCloud(String visibility);

    List<MingCustomsTagCloudItem> listTagCloud(String category, String keyword, String visibility);
}
