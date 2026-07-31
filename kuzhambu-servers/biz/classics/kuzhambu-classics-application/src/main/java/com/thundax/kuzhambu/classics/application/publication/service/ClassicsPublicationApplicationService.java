package com.thundax.kuzhambu.classics.application.publication.service;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationCreateCommand;
import com.thundax.kuzhambu.classics.application.publication.query.ClassicsPublicationJobPageQuery;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationCreateResult;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationJobView;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;

public interface ClassicsPublicationApplicationService {
    ClassicsPublicationCreateResult create(ClassicsPublicationCreateCommand command);

    List<ClassicsPublicationCreateResult> createBatch(List<ClassicsPublicationCreateCommand> commands);

    ClassicsPublicationJobView get(ClassicsPublicationJobId id);

    PageResult<ClassicsPublicationJobView> page(ClassicsPublicationJobPageQuery query, PageQuery page);
}
