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
    private ContentRef contentRef;
    private String contentTitleSnapshot;
    private GraphMaterialStatus status;
    private Instant publishedAt;
    private long lockVersion;

    public boolean editable() {
        return status == GraphMaterialStatus.DRAFT || status == GraphMaterialStatus.READY;
    }

    public void requireEditable() {
        if (!editable()) {
            throw new DomainException("Only draft or ready graph materials can be edited");
        }
    }

    public void requireReady() {
        if (status != GraphMaterialStatus.READY) {
            throw new DomainException("Only ready graph materials can publish");
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
        if (status == GraphMaterialStatus.PUBLISHED) {
            return;
        }
        status = graphEmpty ? GraphMaterialStatus.DRAFT : GraphMaterialStatus.READY;
    }

    public void markReady() {
        if (editable()) {
            status = GraphMaterialStatus.READY;
        }
    }

    public void markDraft() {
        if (editable()) {
            status = GraphMaterialStatus.DRAFT;
        }
    }

    public void publish(Instant completedAt) {
        requireReady();
        status = GraphMaterialStatus.PUBLISHED;
        publishedAt = completedAt;
    }

    public void withdraw() {
        withdraw(false);
    }

    public void withdraw(boolean graphEmpty) {
        requirePublished();
        status = graphEmpty ? GraphMaterialStatus.DRAFT : GraphMaterialStatus.READY;
        publishedAt = null;
    }
}
