import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Tabs } from "antd";
import { useMemo } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { normalizeId } from "@/types/id";
import { CategoryEditDrawer } from "./category-edit-drawer";
import { TagBatchReviewPanel } from "./tag-batch-review-panel";
import { TagDetailDrawer } from "./tag-detail-drawer";
import { TagEditDrawer } from "./tag-edit-drawer";
import { TagExtractionDrawer } from "./tag-extraction-drawer";
import { TagBatchMergePanel } from "./tag-batch-merge-panel";
import { TaxonomyCategorySection } from "./taxonomy-category-section";
import { TaxonomyGovernanceSection } from "./taxonomy-governance-section";
import { TaxonomyTagSection } from "./taxonomy-tag-section";
import * as service from "./taxonomy-service";
import type {
    TagBatchMergeCommand,
    TagBatchReviewCommand,
    TagMergeCommand,
    TagAliasCreateCommand,
    TagAliasRemoveCommand,
    TagCategoryCreateCommand,
    TagCategoryUpdateCommand,
    TagCreateCommand,
    TagDeprecateCommand,
    TagGovernanceMetricsQuery,
    TagReviewCommand,
    TagUpdateCommand
} from "./taxonomy-service";
import type { TagCategoryRecord, TagRecord } from "./taxonomy-types";
import { useTaxonomyEditors } from "./hooks/use-taxonomy-editors";
import "./taxonomy-page.css";

const TAXONOMY_TAB_ITEMS = [
    { key: "categories", label: "标签分类" },
    { key: "tags", label: "统一标签" },
    { key: "reviews", label: "待审核标签", description: "待审核标签列表" }
];

const readTotalCount = <TRecord,>(
    page?: { count: number; totalCount?: number; records: TRecord[] } | null
) => {
    return page?.count ?? page?.totalCount ?? 0;
};

const DEFAULT_GOVERNANCE_METRICS_QUERY: TagGovernanceMetricsQuery = {
    topLimit: 10,
    recentMonths: 6
};

export const TaxonomyPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const canViewTaxonomy = hasPermission("knowledge:taxonomy:view");
    const canEditTaxonomy = hasPermission("knowledge:taxonomy:edit");
    const canViewAiPrompt = hasPermission("ai:prompt:view");
    const {
        activeTabKey,
        categoryEditDrawerOpen,
        categoryQuery,
        editingCategory,
        editingTag,
        removingAliasId,
        reviewQuery,
        selectedReviewRowKeys,
        selectedTag,
        selectedTagRowKeys,
        setActiveTabKey,
        setCategoryEditDrawerOpen,
        setCategoryQuery,
        setEditingCategory,
        setEditingTag,
        setRemovingAliasId,
        setReviewQuery,
        setSelectedReviewRowKeys,
        setSelectedTag,
        setSelectedTagRowKeys,
        setTagBatchMergeOpen,
        setTagBatchMergePreview,
        setTagBatchReviewDecision,
        setTagBatchReviewOpen,
        setTagDetailDrawerOpen,
        setTagDetailReviewMode,
        setTagEditDrawerOpen,
        setTagExtractionOpen,
        setTagExtractionResult,
        setTagMergePreview,
        setTagQuery,
        tagBatchMergeOpen,
        tagBatchMergePreview,
        tagBatchReviewDecision,
        tagBatchReviewOpen,
        tagDetailDrawerOpen,
        tagDetailReviewMode,
        tagEditDrawerOpen,
        tagExtractionOpen,
        tagExtractionResult,
        tagMergePreview,
        tagQuery
    } = useTaxonomyEditors();

    const categoryPageQuery = useQuery({
        queryKey: ["knowledge", "taxonomy", "categories", categoryQuery],
        queryFn: () => service.pageCategories(categoryQuery),
        enabled: canViewTaxonomy || canEditTaxonomy,
        retry: false
    });
    const tagPageQuery = useQuery({
        queryKey: ["knowledge", "taxonomy", "tags", tagQuery],
        queryFn: () => service.pageTags(tagQuery),
        enabled: canViewTaxonomy || canEditTaxonomy,
        retry: false
    });
    const reviewPageQuery = useQuery({
        queryKey: ["knowledge", "taxonomy", "reviews", reviewQuery],
        queryFn: () => service.pagePendingTags(reviewQuery),
        enabled: canViewTaxonomy || canEditTaxonomy,
        retry: false
    });
    const tagDetailQuery = useQuery({
        queryKey: ["knowledge", "taxonomy", "tag-detail", selectedTag?.id],
        queryFn: () => service.getTagDetail({ tagId: selectedTag?.id || "" }),
        enabled: tagDetailDrawerOpen && Boolean(selectedTag?.id),
        retry: false
    });
    const governanceMetricsQuery = useQuery({
        queryKey: ["knowledge", "taxonomy", "metrics", DEFAULT_GOVERNANCE_METRICS_QUERY],
        queryFn: () => service.getTagGovernanceMetrics(DEFAULT_GOVERNANCE_METRICS_QUERY),
        enabled: (canViewTaxonomy || canEditTaxonomy) && activeTabKey === "tags",
        retry: false
    });
    const tagExtractionPromptVersionsQuery = useQuery({
        queryKey: ["knowledge", "taxonomy", "tag-extraction", "prompt-versions"],
        queryFn: service.listTagExtractionPromptVersions,
        enabled: tagExtractionOpen && canViewAiPrompt,
        retry: false
    });

    const saveCategoryMutation = useMutation({
        mutationFn: (request: TagCategoryCreateCommand | TagCategoryUpdateCommand) =>
            editingCategory
                ? service.updateCategory(request as TagCategoryUpdateCommand)
                : service.createCategory(request as TagCategoryCreateCommand),
        onSuccess: async () => {
            setCategoryEditDrawerOpen(false);
            setEditingCategory(null);
            await queryClient.invalidateQueries({
                queryKey: ["knowledge", "taxonomy", "categories"]
            });
            messageApi.success("标签分类已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "标签分类保存失败");
        }
    });
    const categoryStatusMutation = useMutation({
        mutationFn: service.changeCategoryStatus,
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: ["knowledge", "taxonomy", "categories"]
            });
            messageApi.success("标签分类状态已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "标签分类状态更新失败");
        }
    });
    const saveTagMutation = useMutation({
        mutationFn: (request: TagCreateCommand | TagUpdateCommand) =>
            editingTag
                ? service.updateTag(request as TagUpdateCommand)
                : service.createTag(request as TagCreateCommand),
        onSuccess: async () => {
            setTagEditDrawerOpen(false);
            setEditingTag(null);
            await queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "tags"] });
            messageApi.success("统一标签已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "统一标签保存失败");
        }
    });
    const tagStatusMutation = useMutation({
        mutationFn: service.changeTagStatus,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "tags"] });
            messageApi.success("统一标签状态已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "统一标签状态更新失败");
        }
    });
    const previewTagMergeMutation = useMutation({
        mutationFn: service.previewTagMergeImpact,
        onSuccess: (preview) => {
            setTagMergePreview(preview);
            messageApi.success("标签合并影响已生成");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "标签合并预览失败");
        }
    });
    const applyTagMergeMutation = useMutation({
        mutationFn: service.applyTagMerge,
        onSuccess: async () => {
            setTagMergePreview(null);
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "tags"] }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "taxonomy", "metrics"]
                }),
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "reviews"] }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "taxonomy", "tag-detail"]
                })
            ]);
            messageApi.success("标签合并已完成");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "标签合并失败");
        }
    });
    const previewTagBatchMergeMutation = useMutation({
        mutationFn: service.previewTagBatchMergeImpact,
        onSuccess: (preview) => {
            setTagBatchMergePreview(preview);
            messageApi.success("批量合并影响已生成");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量合并预览失败");
        }
    });
    const applyTagBatchMergeMutation = useMutation({
        mutationFn: service.applyTagBatchMerge,
        onSuccess: async () => {
            setTagBatchMergeOpen(false);
            setTagBatchMergePreview(null);
            setSelectedTagRowKeys([]);
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "tags"] }),
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "reviews"] }),
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "metrics"] }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "taxonomy", "tag-detail"]
                })
            ]);
            messageApi.success("批量合并已完成");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量合并失败");
        }
    });
    const deprecateTagMutation = useMutation({
        mutationFn: service.deprecateTag,
        onSuccess: async () => {
            setTagDetailDrawerOpen(false);
            setSelectedTag(null);
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "tags"] }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "taxonomy", "metrics"]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "taxonomy", "tag-detail"]
                })
            ]);
            messageApi.success("标签已废弃");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "标签废弃失败");
        }
    });
    const deprecateBatchTagsMutation = useMutation({
        mutationFn: service.deprecateBatchTags,
        onSuccess: async () => {
            setSelectedTagRowKeys([]);
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "tags"] }),
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "reviews"] }),
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "metrics"] }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "taxonomy", "tag-detail"]
                })
            ]);
            messageApi.success("批量废弃已完成");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量废弃失败");
        }
    });
    const reviewTagMutation = useMutation({
        mutationFn: service.reviewTag,
        onSuccess: async () => {
            setTagDetailDrawerOpen(false);
            setTagDetailReviewMode(false);
            setSelectedTag(null);
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "reviews"] }),
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "tags"] }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "taxonomy", "tag-detail"]
                })
            ]);
            messageApi.success("标签审核已完成");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "标签审核失败");
        }
    });
    const reviewBatchTagsMutation = useMutation({
        mutationFn: service.reviewBatchTags,
        onSuccess: async () => {
            setTagBatchReviewOpen(false);
            setSelectedReviewRowKeys([]);
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "reviews"] }),
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "tags"] }),
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "metrics"] }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "taxonomy", "tag-detail"]
                })
            ]);
            messageApi.success("批量审核已完成");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量审核失败");
        }
    });
    const createAliasMutation = useMutation({
        mutationFn: service.createTagAlias,
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "taxonomy", "tag-detail"]
                }),
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "tags"] })
            ]);
            messageApi.success("标签别名已新增");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "标签别名新增失败");
        }
    });
    const removeAliasMutation = useMutation({
        mutationFn: service.removeTagAlias,
        onMutate: (request) => {
            setRemovingAliasId(request.id);
        },
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: ["knowledge", "taxonomy", "tag-detail"]
            });
            messageApi.success("标签别名已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "标签别名删除失败");
        },
        onSettled: () => {
            setRemovingAliasId(null);
        }
    });
    const requestTagExtractionMutation = useMutation({
        mutationFn: service.requestTagExtraction,
        onSuccess: (result) => {
            setTagExtractionResult(result);
            messageApi.success("AI 标签候选已生成");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "AI 标签抽取失败");
        }
    });
    const applyExtractedTagsMutation = useMutation({
        mutationFn: service.applyExtractedTags,
        onSuccess: async () => {
            setTagExtractionOpen(false);
            setTagExtractionResult(null);
            setActiveTabKey("reviews");
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "tags"] }),
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "reviews"] }),
                queryClient.invalidateQueries({ queryKey: ["knowledge", "taxonomy", "metrics"] })
            ]);
            messageApi.success("AI 标签候选已进入待审核");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "AI 标签候选应用失败");
        }
    });

    const categoryPage = categoryPageQuery.data;
    const tagPage = tagPageQuery.data;
    const reviewPage = reviewPageQuery.data;
    const categories = categoryPage?.records || [];
    const tags = tagPage?.records || [];
    const reviewTags = reviewPage?.records || [];
    const selectedTagIds = selectedTagRowKeys.map(String);
    const selectedTags = tags.filter((tag) => selectedTagIds.includes(normalizeId(tag.id)));
    const candidateTargetTags = tags.filter((tag) => !selectedTagIds.includes(normalizeId(tag.id)));
    const selectedReviewIds = selectedReviewRowKeys.map(String);
    const selectedReviewTags = reviewTags.filter((tag) =>
        selectedReviewIds.includes(normalizeId(tag.id))
    );
    const tagExtractionPromptVersionOptions = useMemo(
        () =>
            (tagExtractionPromptVersionsQuery.data || []).map((version) => ({
                label: `${version.templateName || "知识标签提取"} / v${version.versionNo ?? "-"}`,
                value: version.id || ""
            })),
        [tagExtractionPromptVersionsQuery.data]
    );

    const openCreateCategoryDrawer = () => {
        setEditingCategory(null);
        setCategoryEditDrawerOpen(true);
    };

    const openEditCategoryDrawer = (category: TagCategoryRecord) => {
        setEditingCategory(category);
        setCategoryEditDrawerOpen(true);
    };

    const closeCategoryEditDrawer = () => {
        if (saveCategoryMutation.isPending) {
            return;
        }
        setCategoryEditDrawerOpen(false);
        setEditingCategory(null);
    };

    const openCreateTagDrawer = () => {
        setEditingTag(null);
        setTagEditDrawerOpen(true);
    };

    const openTagExtractionDrawer = () => {
        setTagExtractionResult(null);
        setTagExtractionOpen(true);
    };

    const openEditTagDrawer = (tag: TagRecord) => {
        setEditingTag(tag);
        setTagEditDrawerOpen(true);
    };

    const openTagDetailDrawer = (tag: TagRecord, reviewMode = false) => {
        setSelectedTag(tag);
        setTagDetailReviewMode(reviewMode);
        setTagDetailDrawerOpen(true);
    };

    const closeTagEditDrawer = () => {
        if (saveTagMutation.isPending) {
            return;
        }
        setTagEditDrawerOpen(false);
        setEditingTag(null);
    };

    const closeTagDetailDrawer = () => {
        if (reviewTagMutation.isPending || createAliasMutation.isPending) {
            return;
        }
        setTagDetailDrawerOpen(false);
        setTagDetailReviewMode(false);
        setSelectedTag(null);
        setRemovingAliasId(null);
    };

    const closeTagExtractionDrawer = () => {
        if (requestTagExtractionMutation.isPending || applyExtractedTagsMutation.isPending) {
            return;
        }
        setTagExtractionOpen(false);
        setTagExtractionResult(null);
    };

    const createAlias = async (request: TagAliasCreateCommand) => {
        await createAliasMutation.mutateAsync(request);
    };

    const removeAlias = (request: TagAliasRemoveCommand) => {
        removeAliasMutation.mutate(request);
    };

    const previewTagMergeImpact = (request: TagMergeCommand) => {
        previewTagMergeMutation.mutate(request);
    };

    const applyTagMerge = (request: TagMergeCommand) => {
        applyTagMergeMutation.mutate(request);
    };

    const openTagBatchMergeDrawer = () => {
        setTagBatchMergePreview(null);
        setTagBatchMergeOpen(true);
    };

    const closeTagBatchMergeDrawer = () => {
        if (previewTagBatchMergeMutation.isPending || applyTagBatchMergeMutation.isPending) {
            return;
        }
        setTagBatchMergeOpen(false);
        setTagBatchMergePreview(null);
    };

    const previewTagBatchMergeImpact = (request: TagBatchMergeCommand) => {
        previewTagBatchMergeMutation.mutate(request);
    };

    const applyTagBatchMerge = (request: TagBatchMergeCommand) => {
        applyTagBatchMergeMutation.mutate(request);
    };

    const deprecateTag = (request: TagDeprecateCommand) => {
        deprecateTagMutation.mutate(request);
    };

    const confirmDeprecateBatchTags = () => {
        const tagIds = selectedTagIds;
        if (tagIds.length === 0) {
            return;
        }
        confirm.danger({
            title: "批量废弃标签",
            message: `确认废弃已选择的 ${tagIds.length} 个标签？`,
            okText: "批量废弃",
            onConfirm: () => {
                deprecateBatchTagsMutation.mutate({ tagIds });
            }
        });
    };

    const openTagBatchReviewDrawer = (decision: "APPROVE" | "REJECT") => {
        setTagBatchReviewDecision(decision);
        setTagBatchReviewOpen(true);
    };

    const closeTagBatchReviewDrawer = () => {
        if (reviewBatchTagsMutation.isPending) {
            return;
        }
        setTagBatchReviewOpen(false);
    };

    const reviewBatchTags = (request: TagBatchReviewCommand) => {
        reviewBatchTagsMutation.mutate(request);
    };

    return (
        <div className="taxonomy-page knowledge-taxonomy-page">
            <Tabs
                activeKey={activeTabKey}
                onChange={setActiveTabKey}
                items={TAXONOMY_TAB_ITEMS.map(({ key, label, description }) => {
                    if (key === "categories") {
                        return {
                            key,
                            label,
                            children: (
                                <TaxonomyCategorySection
                                    canEditTaxonomy={canEditTaxonomy}
                                    categories={categories}
                                    loading={categoryPageQuery.isFetching}
                                    totalCount={readTotalCount(categoryPage)}
                                    query={categoryQuery}
                                    onAdd={openCreateCategoryDrawer}
                                    onChange={setCategoryQuery}
                                    onEdit={openEditCategoryDrawer}
                                    onRefresh={() => categoryPageQuery.refetch()}
                                    onStatusChange={(request) =>
                                        categoryStatusMutation.mutate(request)
                                    }
                                />
                            )
                        };
                    }

                    if (key === "tags") {
                        return {
                            key,
                            label,
                            children: (
                                <TaxonomyTagSection
                                    applyingMerge={applyTagMergeMutation.isPending}
                                    canEditTaxonomy={canEditTaxonomy}
                                    loading={tagPageQuery.isFetching}
                                    metrics={governanceMetricsQuery.data}
                                    metricsLoading={governanceMetricsQuery.isFetching}
                                    preview={tagMergePreview}
                                    previewingMerge={previewTagMergeMutation.isPending}
                                    query={tagQuery}
                                    selectedRowKeys={selectedTagRowKeys}
                                    tags={tags}
                                    totalCount={readTotalCount(tagPage)}
                                    onAdd={openCreateTagDrawer}
                                    onApplyMerge={applyTagMerge}
                                    onBatchDeprecate={confirmDeprecateBatchTags}
                                    onBatchMerge={openTagBatchMergeDrawer}
                                    onChange={setTagQuery}
                                    onEdit={openEditTagDrawer}
                                    onExtract={openTagExtractionDrawer}
                                    onOpenDetail={(tag) => openTagDetailDrawer(tag)}
                                    onPreviewMerge={previewTagMergeImpact}
                                    onRefresh={() => tagPageQuery.refetch()}
                                    onRefreshMetrics={() => governanceMetricsQuery.refetch()}
                                    onSelectedRowKeysChange={setSelectedTagRowKeys}
                                    onStatusChange={(request) => tagStatusMutation.mutate(request)}
                                />
                            )
                        };
                    }

                    if (key === "reviews") {
                        return {
                            key,
                            label,
                            children: (
                                <TaxonomyGovernanceSection
                                    loading={reviewPageQuery.isFetching}
                                    query={reviewQuery}
                                    selectedRowKeys={selectedReviewRowKeys}
                                    tags={reviewTags}
                                    totalCount={readTotalCount(reviewPage)}
                                    onBatchApprove={() => openTagBatchReviewDrawer("APPROVE")}
                                    onBatchReject={() => openTagBatchReviewDrawer("REJECT")}
                                    onChange={setReviewQuery}
                                    onOpenReview={(tag) => openTagDetailDrawer(tag, true)}
                                    onRefresh={() => reviewPageQuery.refetch()}
                                    onSelectedRowKeysChange={setSelectedReviewRowKeys}
                                />
                            )
                        };
                    }

                    return {
                        key,
                        label,
                        children: <div className="knowledge-taxonomy-empty">{description}</div>
                    };
                })}
            />

            <CategoryEditDrawer
                open={categoryEditDrawerOpen}
                category={editingCategory}
                saving={saveCategoryMutation.isPending}
                onClose={closeCategoryEditDrawer}
                onCreate={(request) => saveCategoryMutation.mutate(request)}
                onSave={(request) => saveCategoryMutation.mutate(request)}
            />

            <TagEditDrawer
                categories={categories}
                open={tagEditDrawerOpen}
                saving={saveTagMutation.isPending}
                tag={editingTag}
                onClose={closeTagEditDrawer}
                onCreate={(request) => saveTagMutation.mutate(request)}
                onSave={(request) => saveTagMutation.mutate(request)}
            />

            <TagDetailDrawer
                canEditAliases={canEditTaxonomy}
                canDeprecateTag={canEditTaxonomy && selectedTag?.status !== "DISABLED"}
                creatingAlias={createAliasMutation.isPending}
                deprecating={deprecateTagMutation.isPending}
                open={tagDetailDrawerOpen}
                loading={tagDetailQuery.isFetching}
                removingAliasId={removingAliasId}
                reviewMode={tagDetailReviewMode}
                reviewing={reviewTagMutation.isPending}
                tagDetail={tagDetailQuery.data}
                onCreateAlias={createAlias}
                onClose={closeTagDetailDrawer}
                onDeprecate={deprecateTag}
                onApprove={(request: TagReviewCommand) => reviewTagMutation.mutate(request)}
                onReject={(request: TagReviewCommand) => reviewTagMutation.mutate(request)}
                onRemoveAlias={removeAlias}
            />

            {tagExtractionOpen ? (
                <TagExtractionDrawer
                    open={tagExtractionOpen}
                    canCustomizePromptVersion={canViewAiPrompt}
                    extracting={requestTagExtractionMutation.isPending}
                    applying={applyExtractedTagsMutation.isPending}
                    promptVersionOptions={tagExtractionPromptVersionOptions}
                    promptVersionsLoading={tagExtractionPromptVersionsQuery.isFetching}
                    result={tagExtractionResult}
                    onClose={closeTagExtractionDrawer}
                    onExtract={(request) => requestTagExtractionMutation.mutate(request)}
                    onApply={(request) => applyExtractedTagsMutation.mutate(request)}
                    onResetResult={() => setTagExtractionResult(null)}
                />
            ) : null}

            {tagBatchMergeOpen ? (
                <TagBatchMergePanel
                    applying={applyTagBatchMergeMutation.isPending}
                    candidateTargetTags={candidateTargetTags}
                    open={tagBatchMergeOpen}
                    preview={tagBatchMergePreview}
                    previewing={previewTagBatchMergeMutation.isPending}
                    selectedSourceTagIds={selectedTagIds}
                    selectedSourceTags={selectedTags}
                    onApply={applyTagBatchMerge}
                    onClose={closeTagBatchMergeDrawer}
                    onPreview={previewTagBatchMergeImpact}
                />
            ) : null}

            {tagBatchReviewOpen ? (
                <TagBatchReviewPanel
                    categories={categories}
                    decision={tagBatchReviewDecision}
                    open={tagBatchReviewOpen}
                    reviewing={reviewBatchTagsMutation.isPending}
                    selectedTags={selectedReviewTags}
                    onClose={closeTagBatchReviewDrawer}
                    onSubmit={reviewBatchTags}
                />
            ) : null}
        </div>
    );
};
