package com.thundax.kuzhambu.operations.domain.cleanup.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupItem;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupJob;
import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupItemId;
import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupJobId;
import java.util.List;

public interface CleanupJobRepository {

    CleanupJob getById(CleanupJobId id);

    CleanupJobId insert(CleanupJob job);

    int update(CleanupJob job);

    int deleteById(CleanupJobId id);

    List<CleanupItem> listItemsByJobId(CleanupJobId jobId);

    PageResult<CleanupJob> page(
            String cleanupType, String cleanupStatus, Long requesterUserId, int pageNo, int pageSize);

    CleanupItemId insertItem(CleanupItem item);

    int updateItem(CleanupItem item);

    int deleteByJobId(CleanupJobId jobId);
}
