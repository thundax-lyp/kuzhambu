import { DatePicker } from "antd";
import type { FormInstance } from "antd";
import {
    KuzhambuButton,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuSelect,
    KuzhambuSpace
} from "@/components";
import {
    INVOCATION_DATE_TIME_FORMAT,
    type InvocationLogFilterValues
} from "@/pages/ai/invocations/invocation-filter-values";

import "./invocation-calls-filter-panel.css";

const { RangePicker } = DatePicker;

const STATUS_OPTIONS = [
    { label: "成功", value: "SUCCEEDED" },
    { label: "失败", value: "FAILED" },
    { label: "待处理", value: "PENDING" },
    { label: "运行中", value: "RUNNING" }
];

interface InvocationCallsFilterPanelProps {
    form: FormInstance<InvocationLogFilterValues>;
    onReset: () => void;
    onSearch: () => void;
}

export const InvocationCallsFilterPanel = ({
    form,
    onReset,
    onSearch
}: InvocationCallsFilterPanelProps) => {
    return (
        <>
            <KuzhambuForm className="invocations-calls-filter-form" form={form}>
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
                    onClick={onSearch}
                >
                    查询
                </KuzhambuButton>
                <KuzhambuButton testId="ai-invocations-invocations-reset-button" onClick={onReset}>
                    重置
                </KuzhambuButton>
            </KuzhambuSpace>
        </>
    );
};
