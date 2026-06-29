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

export type UploadStage =
    | "idle"
    | "uploading-single"
    | "initiating-multipart"
    | "uploading-parts"
    | "completing-multipart"
    | "success"
    | "error"
    | "aborting"
    | "aborted";

export interface StorageUploadTaskRecord {
    fileName: string;
    fileSize: number;
    stage: UploadStage;
    uploadId?: string | null;
    uploadedBytes: number;
    totalBytes: number;
    uploadedPartCount: number;
    totalPartCount: number;
    errorMessage?: string | null;
    canCancel: boolean;
}

export type StorageContentMode = "preview" | "download";
