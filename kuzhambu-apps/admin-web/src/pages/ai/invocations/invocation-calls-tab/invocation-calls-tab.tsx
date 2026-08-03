import { DatePicker } from "antd";
import type { FormInstance } from "antd";
import type { TablePaginationConfig } from "antd/es/table";
import {
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuSelect,
    KuzhambuSpace
} from "@/components";
import type { Page } from "@/types/page";
import {
    INVOCATION_DATE_TIME_FORMAT,
    type InvocationLogFilterValues
} from "../invocation-filter-values";
import { InvocationTable } from "../invocation-table";
import type { AiInvocationLogRecord } from "../invocations-types";

import "./invocation-calls-tab.css";

const { RangePicker } = DatePicker;

const STATUS_OPTIONS = [
    { label: "成功", value: "SUCCEEDED" },
    { label: "失败", value: "FAILED" },
    { label: "待处理", value: "PENDING" },
    { label: "运行中", value: "RUNNING" }
];

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
            <KuzhambuForm className="invocations-calls-filter-form" form={callsForm}>
                <KuzhambuFormItem label="状态" name="status" layoutSize="small">
                    <KuzhambuSelect
                        allowClear
                        className="invocations-calls-filter-control"
                        options={STATUS_OPTIONS}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="请求时间" name="requestedAt" layoutSize="middle">
                    <RangePicker
                        aria-label="请求时间"
                        format={INVOCATION_DATE_TIME_FORMAT}
                        showTime
                        style={{ width: "100%" }}
                    />
                </KuzhambuFormItem>
            </KuzhambuForm>
            <KuzhambuSpace>
                <KuzhambuButton
                    testId="ai-invocations-invocations-query-button"
                    type="primary"
                    onClick={onSearchCalls}
                >
                    查询
                </KuzhambuButton>
                <KuzhambuButton
                    testId="ai-invocations-invocations-reset-button"
                    onClick={onResetCalls}
                >
                    重置
                </KuzhambuButton>
            </KuzhambuSpace>

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
