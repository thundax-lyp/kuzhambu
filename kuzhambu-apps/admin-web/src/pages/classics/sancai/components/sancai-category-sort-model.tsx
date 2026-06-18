import { ArrowDownOutlined, ArrowUpOutlined, MenuOutlined } from "@ant-design/icons";
import { Button, Empty, Modal } from "antd";
import type { DragEvent } from "react";
import { useState } from "react";
import type { SancaiCategoryRecord } from "../sancai-types";

interface SancaiCategorySortModelProps {
    categories: SancaiCategoryRecord[];
    isSubmitting: boolean;
    onCancel: () => void;
    onSubmit: (orderedIds: number[]) => void;
}

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

const readCategoryTypeLabel = (category: SancaiCategoryRecord) => {
    return category.categoryType === "AUXILIARY" ? "辅助内容" : "正式门类";
};

export const SancaiCategorySortModel = ({
    categories,
    isSubmitting,
    onCancel,
    onSubmit
}: SancaiCategorySortModelProps) => {
    const [sortedCategories, setSortedCategories] = useState<SancaiCategoryRecord[]>(() => categories);
    const [draggedCategoryId, setDraggedCategoryId] = useState<number | null>(null);

    const moveInSortForm = (categoryId: number, direction: -1 | 1) => {
        setSortedCategories((currentCategories) => {
            const index = currentCategories.findIndex((category) => category.id === categoryId);
            const nextIndex = index + direction;
            if (index < 0 || nextIndex < 0 || nextIndex >= currentCategories.length) {
                return currentCategories;
            }
            const nextCategories = [...currentCategories];
            const [category] = nextCategories.splice(index, 1);
            nextCategories.splice(nextIndex, 0, category);
            return nextCategories;
        });
    };

    const dropInSortForm = (targetCategoryId: number) => {
        if (draggedCategoryId === null || draggedCategoryId === targetCategoryId) {
            return;
        }
        setSortedCategories((currentCategories) => {
            const draggedCategory = currentCategories.find(
                (category) => category.id === draggedCategoryId
            );
            const targetIndex = currentCategories.findIndex(
                (category) => category.id === targetCategoryId
            );
            if (!draggedCategory || targetIndex < 0) {
                return currentCategories;
            }
            const remainingCategories = currentCategories.filter(
                (category) => category.id !== draggedCategoryId
            );
            remainingCategories.splice(targetIndex, 0, draggedCategory);
            return remainingCategories;
        });
        setDraggedCategoryId(null);
    };

    const persistSort = () => {
        setDraggedCategoryId(null);
        onSubmit(sortedCategories.map((category) => category.id));
    };

    return (
        <Modal
            title="调整三才图会门类顺序"
            open
            okText="保存"
            cancelText="取消"
            confirmLoading={isSubmitting}
            onCancel={onCancel}
            onOk={persistSort}
            okButtonProps={{
                "aria-label": "保存三才图会门类顺序"
            }}
            cancelButtonProps={{
                "aria-label": "取消调整三才图会门类顺序"
            }}
        >
            <SancaiCategorySortList
                categories={sortedCategories}
                draggedCategoryId={draggedCategoryId}
                isSubmitting={isSubmitting}
                onDragOver={(event) => event.preventDefault()}
                onDragStart={setDraggedCategoryId}
                onDrop={dropInSortForm}
                onMove={moveInSortForm}
            />
        </Modal>
    );
};

const SancaiCategorySortList = ({
    categories,
    draggedCategoryId,
    isSubmitting,
    onDragOver,
    onDragStart,
    onDrop,
    onMove
}: {
    categories: SancaiCategoryRecord[];
    draggedCategoryId: number | null;
    isSubmitting: boolean;
    onDragOver: (event: DragEvent<HTMLDivElement>) => void;
    onDragStart: (categoryId: number) => void;
    onDrop: (targetCategoryId: number) => void;
    onMove: (categoryId: number, direction: -1 | 1) => void;
}) => {
    if (!categories.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无可排序门类" />;
    }

    return (
        <div className="sancai-category-sort-list" aria-label="三才图会门类排序列表">
            {categories.map((category, index) => {
                const title = readTitle(category, "门类");
                return (
                    <div
                        className={
                            draggedCategoryId === category.id
                                ? "sancai-category-sort-item sancai-category-sort-item-dragging"
                                : "sancai-category-sort-item"
                        }
                        draggable
                        key={category.id}
                        role="listitem"
                        aria-label={`门类排序项 ${title}`}
                        onDragStart={() => onDragStart(category.id)}
                        onDragOver={onDragOver}
                        onDrop={() => onDrop(category.id)}
                    >
                        <span className="sancai-category-sort-handle" aria-label={`拖动门类 ${title}`}>
                            <MenuOutlined />
                        </span>
                        <span className="sancai-category-sort-title">
                            <span
                                className={
                                    category.categoryType === "AUXILIARY"
                                        ? "sancai-category-type-dot sancai-category-type-dot-auxiliary"
                                        : "sancai-category-type-dot sancai-category-type-dot-formal"
                                }
                                aria-label={`门类类型 ${readCategoryTypeLabel(category)}`}
                            />
                            <span>{title}</span>
                        </span>
                        <div className="sancai-category-sort-actions">
                            <Button
                                aria-label={`上移门类 ${title}`}
                                icon={<ArrowUpOutlined />}
                                disabled={isSubmitting || index === 0}
                                size="small"
                                type="text"
                                onClick={() => onMove(category.id, -1)}
                            />
                            <Button
                                aria-label={`下移门类 ${title}`}
                                icon={<ArrowDownOutlined />}
                                disabled={isSubmitting || index === categories.length - 1}
                                size="small"
                                type="text"
                                onClick={() => onMove(category.id, 1)}
                            />
                        </div>
                    </div>
                );
            })}
        </div>
    );
};
