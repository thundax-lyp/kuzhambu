package com.thundax.kuzhambu.classics.application.mingcustoms.service;

import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsSaveCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordId;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface MingCustomsApplicationService {

    MingCustomsEntry get(MingCustomsEntryId id);

    PageResult<MingCustomsEntry> page(MingCustomsPageQuery query, PageQuery page);

    MingCustomsEntryId save(MingCustomsSaveCommand command);

    void changeVisibility(MingCustomsEntryId id, String visibility);

    void delete(MingCustomsEntryId id);

    List<MingCustomsKeyword> listKeywords(MingCustomsEntryId customId);

    MingCustomsKeywordId addKeyword(MingCustomsKeywordCommand command);

    void deleteKeyword(MingCustomsKeywordId id);

    List<String> listKeywordCloud(String visibility);
}
