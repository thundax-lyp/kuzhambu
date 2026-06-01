import { postFormData, postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type { StorageUploadRecord, SubmissionRecord } from "./submission-types";

export type SubmissionStatus = "SUBMITTED" | "APPROVED" | "REJECTED" | "CLOSED";
export type SubmissionSortDirection = "ASC" | "DESC";

export interface SubmissionPageQuery {
    pageNo?: number;
    pageSize?: number;
    status?: SubmissionStatus | null;
    submittedAtBegin?: string | null;
    submittedAtEnd?: string | null;
    sortDirection?: SubmissionSortDirection;
}

export interface SubmissionSaveCommand {
    title: string;
    content: string;
    imageObjectIds?: string[];
}

export interface SubmissionStatusCommand {
    id: string;
    status: SubmissionStatus;
}

export interface SubmissionSortCommand {
    orderedIds: string[];
    sortDirection?: SubmissionSortDirection;
}

export const pageSubmissions = (request: SubmissionPageQuery = {}) => {
    return postJson<Page<SubmissionRecord>, SubmissionPageQuery>("/submission/submission/page", {
        body: request
    });
};

export const createSubmission = (request: SubmissionSaveCommand) => {
    return postJson<SubmissionRecord, SubmissionSaveCommand>("/submission/submission/create", {
        body: request
    });
};

export const changeSubmissionStatus = (request: SubmissionStatusCommand) => {
    return postJson<boolean, SubmissionStatusCommand>("/submission/submission/change-status", {
        body: request
    });
};

export const removeSubmissions = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/submission/submission/delete", {
        body: ids.map((id) => ({ id }))
    });
};

export const sortSubmissions = (request: SubmissionSortCommand) => {
    return postJson<boolean, SubmissionSortCommand>("/submission/submission/sort", {
        body: request
    });
};

export const uploadSubmissionImage = (file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return postFormData<StorageUploadRecord>("/submission/submission/image/upload", formData);
};
