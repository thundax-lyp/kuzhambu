import { ReloadOutlined } from "@ant-design/icons";
import { useIsFetching, useQuery, useQueryClient } from "@tanstack/react-query";
import { Tooltip } from "antd";
import { useMemo } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuAlert, KuzhambuButton, KuzhambuPage, KuzhambuTabs } from "@/components";

import { InvocationCallsTab } from "./invocation-calls-tab";
import { InvocationSummaryTab } from "./invocation-summary-tab";
import * as service from "./invocation-service";

import "./invocation-page.css";

const CAPABILITY_LABELS: Record<string, string> = {
    CLASSICS_SUMMARY: "古籍摘要",
    CLASSICS_TAG_EXTRACT: "古籍标签",
    CLASSICS_QA: "古籍问答",
    CLASSICS_TRANSLATE: "古籍翻译",
    CLASSICS_IMAGE_DESCRIBE: "古籍图片理解",
    CLASSICS_IMAGE_GENERATE: "古籍图片生成",
    CLASSICS_IMAGE_PROMPT_FUSION: "古籍图文融合",
    CLASSICS_VISUAL_DESCRIBE: "古籍视觉描述",
    DISCOVERY_QUERY_UNDERSTANDING: "查询理解",
    DISCOVERY_ANSWER_GENERATION: "回答生成",
    KNOWLEDGE_GRAPH_EXTRACT: "知识图谱抽取",
    PROMPT_SUGGEST: "提示词优化",
    PLATFORM_VERSION_SUMMARY: "版本摘要"
};

export const InvocationPage = () => {
    const queryClient = useQueryClient();
    const canViewInvocation = hasPermission("ai:invocation:view");
    const invocationFetchingCount = useIsFetching({ queryKey: ["ai", "invocation"] });

    const invocationCapabilitiesQuery = useQuery({
        queryKey: ["ai", "invocation", "capabilities"],
        queryFn: service.listInvocationCapabilities,
        enabled: canViewInvocation,
        retry: false
    });

    const capabilityOptions = useMemo(() => {
        return (invocationCapabilitiesQuery.data || []).map((record) => ({
            label: record.name || CAPABILITY_LABELS[record.capability] || record.capability,
            value: record.capability
        }));
    }, [invocationCapabilitiesQuery.data]);

    const capabilityLabelMap = useMemo(() => {
        return (invocationCapabilitiesQuery.data || []).reduce<Record<string, string>>(
            (labels, record) => ({
                ...labels,
                [record.capability]:
                    record.name || CAPABILITY_LABELS[record.capability] || record.capability
            }),
            { ...CAPABILITY_LABELS }
        );
    }, [invocationCapabilitiesQuery.data]);

    const formatCapability = (capability?: string | null) => {
        if (!capability) {
            return "-";
        }
        return capabilityLabelMap[capability] || capability;
    };

    const refreshInvocationPage = () => {
        void queryClient.invalidateQueries({ queryKey: ["ai", "invocation"] });
    };

    return (
        <>
            <KuzhambuPage
                className="invocation-page"
                title="调用统计"
                description="查看调用指标、能力排行、调用记录和详情"
                actions={
                    <Tooltip title="刷新">
                        <KuzhambuButton
                            testId="ai-invocation-invocation-refresh-button"
                            icon={<ReloadOutlined />}
                            ariaLabel="刷新调用统计"
                            loading={invocationFetchingCount > 0}
                            onClick={refreshInvocationPage}
                        />
                    </Tooltip>
                }
            >
                {invocationCapabilitiesQuery.isError ? (
                    <KuzhambuAlert
                        showIcon
                        type="warning"
                        title="能力名称加载失败"
                        description="能力筛选和调用记录将暂时显示能力编码。"
                        action={
                            <KuzhambuButton
                                ariaLabel="重试加载能力名称"
                                testId="ai-invocation-capabilities-retry-button"
                                onClick={() => void invocationCapabilitiesQuery.refetch()}
                            >
                                重试
                            </KuzhambuButton>
                        }
                    />
                ) : null}
                <KuzhambuTabs
                    testId="ai-invocation-invocation-tabs"
                    items={[
                        {
                            key: "summary",
                            label: "统计概览",
                            children: (
                                <InvocationSummaryTab
                                    capabilityOptions={capabilityOptions}
                                    canViewInvocation={canViewInvocation}
                                    formatCapability={formatCapability}
                                />
                            )
                        },
                        {
                            key: "calls",
                            label: "调用记录",
                            children: (
                                <InvocationCallsTab
                                    formatCapability={formatCapability}
                                    canViewInvocation={canViewInvocation}
                                />
                            )
                        }
                    ]}
                />
            </KuzhambuPage>
        </>
    );
};
