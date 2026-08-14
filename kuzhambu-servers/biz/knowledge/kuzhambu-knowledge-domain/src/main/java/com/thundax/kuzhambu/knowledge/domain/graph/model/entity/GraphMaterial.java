package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphMaterial {
    public static final String FAILED_OPERATION_PUBLISH = "PUBLISH";
    public static final String FAILED_OPERATION_WITHDRAW = "WITHDRAW";

    private ContentRef contentRef;
    private String contentTitleSnapshot;
    private GraphMaterialStatus status;
    private Instant publishedAt;
    private String failureReason;
    private String failedOperation;
    private long lockVersion;

    public GraphMaterial(
            ContentRef contentRef,
            String contentTitleSnapshot,
            GraphMaterialStatus status,
            Instant publishedAt,
            long lockVersion) {
        this.contentRef = contentRef;
        this.contentTitleSnapshot = contentTitleSnapshot;
        this.status = status;
        this.publishedAt = publishedAt;
        this.lockVersion = lockVersion;
    }

    public boolean editable() {
        return status == GraphMaterialStatus.DRAFT;
    }

    public void requireEditable() {
        if (!editable()) {
            throw new DomainException("Only draft graph materials can be edited");
        }
    }

    public void requirePublishable() {
        if (status != GraphMaterialStatus.DRAFT) {
            throw new DomainException("Only draft graph materials can publish");
        }
    }

    public void requirePublished() {
        if (status != GraphMaterialStatus.PUBLISHED) {
            throw new DomainException("Only published graph materials can withdraw");
        }
    }

    public void requireLockVersion(long expectedLockVersion) {
        if (lockVersion != expectedLockVersion) {
            throw new DomainException("Graph material lock version mismatch");
        }
    }

    public void refreshStatus(boolean graphEmpty) {
        if (status != GraphMaterialStatus.DRAFT) {
            return;
        }
        clearFailure();
    }

    public void markDraft() {
        if (editable()) {
            status = GraphMaterialStatus.DRAFT;
            clearFailure();
        }
    }

    public void startPublishing() {
        requirePublishable();
        status = GraphMaterialStatus.PUBLISHING;
        clearFailure();
    }

    public void publish(Instant completedAt) {
        if (status != GraphMaterialStatus.PUBLISHING) {
            throw new DomainException("Only publishing graph materials can complete publication");
        }
        status = GraphMaterialStatus.PUBLISHED;
        publishedAt = completedAt;
        clearFailure();
    }

    public void failPublication(String failureReason) {
        if (status != GraphMaterialStatus.PUBLISHING) {
            throw new DomainException("Only publishing graph materials can fail publication");
        }
        fail(failureReason, FAILED_OPERATION_PUBLISH);
    }

    public void startWithdrawal() {
        requirePublished();
        status = GraphMaterialStatus.WITHDRAWING;
        clearFailure();
    }

    public void withdraw() {
        if (status != GraphMaterialStatus.WITHDRAWING) {
            throw new DomainException("Only withdrawing graph materials can complete withdrawal");
        }
        status = GraphMaterialStatus.DRAFT;
        publishedAt = null;
        clearFailure();
    }

    public void failWithdrawal(String failureReason) {
        if (status != GraphMaterialStatus.WITHDRAWING) {
            throw new DomainException("Only withdrawing graph materials can fail withdrawal");
        }
        fail(failureReason, FAILED_OPERATION_WITHDRAW);
    }

    public void retryFailure() {
        if (status != GraphMaterialStatus.FAILED) {
            throw new DomainException("Only failed graph materials can retry");
        }
        if (FAILED_OPERATION_PUBLISH.equals(failedOperation)) {
            status = GraphMaterialStatus.DRAFT;
        } else if (FAILED_OPERATION_WITHDRAW.equals(failedOperation)) {
            status = GraphMaterialStatus.PUBLISHED;
        } else {
            throw new DomainException("Graph material failed operation is unknown");
        }
        clearFailure();
    }

    private void fail(String failureReason, String failedOperation) {
        status = GraphMaterialStatus.FAILED;
        this.failureReason = failureReason;
        this.failedOperation = failedOperation;
    }

    private void clearFailure() {
        failureReason = null;
        failedOperation = null;
    }
}
