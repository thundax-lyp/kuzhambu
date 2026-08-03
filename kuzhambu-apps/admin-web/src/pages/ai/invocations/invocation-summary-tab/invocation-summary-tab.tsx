import { Statistic } from "antd";
import type { FormInstance } from "antd";
import { KuzhambuCard } from "@/components";
import {
    InvocationFilterPanel,
    type InvocationLogFilterValues,
    type InvocationSummaryFilterValues
} from "../invocation-filter-panel";
import type { AiInvocationSummaryRecord, AiTopCapabilityRecord } from "../invocations-types";

interface InvocationSummaryTabProps {
    callsForm: FormInstance<InvocationLogFilterValues>;
    capabilityOptions: Array<{ label: string; value: string }>;
    formatCapability: (capability?: string | null) => string;
    summary?: AiInvocationSummaryRecord;
    summaryForm: FormInstance<InvocationSummaryFilterValues>;
    summaryInitialValues: InvocationSummaryFilterValues;
    topCapabilities: AiTopCapabilityRecord[];
    topCapabilityMaxCount: number;
    onRefreshSummary: () => void;
    onResetCalls: () => void;
    onSearchCalls: () => void;
}

export const InvocationSummaryTab = ({
    callsForm,
    capabilityOptions,
    formatCapability,
    summary,
    summaryForm,
    summaryInitialValues,
    topCapabilities,
    topCapabilityMaxCount,
    onRefreshSummary,
    onResetCalls,
    onSearchCalls
}: InvocationSummaryTabProps) => {
    return (
        <>
            <InvocationFilterPanel
                callsForm={callsForm}
                capabilityOptions={capabilityOptions}
                summaryForm={summaryForm}
                summaryInitialValues={summaryInitialValues}
                type="summary"
                onRefreshSummary={onRefreshSummary}
                onResetCalls={onResetCalls}
                onSearchCalls={onSearchCalls}
            />

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
