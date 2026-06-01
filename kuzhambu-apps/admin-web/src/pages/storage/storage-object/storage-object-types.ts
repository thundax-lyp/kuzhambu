export interface StorageRecord {
    id: string;
    originalFilename?: string | null;
    extendName?: string | null;
    contentType?: string | null;
    ownerId?: string | null;
    ownerType?: string | null;
    objectStatus?: string | null;
    referenceStatus?: string | null;
    remarks?: string | null;
    contentUrl?: string | null;
}
