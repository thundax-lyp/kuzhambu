import { Form, Input } from "antd";
import { useEffect, useMemo } from "react";
import {
    KuzhambuDrawer,
    KuzhambuForm,
    KuzhambuFormHiddenItem,
    KuzhambuFormItem,
    KuzhambuFormPlaceholderItem,
    KuzhambuSelect,
    KuzhambuSwitch
} from "@/components";

import type { AiBusinessConfigChangeCommand } from "../business-configs-service";
import type {
    AiBusinessConfigCapabilityRecord,
    AiBusinessConfigModelRecord,
    AiBusinessConfigPromptRecord,
    AiBusinessConfigRecord
} from "../business-configs-types";

export type BusinessConfigFormValues = AiBusinessConfigChangeCommand;

interface BusinessConfigEditDrawerProps {
    canEdit: boolean;
    capabilities: AiBusinessConfigCapabilityRecord[];
    config: AiBusinessConfigRecord | null;
    models: AiBusinessConfigModelRecord[];
    onClose: () => void;
    onSave: (command: AiBusinessConfigChangeCommand) => void;
    open: boolean;
    prompts: AiBusinessConfigPromptRecord[];
    saving: boolean;
}

const EMPTY_JSON_OBJECT = "{}";

const normalizeJsonText = (value?: string | null) => {
    const trimmed = value?.trim();
    return trimmed ? trimmed : EMPTY_JSON_OBJECT;
};

const assertJsonText = (value?: string | null) => {
    try {
        JSON.parse(normalizeJsonText(value));
        return Promise.resolve();
    } catch {
        return Promise.reject(new Error("请输入合法 JSON"));
    }
};

const formatModelLabel = (model: AiBusinessConfigModelRecord) => {
    const displayName = model.displayName?.trim() || model.modelName;
    return `${displayName} / ${model.modelName}`;
};

const formatPromptLabel = (
    prompt: AiBusinessConfigPromptRecord,
    capabilityNameByCode: Map<string, string>
) => {
    const promptName = prompt.name?.trim();
    const capabilityName = prompt.capability ? capabilityNameByCode.get(prompt.capability) : null;
    const label = promptName || capabilityName || prompt.capability || `提示词 ${prompt.id ?? ""}`;
    return prompt.currentVersionNo ? `${label} / v${prompt.currentVersionNo}` : label;
};

export const BusinessConfigEditDrawer = ({
    canEdit,
    capabilities,
    config,
    models,
    onClose,
    onSave,
    open,
    prompts,
    saving
}: BusinessConfigEditDrawerProps) => {
    const [form] = Form.useForm<BusinessConfigFormValues>();
    const isEditingConfig = Boolean(config);
    const selectedCapability = Form.useWatch("capability", form);
    const capabilityNameByCode = useMemo(() => {
        return new Map(capabilities.map((capability) => [capability.capability, capability.name]));
    }, [capabilities]);

    const capabilityOptions = useMemo(
        () =>
            capabilities.map((capability) => ({
                label: `${capability.name} / ${capability.capability}`,
                value: capability.capability
            })),
        [capabilities]
    );

    const promptOptions = useMemo(() => {
        return prompts
            .filter((prompt) => !selectedCapability || prompt.capability === selectedCapability)
            .filter((prompt) => prompt.id != null)
            .map((prompt) => ({
                label: formatPromptLabel(prompt, capabilityNameByCode),
                value: prompt.id ?? ""
            }));
    }, [capabilityNameByCode, prompts, selectedCapability]);

    const modelOptions = useMemo(() => {
        return models.map((model) => ({
            label: formatModelLabel(model),
            value: model.id
        }));
    }, [models]);

    useEffect(() => {
        if (!open) {
            return;
        }
        if (config) {
            form.setFieldsValue({
                id: config.id ?? null,
                capability: config.capability,
                promptTemplateId: config.promptTemplateId,
                modelId: config.modelId,
                defaultParamsJson: normalizeJsonText(config.defaultParamsJson),
                enabled: config.enabled !== false
            });
            return;
        }
        form.setFieldsValue({
            id: null,
            capability: capabilityOptions[0]?.value,
            promptTemplateId: promptOptions[0]?.value,
            modelId: modelOptions[0]?.value,
            defaultParamsJson: EMPTY_JSON_OBJECT,
            enabled: true
        });
    }, [capabilityOptions, config, form, modelOptions, open, promptOptions]);

    useEffect(() => {
        if (!open || config) {
            return;
        }
        const currentPromptId = form.getFieldValue("promptTemplateId");
        const hasPromptOption = promptOptions.some((option) => option.value === currentPromptId);
        if (!hasPromptOption) {
            form.setFieldValue("promptTemplateId", promptOptions[0]?.value);
        }
    }, [config, form, open, promptOptions]);

    const submitBusinessConfig = async () => {
        const values = await form.validateFields();
        onSave({
            id: values.id ?? null,
            capability: config?.capability ?? values.capability,
            promptTemplateId: values.promptTemplateId,
            modelId: values.modelId,
            defaultParamsJson: normalizeJsonText(values.defaultParamsJson),
            enabled: values.enabled !== false
        });
    };

    return (
        <KuzhambuDrawer
            testId="ai-business-configs-business-config-edit-drawer"
            className="business-configs-edit-drawer"
            open={open}
            title={config ? "编辑业务配置" : "新增业务配置"}
            size="large"
            onClose={onClose}
            footerActions={[
                {
                    testId: "ai-business-configs-business-config-cancel-button",
                    title: "取消",
                    action: onClose
                },
                {
                    testId: "ai-business-configs-business-config-save-button",
                    title: "保存",
                    type: "primary",
                    disabled: !canEdit,
                    loading: saving,
                    action: () => void submitBusinessConfig()
                }
            ]}
        >
            <KuzhambuForm<BusinessConfigFormValues> form={form} className="business-configs-form">
                <KuzhambuFormHiddenItem name="id" />
                <KuzhambuFormItem
                    label="业务能力"
                    name="capability"
                    rules={[{ required: true, message: "请选择业务能力" }]}
                    layoutSize="middle"
                    className="business-configs-form-item-compact"
                >
                    <KuzhambuSelect
                        options={capabilityOptions}
                        showSearch
                        disabled={isEditingConfig}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormPlaceholderItem />
                <KuzhambuFormItem
                    label="提示词模板"
                    name="promptTemplateId"
                    rules={[{ required: true, message: "请选择提示词模板" }]}
                    layoutSize="middle"
                    className="business-configs-form-item-compact"
                >
                    <KuzhambuSelect options={promptOptions} showSearch />
                </KuzhambuFormItem>
                <KuzhambuFormPlaceholderItem />
                <KuzhambuFormItem
                    label="模型"
                    name="modelId"
                    rules={[{ required: true, message: "请选择模型" }]}
                    layoutSize="middle"
                    className="business-configs-form-item-compact"
                >
                    <KuzhambuSelect options={modelOptions} showSearch />
                </KuzhambuFormItem>
                <KuzhambuFormPlaceholderItem />
                <KuzhambuFormItem
                    label="启用"
                    name="enabled"
                    valuePropName="checked"
                    className="business-configs-form-item-status"
                >
                    <KuzhambuSwitch checkedChildren="启用" unCheckedChildren="禁用" />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    label="默认参数"
                    name="defaultParamsJson"
                    rules={[{ validator: (_, value) => assertJsonText(value) }]}
                    layoutSize="large"
                    className="business-configs-form-item-json"
                >
                    <Input.TextArea aria-label="默认参数 JSON" rows={8} />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuDrawer>
    );
};
