package com.thundax.kuzhambu.classics.application.sancai.service;

import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntrySaveCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryStatusCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface SancaiApplicationService {

    List<SancaiCategory> listCategories();

    List<SancaiVolume> listVolumes(SancaiCategoryId categoryId);

    SancaiEntry getEntry(SancaiEntryId id);

    PageResult<SancaiEntry> pageEntries(SancaiEntryPageQuery query, PageQuery page);

    SancaiEntryId saveEntry(SancaiEntrySaveCommand command);

    void changeEntryStatus(SancaiEntryStatusCommand command);

    void changeEntryVisibility(SancaiEntryId id, String visibility);

    void deleteEntry(SancaiEntryId id);
}
