/* eslint-disable local/service-helper-contract-types, local/service-method-verb-prefix */

import { ADMIN_API_BASE_URL, postJson } from "@/api/http";
import type { Page, PageQuery } from "@/types/page";
import type {
    OperationsReportDetailRecord,
    OperationsReportFormat,
    OperationsReportRecord,
    OperationsReportType
} from "./reports-types";

const API_PREFIX = "/operations/report";

export interface OperationsReportGenerateCommand {
    reportType: OperationsReportType;
    format: OperationsReportFormat;
    periodStart: string;
    periodEnd: string;
}

export interface OperationsReportGenerateResult {
    reportId: string;
    reportStatus?: string | null;
}

export interface OperationsReportPageQuery {
    reportType?: string | null;
    format?: string | null;
    reportStatus?: string | null;
    requesterUserId?: string | null;
    periodStart?: string | null;
    periodEnd?: string | null;
}

export interface OperationsReportDetailCommand {
    reportId: string;
}

export const generateReport = (command: OperationsReportGenerateCommand) => {
    return postJson<OperationsReportGenerateResult, OperationsReportGenerateCommand>(
        `${API_PREFIX}/generate`,
        { body: command }
    );
};

export const pageReports = (query: PageQuery<OperationsReportPageQuery> = {}) => {
    return postJson<Page<OperationsReportRecord>, PageQuery<OperationsReportPageQuery>>(
        `${API_PREFIX}/page`,
        { body: query }
    );
};

export const getReportDetail = (command: OperationsReportDetailCommand) => {
    return postJson<OperationsReportDetailRecord, OperationsReportDetailCommand>(
        `${API_PREFIX}/detail`,
        { body: command }
    );
};

export const toReportDownloadUrl = (reportId: string) => {
    return `${ADMIN_API_BASE_URL}${API_PREFIX}/${encodeURIComponent(reportId)}/content?download=true`;
};
