import { Button, Drawer, Input, Switch, Typography } from "antd";
import { useState } from "react";
import { toEntryFormValues, type SancaiEntryFormValues } from "./sancai-form-values";
import type { SancaiEntryRecord } from "../sancai-types";

const { Text } = Typography;

interface SancaiEntryModelProps {
    entry: SancaiEntryRecord | undefined;
    isSubmitting: boolean;
    mode?: "create" | "edit";
    open: boolean;
    onCancel: () => void;
    onSubmit: (values: SancaiEntryFormValues) => void;
}

export const SancaiEntryModel = ({
    entry,
    isSubmitting,
    mode = "edit",
    open,
    onCancel,
    onSubmit
}: SancaiEntryModelProps) => {
    const [form, setForm] = useState<SancaiEntryFormValues>(() => toEntryFormValues(entry));

    if (!entry && mode !== "create") {
        return null;
    }

    return (
        <Drawer
            title={mode === "create" ? "新增条目" : "编辑条目"}
            open={open}
            width={720}
            destroyOnHidden
            footer={
                <div className="sancai-drawer-footer">
                    <Button onClick={onCancel}>取消</Button>
                    <Button
                        aria-label={mode === "create" ? "保存新增三才图会条目" : "保存三才图会条目"}
                        type="primary"
                        loading={isSubmitting}
                        onClick={() => onSubmit(form)}
                    >
                        保存
                    </Button>
                </div>
            }
            onClose={onCancel}
        >
            <div className="sancai-detail-card">
                <label className="sancai-form-field">
                    <Text strong>标题</Text>
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
                </label>
                <label className="sancai-form-field">
                    <Text strong>原文</Text>
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
                </label>
                <label className="sancai-form-field">
                    <Text strong>译文</Text>
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
                </label>
                <label className="sancai-form-field">
                    <Text strong>摘要</Text>
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
                </label>
                <div className="sancai-form-switch-field">
                    <Text strong>可见性</Text>
                    <Switch
                        checked={form.visibility === "PUBLIC"}
                        checkedChildren="公开"
                        unCheckedChildren="私有"
                        aria-label="三才图会公开状态"
                        onChange={(checked) =>
                            setForm((currentForm) => ({
                                ...currentForm,
                                visibility: checked ? "PUBLIC" : "PRIVATE"
                            }))
                        }
                    />
                </div>
            </div>
        </Drawer>
    );
};
