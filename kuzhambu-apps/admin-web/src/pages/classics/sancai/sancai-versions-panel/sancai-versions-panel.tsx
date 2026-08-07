import { Empty, Tag, Typography } from "antd";
import {
    KuzhambuList,
    KuzhambuListItem,
    KuzhambuListMeta,
    KuzhambuSpace,
    KuzhambuButton,
    KuzhambuAlert,
    KuzhambuTextCompare,
    KuzhambuDescriptions
} from "@/components";

import type {
    SancaiContentVersionRecord,
    SancaiEntryRecord,
    SancaiVersionSnapshot
} from "@/pages/classics/sancai/sancai-types";
import { isSameId } from "@/types/id";

import "./sancai-versions-panel.css";

const { Text } = Typography;

const compareFields: Array<{
    key: keyof SancaiVersionSnapshot;
    label: string;
    textDiff?: boolean;
}> = [
    { key: "volumeId", label: "卷目" },
    { key: "title", label: "标题" },
    { key: "originalText", label: "原文", textDiff: true },
    { key: "translationText", label: "译文", textDiff: true },
    { key: "summary", label: "摘要", textDiff: true },
    { key: "lifecycleStatus", label: "生命周期" },
    { key: "translationStatus", label: "翻译状态" },
    { key: "imageStatus", label: "配图状态" },
    { key: "visualAssetStatus", label: "视觉处理状态" },
    { key: "refinementStatus", label: "精修状态" },
    { key: "priority", label: "历史排序值" }
];

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
};

const readSnapshot = (
    version?: SancaiContentVersionRecord | null
): SancaiVersionSnapshot | null => {
    if (!version?.snapshotJson) {
        return null;
    }
    try {
        return JSON.parse(version.snapshotJson) as SancaiVersionSnapshot;
    } catch {
        return null;
    }
};

const readCurrentValue = (
    entry: SancaiEntryRecord | null | undefined,
    key: keyof SancaiVersionSnapshot
) => {
    if (!entry) {
        return undefined;
    }
    return entry[key as keyof SancaiEntryRecord];
};

const formatValue = (value: unknown) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    return String(value);
};

interface SancaiVersionVolumeOption {
    label: string;
    value: string;
}

const formatFieldValue = (
    key: keyof SancaiVersionSnapshot,
    value: unknown,
    volumeOptions: SancaiVersionVolumeOption[]
) => {
    if (key !== "volumeId" || value === null || value === undefined || value === "") {
        return formatValue(value);
    }
    const volumeId = String(value);
    return (
        volumeOptions.find((option) => isSameId(option.value, volumeId))?.label || `卷 ${volumeId}`
    );
};

interface SancaiVersionsPanelProps {
    currentEntry?: SancaiEntryRecord | null;
    detailLoading?: boolean;
    listLoading?: boolean;
    resetting?: boolean;
    readOnly?: boolean;
    selectedVersion?: SancaiContentVersionRecord | null;
    volumeOptions?: SancaiVersionVolumeOption[];
    versions: SancaiContentVersionRecord[];
    onResetVersion: (version: SancaiContentVersionRecord) => void;
    onSelectVersion: (version: SancaiContentVersionRecord) => void;
}

export const SancaiVersionsPanel = ({
    currentEntry,
    detailLoading = false,
    listLoading = false,
    resetting = false,
    readOnly = false,
    selectedVersion,
    volumeOptions = [],
    versions,
    onResetVersion,
    onSelectVersion
}: SancaiVersionsPanelProps) => {
    const snapshot = readSnapshot(selectedVersion);

    return (
        <section className="sancai-versions-panel" aria-label="三才图会版本面板">
            <div className="sancai-versions-panel-grid">
                <KuzhambuList<SancaiContentVersionRecord>
                    as="ol"
                    className="sancai-version-history-list"
                    aria-label="三才图会版本历史列表"
                    loading={listLoading}
                    dataSource={versions}
                    empty="暂无版本历史"
                    renderItem={(version) => (
                        <KuzhambuListItem
                            className="sancai-version-history-list-item"
                            key={version.id}
                            actions={[
                                <KuzhambuButton
                                    testId="classics-sancai-sancai-version-history-action-button"
                                    key="view"
                                    type="link"
                                    onClick={() => onSelectVersion(version)}
                                >
                                    查看
                                </KuzhambuButton>
                            ]}
                        >
                            <KuzhambuListMeta
                                title={
                                    <KuzhambuSpace wrap>
                                        <Text strong>版本 {version.versionNo ?? "-"}</Text>
                                        <Tag>{version.changeType || "UNKNOWN"}</Tag>
                                    </KuzhambuSpace>
                                }
                                description={
                                    <KuzhambuSpace orientation="vertical" size={2}>
                                        <Text>{version.changeSummary || "未填写变更说明"}</Text>
                                        <Text type="secondary">
                                            {formatDateTime(version.versionedAt)}
                                        </Text>
                                    </KuzhambuSpace>
                                }
                            />
                        </KuzhambuListItem>
                    )}
                />
                <div className="sancai-version-history-detail" aria-busy={detailLoading}>
                    {selectedVersion ? (
                        <KuzhambuSpace
                            orientation="vertical"
                            size="middle"
                            className="sancai-version-history-detail-stack"
                        >
                            <KuzhambuDescriptions
                                size="small"
                                column={2}
                                bordered
                                items={[
                                    {
                                        key: "versionNo",
                                        label: "版本号",
                                        children: selectedVersion.versionNo ?? "-"
                                    },
                                    {
                                        key: "changeType",
                                        label: "变更类型",
                                        children: selectedVersion.changeType || "-"
                                    },
                                    {
                                        key: "versionedAt",
                                        label: "版本时间",
                                        children: formatDateTime(selectedVersion.versionedAt)
                                    },
                                    {
                                        key: "changeSummary",
                                        label: "变更说明",
                                        children: selectedVersion.changeSummary || "-"
                                    }
                                ]}
                            />
                            {snapshot ? (
                                <KuzhambuDescriptions
                                    className="sancai-version-compare"
                                    size="small"
                                    column={1}
                                    bordered
                                    variant="compare"
                                    items={compareFields.map((field) => {
                                        const currentValue = readCurrentValue(
                                            currentEntry,
                                            field.key
                                        );
                                        const historyValue = snapshot[field.key];
                                        const currentDisplayValue = formatFieldValue(
                                            field.key,
                                            currentValue,
                                            volumeOptions
                                        );
                                        const historyDisplayValue = formatFieldValue(
                                            field.key,
                                            historyValue,
                                            volumeOptions
                                        );
                                        const changed = currentDisplayValue !== historyDisplayValue;
                                        return {
                                            key: field.key,
                                            label: field.label,
                                            className: changed ? "is-changed" : undefined,
                                            children: field.textDiff ? (
                                                <KuzhambuTextCompare
                                                    baseline={historyDisplayValue}
                                                    candidate={currentDisplayValue}
                                                    emptyText="历史版本与当前内容一致"
                                                    testId={`classics-sancai-version-${field.key}-compare`}
                                                    title={`${field.label}差异（历史 → 当前）`}
                                                />
                                            ) : (
                                                <KuzhambuSpace orientation="vertical" size={2}>
                                                    <Text>当前：{currentDisplayValue}</Text>
                                                    <Text type={changed ? "warning" : "secondary"}>
                                                        历史：{historyDisplayValue}
                                                    </Text>
                                                </KuzhambuSpace>
                                            )
                                        };
                                    })}
                                />
                            ) : (
                                <KuzhambuAlert
                                    type="warning"
                                    showIcon
                                    title="版本快照为空或无法解析"
                                />
                            )}
                            {!readOnly ? (
                                <div className="sancai-version-history-detail-actions">
                                    <KuzhambuButton
                                        testId="classics-sancai-sancai-version-history-action-button-2"
                                        danger
                                        loading={resetting}
                                        disabled={!snapshot}
                                        onClick={() => onResetVersion(selectedVersion)}
                                    >
                                        恢复此版本
                                    </KuzhambuButton>
                                </div>
                            ) : null}
                        </KuzhambuSpace>
                    ) : (
                        <Empty
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                            description="请选择版本查看对比"
                        />
                    )}
                </div>
            </div>
        </section>
    );
};
