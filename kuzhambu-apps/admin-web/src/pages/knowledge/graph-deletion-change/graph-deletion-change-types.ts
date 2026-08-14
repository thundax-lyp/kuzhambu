export type GraphDeletionDecision = "PRESERVE_CONTRIBUTION" | "WITHDRAW_ASSOCIATIONS";

export interface GraphDeletionChangeRecord {
    id: string;
    materialTitle: string;
    affectedNodeCount: number;
    affectedRelationCount: number;
}
