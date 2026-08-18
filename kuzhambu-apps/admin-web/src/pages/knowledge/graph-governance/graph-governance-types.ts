export type GraphGovernanceObjectType = "NODE" | "EDGE";

export interface GraphGovernanceNodeRecord {
    id: string;
    lockVersion?: string | null;
    name?: string | null;
    nodeType?: string | null;
    source?: string | null;
    status?: string | null;
}

export interface GraphGovernanceRelationRecord {
    id: string;
    lockVersion?: string | null;
    relationType?: string | null;
    source?: string | null;
    sourceNodeId?: string | null;
    status?: string | null;
    targetNodeId?: string | null;
}

export interface GraphGovernanceAdjacencyRecord {
    isolated: boolean;
    object?: GraphGovernanceNodeRecord | null;
    relation?: GraphGovernanceRelationRecord | null;
    subject: GraphGovernanceNodeRecord;
}

export interface GraphGovernancePropertyRecord {
    id: string;
    preferred: boolean;
    propertyName: string;
    sourceType?: string | null;
    value: string;
}

export interface GraphGovernanceMappingRecord {
    contentRef: {
        contentRefId: string;
        contentType: string;
    };
    id: string;
    mappingType: string;
    status: string;
}

export interface GraphGovernanceOperationRecord {
    id: string;
    occurredAt?: string | null;
    operationType: string;
    operatorName?: string | null;
    reason?: string | null;
}

export interface GraphGovernanceNodeDetailRecord {
    incidentEdges: GraphGovernanceRelationRecord[];
    materials: GraphGovernanceMappingRecord[];
    node: GraphGovernanceNodeRecord;
    operations: GraphGovernanceOperationRecord[];
    properties: GraphGovernancePropertyRecord[];
}

export interface GraphGovernanceRelationDetailRecord {
    edge: GraphGovernanceRelationRecord;
    materials: GraphGovernanceMappingRecord[];
    operations: GraphGovernanceOperationRecord[];
    properties: GraphGovernancePropertyRecord[];
    sourceNode: GraphGovernanceNodeRecord;
    targetNode: GraphGovernanceNodeRecord;
}
