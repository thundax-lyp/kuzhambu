import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Input, Modal, Select } from "antd";
import { useState } from "react";
import type { DictItem } from "@/types/dict";
import { toCategoryFormValues, type SancaiCategoryFormValues } from "./sancai-form-values";
import * as service from "../sancai-service";
import type { SancaiCategoryRecord } from "../sancai-types";

interface SancaiCategoryModelProps {
    category: SancaiCategoryRecord | null;
    onCancel: () => void;
}

const fallbackCategoryTypeOptions: DictItem[] = [
    { label: "正式门类", type: "SANCAI_CATEGORY_TYPE", value: "FORMAL" },
    { label: "辅助内容", type: "SANCAI_CATEGORY_TYPE", value: "AUXILIARY" }
];

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

export const SancaiCategoryModel = ({ category, onCancel }: SancaiCategoryModelProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [form, setForm] = useState<SancaiCategoryFormValues>(() =>
        toCategoryFormValues(category ?? undefined)
    );
    const typesQuery = useQuery<DictItem[]>({
        queryKey: ["classics", "sancai", "categories", "types"],
        queryFn: service.listCategoryTypes,
        retry: false
    });
    const categoryTypeItems = typesQuery.data?.length
        ? typesQuery.data
        : fallbackCategoryTypeOptions;

    const afterChanged = async () => {
        await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "categories"] });
        onCancel();
        messageApi.success("三才图会门类已保存");
    };

    const addMutation = useMutation({
        mutationFn: service.addCategory,
        onSuccess: afterChanged,
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "门类新增失败");
        }
    });
    const updateMutation = useMutation({
        mutationFn: service.updateCategory,
        onSuccess: afterChanged,
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "门类更新失败");
        }
    });

    const persist = () => {
        const request = {
            id: category?.id,
            title: form.title,
            categoryType: form.categoryType
        };
        if (category) {
            updateMutation.mutate(request);
            return;
        }
        addMutation.mutate(request);
    };

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
                    options={categoryTypeItems}
                    loading={typesQuery.isFetching}
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
                    loading={addMutation.isPending || updateMutation.isPending}
                    type="primary"
                    onClick={persist}
                >
                    保存
                </Button>
            </div>
        </Modal>
    );
};
