import { PlusOutlined, ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Card, Form, Select, Tooltip } from "antd";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { CapabilityMappingDrawer } from "./components/capability-mapping-drawer";
import type { MappingFormValues } from "./components/capability-mapping-drawer";
import { CapabilityMappingTable } from "./components/capability-mapping-table";
import type { MappingTableRow } from "./components/capability-mapping-table";
import * as service from "./capability-mappings-service";
import type { AiCapabilityQuery } from "./capability-mappings-service";
import type {
    AiCapabilityMappingRecord,
    AiCapabilityModelRecord,
    AiCapabilityRecord
} from "./capability-mappings-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./capability-mappings-page.css";

const SCOPE_OPTIONS = [
    { label: "classics", value: "classics" },
    { label: "knowledge", value: "knowledge" },
    { label: "discovery", value: "discovery" },
    { label: "platform", value: "platform" }
];

const parseEnabled = (value?: string) => {
    if (value === "true") {
        return true;
    }
    if (value === "false") {
        return false;
    }
    return null;
};

const formatModelLabel = (model: AiCapabilityModelRecord) => {
    const displayName = model.displayName || model.modelName;
    return `${displayName} / ${model.modelName}`;
};

const buildTagMatch = (
    capability: AiCapabilityRecord | undefined,
    model: AiCapabilityModelRecord | undefined
) => {
    const requiredTags = capability?.requiredTags || [];
    const modelTags = model?.capabilityTags || [];
    const missingTags = requiredTags.filter((tag) => !modelTags.includes(tag));
    return {
        requiredTags,
        modelTags,
        missingTags,
        matched: missingTags.length === 0
    };
};

export const CapabilityMappingsPage = () => {
    const { message } = App.useApp();
    const queryClient = useQueryClient();
    const [form] = Form.useForm<MappingFormValues>();
    const canViewConfig = hasPermission("ai:config:view");
    const canEditConfig = hasPermission("ai:config:edit");
    const [query, setQuery] = useState<AiCapabilityQuery>({});
    const [editingMapping, setEditingMapping] = useState<AiCapabilityMappingRecord | null>(null);
    const [capabilityMappingEditDrawerOpen, setCapabilityMappingEditDrawerOpen] = useState(false);
    const selectedCapabilityCode = Form.useWatch("capability", form);
    const selectedModelId = Form.useWatch("modelId", form);

    const mappingCapabilitiesQuery = useQuery({
        queryKey: ["ai", "capability-mappings", "capabilities"],
        queryFn: () => service.listCapabilities({ enabled: true }),
        enabled: canViewConfig,
        retry: false
    });

    const capabilityMappingPageQuery = useQuery({
        queryKey: ["ai", "capability-mappings", query],
        queryFn: () => service.listCapabilityMappings(query),
        enabled: canViewConfig,
        retry: false
    });

    const mappingModelsQuery = useQuery({
        queryKey: ["ai", "capability-mappings", "enabled-models"],
        queryFn: () => service.listEnabledModels({ enabled: true }),
        enabled: canViewConfig,
        retry: false
    });

    const capabilityByCode = useMemo(() => {
        return new Map(
            (mappingCapabilitiesQuery.data || []).map((record) => [record.capability, record])
        );
    }, [mappingCapabilitiesQuery.data]);

    const modelById = useMemo(() => {
        return new Map((mappingModelsQuery.data || []).map((record) => [record.modelId, record]));
    }, [mappingModelsQuery.data]);

    const capabilityOptions = useMemo(() => {
        return (mappingCapabilitiesQuery.data || []).map((record) => ({
            label: `${record.name} / ${record.capability}`,
            value: record.capability
        }));
    }, [mappingCapabilitiesQuery.data]);

    const modelOptions = useMemo(() => {
        return (mappingModelsQuery.data || []).map((record) => ({
            label: formatModelLabel(record),
            value: record.modelId
        }));
    }, [mappingModelsQuery.data]);

    const tableData = useMemo<MappingTableRow[]>(() => {
        return (capabilityMappingPageQuery.data || []).map((mapping) => {
            const capability = capabilityByCode.get(mapping.capability);
            const model = modelById.get(mapping.modelId);
            return {
                ...mapping,
                capabilityName: capability?.name || mapping.capability,
                requiredTags: capability?.requiredTags || [],
                outputMode: capability?.outputMode || "-",
                modelName: model ? formatModelLabel(model) : String(mapping.modelId),
                modelTags: model?.capabilityTags || []
            };
        });
    }, [capabilityByCode, capabilityMappingPageQuery.data, modelById]);

    const selectedCapability = selectedCapabilityCode
        ? capabilityByCode.get(selectedCapabilityCode)
        : undefined;
    const selectedModel = selectedModelId ? modelById.get(selectedModelId) : undefined;
    const tagMatch = buildTagMatch(selectedCapability, selectedModel);

    const invalidateMappings = async () => {
        await queryClient.invalidateQueries({ queryKey: ["ai", "capability-mappings"] });
    };

    const updateMappingMutation = useMutation({
        mutationFn: service.changeCapabilityMapping,
        onSuccess: async () => {
            await invalidateMappings();
            setCapabilityMappingEditDrawerOpen(false);
            setEditingMapping(null);
            message.success("能力映射已保存");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "能力映射保存失败");
        }
    });

    useEffect(() => {
        if (capabilityMappingPageQuery.isError) {
            const error = capabilityMappingPageQuery.error;
            message.error(error instanceof Error ? error.message : "能力映射列表加载失败");
        }
    }, [message, capabilityMappingPageQuery.error, capabilityMappingPageQuery.isError]);

    const openCreateCapabilityMappingDrawer = () => {
        setEditingMapping(null);
        form.setFieldsValue({
            mappingId: null,
            scope: query.scope || SCOPE_OPTIONS[0]?.value,
            capability: query.capability || capabilityOptions[0]?.value,
            modelId: modelOptions[0]?.value,
            enabled: true
        });
        setCapabilityMappingEditDrawerOpen(true);
    };

    const openEditCapabilityMappingDrawer = (record: AiCapabilityMappingRecord) => {
        setEditingMapping(record);
        form.setFieldsValue({
            mappingId: record.mappingId,
            scope: record.scope,
            capability: record.capability,
            modelId: record.modelId,
            enabled: record.enabled
        });
        setCapabilityMappingEditDrawerOpen(true);
    };

    const submitForm = async () => {
        const values = await form.validateFields();
        await updateMappingMutation.mutateAsync({
            mappingId: editingMapping?.mappingId || values.mappingId || null,
            scope: values.scope,
            capability: values.capability,
            modelId: values.modelId,
            enabled: values.enabled
        });
    };

    const changeEnabled = async (record: AiCapabilityMappingRecord, enabled: boolean) => {
        await updateMappingMutation.mutateAsync({
            mappingId: record.mappingId || null,
            scope: record.scope,
            capability: record.capability,
            modelId: record.modelId,
            enabled
        });
    };

    return (
        <KuzhambuPage
            className="capability-mappings-page"
            title="AI 能力映射"
            description="配置 scope + capability 到启用模型的治理映射"
            actions={
                <KuzhambuSpace>
                    <Tooltip title="刷新">
                        <KuzhambuButton
                            testId="ai-capability-mappings-capability-mappings-refresh-button"
                            icon={<ReloadOutlined />}
                            loading={
                                capabilityMappingPageQuery.isFetching ||
                                mappingCapabilitiesQuery.isFetching ||
                                mappingModelsQuery.isFetching
                            }
                            onClick={() => void invalidateMappings()}
                        />
                    </Tooltip>
                    <KuzhambuButton
                        testId="ai-capability-mappings-capability-mappings-create-mapping-button"
                        type="primary"
                        icon={<PlusOutlined />}
                        disabled={!canEditConfig}
                        onClick={openCreateCapabilityMappingDrawer}
                    >
                        新增映射
                    </KuzhambuButton>
                </KuzhambuSpace>
            }
        >
            <Card className="capability-mappings-filter-card">
                <Form layout="inline" className="capability-mappings-filter-form">
                    <Form.Item label="scope">
                        <Select
                            allowClear
                            className="capability-mappings-filter-control"
                            options={SCOPE_OPTIONS}
                            value={query.scope ?? undefined}
                            onChange={(scope) =>
                                setQuery((current) => ({
                                    ...current,
                                    scope: scope ?? null
                                }))
                            }
                        />
                    </Form.Item>
                    <Form.Item label="capability">
                        <Select
                            allowClear
                            className="capability-mappings-filter-control"
                            options={capabilityOptions}
                            value={query.capability ?? undefined}
                            onChange={(capability) =>
                                setQuery((current) => ({
                                    ...current,
                                    capability: capability ?? null
                                }))
                            }
                        />
                    </Form.Item>
                    <Form.Item label="enabled">
                        <Select
                            allowClear
                            className="capability-mappings-filter-control"
                            value={query.enabled == null ? undefined : String(query.enabled)}
                            options={[
                                { label: "启用", value: "true" },
                                { label: "禁用", value: "false" }
                            ]}
                            onChange={(enabled) =>
                                setQuery((current) => ({
                                    ...current,
                                    enabled: parseEnabled(enabled)
                                }))
                            }
                        />
                    </Form.Item>
                    <Form.Item>
                        <KuzhambuButton
                            testId="ai-capability-mappings-capability-mappings-reset-button"
                            onClick={() => setQuery({})}
                        >
                            重置
                        </KuzhambuButton>
                    </Form.Item>
                </Form>
            </Card>

            <CapabilityMappingTable
                canEditConfig={canEditConfig}
                dataSource={tableData}
                loading={
                    capabilityMappingPageQuery.isFetching ||
                    mappingCapabilitiesQuery.isFetching ||
                    mappingModelsQuery.isFetching
                }
                onChangeEnabled={(record, enabled) => void changeEnabled(record, enabled)}
                onOpenEdit={openEditCapabilityMappingDrawer}
            />

            <CapabilityMappingDrawer
                canEditConfig={canEditConfig}
                capabilityOptions={capabilityOptions}
                editingMapping={editingMapping}
                form={form}
                modelOptions={modelOptions}
                onClose={() => setCapabilityMappingEditDrawerOpen(false)}
                onSubmit={() => void submitForm()}
                open={capabilityMappingEditDrawerOpen}
                saving={updateMappingMutation.isPending}
                scopeOptions={SCOPE_OPTIONS}
                tagMatch={tagMatch}
            />
        </KuzhambuPage>
    );
};
