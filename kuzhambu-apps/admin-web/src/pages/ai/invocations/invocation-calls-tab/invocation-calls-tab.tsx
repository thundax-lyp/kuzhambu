import type { FormInstance } from "antd";
import type { TablePaginationConfig } from "antd/es/table";
import { KuzhambuCard } from "@/components";
import type { Page } from "@/types/page";
import type { InvocationLogFilterValues } from "../invocation-filter-values";
import { InvocationTable } from "../invocation-table";
import type { AiInvocationLogRecord } from "../invocations-types";
import { InvocationCallsFilterPanel } from "./invocation-calls-filter-panel";

import "./invocation-calls-tab.css";

interface InvocationCallsTabProps {
    callsForm: FormInstance<InvocationLogFilterValues>;
    currentPageNo: number;
    currentPageSize: number;
    formatCapability: (capability?: string | null) => string;
    invocationLogPage?: Page<AiInvocationLogRecord>;
    loading: boolean;
    onChange: (pagination: TablePaginationConfig) => void;
    onOpenDetail: (record: AiInvocationLogRecord) => void;
    onResetCalls: () => void;
    onSearchCalls: () => void;
}

export const InvocationCallsTab = ({
    callsForm,
    currentPageNo,
    currentPageSize,
    formatCapability,
    invocationLogPage,
    loading,
    onChange,
    onOpenDetail,
    onResetCalls,
    onSearchCalls
}: InvocationCallsTabProps) => {
    return (
        <KuzhambuCard className="invocations-section-card">
            <InvocationCallsFilterPanel
                form={callsForm}
                onReset={onResetCalls}
                onSearch={onSearchCalls}
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
