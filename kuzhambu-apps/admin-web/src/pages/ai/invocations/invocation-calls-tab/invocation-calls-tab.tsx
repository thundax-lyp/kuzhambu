import type { FormInstance } from "antd";
import type { TablePaginationConfig } from "antd/es/table";
import { KuzhambuCard } from "@/components";
import type { Page } from "@/types/page";
import {
    InvocationFilterPanel,
    type InvocationLogFilterValues,
    type InvocationSummaryFilterValues
} from "../invocation-filter-panel";
import { InvocationTable } from "../invocation-table";
import type { AiInvocationLogRecord } from "../invocations-types";

interface InvocationCallsTabProps {
    callsForm: FormInstance<InvocationLogFilterValues>;
    capabilityOptions: Array<{ label: string; value: string }>;
    currentPageNo: number;
    currentPageSize: number;
    formatCapability: (capability?: string | null) => string;
    invocationLogPage?: Page<AiInvocationLogRecord>;
    loading: boolean;
    summaryForm: FormInstance<InvocationSummaryFilterValues>;
    summaryInitialValues: InvocationSummaryFilterValues;
    onChange: (pagination: TablePaginationConfig) => void;
    onOpenDetail: (record: AiInvocationLogRecord) => void;
    onRefreshSummary: () => void;
    onResetCalls: () => void;
    onSearchCalls: () => void;
}

export const InvocationCallsTab = ({
    callsForm,
    capabilityOptions,
    currentPageNo,
    currentPageSize,
    formatCapability,
    invocationLogPage,
    loading,
    summaryForm,
    summaryInitialValues,
    onChange,
    onOpenDetail,
    onRefreshSummary,
    onResetCalls,
    onSearchCalls
}: InvocationCallsTabProps) => {
    return (
        <KuzhambuCard className="invocations-section-card">
            <InvocationFilterPanel
                callsForm={callsForm}
                capabilityOptions={capabilityOptions}
                summaryForm={summaryForm}
                summaryInitialValues={summaryInitialValues}
                type="calls"
                onRefreshSummary={onRefreshSummary}
                onResetCalls={onResetCalls}
                onSearchCalls={onSearchCalls}
            />

            <InvocationTable
                currentPageNo={currentPageNo}
                currentPageSize={currentPageSize}
                formatCapability={formatCapability}
                invocationLogPage={invocationLogPage}
                loading={loading}
                onChange={onChange}
                onOpenDetail={onOpenDetail}
            />
        </KuzhambuCard>
    );
};
