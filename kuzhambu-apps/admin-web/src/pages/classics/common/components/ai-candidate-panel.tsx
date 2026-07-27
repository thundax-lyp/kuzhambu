import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty } from "antd";
import { useCallback, useMemo, useState } from "react";
import {
    KuzhambuList,
    KuzhambuListItem,
    KuzhambuSpace,
    KuzhambuButton,
    KuzhambuAlert,
    KuzhambuCard
} from "@/components";

import * as aiCandidateService from "../ai-candidate-service";
import type { AiCandidateCapability, AiCandidateRecord } from "../ai-candidate-types";
import { AiCandidatePayloadEditor } from "./ai-candidate-payload-editor";

type CandidateContentType = "SANCAI_ENTRY" | "WANGQI_DOCUMENT" | "MING_CUSTOMS";

interface AiCandidatePanelProps {
    capabilities: AiCandidateCapability[];
    contentId: number;
    contentType: CandidateContentType;
    objectId?: number | null;
    onApplied?: () => void;
    onRejected?: () => void;
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
    return [
        "translate",
        "summary",
        "tags",
        "qa",
        "image_analysis",
        "visual",
        "fusion",
        "image_gen"
    ].includes(capability);
};

export const AiCandidatePanel = ({
    capabilities,
    contentId,
    contentType,
    objectId = null,
    onApplied,
    onRejected
}: AiCandidatePanelProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [payloads, setPayloads] = useState<Record<number, string>>({});
    const [submitEnabled, setSubmitEnabled] = useState<Record<number, boolean>>({});
    const [applyingCandidateId, setApplyingCandidateId] = useState<number | null>(null);
    const [rejectingCandidateId, setRejectingCandidateId] = useState<number | null>(null);

    const capabilityFilter = useMemo(() => toCapabilityFilterSet(capabilities), [capabilities]);

    const pendingCandidatesQuery = useQuery({
        queryKey: ["ai", "candidates", contentType, contentId, objectId ?? null],
        queryFn: () =>
            aiCandidateService.list({
                contentId,
                contentType,
                status: "PENDING",
                objectId
            }),
        enabled: capabilityFilter.size > 0 && Boolean(contentId),
        retry: false
    });

    const pendingCandidates: AiCandidateRecord[] = useMemo(() => {
        const candidates = Array.isArray(pendingCandidatesQuery.data)
            ? pendingCandidatesQuery.data
            : [];
        const result = candidates.filter(
            (candidate) =>
                candidate.status === "PENDING" &&
                isSupportCapability(candidate.capability) &&
                capabilityFilter.has(candidate.capability) &&
                (objectId == null || candidate.objectId === objectId)
        );
        return result;
    }, [capabilityFilter, objectId, pendingCandidatesQuery.data]);

    const refreshCandidates = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["ai", "candidates", contentType, contentId, objectId ?? null]
        });
    };

    const applyMutation = useMutation({
        mutationFn: aiCandidateService.apply,
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
        mutationFn: aiCandidateService.reject,
        onMutate: (command) => {
            setRejectingCandidateId(command.candidateId);
        },
        onSuccess: async () => {
            await refreshCandidates();
            if (onRejected) {
                onRejected();
            }
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
            objectId: candidate.objectId,
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
            <KuzhambuAlert
                type="warning"
                title="候选列表加载失败"
                description="请稍后重试或联系管理员。"
            />
        );
    }

    if (!pendingCandidates.length) {
        return <Empty description="暂无待处理候选" image={Empty.PRESENTED_IMAGE_SIMPLE} />;
    }

    return (
        <KuzhambuCard size="small" title="AI 候选确认">
            <KuzhambuList
                dataSource={pendingCandidates}
                renderItem={(candidate) => (
                    <KuzhambuListItem key={candidate.candidateId}>
                        <KuzhambuSpace orientation="vertical" style={{ width: "100%" }}>
                            <KuzhambuSpace wrap>
                                <span>能力：{candidate.capability}</span>
                                <span>格式：{candidate.resultFormat || "未设置"}</span>
                                <span>时间：{formatDateTime(candidate.requestedAt)}</span>
                            </KuzhambuSpace>
                            <AiCandidatePayloadEditor
                                candidateId={candidate.candidateId}
                                capability={candidate.capability as AiCandidateCapability}
                                initialPayload={candidate.resultPayload}
                                key={`${candidate.candidateId}-${candidate.resultPayload ?? ""}`}
                                onPayloadChange={updateCandidatePayload}
                                onSubmitEnabledChange={updateCandidateSubmitEnabled}
                            />
                            <KuzhambuSpace wrap>
                                <KuzhambuButton
                                    testId="classics-common-ai-candidate-action-button"
                                    disabled={
                                        applyingCandidateId === candidate.candidateId ||
                                        !canApply(candidate)
                                    }
                                    type="primary"
                                    onClick={() => apply(candidate)}
                                >
                                    应用
                                </KuzhambuButton>
                                <KuzhambuButton
                                    testId="classics-common-ai-candidate-action-button-2"
                                    disabled={
                                        rejectingCandidateId === candidate.candidateId ||
                                        rejectMutation.isPending
                                    }
                                    onClick={() => reject(candidate)}
                                >
                                    拒绝
                                </KuzhambuButton>
                            </KuzhambuSpace>
                        </KuzhambuSpace>
                    </KuzhambuListItem>
                )}
            />
        </KuzhambuCard>
    );
};
