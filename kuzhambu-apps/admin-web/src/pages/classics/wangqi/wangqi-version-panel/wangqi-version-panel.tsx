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
    WangqiContentVersionRecord,
    WangqiDocumentRecord,
    WangqiVersionSnapshot
} from "@/pages/classics/wangqi/wangqi-types";

import "./wangqi-version-panel.css";

const { Text } = Typography;

const compareFields: Array<{
    key: keyof WangqiVersionSnapshot;
    label: string;
}> = [
    { key: "title", label: "标题" },
    { key: "summary", label: "摘要" },
    { key: "contentFormat", label: "格式" },
    { key: "content", label: "正文" },
    { key: "documentTime", label: "文档时间" },
    { key: "visibility", label: "可见性" }
];

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
};

const formatYearMonth = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    const month = String(date.getMonth() + 1).padStart(2, "0");
    return `${date.getFullYear()}/${month}`;
};

const readSnapshot = (
    version?: WangqiContentVersionRecord | null
): WangqiVersionSnapshot | null => {
    if (!version?.snapshotJson) {
        return null;
    }
    try {
        return JSON.parse(version.snapshotJson) as WangqiVersionSnapshot;
    } catch {
        return null;
    }
};

const readCurrentValue = (
    document: WangqiDocumentRecord | null | undefined,
    key: keyof WangqiVersionSnapshot
) => {
    if (!document) {
        return undefined;
    }
    return document[key as keyof WangqiDocumentRecord];
};

const formatValue = (value: unknown, key?: keyof WangqiVersionSnapshot) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    if (key === "documentTime") {
        return formatYearMonth(String(value));
    }
    return String(value);
};

const readTagNames = (snapshot: WangqiVersionSnapshot | null) => {
    if (!snapshot?.tags?.length) {
        return ["-"];
    }
    return snapshot.tags
        .map((tag) => tag.tagNameSnapshot || "-")
        .filter((tagName) => Boolean(tagName));
};

const readQaPairs = (snapshot: WangqiVersionSnapshot | null) => {
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

interface WangqiVersionPanelProps {
    currentDocument?: WangqiDocumentRecord | null;
    detailLoading?: boolean;
    listLoading?: boolean;
    resetting?: boolean;
    selectedVersion?: WangqiContentVersionRecord | null;
    versions: WangqiContentVersionRecord[];
    onResetVersion: (version: WangqiContentVersionRecord) => void;
    onSelectVersion: (version: WangqiContentVersionRecord) => void;
}

export const WangqiVersionPanel = ({
    currentDocument,
    detailLoading = false,
    listLoading = false,
    resetting = false,
    selectedVersion,
    versions,
    onResetVersion,
    onSelectVersion
}: WangqiVersionPanelProps) => {
    const snapshot = readSnapshot(selectedVersion);

    return (
        <section className="wangqi-version-panel" aria-label="王圻版本历史面板">
            <div className="wangqi-version-panel-grid">
                <KuzhambuList<WangqiContentVersionRecord>
                    as="ol"
                    className="wangqi-version-list"
                    aria-label="王圻版本历史列表"
                    loading={listLoading}
                    dataSource={versions}
                    empty="暂无版本历史"
                    renderItem={(version) => (
                        <KuzhambuListItem
                            key={version.id}
                            actions={[
                                <KuzhambuButton
                                    testId={`wangqi-version-view-${version.id}-button`}
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
                <div className="wangqi-version-detail" aria-busy={detailLoading}>
                    {selectedVersion ? (
                        <KuzhambuSpace
                            orientation="vertical"
                            size="middle"
                            className="wangqi-version-detail-stack"
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
                                        className="wangqi-version-compare"
                                        size="small"
                                        column={1}
                                        bordered
                                    >
                                        {compareFields.map((field) => {
                                            const currentValue = readCurrentValue(
                                                currentDocument,
                                                field.key
                                            );
                                            const historyValue = snapshot[field.key];
                                            const changed =
                                                formatValue(currentValue, field.key) !==
                                                formatValue(historyValue, field.key);
                                            return (
                                                <Descriptions.Item
                                                    key={field.key}
                                                    label={field.label}
                                                    className={changed ? "is-changed" : undefined}
                                                >
                                                    <KuzhambuSpace orientation="vertical" size={2}>
                                                        <Text>
                                                            当前：
                                                            {formatValue(currentValue, field.key)}
                                                        </Text>
                                                        <Text
                                                            type={changed ? "warning" : "secondary"}
                                                        >
                                                            历史：
                                                            {formatValue(historyValue, field.key)}
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
                                testId={`wangqi-version-restore-${selectedVersion.id}-button`}
                                danger
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
