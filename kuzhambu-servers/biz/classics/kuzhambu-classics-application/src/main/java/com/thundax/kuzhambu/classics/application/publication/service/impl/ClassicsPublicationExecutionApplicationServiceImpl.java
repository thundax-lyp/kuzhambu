package com.thundax.kuzhambu.classics.application.publication.service.impl;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationWorkflowCommand;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationExecutionApplicationService;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassicsPublicationExecutionApplicationServiceImpl
        implements ClassicsPublicationExecutionApplicationService {
    private final ClassicsPublicationJobRepository jobRepository;

    public ClassicsPublicationExecutionApplicationServiceImpl(ClassicsPublicationJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean claim(ClassicsPublicationWorkflowCommand command) {
        return jobRepository.claimExecution(
                        command.jobId(), command.executionToken(), command.occurredAt(), command.expiresAt())
                == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsPublicationJob start(ClassicsPublicationWorkflowCommand command) {
        if (jobRepository.markThreadStarted(
                        command.jobId(), command.executionToken(), command.occurredAt(), command.expiresAt())
                != 1) {
            return null;
        }
        return jobRepository.getById(command.jobId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean releaseClaim(ClassicsPublicationWorkflowCommand command) {
        return jobRepository.releaseExecutionClaim(command.jobId(), command.executionToken()) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean retry(ClassicsPublicationWorkflowCommand command) {
        return jobRepository.releaseForRetry(
                        command.jobId(),
                        command.executionToken(),
                        command.occurredAt(),
                        command.failureReason(),
                        command.detailJson())
                == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean fail(ClassicsPublicationWorkflowCommand command) {
        return jobRepository.markTerminalFailure(
                        command.jobId(),
                        command.executionToken(),
                        command.occurredAt(),
                        command.failureReason(),
                        command.detailJson())
                == 1;
    }
}
