import { Button, Empty, Input, Select, Typography } from "antd";
import { useState } from "react";
import { toEntryFormValues, type SancaiEntryFormValues } from "./sancai-form-values";
import type { SancaiEntryRecord } from "../sancai-types";

const { Text } = Typography;

const visibilityOptions = [
    { label: "公开", value: "PUBLIC" },
    { label: "私有", value: "PRIVATE" }
];

interface SancaiEntryModelProps {
    entry: SancaiEntryRecord | undefined;
    isSubmitting: boolean;
    onSubmit: (values: SancaiEntryFormValues) => void;
}

export const SancaiEntryModel = ({
    entry,
    isSubmitting,
    onSubmit
}: SancaiEntryModelProps) => {
    const [form, setForm] = useState<SancaiEntryFormValues>(() => toEntryFormValues(entry));

    if (!entry) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="选择条目后查看详情" />;
    }

    return (
        <div className="sancai-detail-card">
            <Text type="secondary">当前预览</Text>
            <Input
                aria-label="三才图会条目标题"
                value={form.title}
                onChange={(event) =>
                    setForm((currentForm) => ({
                        ...currentForm,
                        title: event.target.value
                    }))
                }
            />
            <Input.TextArea
                aria-label="三才图会原文"
                value={form.originalText}
                autoSize={{ minRows: 4, maxRows: 8 }}
                onChange={(event) =>
                    setForm((currentForm) => ({
                        ...currentForm,
                        originalText: event.target.value
                    }))
                }
            />
            <Input.TextArea
                aria-label="三才图会译文"
                value={form.translationText}
                autoSize={{ minRows: 4, maxRows: 8 }}
                onChange={(event) =>
                    setForm((currentForm) => ({
                        ...currentForm,
                        translationText: event.target.value
                    }))
                }
            />
            <Input.TextArea
                aria-label="三才图会摘要"
                value={form.summary}
                autoSize={{ minRows: 3, maxRows: 6 }}
                onChange={(event) =>
                    setForm((currentForm) => ({
                        ...currentForm,
                        summary: event.target.value
                    }))
                }
            />
            <Select
                aria-label="三才图会公开状态"
                value={form.visibility}
                options={visibilityOptions}
                onChange={(visibility) =>
                    setForm((currentForm) => ({
                        ...currentForm,
                        visibility
                    }))
                }
            />
            <Button
                aria-label="保存三才图会条目"
                type="primary"
                loading={isSubmitting}
                onClick={() => onSubmit(form)}
            >
                保存
            </Button>
        </div>
    );
};
