import {
    ApiError,
    ADMIN_API_BASE_URL,
    postFormData,
    postFormDataWithProgress,
    postJson
} from "@/api/http";
import type { Page } from "@/types/page";
import type {
    InitMultipartUploadRecord,
    StorageContentMode,
    StorageRecord,
    StorageUploadTaskRecord
} from "./storage-object-types";

const MULTIPART_UPLOAD_THRESHOLD_BYTES = 20 * 1024 * 1024;
const MULTIPART_PART_SIZE_BYTES = 5 * 1024 * 1024;
const MULTIPART_UPLOAD_CONCURRENCY = 3;

export interface StoragePageQuery {
    pageNo?: number;
    pageSize?: number;
    contentType?: string | null;
    objectStatus?: string | null;
    referenceStatus?: string | null;
    referenceOwnerId?: string | null;
    referenceOwnerType?: string | null;
    originalFilename?: string | null;
    remarks?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}

export interface StorageSortCommand {
    orderedIds: string[];
    sortDirection?: "ASC" | "DESC";
}

export interface StorageObjectContentUrlCommand {
    mode?: StorageContentMode;
    storageObjectId: string;
}

export const pageStorageObjects = (request: StoragePageQuery = {}) => {
    return postJson<Page<StorageRecord>, StoragePageQuery>("/storage/object/page", {
        body: request
    });
};

export const removeStorageObjects = (ids: string[]) => {
    return postJson<boolean, { ids: string[] }>("/storage/object/delete", {
        body: { ids }
    });
};

export const uploadStorageObject = (file: File) => {
    const body = new FormData();
    body.append("file", file);
    return postFormData<StorageRecord>("/storage/object/upload", body);
};

export interface UploadMultipartPartCommand {
    uploadId: string;
    partNumber: number;
    etag: string;
    size: number;
    file: Blob;
    signal?: AbortSignal;
    onProgress?: (uploadedBytes: number, totalBytes: number) => void;
}

export interface InitMultipartUploadCommand {
    originalFilename: string;
    mimeType: string;
    businessType?: string | null;
    bucketName?: string | null;
    objectKey?: string | null;
    providerUploadId?: string | null;
    uploadId?: string | null;
    totalSize: number;
    partSize: number;
}

export interface CompleteMultipartUploadCommand {
    uploadId: string;
    bucketName?: string | null;
    objectKey?: string | null;
    size?: number | null;
    accessEndpoint?: string | null;
}

export interface AbortMultipartUploadCommand {
    uploadId: string;
}

export interface UploadStorageFileCommand {
    file: File;
    businessType?: string | null;
    bucketName?: string | null;
    objectKey?: string | null;
    providerUploadId?: string | null;
    uploadId?: string | null;
    partSize?: number;
    concurrency?: number;
    signal?: AbortSignal;
    onTaskUpdate?: (task: StorageUploadTaskRecord) => void;
}

export const uploadMultipartPart = (request: UploadMultipartPartCommand) => {
    const body = new FormData();
    body.append("uploadId", request.uploadId);
    body.append("partNumber", request.partNumber.toString());
    body.append("etag", request.etag);
    body.append("size", request.size.toString());
    body.append("file", request.file);

    return postFormDataWithProgress("/storage/object/multipart/uploadPart", body, {
        signal: request.signal,
        onProgress: request.onProgress
    });
};

export const initMultipartUpload = (request: InitMultipartUploadCommand) => {
    return postJson<InitMultipartUploadRecord, InitMultipartUploadCommand>(
        "/storage/object/multipart/initiate",
        {
            body: request
        }
    );
};

export const completeMultipartUpload = (request: CompleteMultipartUploadCommand) => {
    return postJson<StorageRecord, CompleteMultipartUploadCommand>(
        "/storage/object/multipart/complete",
        {
            body: request
        }
    );
};

export const abortMultipartUpload = (request: AbortMultipartUploadCommand) => {
    return postJson<{ uploadId: string; uploadStatus: string }, AbortMultipartUploadCommand>(
        "/storage/object/multipart/abort",
        {
            body: request
        }
    );
};

const readErrorMessage = (error: unknown) => {
    return error instanceof Error ? error.message : "未知错误";
};

const isAbortedError = (error: unknown) => {
    return (
        (error instanceof ApiError && error.code === "ABORTED") ||
        (error instanceof DOMException && error.name === "AbortError") ||
        (error instanceof Error && error.name === "AbortError")
    );
};

const createStorageUploadTask = (
    file: File,
    partialTask: Partial<StorageUploadTaskRecord> = {}
): StorageUploadTaskRecord => {
    return {
        fileName: file.name,
        fileSize: file.size,
        stage: "idle",
        uploadedBytes: 0,
        totalBytes: file.size,
        uploadedPartCount: 0,
        totalPartCount: 1,
        canCancel: false,
        ...partialTask
    };
};

const createMultipartEtag = (partNumber: number, partBlob: Blob) => {
    return `part-${partNumber}-${partBlob.size}-${partBlob.type || "bin"}`;
};

const normalizePositiveInteger = (value: number | undefined, fallback: number) => {
    if (typeof value !== "number" || !Number.isFinite(value) || value <= 0) {
        return fallback;
    }

    return Math.max(1, Math.floor(value));
};

export const uploadStorageFile = async (request: UploadStorageFileCommand) => {
    const emitTask = (task: StorageUploadTaskRecord) => {
        request.onTaskUpdate?.(task);
    };

    const {
        file,
        businessType,
        bucketName,
        objectKey,
        providerUploadId,
        uploadId,
        partSize,
        concurrency,
        signal
    } = request;

    if (signal?.aborted) {
        const abortedTask = createStorageUploadTask(file, {
            stage: "aborted",
            canCancel: false,
            errorMessage: "上传已取消"
        });
        emitTask(abortedTask);
        throw new ApiError("ABORTED", "Request was aborted");
    }

    if (file.size < MULTIPART_UPLOAD_THRESHOLD_BYTES) {
        const singleTask = createStorageUploadTask(file, {
            stage: "uploading-single",
            totalPartCount: 0,
            canCancel: false
        });
        emitTask(singleTask);
        try {
            const record = await uploadStorageObject(file);
            emitTask(
                createStorageUploadTask(file, {
                    stage: "success",
                    uploadedBytes: file.size,
                    totalBytes: file.size,
                    canCancel: false
                })
            );
            return record;
        } catch (error) {
            const errorMessage = `上传失败：${readErrorMessage(error)}`;
            emitTask(
                createStorageUploadTask(file, {
                    stage: "error",
                    uploadedBytes: 0,
                    errorMessage,
                    canCancel: false
                })
            );
            throw error;
        }
    }

    const normalizedPartSize = normalizePositiveInteger(partSize, MULTIPART_PART_SIZE_BYTES);
    const normalizedConcurrency = normalizePositiveInteger(
        concurrency,
        MULTIPART_UPLOAD_CONCURRENCY
    );
    let task = createStorageUploadTask(file, {
        stage: "initiating-multipart",
        totalPartCount: 0,
        canCancel: true
    });
    emitTask(task);

    let initRecord: InitMultipartUploadRecord;
    try {
        initRecord = await initMultipartUpload({
            originalFilename: file.name,
            mimeType: file.type || "application/octet-stream",
            businessType: businessType ?? null,
            bucketName: bucketName ?? null,
            objectKey: objectKey ?? null,
            providerUploadId: providerUploadId ?? null,
            uploadId: uploadId ?? null,
            totalSize: file.size,
            partSize: normalizedPartSize
        });
    } catch (error) {
        const errorMessage = `初始化分片上传失败：${readErrorMessage(error)}`;
        emitTask(
            createStorageUploadTask(file, {
                ...task,
                stage: "error",
                errorMessage,
                canCancel: false
            })
        );
        throw error;
    }

    const uploadPartSize = Math.max(normalizedPartSize, initRecord.partSize || normalizedPartSize);
    const parts: { partNumber: number; file: Blob }[] = [];
    for (
        let start = 0, partNumber = 1;
        start < file.size;
        partNumber += 1, start += uploadPartSize
    ) {
        const fileSlice = file.slice(start, start + uploadPartSize);
        parts.push({
            partNumber,
            file: fileSlice
        });
    }

    task = createStorageUploadTask(file, {
        ...task,
        stage: "uploading-parts",
        uploadId: initRecord.uploadId,
        totalPartCount: parts.length,
        uploadedBytes: 0,
        uploadedPartCount: 0,
        canCancel: Boolean(signal && !signal.aborted)
    });
    emitTask(task);

    const partUploadedBytes = new Array(parts.length).fill(0);
    const updateTaskBytes = () => {
        const uploadedBytes = partUploadedBytes.reduce((sum, uploaded) => sum + uploaded, 0);
        emitTask({
            ...task,
            uploadedBytes,
            canCancel: Boolean(signal && !signal.aborted)
        });
    };

    try {
        let nextPartIndex = 0;
        const worker = async () => {
            while (nextPartIndex < parts.length && !signal?.aborted) {
                const partIndex = nextPartIndex;
                nextPartIndex += 1;

                const part = parts[partIndex];
                const partRequest: UploadMultipartPartCommand = {
                    uploadId: initRecord.uploadId,
                    partNumber: part.partNumber,
                    etag: createMultipartEtag(part.partNumber, part.file),
                    size: part.file.size,
                    file: part.file,
                    signal,
                    onProgress: (uploadedBytes, totalBytes) => {
                        partUploadedBytes[partIndex] = uploadedBytes;
                        if (totalBytes > 0 && uploadedBytes >= totalBytes) {
                            partUploadedBytes[partIndex] = totalBytes;
                        }
                        updateTaskBytes();
                    }
                };

                try {
                    await uploadMultipartPart(partRequest);
                } catch (error) {
                    if (isAbortedError(error)) {
                        throw error;
                    }

                    await abortMultipartUpload({ uploadId: initRecord.uploadId }).catch(
                        () => undefined
                    );
                    throw error;
                }

                task = createStorageUploadTask(file, {
                    ...task,
                    uploadedPartCount: task.uploadedPartCount + 1
                });
                emitTask(task);
            }
        };

        const workers = Array.from({ length: Math.min(normalizedConcurrency, parts.length) }, () =>
            worker()
        );
        await Promise.all(workers);
    } catch (error) {
        const isAborted = isAbortedError(error);
        task = createStorageUploadTask(file, {
            ...task,
            uploadedBytes: partUploadedBytes.reduce((sum, uploaded) => sum + uploaded, 0),
            stage: isAborted ? "aborting" : "error",
            errorMessage: readErrorMessage(error),
            canCancel: false
        });
        emitTask(task);

        if (!isAborted) {
            await abortMultipartUpload({ uploadId: initRecord.uploadId }).catch(() => undefined);
            emitTask({
                ...task,
                stage: "error",
                canCancel: false
            });
        } else {
            emitTask({
                ...task,
                stage: "aborted",
                canCancel: false
            });
        }
        throw error;
    }

    try {
        task = createStorageUploadTask(file, {
            ...task,
            stage: "completing-multipart",
            uploadedBytes: file.size,
            canCancel: Boolean(signal && !signal.aborted)
        });
        emitTask(task);

        const result = await completeMultipartUpload({
            uploadId: initRecord.uploadId,
            bucketName: initRecord.bucketName || bucketName || null,
            objectKey: initRecord.objectKey || objectKey || null,
            size: file.size,
            accessEndpoint: null
        });

        emitTask(
            createStorageUploadTask(file, {
                ...task,
                stage: "success",
                uploadedBytes: file.size,
                totalBytes: file.size,
                uploadedPartCount: task.totalPartCount,
                canCancel: false
            })
        );
        return result;
    } catch (error) {
        const isAborted = isAbortedError(error);
        if (!isAborted) {
            await abortMultipartUpload({ uploadId: initRecord.uploadId }).catch(() => undefined);
        }

        emitTask(
            createStorageUploadTask(file, {
                ...task,
                stage: isAborted ? "aborted" : "error",
                errorMessage: isAborted
                    ? readErrorMessage(error)
                    : `分片合并失败：${readErrorMessage(error)}`,
                uploadedBytes: partUploadedBytes.reduce((sum, uploaded) => sum + uploaded, 0),
                canCancel: false
            })
        );
        throw error;
    }
};

export const sortStorageObjects = (request: StorageSortCommand) => {
    return postJson<boolean, StorageSortCommand>("/storage/object/sort", {
        body: request
    });
};

export const getStorageObjectContentUrl = (request: StorageObjectContentUrlCommand) => {
    const mode = request.mode || "preview";
    const search = mode === "download" ? "?download=true" : "";
    return `${ADMIN_API_BASE_URL}/storage/object/${encodeURIComponent(request.storageObjectId)}/content${search}`;
};
