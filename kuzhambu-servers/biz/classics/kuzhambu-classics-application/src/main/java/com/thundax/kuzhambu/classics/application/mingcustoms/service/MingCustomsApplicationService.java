package com.thundax.kuzhambu.classics.application.mingcustoms.service;

import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsSaveCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface MingCustomsApplicationService {

    MingCustomsEntry get(Long id);

    PageResult<MingCustomsEntry> page(MingCustomsPageQuery query, PageQuery page);

    Long save(MingCustomsSaveCommand command);

    void changeVisibility(Long id, String visibility);

    void delete(Long id);

    List<MingCustomsKeyword> listKeywords(Long customId);

    Long addKeyword(MingCustomsKeywordCommand command);

    void deleteKeyword(Long id);

    List<String> listKeywordCloud(String visibility);
}
