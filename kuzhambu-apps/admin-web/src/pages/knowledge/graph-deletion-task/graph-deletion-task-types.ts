export type GraphDeletionTaskStatus = "FAILED" | "RETRYING" | "SUCCEEDED";

export interface GraphDeletionTaskRecord {
    id: string;
    materialId: string;
    status: GraphDeletionTaskStatus;
    failureReason?: string;
}
