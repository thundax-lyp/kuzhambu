package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.PublishedGraphStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.PublishedGraphNodeId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublishedGraphNode {
    private PublishedGraphNodeId id;
    private String nodeKey;
    private String nodeType;
    private String name;
    private PublishedGraphStatus status;
    private Instant publishedAt;
    private long version;

    public void rename(String newName) {
        if (status == PublishedGraphStatus.DELETED) {
            throw new IllegalStateException("Deleted published graph nodes cannot be renamed");
        }
        name = newName;
    }

    public void delete() {
        status = PublishedGraphStatus.DELETED;
    }
}
