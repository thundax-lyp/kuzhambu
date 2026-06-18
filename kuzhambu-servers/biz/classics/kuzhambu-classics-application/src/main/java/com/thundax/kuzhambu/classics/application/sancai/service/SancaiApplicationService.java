package com.thundax.kuzhambu.classics.application.sancai.service;

import com.thundax.kuzhambu.classics.application.sancai.command.SancaiCategoryCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiCategorySortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntrySortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryStatusCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVolumeCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVolumeSortCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface SancaiApplicationService {

    List<SancaiCategory> listCategories();

    SancaiCategory getCategory(SancaiCategoryId id);

    SancaiCategoryId addCategory(SancaiCategoryCommand command);

    SancaiCategoryId updateCategory(SancaiCategoryCommand command);

    void deleteCategory(SancaiCategoryId id);

    List<SancaiVolume> listVolumes(SancaiCategoryId categoryId);

    SancaiVolume getVolume(SancaiVolumeId id);

    SancaiVolumeId addVolume(SancaiVolumeCommand command);

    SancaiVolumeId updateVolume(SancaiVolumeCommand command);

    void deleteVolume(SancaiVolumeId id);

    void sortCategories(SancaiCategorySortCommand command);

    void sortVolumes(SancaiVolumeSortCommand command);

    void sortEntries(SancaiEntrySortCommand command);

    SancaiEntry getEntry(SancaiEntryId id);

    PageResult<SancaiEntry> pageEntries(SancaiEntryPageQuery query, PageQuery page);

    SancaiEntryId addEntry(SancaiEntryCommand command);

    SancaiEntryId updateEntry(SancaiEntryCommand command);

    void changeEntryStatus(SancaiEntryStatusCommand command);

    void changeEntryVisibility(SancaiEntryId id, String visibility);

    void deleteEntry(SancaiEntryId id);
}
