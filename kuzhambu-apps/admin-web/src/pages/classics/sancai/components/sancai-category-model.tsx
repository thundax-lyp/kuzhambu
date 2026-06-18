import { Button, Input, Modal, Select } from "antd";
import { useState } from "react";
import type { DictItem } from "@/types/dict";
import { toCategoryFormValues, type SancaiCategoryFormValues } from "./sancai-form-values";
import type { SancaiCategoryRecord } from "../sancai-types";

interface SancaiCategoryModelProps {
    category: SancaiCategoryRecord | null;
    categoryTypeOptions: DictItem[];
    isSubmitting: boolean;
    onCancel: () => void;
    onSubmit: (values: SancaiCategoryFormValues) => void;
}

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

export const SancaiCategoryModel = ({
    category,
    categoryTypeOptions,
    isSubmitting,
    onCancel,
    onSubmit
}: SancaiCategoryModelProps) => {
    const [form, setForm] = useState<SancaiCategoryFormValues>(() =>
        toCategoryFormValues(category ?? undefined)
    );

    return (
        <Modal
            title={category ? "编辑门类" : "新增门类"}
            open
            footer={null}
            destroyOnHidden
            onCancel={onCancel}
        >
            <div className="sancai-category-editor" aria-label={category ? "编辑门类" : "新增门类"}>
                <Input
                    aria-label="三才图会门类标题"
                    placeholder="门类标题"
                    value={form.title}
                    onChange={(event) =>
                        setForm((currentForm) => ({
                            ...currentForm,
                            title: event.target.value
                        }))
                    }
                />
                <Select
                    aria-label="三才图会门类类型"
                    value={form.categoryType}
                    options={categoryTypeOptions}
                    onChange={(categoryType) =>
                        setForm((currentForm) => ({
                            ...currentForm,
                            categoryType
                        }))
                    }
                />
                <Button
                    className="sancai-modal-submit"
                    aria-label={
                        category ? `保存门类 ${readTitle(category, "门类")}` : "保存新增门类"
                    }
                    loading={isSubmitting}
                    type="primary"
                    onClick={() => onSubmit(form)}
                >
                    保存
                </Button>
            </div>
        </Modal>
    );
};
