import { App, Form, Input, Select } from "antd";
import { useState } from "react";
import { ADMIN_FORM_HORIZONTAL_LAYOUT } from "@/components/form/form-layout";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSwitch } from "@/components/kuzhambu-switch";
import type { AiModelChangeCommand } from "../ai-models-service";
import type { AiModelRecord } from "../ai-models-types";
import {
    API_SOURCE_OPTIONS,
    DEFAULT_MODEL_PARAMS,
    MODEL_CAPABILITY_OPTIONS,
    normalizeJsonText,
    readApiSourceMeta,
    readCapabilityMeta
} from "../ai-models-metadata";

type AiModelFormValues = AiModelChangeCommand;

const createEmptyForm = (): AiModelFormValues => ({
    apiSource: API_SOURCE_OPTIONS[0],
    baseUrl: "",
    apiKey: "",
    modelName: "",
    displayName: "",
    capabilities: [],
    defaultParamsJson: DEFAULT_MODEL_PARAMS,
    description: "",
    enabled: true
});

const toFormValues = (model: AiModelRecord | null): AiModelFormValues => {
    if (!model) {
        return createEmptyForm();
    }

    return {
        id: model.id,
        apiSource: model.apiSource,
        baseUrl: model.baseUrl || "",
        apiKey: "",
        modelName: model.modelName,
        displayName: model.displayName || "",
        capabilities: model.capabilities || [],
        defaultParamsJson: normalizeJsonText(model.defaultParamsJson),
        description: model.description || "",
        enabled: model.enabled
    };
};

interface AiModelEditDrawerProps {
    canEdit: boolean;
    model: AiModelRecord | null;
    open: boolean;
    saving: boolean;
    onClose: () => void;
    onSave: (command: AiModelChangeCommand) => void;
}

const omitBlankApiKey = (command: AiModelChangeCommand): AiModelChangeCommand => {
    if (!command.apiKey?.trim()) {
        const sanitized = { ...command };
        delete sanitized.apiKey;
        return sanitized;
    }
    return command;
};

export const AiModelEditDrawer = ({
    canEdit,
    model,
    open,
    saving,
    onClose,
    onSave
}: AiModelEditDrawerProps) => {
    const formKey = `${open ? "open" : "closed"}-${model?.id ?? "create"}`;

    return (
        <AiModelEditDrawerForm
            key={formKey}
            canEdit={canEdit}
            model={model}
            open={open}
            saving={saving}
            onClose={onClose}
            onSave={onSave}
        />
    );
};

const AiModelEditDrawerForm = ({
    canEdit,
    model,
    open,
    saving,
    onClose,
    onSave
}: AiModelEditDrawerProps) => {
    const { message: messageApi } = App.useApp();
    const [form, setForm] = useState<AiModelFormValues>(() => toFormValues(model));

    const submitForm = () => {
        if (!form.displayName?.trim()) {
            messageApi.warning("请输入模型名称");
            return;
        }
        if (!form.modelName.trim()) {
            messageApi.warning("请输入模型标识");
            return;
        }
        if (!form.apiSource) {
            messageApi.warning("请选择供应商");
            return;
        }
        if (!form.baseUrl.trim()) {
            messageApi.warning("请输入服务地址");
            return;
        }
        try {
            JSON.parse(normalizeJsonText(form.defaultParamsJson));
        } catch {
            messageApi.warning("请输入合法 JSON");
            return;
        }

        const command = {
            ...form,
            id: model?.id || form.id || null,
            baseUrl: form.baseUrl.trim(),
            modelName: form.modelName.trim(),
            defaultParamsJson: normalizeJsonText(form.defaultParamsJson),
            displayName: form.displayName?.trim() || null,
            description: form.description?.trim() || null,
            capabilities: form.capabilities || []
        };
        onSave(model ? omitBlankApiKey(command) : command);
    };

    const formProps = {
        className: "ai-models-form",
        colon: false,
        component: "div" as const,
        labelCol: ADMIN_FORM_HORIZONTAL_LAYOUT.labelCol,
        layout: "horizontal" as const,
        wrapperCol: ADMIN_FORM_HORIZONTAL_LAYOUT.wrapperCol
    };

    return (
        <KuzhambuDrawer
            testId="ai-ai-models-ai-model-edit-drawer"
            open={open}
            title={model ? "编辑模型" : "新增模型"}
            size="large"
            onClose={onClose}
            footerActions={[
                { testId: "ai-models-cancel-button", title: "取消", action: onClose },
                {
                    testId: "ai-models-save-button",
                    title: "保存",
                    type: "primary",
                    disabled: !canEdit,
                    loading: saving,
                    action: submitForm
                }
            ]}
        >
            <Form {...formProps}>
                <Form.Item label="模型名称" className="ai-models-form-item-compact">
                    <Input
                        aria-label="AI模型名称"
                        placeholder="对后台用户展示的名称"
                        value={form.displayName ?? ""}
                        onChange={(event) =>
                            setForm((currentForm) => ({
                                ...currentForm,
                                displayName: event.target.value
                            }))
                        }
                    />
                </Form.Item>
                <Form.Item label="供应商" className="ai-models-form-item-compact">
                    <Select
                        aria-label="AI模型供应商"
                        options={API_SOURCE_OPTIONS.map((value) => ({
                            label: readApiSourceMeta(value).label,
                            value
                        }))}
                        value={form.apiSource}
                        onChange={(value) =>
                            setForm((currentForm) => ({
                                ...currentForm,
                                apiSource: value
                            }))
                        }
                    />
                </Form.Item>
                <Form.Item label="服务地址" className="ai-models-form-item-wide">
                    <Input
                        aria-label="AI模型服务地址"
                        placeholder="https://api.example.com/v1"
                        value={form.baseUrl}
                        onChange={(event) =>
                            setForm((currentForm) => ({
                                ...currentForm,
                                baseUrl: event.target.value
                            }))
                        }
                    />
                </Form.Item>
                <Form.Item label="API 密钥" className="ai-models-form-item-wide">
                    <Input.Password
                        aria-label="AI模型API密钥"
                        placeholder={model?.apiKeyConfigured ? "已配置，留空则不更新" : ""}
                        value={form.apiKey ?? ""}
                        onChange={(event) =>
                            setForm((currentForm) => ({
                                ...currentForm,
                                apiKey: event.target.value
                            }))
                        }
                    />
                </Form.Item>
                <Form.Item label="模型标识" className="ai-models-form-item-wide">
                    <Input
                        aria-label="AI模型标识"
                        placeholder="gpt-4o"
                        value={form.modelName}
                        onChange={(event) =>
                            setForm((currentForm) => ({
                                ...currentForm,
                                modelName: event.target.value
                            }))
                        }
                    />
                </Form.Item>
                <Form.Item label="能力" className="ai-models-form-item-medium">
                    <Select
                        aria-label="AI模型能力"
                        mode="multiple"
                        options={MODEL_CAPABILITY_OPTIONS.map((value) => ({
                            label: readCapabilityMeta(value).label,
                            value
                        }))}
                        value={form.capabilities}
                        onChange={(value) =>
                            setForm((currentForm) => ({
                                ...currentForm,
                                capabilities: value
                            }))
                        }
                    />
                </Form.Item>
                <Form.Item label="状态">
                    <KuzhambuSwitch
                        aria-label="AI模型状态"
                        checked={form.enabled}
                        checkedChildren="启用"
                        unCheckedChildren="禁用"
                        onChange={(checked) =>
                            setForm((currentForm) => ({
                                ...currentForm,
                                enabled: checked
                            }))
                        }
                    />
                </Form.Item>
                <Form.Item label="默认参数 JSON" className="ai-models-form-item-top">
                    <Input.TextArea
                        aria-label="AI模型默认参数JSON"
                        value={form.defaultParamsJson ?? ""}
                        autoSize={resolveTextAreaAutoSize({ minRows: 6, maxRows: 10 })}
                        onChange={(event) =>
                            setForm((currentForm) => ({
                                ...currentForm,
                                defaultParamsJson: event.target.value
                            }))
                        }
                    />
                </Form.Item>
                <Form.Item label="说明" className="ai-models-form-item-top">
                    <Input.TextArea
                        aria-label="AI模型说明"
                        value={form.description ?? ""}
                        autoSize={resolveTextAreaAutoSize({ minRows: 3, maxRows: 6 })}
                        onChange={(event) =>
                            setForm((currentForm) => ({
                                ...currentForm,
                                description: event.target.value
                            }))
                        }
                    />
                </Form.Item>
            </Form>
        </KuzhambuDrawer>
    );
};
