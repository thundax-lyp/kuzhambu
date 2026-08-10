package com.thundax.kuzhambu.classics.application.publication.service.impl;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationBatchCreateCommand;
import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationCreateCommand;
import com.thundax.kuzhambu.classics.application.publication.query.ClassicsPublicationJobQuery;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationCreateResult;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationJobView;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationApplicationService;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationCreationApplicationService;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassicsPublicationApplicationServiceImpl implements ClassicsPublicationApplicationService {
    private final ClassicsPublicationCreationApplicationService creationService;
    private final ClassicsPublicationJobRepository jobRepository;

    public ClassicsPublicationApplicationServiceImpl(
            ClassicsPublicationCreationApplicationService creationService,
            ClassicsPublicationJobRepository jobRepository) {
        this.creationService = creationService;
        this.jobRepository = jobRepository;
    }

    @Override
    public ClassicsPublicationCreateResult create(ClassicsPublicationCreateCommand command) {
        return creationService.create(command);
    }

    @Override
    public List<ClassicsPublicationCreateResult> createBatch(ClassicsPublicationBatchCreateCommand command) {
        List<ClassicsPublicationCreateCommand> commands = command == null ? null : command.commands();
        if (commands == null || commands.isEmpty()) {
            return List.of();
        }
        List<ClassicsPublicationCreateResult> results = new ArrayList<>(commands.size());
        for (ClassicsPublicationCreateCommand item : commands) {
            try {
                results.add(creationService.create(item));
            } catch (BizException exception) {
                results.add(ClassicsPublicationCreateResult.failure(
                        item == null ? null : item.contentType(),
                        item == null ? null : item.contentId(),
                        exception.getDefaultMessage()));
            }
        }
        return List.copyOf(results);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassicsPublicationJobView get(ClassicsPublicationJobId id) {
        return toView(jobRepository.getById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClassicsPublicationJobView> page(ClassicsPublicationJobQuery query, PageQuery page) {
        PageQuery normalized = page == null ? new PageQuery() : page;
        normalized.normalize();
        PageResult<ClassicsPublicationJob> jobs = jobRepository.page(
                query == null ? null : query.jobType(),
                query == null ? null : query.jobResultStatus(),
                query == null ? null : query.jobStatus(),
                query == null ? null : query.contentType(),
                query == null ? null : query.keyword(),
                normalized.getPageNo(),
                normalized.getPageSize());
        return PageResult.of(
                jobs.getPageNo(),
                jobs.getPageSize(),
                jobs.getTotalCount(),
                jobs.getRecords().stream()
                        .map(ClassicsPublicationApplicationServiceImpl::toView)
                        .toList());
    }

    private static ClassicsPublicationJobView toView(ClassicsPublicationJob job) {
        return job == null
                ? null
                : new ClassicsPublicationJobView(
                        job, ClassicsPublicationStateMachine.nextStep(job.getJobType(), job.getJobStatus()));
    }
}
