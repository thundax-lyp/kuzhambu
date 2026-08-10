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
    graphVersionId: string;
    generatedBy?: number | null;
}

export interface ReextractLowQualityCategoryCommand {
    reportId: string;
    sourceCategoryCode: string;
    taskType?: string | null;
    replaceUnconfirmedOnly?: boolean | null;
    modelId?: string | null;
    modelName?: string | null;
    promptMessagesJson?: string | null;
    inputPayloadJson?: string | null;
    requestedBy?: string | null;
}

export type QualityReportPageQuery = PageQuery<{
    graphVersionId?: string | null;
    sourceContentType?: string | null;
    sourceContentId?: string | null;
    reportStatus?: QualityReportStatus | null;
}>;

export interface QualityReportDetailQuery {
    reportId: string;
}

export interface QualityReportLatestQuery {
    graphVersionId?: string | null;
}

export const generateReport = (request: GenerateQualityReportCommand) => {
    return postJson<QualityReportDetailRecord, GenerateQualityReportCommand>(
        `${API_PREFIX}/create`,
        { body: request }
    );
};

export const pageReports = (request: QualityReportPageQuery = {}) => {
    return postJson<Page<QualityReportRecord>, QualityReportPageQuery>(`${API_PREFIX}/page`, {
        body: request
    });
};

export const getReportDetail = (request: QualityReportDetailQuery) => {
    return postJson<QualityReportDetailRecord, QualityReportDetailQuery>(`${API_PREFIX}/get`, {
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
        `${API_PREFIX}/extract`,
        { body: request }
    );
};
