import { Descriptions, Empty, Tag, Typography } from "antd";
import {
    KuzhambuList,
    KuzhambuListItem,
    KuzhambuListMeta,
    KuzhambuSpace,
    KuzhambuButton,
    KuzhambuAlert
} from "@/components";

import type {
    SancaiContentVersionRecord,
    SancaiEntryRecord,
    SancaiVersionSnapshot
} from "@/pages/classics/sancai/sancai-types";

import "./sancai-versions-panel.css";

const { Text } = Typography;

const compareFields: Array<{
    key: keyof SancaiVersionSnapshot;
    label: string;
}> = [
    { key: "volumeId", label: "卷目 ID" },
    { key: "title", label: "标题" },
    { key: "originalText", label: "原文" },
    { key: "translationText", label: "译文" },
    { key: "summary", label: "摘要" },
    { key: "lifecycleStatus", label: "生命周期" },
    { key: "visibility", label: "可见性" },
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

interface SancaiVersionsPanelProps {
    currentEntry?: SancaiEntryRecord | null;
    detailLoading?: boolean;
    listLoading?: boolean;
    resetting?: boolean;
    selectedVersion?: SancaiContentVersionRecord | null;
    versions: SancaiContentVersionRecord[];
    onResetVersion: (version: SancaiContentVersionRecord) => void;
    onSelectVersion: (version: SancaiContentVersionRecord) => void;
}

export const SancaiVersionsPanel = ({
    currentEntry,
    detailLoading = false,
    listLoading = false,
    resetting = false,
    selectedVersion,
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
                            <Descriptions size="small" column={2} bordered>
                                <Descriptions.Item label="版本号">
                                    {selectedVersion.versionNo ?? "-"}
                                </Descriptions.Item>
                                <Descriptions.Item label="变更类型">
                                    {selectedVersion.changeType || "-"}
                                </Descriptions.Item>
                                <Descriptions.Item label="版本时间">
                                    {formatDateTime(selectedVersion.versionedAt)}
                                </Descriptions.Item>
                                <Descriptions.Item label="变更说明">
                                    {selectedVersion.changeSummary || "-"}
                                </Descriptions.Item>
                            </Descriptions>
                            {snapshot ? (
                                <Descriptions
                                    className="sancai-version-compare"
                                    size="small"
                                    column={1}
                                    bordered
                                >
                                    {compareFields.map((field) => {
                                        const currentValue = readCurrentValue(
                                            currentEntry,
                                            field.key
                                        );
                                        const historyValue = snapshot[field.key];
                                        const changed =
                                            formatValue(currentValue) !== formatValue(historyValue);
                                        return (
                                            <Descriptions.Item
                                                key={field.key}
                                                label={field.label}
                                                className={changed ? "is-changed" : undefined}
                                            >
                                                <KuzhambuSpace orientation="vertical" size={2}>
                                                    <Text>当前：{formatValue(currentValue)}</Text>
                                                    <Text type={changed ? "warning" : "secondary"}>
                                                        历史：{formatValue(historyValue)}
                                                    </Text>
                                                </KuzhambuSpace>
                                            </Descriptions.Item>
                                        );
                                    })}
                                </Descriptions>
                            ) : (
                                <KuzhambuAlert
                                    type="warning"
                                    showIcon
                                    title="版本快照为空或无法解析"
                                />
                            )}
                            <KuzhambuButton
                                testId="classics-sancai-sancai-version-history-action-button-2"
                                danger
                                loading={resetting}
                                disabled={!snapshot}
                                onClick={() => onResetVersion(selectedVersion)}
                            >
                                恢复此版本
                            </KuzhambuButton>
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
