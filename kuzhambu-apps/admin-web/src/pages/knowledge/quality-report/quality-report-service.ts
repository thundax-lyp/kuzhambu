/* eslint-disable local/service-method-verb-prefix */

import { postJson } from "@/api/http";
import type { Page, PageQuery } from "@/types/page";
import type {
    QualityReportDetailRecord,
    QualityReportRecord,
    QualityReportStatus,
    ReextractLowQualityCategoryRecord
} from "./quality-report-types";

const API_PREFIX = "/knowledge/quality/report";

export interface GenerateQualityReportCommand {
    graphVersionId: number;
    generatedBy?: number | null;
}

export interface ReextractLowQualityCategoryCommand {
    reportId: number;
    sourceCategoryCode: string;
    taskType?: string | null;
    replaceUnconfirmedOnly?: boolean | null;
    modelId?: number | null;
    modelName?: string | null;
    promptMessagesJson?: string | null;
    inputPayloadJson?: string | null;
    requestedBy?: number | null;
}

export type QualityReportPageQuery = PageQuery<{
    graphVersionId?: number | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    reportStatus?: QualityReportStatus | null;
}>;

export interface QualityReportDetailQuery {
    reportId: number;
}

export interface QualityReportLatestQuery {
    graphVersionId?: number | null;
}

export const generateReport = (request: GenerateQualityReportCommand) => {
    return postJson<QualityReportDetailRecord, GenerateQualityReportCommand>(
        `${API_PREFIX}/generate`,
        { body: request }
    );
};

export const pageReports = (request: QualityReportPageQuery = {}) => {
    return postJson<Page<QualityReportRecord>, QualityReportPageQuery>(`${API_PREFIX}/page`, {
        body: request
    });
};

export const getReportDetail = (request: QualityReportDetailQuery) => {
    return postJson<QualityReportDetailRecord, QualityReportDetailQuery>(`${API_PREFIX}/detail`, {
        body: request
    });
};

export const getLatestReport = (request: QualityReportLatestQuery = {}) => {
    return postJson<QualityReportDetailRecord, QualityReportLatestQuery>(`${API_PREFIX}/latest`, {
        body: request
    });
};

export const reextractLowQualityCategory = (request: ReextractLowQualityCategoryCommand) => {
    return postJson<ReextractLowQualityCategoryRecord, ReextractLowQualityCategoryCommand>(
        `${API_PREFIX}/reextract-low-quality-category`,
        { body: request }
    );
};
