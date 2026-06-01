import type { SubmissionStatus } from "./submission-service";

export interface SubmissionRecord {
    id: string;
    title: string;
    content: string;
    status?: SubmissionStatus | string | null;
    submittedAt?: string | null;
    imageObjectIds?: string[] | null;
}

export interface StorageUploadRecord {
    id?: string | null;
    originalFilename?: string | null;
    extendName?: string | null;
    contentType?: string | null;
    contentUrl?: string | null;
    error?: string | null;
}
