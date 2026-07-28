import { useMutation, useQueries, useQueryClient } from "@tanstack/react-query";
import { App, Empty, Typography } from "antd";
import { useMemo, useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import {
    KuzhambuDrawer,
    KuzhambuTable,
    type KuzhambuTableProps,
    KuzhambuAlert
} from "@/components";

import * as aiCandidateService from "../ai-candidate-service";
import type { AiCandidateApplyCommand } from "../ai-candidate-service";
import * as aiRefinementTaskService from "../ai-refinement-task-service";
import * as classicsContentService from "../classics-content-service";
import { type AiCandidateCapability, type AiCandidateRecord } from "../ai-candidate-types";
import type { ClassicsBatchOperationRecord, ClassicsContentType } from "../classics-content-types";
import type { ClassicsAiCandidateBatchApplyCommand } from "../classics-content-service";
import { AiCandidatePayloadEditor } from "./ai-candidate-payload-editor";
import { normalizeId } from "@/types/id";

const { Text } = Typography;

const REJECT_ERROR_TYPE = "USER_REJECTED";
const REJECT_ERROR_MESSAGE = "用户已批量拒绝该 AI 候选";

interface AiCandidateBatchDrawerProps {
    open: boolean;
    contentType: ClassicsContentType;
    contentIds: string[];
    capabilities: AiCandidateCapability[];
    contentTitleById?: Record<string, string>;
    canEdit: boolean;
    onClose: () => void;
    onChanged: () => Promise<void> | void;
}

interface BatchOperationState {
    action: "apply" | "reject";
    result: ClassicsBatchOperationRecord;
}

const defaultResultFormatForCapability = (capability: string) => {
    const normalizedCapability = aiRefinementTaskService.getNormalizedTaskCapability(capability);
    if (normalizedCapability === "tags" || normalizedCapability === "qa") {
        return "STRUCTURED";
    }
    return "TEXT";
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

const buildCandidateQueryKey = (contentType: ClassicsContentType, contentId: string) => {
    return ["ai", "candidates", "batch", contentType, contentId];
};

const isSupportCapability = (capability: string): capability is AiCandidateCapability => {
    const normalizedCapability = aiRefinementTaskService.getNormalizedTaskCapability(capability);
    return [
        "translate",
        "summary",
        "tags",
        "qa",
        "image_analysis",
        "visual",
        "fusion",
        "image_gen"
    ].includes(normalizedCapability);
};

export const AiCandidateBatchDrawer = ({
    open,
    contentType,
    contentIds,
    capabilities,
    contentTitleById,
    canEdit,
    onClose,
    onChanged
}: AiCandidateBatchDrawerProps) => {
    const confirm = useKuzhambuConfirm();
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();

    const [selectedCandidateIds, setSelectedCandidateIds] = useState<string[]>([]);
    const [payloads, setPayloads] = useState<Record<string, string>>({});
    const [submitEnabled, setSubmitEnabled] = useState<Record<string, boolean>>({});
    const [operationResult, setOperationResult] = useState<BatchOperationState | null>(null);

    const requestCandidateIds = useMemo(
        () => Array.from(new Set(contentIds)).filter(Boolean),
        [contentIds]
    );
    const capabilitySet = useMemo(() => new Set(capabilities), [capabilities]);
    const enabledQueries = open && requestCandidateIds.length > 0 && capabilitySet.size > 0;

    const pendingCandidateQueries = useQueries({
        queries: requestCandidateIds.map((contentId) => {
            return {
                queryKey: buildCandidateQueryKey(contentType, contentId),
                queryFn: () =>
                    aiCandidateService.list({
                        contentType,
                        contentId,
                        status: "PENDING"
                    }),
                enabled: enabledQueries,
                retry: false
            };
        })
    });

    const pendingCandidates: AiCandidateRecord[] = useMemo(() => {
        const allCandidates = pendingCandidateQueries.flatMap((query) =>
            Array.isArray(query.data) ? query.data : []
        );
        const filteredCandidates = allCandidates.filter((candidate) => {
            const normalizedCapability = aiRefinementTaskService.getNormalizedTaskCapability(
                candidate.capability
            );
            return (
                candidate.status === "PENDING" &&
                isSupportCapability(candidate.capability) &&
                capabilitySet.has(normalizedCapability as AiCandidateCapability)
            );
        });

        const candidatesById = new Map<string, AiCandidateRecord>();
        filteredCandidates.forEach((candidate) => {
            const candidateId = normalizeId(candidate.candidateId);
            if (!candidatesById.has(candidateId)) {
                candidatesById.set(candidateId, candidate);
            }
        });

        return Array.from(candidatesById.values());
    }, [capabilitySet, pendingCandidateQueries]);

    const hasLoadError = pendingCandidateQueries.some((query) => query.isError);
    const isLoading = pendingCandidateQueries.some((query) => query.isLoading);

    const effectivePayloads = useMemo(() => {
        return Object.fromEntries(
            pendingCandidates.map((candidate) => {
                const candidateId = normalizeId(candidate.candidateId);
                return [
                    candidateId,
                    payloads[candidateId] ?? candidate.resultPayload?.trim() ?? ""
                ];
            })
        );
    }, [payloads, pendingCandidates]);

    const effectiveSubmitEnabled = useMemo(() => {
        return Object.fromEntries(
            pendingCandidates.map((candidate) => {
                const candidateId = normalizeId(candidate.candidateId);
                return [
                    candidateId,
                    submitEnabled[candidateId] ?? (candidate.resultPayload?.trim() || "").length > 0
                ];
            })
        );
    }, [pendingCandidates, submitEnabled]);

    const refreshCandidates = async () => {
        await Promise.all(
            requestCandidateIds.map((contentId) =>
                queryClient.invalidateQueries({
                    queryKey: buildCandidateQueryKey(contentType, contentId)
                })
            )
        );
    };

    const applyBatchMutation = useMutation({
        mutationFn: classicsContentService.applyAiCandidatesBatch,
        onMutate: () => {
            setOperationResult(null);
        },
        onSuccess: async (result) => {
            setOperationResult({ action: "apply", result });
            messageApi.success(
                `批量候选应用结果：成功 ${result.successCount}，失败 ${result.failureCount}`
            );
            await refreshCandidates();
            await onChanged();
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量候选应用失败");
        }
    });

    const rejectBatchMutation = useMutation({
        mutationFn: classicsContentService.rejectAiCandidatesBatch,
        onMutate: () => {
            setOperationResult(null);
        },
        onSuccess: async (result) => {
            setOperationResult({ action: "reject", result });
            messageApi.success(
                `批量候选拒绝结果：成功 ${result.successCount}，失败 ${result.failureCount}`
            );
            await refreshCandidates();
            await onChanged();
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量候选拒绝失败");
        }
    });

    const selectedCandidates = useMemo(
        () =>
            pendingCandidates.filter((candidate) =>
                selectedCandidateIds.includes(normalizeId(candidate.candidateId))
            ),
        [pendingCandidates, selectedCandidateIds]
    );

    const canApplySelected = useMemo(
        () =>
            selectedCandidates.length > 0 &&
            selectedCandidates.every(
                (candidate) => effectiveSubmitEnabled[normalizeId(candidate.candidateId)]
            ),
        [effectiveSubmitEnabled, selectedCandidates]
    );

    const buildFailureSummary = (operationResult: ClassicsBatchOperationRecord) => {
        return operationResult.failures
            .map(
                (failure) =>
                    `${failure.candidateId ?? "-"} / ${failure.capability ?? "-"} / ${failure.failureReason || failure.failureCode || "未知失败"}`
            )
            .join("；");
    };

    const applyCandidates = () => {
        if (!canEdit) {
            messageApi.warning("当前账号缺少编辑权限");
            return;
        }
        if (!selectedCandidateIds.length) {
            messageApi.warning("请选择要应用的候选");
            return;
        }

        if (!canApplySelected) {
            messageApi.warning("请先修正候选内容");
            return;
        }

        const payload: ClassicsAiCandidateBatchApplyCommand = {
            items: selectedCandidates.map((candidate) => {
                const payload = effectivePayloads[normalizeId(candidate.candidateId)] ?? "";
                const normalizedCapability = aiRefinementTaskService.getNormalizedTaskCapability(
                    candidate.capability
                );
                const command: AiCandidateApplyCommand = {
                    candidateId: candidate.candidateIdText || String(candidate.candidateId),
                    contentType: candidate.contentType,
                    contentId: normalizeId(candidate.contentId),
                    capability: normalizedCapability,
                    objectId: candidate.objectId,
                    resultFormat:
                        candidate.resultFormat ||
                        defaultResultFormatForCapability(normalizedCapability),
                    resultPayload: payload,
                    changeSummary: `AI 应用：${normalizedCapability}`
                };
                return command;
            })
        };

        applyBatchMutation.mutate(payload);
    };

    const rejectCandidates = () => {
        if (!canEdit) {
            messageApi.warning("当前账号缺少编辑权限");
            return;
        }
        if (!selectedCandidateIds.length) {
            messageApi.warning("请选择要拒绝的候选");
            return;
        }

        confirm.danger({
            title: "确认批量拒绝",
            message: "确认批量拒绝已选择的 AI 候选？拒绝后不会修改正式内容。",
            okText: "拒绝",
            onConfirm: () =>
                rejectBatchMutation.mutate({
                    errorType: REJECT_ERROR_TYPE,
                    errorMessage: REJECT_ERROR_MESSAGE,
                    items: selectedCandidates.map((candidate) => ({
                        candidateId: candidate.candidateIdText || String(candidate.candidateId),
                        contentType: candidate.contentType,
                        contentId: normalizeId(candidate.contentId),
                        capability: candidate.capability,
                        objectId: candidate.objectId
                    }))
                })
        });
    };

    const closeDrawer = () => {
        setSelectedCandidateIds([]);
        setPayloads({});
        setSubmitEnabled({});
        setOperationResult(null);
        onClose();
    };

    const updateCandidatePayload = (candidateId: string, payload: string) => {
        setPayloads((currentPayloads) => {
            if (currentPayloads[candidateId] === payload) {
                return currentPayloads;
            }
            return {
                ...currentPayloads,
                [candidateId]: payload
            };
        });
    };

    const updateCandidateSubmitEnabled = (candidateId: string, canSubmit: boolean) => {
        setSubmitEnabled((currentSubmitEnabled) => {
            if ((currentSubmitEnabled[candidateId] ?? false) === canSubmit) {
                return currentSubmitEnabled;
            }
            return {
                ...currentSubmitEnabled,
                [candidateId]: canSubmit
            };
        });
    };

    const columns = useMemo(
        () =>
            [
                {
                    title: "内容",
                    key: "content",
                    width: 220,
                    render: (_: unknown, candidate: AiCandidateRecord) => (
                        <Text>
                            {contentTitleById?.[normalizeId(candidate.contentId)]?.trim() ||
                                candidate.contentId}
                        </Text>
                    )
                },
                {
                    title: "contentType",
                    dataIndex: "contentType",
                    width: 120,
                    render: (value: string) => <Text>{value}</Text>
                },
                {
                    title: "contentId",
                    dataIndex: "contentId",
                    width: 110,
                    render: (value: number) => <Text>{value}</Text>
                },
                {
                    title: "capability",
                    dataIndex: "capability",
                    width: 120,
                    render: (value: string) => <Text>{value}</Text>
                },
                {
                    title: "objectId",
                    dataIndex: "objectId",
                    width: 120,
                    render: (value?: number | null) => <Text>{value ?? "—"}</Text>
                },
                {
                    title: "requestedAt",
                    dataIndex: "requestedAt",
                    width: 170,
                    render: (value?: string | null) => <Text>{formatDateTime(value)}</Text>
                },
                {
                    title: "单条校验状态",
                    key: "submitStatus",
                    width: 120,
                    render: (_: unknown, candidate: AiCandidateRecord) => {
                        const canSubmit =
                            effectiveSubmitEnabled[normalizeId(candidate.candidateId)];
                        return canSubmit ? (
                            <Text type="success">校验通过</Text>
                        ) : (
                            <Text type="danger">未通过</Text>
                        );
                    }
                },
                {
                    title: "payload",
                    key: "payload",
                    width: 420,
                    render: (_: unknown, candidate: AiCandidateRecord) => {
                        const candidateId = normalizeId(candidate.candidateId);
                        return (
                            <AiCandidatePayloadEditor
                                candidateId={candidateId}
                                capability={
                                    aiRefinementTaskService.getNormalizedTaskCapability(
                                        candidate.capability
                                    ) as AiCandidateCapability
                                }
                                initialPayload={
                                    effectivePayloads[candidateId] ?? candidate.resultPayload
                                }
                                onPayloadChange={updateCandidatePayload}
                                onSubmitEnabledChange={updateCandidateSubmitEnabled}
                            />
                        );
                    }
                }
            ] as KuzhambuTableProps<AiCandidateRecord>["columns"],
        [contentTitleById, effectivePayloads, effectiveSubmitEnabled]
    );

    return (
        <KuzhambuDrawer
            testId="classics-common-ai-candidate-batch-drawer"
            title="AI 候选批量治理"
            open={open}
            size="large"
            onClose={closeDrawer}
            footerActions={[
                {
                    testId: "classics-common-ai-candidate-batch-close-button",
                    title: "关闭",
                    action: closeDrawer
                },
                {
                    testId: "classics-common-ai-candidate-batch-action-button",
                    title: "刷新候选",
                    loading: applyBatchMutation.isPending || rejectBatchMutation.isPending,
                    action: () => {
                        void refreshCandidates();
                    }
                },
                {
                    testId: "classics-common-ai-candidate-batch-action-button-2",
                    title: "批量应用",
                    type: "primary",
                    loading: applyBatchMutation.isPending,
                    disabled: !canEdit,
                    action: applyCandidates
                },
                {
                    testId: "classics-common-ai-candidate-batch-action-button-3",
                    title: "批量拒绝",
                    danger: true,
                    loading: rejectBatchMutation.isPending,
                    disabled: !canEdit,
                    action: rejectCandidates
                }
            ]}
        >
            {hasLoadError ? (
                <KuzhambuAlert
                    type="warning"
                    title="候选列表加载失败"
                    description="请稍后重试或联系管理员。"
                    showIcon
                />
            ) : null}

            {operationResult ? (
                <KuzhambuAlert
                    showIcon
                    type={operationResult.result.failureCount > 0 ? "warning" : "success"}
                    style={{ marginTop: 8 }}
                    title={
                        operationResult.action === "apply"
                            ? `批量候选应用结果：成功 ${operationResult.result.successCount}，失败 ${operationResult.result.failureCount}`
                            : `批量候选拒绝结果：成功 ${operationResult.result.successCount}，失败 ${operationResult.result.failureCount}`
                    }
                    description={
                        operationResult.result.failures.length
                            ? buildFailureSummary(operationResult.result)
                            : "操作成功完成。"
                    }
                />
            ) : null}

            {isLoading ? <Text>候选加载中...</Text> : null}

            {!pendingCandidates.length ? (
                <Empty description="暂无待处理候选" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            ) : (
                <KuzhambuTable
                    className="ai-candidate-batch-table"
                    ariaLabel="AI 候选批量治理列表"
                    columns={columns}
                    dataSource={pendingCandidates}
                    toolbar={{
                        leading: (
                            <Text>
                                已选内容 {requestCandidateIds.length} 个 / 待处理候选{" "}
                                {pendingCandidates.length} 个 / 已选择候选{" "}
                                {selectedCandidateIds.length} 个
                            </Text>
                        )
                    }}
                    rowKey={(candidate) => normalizeId(candidate.candidateId)}
                    rowSelection={{
                        selectedRowKeys: selectedCandidateIds,
                        preserveSelectedRowKeys: true,
                        onChange: (keys) => {
                            setSelectedCandidateIds(keys.map(String));
                        }
                    }}
                    size="middle"
                    scroll={{ x: 1320 }}
                />
            )}
        </KuzhambuDrawer>
    );
};
