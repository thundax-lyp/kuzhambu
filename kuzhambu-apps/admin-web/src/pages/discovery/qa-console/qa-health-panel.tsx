import { Descriptions } from "antd";
import { KuzhambuButton, KuzhambuSpace, KuzhambuCard } from "@/components";

import type { KnowledgeHealthRecord } from "./qa-console-types";

const formatTime = (value?: number | string | null) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    const timestamp = typeof value === "number" ? value : Date.parse(value);
    if (Number.isNaN(timestamp)) {
        return String(value);
    }
    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(timestamp));
};

interface QaHealthPanelProps {
    data?: KnowledgeHealthRecord;
    loading: boolean;
    onRefresh: () => void;
}

export const QaHealthPanel = ({ data, loading, onRefresh }: QaHealthPanelProps) => {
    return (
        <KuzhambuCard title="知识库健康" size="small">
            <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                <KuzhambuSpace wrap>
                    <KuzhambuButton
                        testId="discovery-qa-console-qa-console-refresh-health-button"
                        loading={loading}
                        onClick={onRefresh}
                        type="primary"
                    >
                        刷新健康
                    </KuzhambuButton>
                </KuzhambuSpace>
                <Descriptions
                    bordered
                    column={2}
                    items={[
                        {
                            key: "knowledgeBaseName",
                            label: "知识库",
                            children: data?.knowledgeBaseName ?? "-"
                        },
                        {
                            key: "status",
                            label: "状态",
                            children: data?.status ?? "-"
                        },
                        {
                            key: "provider",
                            label: "Provider",
                            children: data?.provider ?? "-"
                        },
                        {
                            key: "checkedAt",
                            label: "检查时间",
                            children: formatTime(data?.checkedAt)
                        },
                        {
                            key: "failureReason",
                            label: "失败原因",
                            children: data?.failureReason ?? "-"
                        }
                    ]}
                    size="small"
                />
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};
