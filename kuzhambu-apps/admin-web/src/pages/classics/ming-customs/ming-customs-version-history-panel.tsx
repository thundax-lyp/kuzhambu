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
    MingCustomsContentVersionRecord,
    MingCustomsRecord,
    MingCustomsVersionSnapshot
} from "./ming-customs-types";

const { Text } = Typography;

const compareFields: Array<{
    key: keyof MingCustomsVersionSnapshot;
    label: string;
}> = [
    { key: "title", label: "标题" },
    { key: "category", label: "分类" },
    { key: "chapter", label: "章节" },
    { key: "section", label: "节" },
    { key: "summary", label: "概述" },
    { key: "contentFormat", label: "正文格式" },
    { key: "content", label: "正文" },
    { key: "originalExcerpts", label: "原文摘录" }
];

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
};

const readSnapshot = (
    version?: MingCustomsContentVersionRecord | null
): MingCustomsVersionSnapshot | null => {
    if (!version?.snapshotJson) {
        return null;
    }
    try {
        return JSON.parse(version.snapshotJson) as MingCustomsVersionSnapshot;
    } catch {
        return null;
    }
};

const readCurrentValue = (
    entry: MingCustomsRecord | null | undefined,
    key: keyof MingCustomsVersionSnapshot
) => {
    if (!entry) {
        return undefined;
    }
    return entry[key as keyof MingCustomsRecord];
};

const formatValue = (value: unknown) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    return String(value);
};

const readTagNames = (snapshot: MingCustomsVersionSnapshot | null) => {
    if (!snapshot?.tags?.length) {
        return ["-"];
    }
    return snapshot.tags
        .map((tag) => tag.tagNameSnapshot || "-")
        .filter((tagName) => Boolean(tagName));
};

const readQaPairs = (snapshot: MingCustomsVersionSnapshot | null) => {
    if (!snapshot?.qaPairs?.length) {
        return [];
    }
    return snapshot.qaPairs
        .map((qaPair) => {
            const question = qaPair.question?.trim();
            const answer = qaPair.answer?.trim();
            if (!question && !answer) {
                return null;
            }

            return {
                answer: answer || "-",
                question: question || "-"
            };
        })
        .filter((qaPair): qaPair is { question: string; answer: string } => qaPair !== null);
};

interface MingCustomsVersionHistoryPanelProps {
    currentEntry?: MingCustomsRecord | null;
    detailLoading?: boolean;
    listLoading?: boolean;
    resetting?: boolean;
    selectedVersion?: MingCustomsContentVersionRecord | null;
    versions: MingCustomsContentVersionRecord[];
    onResetVersion: (version: MingCustomsContentVersionRecord) => void;
    onSelectVersion: (version: MingCustomsContentVersionRecord) => void;
}

export const MingCustomsVersionHistoryPanel = ({
    currentEntry,
    detailLoading = false,
    listLoading = false,
    resetting = false,
    selectedVersion,
    versions,
    onResetVersion,
    onSelectVersion
}: MingCustomsVersionHistoryPanelProps) => {
    const snapshot = readSnapshot(selectedVersion);
    const canResetVersion = Boolean(selectedVersion && snapshot);

    return (
        <section className="ming-customs-version-history-panel" aria-label="明代习俗版本历史面板">
            <div className="ming-customs-version-history-panel-grid">
                <KuzhambuList<MingCustomsContentVersionRecord>
                    as="ol"
                    className="ming-customs-version-history-list"
                    aria-label="明代习俗版本历史列表"
                    loading={listLoading}
                    dataSource={versions}
                    empty="暂无版本历史"
                    renderItem={(version) => (
                        <KuzhambuListItem
                            key={version.id}
                            actions={[
                                <KuzhambuButton
                                    testId={`ming-customs-version-view-${version.id}-button`}
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
                <div className="ming-customs-version-history-detail" aria-busy={detailLoading}>
                    {selectedVersion ? (
                        <KuzhambuSpace
                            orientation="vertical"
                            size="middle"
                            className="ming-customs-version-history-detail-stack"
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
                                <>
                                    <Descriptions
                                        className="ming-customs-version-compare"
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
                                                formatValue(currentValue) !==
                                                formatValue(historyValue);
                                            return (
                                                <Descriptions.Item
                                                    key={field.key}
                                                    label={field.label}
                                                    className={changed ? "is-changed" : undefined}
                                                >
                                                    <KuzhambuSpace orientation="vertical" size={2}>
                                                        <Text>
                                                            当前：{formatValue(currentValue)}
                                                        </Text>
                                                        <Text
                                                            type={changed ? "warning" : "secondary"}
                                                        >
                                                            历史：{formatValue(historyValue)}
                                                        </Text>
                                                    </KuzhambuSpace>
                                                </Descriptions.Item>
                                            );
                                        })}
                                    </Descriptions>
                                    <Descriptions size="small" column={1} bordered>
                                        <Descriptions.Item label="确认标签">
                                            <KuzhambuSpace wrap size="small">
                                                {readTagNames(snapshot).map((tagName, index) => (
                                                    <Tag key={`${tagName}-${index}`}>{tagName}</Tag>
                                                ))}
                                            </KuzhambuSpace>
                                        </Descriptions.Item>
                                        <Descriptions.Item label="确认问答">
                                            {readQaPairs(snapshot).length ? (
                                                <KuzhambuSpace orientation="vertical" size={2}>
                                                    {readQaPairs(snapshot).map((qaPair, index) => (
                                                        <Text key={`${qaPair.question}-${index}`}>
                                                            Q: {qaPair.question}；A: {qaPair.answer}
                                                        </Text>
                                                    ))}
                                                </KuzhambuSpace>
                                            ) : (
                                                "-"
                                            )}
                                        </Descriptions.Item>
                                    </Descriptions>
                                </>
                            ) : (
                                <KuzhambuAlert
                                    type="warning"
                                    showIcon
                                    title="版本快照为空或无法解析"
                                />
                            )}
                            <KuzhambuButton
                                testId={`ming-customs-version-restore-${selectedVersion.id}-button`}
                                danger
                                disabled={!canResetVersion}
                                loading={resetting}
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
