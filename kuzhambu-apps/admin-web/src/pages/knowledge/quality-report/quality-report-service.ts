/* eslint-disable local/service-method-verb-prefix */

import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    GenerateQualityReportCommand,
    QualityReportDetailQuery,
    QualityReportDetailRecord,
    QualityReportLatestQuery,
    QualityReportPageQuery,
    QualityReportRecord,
    ReextractLowQualityCategoryCommand,
    ReextractLowQualityCategoryRecord
} from "./quality-report-types";

const API_PREFIX = "/knowledge/quality/report";

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
