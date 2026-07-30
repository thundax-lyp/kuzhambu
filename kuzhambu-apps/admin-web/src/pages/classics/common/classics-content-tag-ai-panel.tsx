import { RobotOutlined } from "@ant-design/icons";
import { App, Empty } from "antd";
import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuModal,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";

import * as aiCandidateService from "./ai-candidate-service";
import * as contentService from "./classics-content-service";
import * as aiRefinementTaskService from "./ai-refinement-task-service";
import type { AiCandidateRecord } from "./ai-candidate-types";
import type { ClassicsContentTagRecord, ClassicsContentType } from "./classics-content-types";

interface ClassicsContentTagAiPanelProps {
    canCreateTask?: boolean;
    contentId: string;
    contentType: ClassicsContentType;
    creatingTask?: boolean;
    onChanged?: () => void | Promise<void>;
    onCreateTask?: () => void;
}

const REJECT_ERROR_TYPE = "USER_REJECTED";
const REJECT_ERROR_MESSAGE = "用户已放弃该 AI 标签候选";

const normalizeTagName = (value?: string | null) => value?.trim() || "";

const uniqueTagNames = (values: Array<string | null | undefined>) => {
    const seen = new Set<string>();
    return values.map(normalizeTagName).filter((value) => {
        if (!value) {
            return false;
        }
        const key = value.toLocaleLowerCase();
        if (seen.has(key)) {
            return false;
        }
        seen.add(key);
        return true;
    });
};

const parseCandidateTags = (payload?: string | null) => {
    if (!payload?.trim()) {
        return [];
    }
    try {
        const parsed = JSON.parse(payload);
        if (Array.isArray(parsed)) {
            return uniqueTagNames(parsed.map((tag) => String(tag ?? "")));
        }
        if (Array.isArray(parsed?.tags)) {
            return uniqueTagNames(parsed.tags.map((tag: unknown) => String(tag ?? "")));
        }
    } catch {
        return uniqueTagNames(payload.split(/\r?\n|,|，/));
    }
    return [];
};

const tagKey = (tagName: string) => tagName.toLocaleLowerCase();

const getCandidateStableId = (candidate: AiCandidateRecord) => {
    return candidate.candidateIdText || String(candidate.candidateId);
};

export const ClassicsContentTagAiPanel = ({
    canCreateTask = true,
    contentId,
    contentType,
    creatingTask = false,
    onChanged,
    onCreateTask
}: ClassicsContentTagAiPanelProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [open, setOpen] = useState(false);
    const [handlingCandidateId, setHandlingCandidateId] = useState<string | null>(null);

    const candidatesQuery = useQuery({
        queryKey: ["ai", "candidates", contentType, contentId, null],
        queryFn: () =>
            aiCandidateService.list({
                contentId,
                contentType,
                status: "PENDING",
                objectId: null
            }),
        enabled: open && Boolean(contentId),
        retry: false
    });

    const tagsQuery = useQuery({
        queryKey: ["classics", "content", "tags", contentType, contentId],
        queryFn: () => contentService.listTags({ contentId, contentType }),
        enabled: open && Boolean(contentId),
        retry: false
    });

    const pendingTagCandidates = useMemo(
        () =>
            (candidatesQuery.data || []).filter(
                (candidate) =>
                    candidate.status === "PENDING" &&
                    aiRefinementTaskService.getNormalizedTaskCapability(candidate.capability) ===
                        "tags"
            ),
        [candidatesQuery.data]
    );

    const refresh = async () => {
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: ["ai", "candidates", contentType, contentId]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "tags", contentType, contentId]
            })
        ]);
        if (onChanged) {
            await onChanged();
        }
    };

    const addTag = async (tagName: string, source: string = "AI") => {
        await contentService.addTag({
            contentId,
            contentType,
            tagNameSnapshot: tagName,
            source,
            status: "ACTIVE"
        });
    };

    const deleteTag = async (tag: ClassicsContentTagRecord) => {
        if (tag.id) {
            await contentService.deleteTag({ id: tag.id });
        }
    };

    const markCandidateApplied = async (candidate: AiCandidateRecord, appliedTags: string[]) => {
        if (!appliedTags.length) {
            await aiCandidateService.reject({
                candidateId: getCandidateStableId(candidate),
                errorType: "NO_TAG_CHANGE",
                errorMessage: "候选标签已存在，追加操作未产生新标签"
            });
            return;
        }
        await aiCandidateService.apply({
            candidateId: getCandidateStableId(candidate),
            contentId,
            contentType,
            capability: "tags",
            objectId: candidate.objectId,
            resultFormat: candidate.resultFormat || "STRUCTURED",
            resultPayload: JSON.stringify({ tags: appliedTags }),
            changeSummary: "AI 应用：标签"
        });
    };

    const readCandidateTagsOrThrow = (candidate: AiCandidateRecord) => {
        const tags = parseCandidateTags(candidate.resultPayload);
        if (!tags.length) {
            throw new Error("AI 候选标签为空");
        }
        return tags;
    };

    const coverMutation = useMutation({
        mutationFn: async (candidate: AiCandidateRecord) => {
            setHandlingCandidateId(getCandidateStableId(candidate));
            const candidateTags = readCandidateTagsOrThrow(candidate);
            const desiredKeys = new Set(candidateTags.map(tagKey));
            const beforeApplyTags = await contentService.listTags({ contentId, contentType });
            await Promise.all(
                beforeApplyTags
                    .filter((tag) => (tag.status || "ACTIVE") !== "REMOVED")
                    .filter((tag) => desiredKeys.has(tagKey(normalizeTagName(tag.tagNameSnapshot))))
                    .map(deleteTag)
            );
            await markCandidateApplied(candidate, candidateTags);
            const currentTags = await contentService.listTags({ contentId, contentType });
            await Promise.all(
                currentTags
                    .filter((tag) => (tag.status || "ACTIVE") !== "REMOVED")
                    .filter(
                        (tag) => !desiredKeys.has(tagKey(normalizeTagName(tag.tagNameSnapshot)))
                    )
                    .map(deleteTag)
            );
        },
        onSuccess: async () => {
            await refresh();
            messageApi.success("AI 标签已覆盖当前条目标签");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "覆盖 AI 标签失败");
        },
        onSettled: () => setHandlingCandidateId(null)
    });

    const appendMutation = useMutation({
        mutationFn: async (candidate: AiCandidateRecord) => {
            setHandlingCandidateId(getCandidateStableId(candidate));
            const existingTags = (tagsQuery.data || []).filter(
                (tag) => (tag.status || "ACTIVE") !== "REMOVED"
            );
            const existingByName = new Map(
                existingTags.map((tag) => [tagKey(normalizeTagName(tag.tagNameSnapshot)), tag])
            );
            const existingNames = uniqueTagNames(existingTags.map((tag) => tag.tagNameSnapshot));
            const candidateTags = readCandidateTagsOrThrow(candidate);
            const candidateTagsToApply = candidateTags.filter(
                (tagName) => !existingByName.has(tagKey(tagName))
            );
            await markCandidateApplied(candidate, candidateTagsToApply);
            const targetNames = uniqueTagNames([...existingNames, ...candidateTags]);
            const refreshedTags = await contentService.listTags({ contentId, contentType });
            const refreshedKeys = new Set(
                refreshedTags
                    .filter((tag) => (tag.status || "ACTIVE") !== "REMOVED")
                    .map((tag) => tagKey(normalizeTagName(tag.tagNameSnapshot)))
            );
            await Promise.all(
                targetNames
                    .filter((tagName) => !refreshedKeys.has(tagKey(tagName)))
                    .map((tagName) =>
                        addTag(
                            tagName,
                            existingByName.get(tagKey(tagName))?.source === "AI" ? "AI" : "MANUAL"
                        )
                    )
            );
        },
        onSuccess: async () => {
            await refresh();
            messageApi.success("AI 标签已追加到当前条目");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "追加 AI 标签失败");
        },
        onSettled: () => setHandlingCandidateId(null)
    });

    const discardMutation = useMutation({
        mutationFn: async (candidate: AiCandidateRecord) => {
            setHandlingCandidateId(getCandidateStableId(candidate));
            await aiCandidateService.reject({
                candidateId: getCandidateStableId(candidate),
                errorType: REJECT_ERROR_TYPE,
                errorMessage: REJECT_ERROR_MESSAGE
            });
        },
        onSuccess: async () => {
            await refresh();
            messageApi.success("AI 标签候选已放弃");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "放弃 AI 标签失败");
        },
        onSettled: () => setHandlingCandidateId(null)
    });

    return (
        <>
            <KuzhambuButton
                testId="classics-common-content-tag-ai-button"
                icon={<RobotOutlined />}
                onClick={() => setOpen(true)}
            >
                AI 生成
            </KuzhambuButton>
            <KuzhambuModal
                testId="classics-content-tag-ai-modal"
                footer={null}
                open={open}
                title="AI 生成标签"
                onCancel={() => setOpen(false)}
            >
                <KuzhambuSpace orientation="vertical" size={16}>
                    <KuzhambuButton
                        testId="classics-content-tag-ai-create-task-button"
                        disabled={!canCreateTask}
                        icon={<RobotOutlined />}
                        loading={creatingTask}
                        type="primary"
                        onClick={onCreateTask}
                    >
                        创建标签任务
                    </KuzhambuButton>

                    {candidatesQuery.isError ? (
                        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="候选加载失败" />
                    ) : pendingTagCandidates.length ? (
                        pendingTagCandidates.map((candidate) => {
                            const candidateStableId = getCandidateStableId(candidate);
                            const candidateTags = parseCandidateTags(candidate.resultPayload);
                            const loading = handlingCandidateId === candidateStableId;
                            return (
                                <KuzhambuCard size="small" key={candidateStableId}>
                                    <KuzhambuSpace orientation="vertical" size={12}>
                                        <KuzhambuSpace wrap>
                                            {candidateTags.map((tagName) => (
                                                <KuzhambuTag key={tagName} type="accent">
                                                    {tagName}
                                                </KuzhambuTag>
                                            ))}
                                        </KuzhambuSpace>
                                        <KuzhambuSpace wrap>
                                            <KuzhambuButton
                                                testId="classics-content-tag-ai-cover-button"
                                                disabled={!candidateTags.length}
                                                loading={loading && coverMutation.isPending}
                                                type="primary"
                                                onClick={() => coverMutation.mutate(candidate)}
                                            >
                                                覆盖
                                            </KuzhambuButton>
                                            <KuzhambuButton
                                                testId="classics-content-tag-ai-append-button"
                                                disabled={!candidateTags.length}
                                                loading={loading && appendMutation.isPending}
                                                onClick={() => appendMutation.mutate(candidate)}
                                            >
                                                追加
                                            </KuzhambuButton>
                                            <KuzhambuButton
                                                testId="classics-content-tag-ai-discard-button"
                                                loading={loading && discardMutation.isPending}
                                                onClick={() => discardMutation.mutate(candidate)}
                                            >
                                                放弃
                                            </KuzhambuButton>
                                        </KuzhambuSpace>
                                    </KuzhambuSpace>
                                </KuzhambuCard>
                            );
                        })
                    ) : (
                        <Empty
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                            description="暂无 AI 标签候选"
                        />
                    )}
                </KuzhambuSpace>
            </KuzhambuModal>
        </>
    );
};
