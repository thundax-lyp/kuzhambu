import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Tabs } from "antd";
import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { CategoryEdit } from "./components/category-edit";
import { CategoryTable } from "./components/category-table";
import { SynonymEdit } from "./components/synonym-edit";
import { SynonymTable } from "./components/synonym-table";
import { TagDetailDrawer } from "./components/tag-detail-drawer";
import { TagEdit } from "./components/tag-edit";
import { TagExtractionDrawer } from "./components/tag-extraction-drawer";
import { TagGovernanceMetricsPanel } from "./components/tag-governance-metrics-panel";
import { TagMergePanel } from "./components/tag-merge-panel";
import { TagReviewTable } from "./components/tag-review-table";
import { TagTable } from "./components/tag-table";
import * as service from "./taxonomy-service";
import type {
    TagMergeCommand,
    TagAliasCreateCommand,
    TagAliasRemoveCommand,
    TagCategoryCreateCommand,
    TagCategoryUpdateCommand,
    TagCreateCommand,
    TagDeprecateCommand,
    TagGovernanceMetricsQuery,
    TagReviewCommand,
    TagUpdateCommand,
    SynonymCreateCommand,
    SynonymRemoveCommand,
    SynonymUpdateCommand
} from "./taxonomy-service";
import type {
    SynonymPageQuery,
    SynonymRecord,
    TagCategoryPageQuery,
    TagCategoryRecord,
    TagExtractionResultRecord,
    TagGovernanceMetricsRecord,
    TagMergePreviewRecord,
    TagPageQuery,
    TagRecord,
    TagReviewPageQuery
} from "./taxonomy-types";
import "./taxonomy-page.css";

const TAXONOMY_TAB_ITEMS = [
    { key: "categories", label: "标签分类" },
    { key: "tags", label: "统一标签" },
    { key: "reviews", label: "待审核标签", description: "待审核标签列表" },
    { key: "synonyms", label: "同义词", description: "同义词管理" }
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
    const queryClient = useQueryClient();
    const canViewTaxonomy = hasPermission("knowledge:taxonomy:view");
    const canEditTaxonomy = hasPermission("knowledge:taxonomy:edit");
    const [activeTabKey, setActiveTabKey] = useState("categories");
    const [categoryQuery, setCategoryQuery] = useState<TagCategoryPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [tagQuery, setTagQuery] = useState<TagPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [reviewQuery, setReviewQuery] = useState<TagReviewPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [synonymQuery, setSynonymQuery] = useState<SynonymPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [editingCategory, setEditingCategory] = useState<TagCategoryRecord | null>(null);
    const [editingTag, setEditingTag] = useState<TagRecord | null>(null);
    const [editingSynonym, setEditingSynonym] = useState<SynonymRecord | null>(null);
    const [selectedTag, setSelectedTag] = useState<TagRecord | null>(null);
    const [categoryEditorOpen, setCategoryEditorOpen] = useState(false);
    const [tagEditorOpen, setTagEditorOpen] = useState(false);
    const [synonymEditorOpen, setSynonymEditorOpen] = useState(false);
    const [tagDetailOpen, setTagDetailOpen] = useState(false);
    const [tagDetailReviewMode, setTagDetailReviewMode] = useState(false);
    const [removingAliasId, setRemovingAliasId] = useState<string | null>(null);
    const [tagMergePreview, setTagMergePreview] = useState<TagMergePreviewRecord | null>(null);
    const [tagExtractionOpen, setTagExtractionOpen] = useState(false);
    const [tagExtractionResult, setTagExtractionResult] =
        useState<TagExtractionResultRecord | null>(null);

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
        enabled: tagDetailOpen && Boolean(selectedTag?.id),
        retry: false
    });
    const synonymPageQuery = useQuery({
        queryKey: ["knowledge", "taxonomy", "synonyms", synonymQuery],
        queryFn: () => service.pageSynonyms(synonymQuery),
        enabled: canViewTaxonomy || canEditTaxonomy,
        retry: false
    });
    const governanceMetricsQuery = useQuery({
        queryKey: ["knowledge", "taxonomy", "metrics", DEFAULT_GOVERNANCE_METRICS_QUERY],
        queryFn: () => service.getTagGovernanceMetrics(DEFAULT_GOVERNANCE_METRICS_QUERY),
        enabled: (canViewTaxonomy || canEditTaxonomy) && activeTabKey === "tags",
        retry: false
    });

    const saveCategoryMutation = useMutation({
        mutationFn: (request: TagCategoryCreateCommand | TagCategoryUpdateCommand) =>
            editingCategory
                ? service.updateCategory(request as TagCategoryUpdateCommand)
                : service.createCategory(request as TagCategoryCreateCommand),
        onSuccess: async () => {
            setCategoryEditorOpen(false);
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
            setTagEditorOpen(false);
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
    const deprecateTagMutation = useMutation({
        mutationFn: service.deprecateTag,
        onSuccess: async () => {
            setTagDetailOpen(false);
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
    const reviewTagMutation = useMutation({
        mutationFn: service.reviewTag,
        onSuccess: async () => {
            setTagDetailOpen(false);
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
    const saveSynonymMutation = useMutation({
        mutationFn: (request: SynonymCreateCommand | SynonymUpdateCommand) =>
            editingSynonym
                ? service.updateSynonym(request as SynonymUpdateCommand)
                : service.createSynonym(request as SynonymCreateCommand),
        onSuccess: async () => {
            setSynonymEditorOpen(false);
            setEditingSynonym(null);
            await queryClient.invalidateQueries({
                queryKey: ["knowledge", "taxonomy", "synonyms"]
            });
            messageApi.success("同义词已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "同义词保存失败");
        }
    });
    const synonymStatusMutation = useMutation({
        mutationFn: service.changeSynonymStatus,
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: ["knowledge", "taxonomy", "synonyms"]
            });
            messageApi.success("同义词状态已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "同义词状态更新失败");
        }
    });
    const removeSynonymMutation = useMutation({
        mutationFn: service.removeSynonym,
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: ["knowledge", "taxonomy", "synonyms"]
            });
            messageApi.success("同义词已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "同义词删除失败");
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
    const synonymPage = synonymPageQuery.data;
    const categories = categoryPage?.records || [];
    const tags = tagPage?.records || [];
    const reviewTags = reviewPage?.records || [];
    const synonyms = synonymPage?.records || [];

    const openCreateCategory = () => {
        setEditingCategory(null);
        setCategoryEditorOpen(true);
    };

    const openEditCategory = (category: TagCategoryRecord) => {
        setEditingCategory(category);
        setCategoryEditorOpen(true);
    };

    const closeCategoryEditor = () => {
        if (saveCategoryMutation.isPending) {
            return;
        }
        setCategoryEditorOpen(false);
        setEditingCategory(null);
    };

    const openCreateTag = () => {
        setEditingTag(null);
        setTagEditorOpen(true);
    };

    const openTagExtraction = () => {
        setTagExtractionResult(null);
        setTagExtractionOpen(true);
    };

    const openEditTag = (tag: TagRecord) => {
        setEditingTag(tag);
        setTagEditorOpen(true);
    };

    const openTagDetail = (tag: TagRecord, reviewMode = false) => {
        setSelectedTag(tag);
        setTagDetailReviewMode(reviewMode);
        setTagDetailOpen(true);
    };

    const openCreateSynonym = () => {
        setEditingSynonym(null);
        setSynonymEditorOpen(true);
    };

    const openEditSynonym = (record: SynonymRecord) => {
        setEditingSynonym(record);
        setSynonymEditorOpen(true);
    };

    const closeTagEditor = () => {
        if (saveTagMutation.isPending) {
            return;
        }
        setTagEditorOpen(false);
        setEditingTag(null);
    };

    const closeSynonymEditor = () => {
        if (saveSynonymMutation.isPending) {
            return;
        }
        setSynonymEditorOpen(false);
        setEditingSynonym(null);
    };

    const closeTagDetail = () => {
        if (reviewTagMutation.isPending || createAliasMutation.isPending) {
            return;
        }
        setTagDetailOpen(false);
        setTagDetailReviewMode(false);
        setSelectedTag(null);
        setRemovingAliasId(null);
    };

    const closeTagExtraction = () => {
        if (requestTagExtractionMutation.isPending || applyExtractedTagsMutation.isPending) {
            return;
        }
        setTagExtractionOpen(false);
        setTagExtractionResult(null);
    };

    const createAlias = (request: TagAliasCreateCommand) => {
        createAliasMutation.mutate(request);
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

    const deprecateTag = (request: TagDeprecateCommand) => {
        deprecateTagMutation.mutate(request);
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
                                <CategoryTable
                                    canEditCategory={canEditTaxonomy}
                                    categories={categories}
                                    loading={categoryPageQuery.isFetching}
                                    totalCount={readTotalCount(categoryPage)}
                                    query={categoryQuery}
                                    onAdd={openCreateCategory}
                                    onChange={setCategoryQuery}
                                    onEdit={openEditCategory}
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
                                <div className="knowledge-taxonomy-tag-governance">
                                    <TagMergePanel
                                        applying={applyTagMergeMutation.isPending}
                                        canEditTag={canEditTaxonomy}
                                        preview={tagMergePreview}
                                        previewing={previewTagMergeMutation.isPending}
                                        tags={tags}
                                        onApply={applyTagMerge}
                                        onPreview={previewTagMergeImpact}
                                    />
                                    <TagGovernanceMetricsPanel
                                        loading={governanceMetricsQuery.isFetching}
                                        metrics={
                                            governanceMetricsQuery.data as
                                                TagGovernanceMetricsRecord | null | undefined
                                        }
                                        onRefresh={() => governanceMetricsQuery.refetch()}
                                    />
                                    <TagTable
                                        canEditTag={canEditTaxonomy}
                                        loading={tagPageQuery.isFetching}
                                        query={tagQuery}
                                        tags={tags}
                                        totalCount={readTotalCount(tagPage)}
                                        onAdd={openCreateTag}
                                        onChange={setTagQuery}
                                        onEdit={openEditTag}
                                        onOpenDetail={(tag) => openTagDetail(tag)}
                                        onRefresh={() => tagPageQuery.refetch()}
                                        pageActions={
                                            canEditTaxonomy ? (
                                                <Button type="primary" onClick={openTagExtraction}>
                                                    AI 抽取标签
                                                </Button>
                                            ) : null
                                        }
                                        onStatusChange={(request) =>
                                            tagStatusMutation.mutate(request)
                                        }
                                    />
                                </div>
                            )
                        };
                    }

                    if (key === "reviews") {
                        return {
                            key,
                            label,
                            children: (
                                <TagReviewTable
                                    loading={reviewPageQuery.isFetching}
                                    query={reviewQuery}
                                    tags={reviewTags}
                                    totalCount={readTotalCount(reviewPage)}
                                    onChange={setReviewQuery}
                                    onOpenReview={(tag) => openTagDetail(tag, true)}
                                    onRefresh={() => reviewPageQuery.refetch()}
                                />
                            )
                        };
                    }

                    if (key === "synonyms") {
                        return {
                            key,
                            label,
                            children: (
                                <SynonymTable
                                    canEditSynonym={canEditTaxonomy}
                                    loading={synonymPageQuery.isFetching}
                                    query={synonymQuery}
                                    removing={removeSynonymMutation.isPending}
                                    synonyms={synonyms}
                                    totalCount={readTotalCount(synonymPage)}
                                    onAdd={openCreateSynonym}
                                    onChange={setSynonymQuery}
                                    onEdit={openEditSynonym}
                                    onRefresh={() => synonymPageQuery.refetch()}
                                    onRemove={(request: SynonymRemoveCommand) =>
                                        removeSynonymMutation.mutate(request)
                                    }
                                    onStatusChange={(request) =>
                                        synonymStatusMutation.mutate(request)
                                    }
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

            <CategoryEdit
                open={categoryEditorOpen}
                category={editingCategory}
                saving={saveCategoryMutation.isPending}
                onClose={closeCategoryEditor}
                onCreate={(request) => saveCategoryMutation.mutate(request)}
                onSave={(request) => saveCategoryMutation.mutate(request)}
            />

            <TagEdit
                categories={categories}
                open={tagEditorOpen}
                saving={saveTagMutation.isPending}
                tag={editingTag}
                onClose={closeTagEditor}
                onCreate={(request) => saveTagMutation.mutate(request)}
                onSave={(request) => saveTagMutation.mutate(request)}
            />

            <SynonymEdit
                open={synonymEditorOpen}
                saving={saveSynonymMutation.isPending}
                synonym={editingSynonym}
                onClose={closeSynonymEditor}
                onCreate={(request) => saveSynonymMutation.mutate(request)}
                onSave={(request) => saveSynonymMutation.mutate(request)}
            />

            <TagDetailDrawer
                canEditAliases={canEditTaxonomy}
                canDeprecateTag={canEditTaxonomy && selectedTag?.status !== "DISABLED"}
                creatingAlias={createAliasMutation.isPending}
                deprecating={deprecateTagMutation.isPending}
                open={tagDetailOpen}
                loading={tagDetailQuery.isFetching}
                removingAliasId={removingAliasId}
                reviewMode={tagDetailReviewMode}
                reviewing={reviewTagMutation.isPending}
                tagDetail={tagDetailQuery.data}
                onCreateAlias={createAlias}
                onClose={closeTagDetail}
                onDeprecate={deprecateTag}
                onApprove={(request: TagReviewCommand) => reviewTagMutation.mutate(request)}
                onReject={(request: TagReviewCommand) => reviewTagMutation.mutate(request)}
                onRemoveAlias={removeAlias}
            />

            {tagExtractionOpen ? (
                <TagExtractionDrawer
                    open={tagExtractionOpen}
                    extracting={requestTagExtractionMutation.isPending}
                    applying={applyExtractedTagsMutation.isPending}
                    result={tagExtractionResult}
                    onClose={closeTagExtraction}
                    onExtract={(request) => requestTagExtractionMutation.mutate(request)}
                    onApply={(request) => applyExtractedTagsMutation.mutate(request)}
                    onResetResult={() => setTagExtractionResult(null)}
                />
            ) : null}
        </div>
    );
};
