export type GraphMaterialStatus = "DRAFT" | "PUBLISHING" | "PUBLISHED" | "WITHDRAWING" | "FAILED";

export interface GraphMaterialRecord {
    id: string;
    title: string;
    status: GraphMaterialStatus;
    failureReason?: string;
}
