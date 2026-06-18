import { Button, Input, Modal } from "antd";
import { useState } from "react";
import { toContentFormValues, type SancaiContentFormValues } from "./sancai-form-values";
import type { SancaiContentRecord } from "../sancai-types";

interface SancaiContentModelProps {
    content: SancaiContentRecord | null;
    isSubmitting: boolean;
    onCancel: () => void;
    onSubmit: (values: SancaiContentFormValues) => void;
}

const readQuestion = (content: SancaiContentRecord) => {
    return content.question?.trim() || `内容 ${content.id}`;
};

export const SancaiContentModel = ({
    content,
    isSubmitting,
    onCancel,
    onSubmit
}: SancaiContentModelProps) => {
    const [form, setForm] = useState<SancaiContentFormValues>(() =>
        toContentFormValues(content ?? undefined)
    );

    return (
        <Modal
            title={content ? "编辑内容" : "新增内容"}
            open
            footer={null}
            destroyOnHidden
            onCancel={onCancel}
        >
            <div className="sancai-category-editor" aria-label={content ? "编辑内容" : "新增内容"}>
                <Input.TextArea
                    aria-label="三才图会内容问题"
                    placeholder="问题"
                    value={form.question}
                    autoSize={{ minRows: 2, maxRows: 5 }}
                    onChange={(event) =>
                        setForm((currentForm) => ({
                            ...currentForm,
                            question: event.target.value
                        }))
                    }
                />
                <Input.TextArea
                    aria-label="三才图会内容答案"
                    placeholder="答案"
                    value={form.answer}
                    autoSize={{ minRows: 5, maxRows: 12 }}
                    onChange={(event) =>
                        setForm((currentForm) => ({
                            ...currentForm,
                            answer: event.target.value
                        }))
                    }
                />
                <Button
                    className="sancai-modal-submit"
                    aria-label={
                        content ? `保存内容 ${readQuestion(content)}` : "保存新增内容"
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
