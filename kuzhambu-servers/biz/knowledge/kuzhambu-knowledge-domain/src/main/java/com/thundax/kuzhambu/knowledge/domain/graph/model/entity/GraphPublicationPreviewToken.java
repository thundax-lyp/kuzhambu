package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphPublicationPreviewToken {
    public static final String STALE_CODE = "GRAPH_PREVIEW_STALE";

    private String token;
    private Long materialId;
    private long materialLockVersion;
    private String snapshotJson;
    private Instant expiresAt;
    private Instant consumedAt;

    public boolean consumableAt(Instant now) {
        return consumedAt == null && expiresAt != null && now != null && expiresAt.isAfter(now);
    }

    public static DomainException stale() {
        return new DomainException(STALE_CODE, "knowledge.graph.preview-stale", "图谱预览已失效，请刷新后重试");
    }
}
