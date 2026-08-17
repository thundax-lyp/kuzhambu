package com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject;

import java.time.Instant;
import lombok.Data;

@Data
public class GraphPublishedAdjacencyDO {
    private Long subjectId;
    private String subjectNodeKey;
    private String subjectNodeType;
    private String subjectName;
    private String subjectSource;
    private String subjectStatus;
    private Instant subjectModifiedAt;
    private Long subjectLockVersion;
    private Long relationId;
    private String relationEdgeKey;
    private Long relationSourcePublishedNodeId;
    private Long relationTargetPublishedNodeId;
    private String relationType;
    private String relationSource;
    private String relationQualifiersJson;
    private String relationStatus;
    private Instant relationModifiedAt;
    private Long relationLockVersion;
    private Long objectId;
    private String objectNodeKey;
    private String objectNodeType;
    private String objectName;
    private String objectSource;
    private String objectStatus;
    private Instant objectModifiedAt;
    private Long objectLockVersion;
}
