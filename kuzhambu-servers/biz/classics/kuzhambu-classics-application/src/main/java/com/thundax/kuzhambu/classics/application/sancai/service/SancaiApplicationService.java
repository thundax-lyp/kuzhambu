package com.thundax.kuzhambu.classics.application.sancai.service;

import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntrySaveCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryStatusCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface SancaiApplicationService {

    List<SancaiCategory> listCategories();

    List<SancaiVolume> listVolumes(Long categoryId);

    SancaiEntry getEntry(Long id);

    PageResult<SancaiEntry> pageEntries(SancaiEntryPageQuery query, PageQuery page);

    Long saveEntry(SancaiEntrySaveCommand command);

    void changeEntryStatus(SancaiEntryStatusCommand command);

    void changeEntryVisibility(Long id, String visibility);

    void deleteEntry(Long id);
}
