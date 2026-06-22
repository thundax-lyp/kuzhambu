import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Tabs } from "antd";
import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { CategoryEdit } from "./components/category-edit";
import { CategoryTable } from "./components/category-table";
import { TagEdit } from "./components/tag-edit";
import { TagTable } from "./components/tag-table";
import * as service from "./taxonomy-service";
import type {
    TagCategoryCreateCommand,
    TagCategoryUpdateCommand,
    TagCreateCommand,
    TagUpdateCommand
} from "./taxonomy-service";
import type {
    TagCategoryPageQuery,
    TagCategoryRecord,
    TagPageQuery,
    TagRecord
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
    const [editingCategory, setEditingCategory] = useState<TagCategoryRecord | null>(null);
    const [editingTag, setEditingTag] = useState<TagRecord | null>(null);
    const [categoryEditorOpen, setCategoryEditorOpen] = useState(false);
    const [tagEditorOpen, setTagEditorOpen] = useState(false);

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

    const categoryPage = categoryPageQuery.data;
    const tagPage = tagPageQuery.data;
    const categories = categoryPage?.records || [];
    const tags = tagPage?.records || [];

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

    const openEditTag = (tag: TagRecord) => {
        setEditingTag(tag);
        setTagEditorOpen(true);
    };

    const closeTagEditor = () => {
        if (saveTagMutation.isPending) {
            return;
        }
        setTagEditorOpen(false);
        setEditingTag(null);
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
                                <TagTable
                                    canEditTag={canEditTaxonomy}
                                    loading={tagPageQuery.isFetching}
                                    query={tagQuery}
                                    tags={tags}
                                    totalCount={readTotalCount(tagPage)}
                                    onAdd={openCreateTag}
                                    onChange={setTagQuery}
                                    onEdit={openEditTag}
                                    onRefresh={() => tagPageQuery.refetch()}
                                    onStatusChange={(request) => tagStatusMutation.mutate(request)}
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
        </div>
    );
};
