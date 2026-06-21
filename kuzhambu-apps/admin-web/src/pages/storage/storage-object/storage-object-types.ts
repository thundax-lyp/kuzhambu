export interface StorageRecord {
    id: string;
    originalFilename?: string | null;
    contentType?: string | null;
    ownerId?: string | null;
    ownerType?: string | null;
    size?: number | null;
    accessEndpoint?: string | null;
    objectStatus?: string | null;
    referenceStatus?: string | null;
    priority?: number | null;
    remarks?: string | null;
}

export type StorageContentMode = "preview" | "download";
