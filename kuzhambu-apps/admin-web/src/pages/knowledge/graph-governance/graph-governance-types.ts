export interface GraphGovernanceNodeRecord {
    id: string;
    name: string;
    type: string;
    sourceCount: number;
}

export interface GraphGovernanceRelationRecord {
    id: string;
    sourceId: string;
    targetId: string;
    type: string;
}
