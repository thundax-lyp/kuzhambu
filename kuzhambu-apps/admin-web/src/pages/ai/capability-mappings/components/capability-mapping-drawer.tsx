import { Switch } from "antd";
import type { FormInstance } from "antd";
import { KuzhambuDrawer, KuzhambuForm, KuzhambuFormItem, KuzhambuSelect } from "@/components";

import { CapabilityModelMatchPanel } from "./capability-model-match-panel";
import type { CapabilityModelTagMatch } from "./capability-model-match-panel";
import type { AiCapabilityMappingChangeCommand } from "../capability-mappings-service";
import type { AiCapabilityMappingRecord } from "../capability-mappings-types";

export type MappingFormValues = AiCapabilityMappingChangeCommand;

interface CapabilityMappingDrawerProps {
    canEditConfig: boolean;
    capabilityOptions: Array<{ label: string; value: string }>;
    editingMapping: AiCapabilityMappingRecord | null;
    form: FormInstance<MappingFormValues>;
    modelOptions: Array<{ label: string; value: number }>;
    onClose: () => void;
    onSubmit: () => void;
    open: boolean;
    saving: boolean;
    scopeOptions: Array<{ label: string; value: string }>;
    tagMatch: CapabilityModelTagMatch;
}

export const CapabilityMappingDrawer = ({
    canEditConfig,
    capabilityOptions,
    editingMapping,
    form,
    modelOptions,
    onClose,
    onSubmit,
    open,
    saving,
    scopeOptions,
    tagMatch
}: CapabilityMappingDrawerProps) => {
    return (
        <KuzhambuDrawer
            testId="ai-capability-mappings-capability-mappings-drawer"
            open={open}
            title={editingMapping ? "配置模型" : "新增映射"}
            size="large"
            onClose={onClose}
            footerActions={[
                {
                    testId: "ai-capability-mappings-capability-mappings-cancel-button",
                    title: "取消",
                    action: onClose
                },
                {
                    testId: "ai-capability-mappings-capability-mappings-save-button",
                    title: "保存",
                    type: "primary",
                    disabled: !canEditConfig,
                    loading: saving,
                    action: onSubmit
                }
            ]}
        >
            <KuzhambuForm form={form} className="capability-mappings-form">
                <KuzhambuFormItem
                    label="scope"
                    name="scope"
                    rules={[{ required: true, message: "请选择 scope" }]}
                >
                    <KuzhambuSelect options={scopeOptions} />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    label="capability"
                    name="capability"
                    rules={[{ required: true, message: "请选择 capability" }]}
                >
                    <KuzhambuSelect options={capabilityOptions} />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    label="modelId"
                    name="modelId"
                    rules={[{ required: true, message: "请选择启用模型" }]}
                >
                    <KuzhambuSelect options={modelOptions} />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="enabled" name="enabled" valuePropName="checked">
                    <Switch checkedChildren="启用" unCheckedChildren="禁用" />
                </KuzhambuFormItem>
            </KuzhambuForm>
            <CapabilityModelMatchPanel tagMatch={tagMatch} />
        </KuzhambuDrawer>
    );
};
