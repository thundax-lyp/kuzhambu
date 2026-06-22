import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Alert, Button, Card, Empty, List, Space } from "antd";
import { useCallback, useMemo, useState } from "react";
import * as aiCandidateService from "@/api/ai/ai-candidate-service";
import type { AiCandidateRecord } from "@/api/ai/ai-candidate-types";
import { AiCandidatePayloadEditor } from "./ai-candidate-payload-editor";

type AiCandidateCapability = "translate" | "summary" | "tags" | "qa";
type CandidateContentType = "SANCAI_ENTRY" | "WANGQI_DOCUMENT" | "MING_CUSTOMS";

interface AiCandidatePanelProps {
    capabilities: AiCandidateCapability[];
    contentId: number;
    contentType: CandidateContentType;
    onApplied?: () => void;
}

const REJECT_ERROR_TYPE = "USER_REJECTED";
const REJECT_ERROR_MESSAGE = "用户已拒绝该 AI 候选";

const defaultResultFormatForCapability = (capability: string) => {
    if (capability === "tags" || capability === "qa") {
        return "STRUCTURED";
    }
    return "TEXT";
};

const toCapabilityFilterSet = (capabilities: AiCandidateCapability[]) => {
    return new Set(capabilities);
};

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "—";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hour = String(date.getHours()).padStart(2, "0");
    const minute = String(date.getMinutes()).padStart(2, "0");
    const second = String(date.getSeconds()).padStart(2, "0");
    return `${year}/${month}/${day} ${hour}:${minute}:${second}`;
};

const isSupportCapability = (capability: string): capability is AiCandidateCapability => {
    return ["translate", "summary", "tags", "qa"].includes(capability);
};

export const AiCandidatePanel = ({
    capabilities,
    contentId,
    contentType,
    onApplied
}: AiCandidatePanelProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [payloads, setPayloads] = useState<Record<number, string>>({});
    const [submitEnabled, setSubmitEnabled] = useState<Record<number, boolean>>({});
    const [applyingCandidateId, setApplyingCandidateId] = useState<number | null>(null);
    const [rejectingCandidateId, setRejectingCandidateId] = useState<number | null>(null);

    const capabilityFilter = useMemo(() => toCapabilityFilterSet(capabilities), [capabilities]);

    const pendingCandidatesQuery = useQuery({
        queryKey: ["ai", "candidates", contentType, contentId],
        queryFn: () =>
            aiCandidateService.listCandidates({
                contentId,
                contentType,
                status: "PENDING"
            }),
        enabled: capabilityFilter.size > 0 && Boolean(contentId),
        retry: false
    });

    const pendingCandidates: AiCandidateRecord[] = useMemo(() => {
        return (pendingCandidatesQuery.data || []).filter(
            (candidate) =>
                candidate.status === "PENDING" &&
                isSupportCapability(candidate.capability) &&
                capabilityFilter.has(candidate.capability)
        );
    }, [capabilityFilter, pendingCandidatesQuery.data]);

    const refreshCandidates = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["ai", "candidates", contentType, contentId]
        });
    };

    const applyMutation = useMutation({
        mutationFn: aiCandidateService.applyCandidate,
        onMutate: (command) => {
            setApplyingCandidateId(command.candidateId);
        },
        onSuccess: async () => {
            await refreshCandidates();
            if (onApplied) {
                onApplied();
            }
            messageApi.success("候选已应用");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "应用失败");
        },
        onSettled: () => {
            setApplyingCandidateId(null);
        }
    });

    const rejectMutation = useMutation({
        mutationFn: aiCandidateService.rejectCandidate,
        onMutate: (command) => {
            setRejectingCandidateId(command.candidateId);
        },
        onSuccess: async () => {
            await refreshCandidates();
            messageApi.success("候选已拒绝");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "拒绝失败");
        },
        onSettled: () => {
            setRejectingCandidateId(null);
        }
    });

    const canApply = (candidate: AiCandidateRecord) => {
        return submitEnabled[candidate.candidateId] === true;
    };

    const apply = (candidate: AiCandidateRecord) => {
        const payload = payloads[candidate.candidateId];
        if (!payload || !payload.trim()) {
            messageApi.warning("请先填写候选内容");
            return;
        }
        if (!canApply(candidate)) {
            messageApi.warning("候选内容未通过完整性校验");
            return;
        }
        applyMutation.mutate({
            candidateId: candidate.candidateId,
            contentId,
            contentType,
            capability: candidate.capability,
            resultFormat:
                candidate.resultFormat?.trim() ||
                defaultResultFormatForCapability(candidate.capability),
            resultPayload: payload,
            changeSummary: `AI 应用：${candidate.capability}`
        });
    };

    const reject = (candidate: AiCandidateRecord) => {
        rejectMutation.mutate({
            candidateId: candidate.candidateId,
            errorType: REJECT_ERROR_TYPE,
            errorMessage: REJECT_ERROR_MESSAGE
        });
    };

    const updateCandidateSubmitEnabled = useCallback((candidateId: number, canSubmit: boolean) => {
        setSubmitEnabled((currentSubmitEnabled) => {
            if ((currentSubmitEnabled[candidateId] ?? false) === canSubmit) {
                return currentSubmitEnabled;
            }
            return {
                ...currentSubmitEnabled,
                [candidateId]: canSubmit
            };
        });
    }, []);

    const updateCandidatePayload = useCallback((candidateId: number, payload: string) => {
        setPayloads((currentPayloads) => {
            if (currentPayloads[candidateId] === payload) {
                return currentPayloads;
            }
            return {
                ...currentPayloads,
                [candidateId]: payload
            };
        });
    }, []);

    if (pendingCandidatesQuery.isError) {
        return (
            <Alert
                type="warning"
                message="候选列表加载失败"
                description="请稍后重试或联系管理员。"
            />
        );
    }

    if (!pendingCandidates.length) {
        return <Empty description="暂无待处理候选" image={Empty.PRESENTED_IMAGE_SIMPLE} />;
    }

    return (
        <Card size="small" title="AI 候选确认">
            <List
                dataSource={pendingCandidates}
                itemLayout="vertical"
                renderItem={(candidate) => (
                    <List.Item>
                        <Space direction="vertical" style={{ width: "100%" }}>
                            <Space wrap>
                                <span>能力：{candidate.capability}</span>
                                <span>格式：{candidate.resultFormat || "未设置"}</span>
                                <span>时间：{formatDateTime(candidate.requestedAt)}</span>
                            </Space>
                            <AiCandidatePayloadEditor
                                candidateId={candidate.candidateId}
                                capability={candidate.capability as AiCandidateCapability}
                                initialPayload={candidate.resultPayload}
                                onPayloadChange={updateCandidatePayload}
                                onSubmitEnabledChange={updateCandidateSubmitEnabled}
                            />
                            <Space wrap>
                                <Button
                                    disabled={
                                        applyingCandidateId === candidate.candidateId ||
                                        !canApply(candidate)
                                    }
                                    type="primary"
                                    onClick={() => apply(candidate)}
                                >
                                    应用
                                </Button>
                                <Button
                                    disabled={
                                        rejectingCandidateId === candidate.candidateId ||
                                        rejectMutation.isPending
                                    }
                                    onClick={() => reject(candidate)}
                                >
                                    拒绝
                                </Button>
                            </Space>
                        </Space>
                    </List.Item>
                )}
            />
        </Card>
    );
};
