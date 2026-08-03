import { DatePicker, Statistic } from "antd";
import { Form } from "antd";
import dayjs from "dayjs";
import type { Dayjs } from "dayjs";
import { useMemo } from "react";
import {
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuSelect
} from "@/components";
import type { AiInvocationSummaryQuery } from "../invocations-service";
import type { AiInvocationSummaryRecord, AiTopCapabilityRecord } from "../invocations-types";

import "./invocation-summary-tab.css";

const { RangePicker } = DatePicker;

const INVOCATION_DATE_TIME_FORMAT = "YYYYMMDD HH:mm";

type InvocationDateRangeValue = [Dayjs | null, Dayjs | null] | null;

type InvocationSummaryFilterValues = AiInvocationSummaryQuery & {
    period?: InvocationDateRangeValue;
};

interface InvocationSummaryTabProps {
    capabilityOptions: Array<{ label: string; value: string }>;
    formatCapability: (capability?: string | null) => string;
    summary?: AiInvocationSummaryRecord;
    onRefreshSummary: (values: InvocationSummaryFilterValues) => void;
}

export const InvocationSummaryTab = ({
    capabilityOptions,
    formatCapability,
    summary,
    onRefreshSummary
}: InvocationSummaryTabProps) => {
    const [summaryForm] = Form.useForm<InvocationSummaryFilterValues>();
    const summaryInitialValues = useMemo<InvocationSummaryFilterValues>(
        () => ({
            period: [dayjs().subtract(7, "day"), dayjs()],
            bucketType: "DAY"
        }),
        []
    );
    const topCapabilities: AiTopCapabilityRecord[] = summary?.topCapabilities || [];
    const topCapabilityMaxCount = Math.max(
        ...topCapabilities.map((record) => record.invocationCount),
        1
    );

    const refreshSummary = async () => {
        const values = await summaryForm.validateFields();
        onRefreshSummary(values);
    };

    return (
        <>
            <KuzhambuCard className="invocations-summary-filter-card">
                <KuzhambuForm
                    className="invocations-summary-filter-form"
                    form={summaryForm}
                    initialValues={summaryInitialValues}
                >
                    <KuzhambuFormItem label="周期" name="period" layoutSize="middle">
                        <RangePicker
                            aria-label="周期"
                            format={INVOCATION_DATE_TIME_FORMAT}
                            showTime
                            style={{ width: "100%" }}
                        />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem label="统计粒度" name="bucketType" layoutSize="small">
                        <KuzhambuSelect
                            className="invocations-summary-filter-control"
                            options={[
                                { label: "按天", value: "DAY" },
                                { label: "按小时", value: "HOUR" }
                            ]}
                        />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem label="能力" name="capability" layoutSize="small">
                        <KuzhambuSelect
                            allowClear
                            className="invocations-summary-filter-control"
                            options={capabilityOptions}
                        />
                    </KuzhambuFormItem>
                </KuzhambuForm>
                <KuzhambuButton
                    testId="ai-invocations-invocations-refresh-button-2"
                    type="primary"
                    onClick={() => void refreshSummary()}
                >
                    刷新
                </KuzhambuButton>
            </KuzhambuCard>

            <div className="invocations-metrics">
                <KuzhambuCard>
                    <Statistic title="调用次数" value={summary?.invocationCount || 0} />
                </KuzhambuCard>
                <KuzhambuCard>
                    <Statistic
                        title="成功调用次数"
                        value={summary?.succeededInvocationCount || 0}
                    />
                </KuzhambuCard>
                <KuzhambuCard>
                    <Statistic title="失败调用次数" value={summary?.failedInvocationCount || 0} />
                </KuzhambuCard>
                <KuzhambuCard>
                    <Statistic title="平均耗时毫秒" value={summary?.avgLatencyMs || 0} />
                </KuzhambuCard>
            </div>

            <KuzhambuCard className="invocations-section-card" title="能力排行">
                <div aria-label="AI 能力排行" className="invocations-capability-bars">
                    {topCapabilities.length > 0 ? (
                        topCapabilities.map((record) => (
                            <div className="invocations-capability-bar-row" key={record.capability}>
                                <div className="invocations-capability-bar-label">
                                    {formatCapability(record.capability)}
                                </div>
                                <div className="invocations-capability-bar-track">
                                    <div
                                        className="invocations-capability-bar-fill"
                                        style={{
                                            width: `${Math.max(
                                                (record.invocationCount / topCapabilityMaxCount) *
                                                    100,
                                                4
                                            )}%`
                                        }}
                                    />
                                </div>
                                <div className="invocations-capability-bar-value">
                                    {record.invocationCount}
                                </div>
                            </div>
                        ))
                    ) : (
                        <div className="invocations-capability-bar-empty">暂无能力排行</div>
                    )}
                </div>
            </KuzhambuCard>
        </>
    );
};
