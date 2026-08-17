import { useState } from "react";
import { Typography } from "antd";
import { EyeOutlined, ExportOutlined, ImportOutlined, RobotOutlined } from "@ant-design/icons";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";
import type {
    GraphBatchExtractionResultRecord,
    GraphBatchWithdrawalResultRecord,
    GraphContentRefRecord,
    GraphMaterialBatchPublicationResult,
    GraphMaterialListRecord
} from "@/pages/knowledge/graph-material/graph-material-types";

const { Text } = Typography;

type MaterialBatchActionKind = "EXTRACT" | "PUBLISH" | "WITHDRAW";

interface MaterialBatchActionResult {
    contentRef: GraphContentRefRecord;
    failureMessage?: string | null;
    key: string;
    statusText: string;
    success: boolean;
    title: string;
}

interface MaterialBatchActionsProps {
    canApplyGraph: boolean;
    extracting?: boolean;
    onExtract: (contentRefs: GraphContentRefRecord[]) => Promise<GraphBatchExtractionResultRecord>;
    onPublish: (
        records: GraphMaterialListRecord[]
    ) => Promise<readonly GraphMaterialBatchPublicationResult[]>;
    onViewTasks: (contentRefs: GraphContentRefRecord[]) => void;
    onWithdraw: (records: GraphMaterialListRecord[]) => Promise<GraphBatchWithdrawalResultRecord>;
    publishing?: boolean;
    selectedRecords: GraphMaterialListRecord[];
    withdrawing?: boolean;
}

const toContentRefKey = (contentRef: GraphContentRefRecord) =>
    `${contentRef.contentType}:${contentRef.contentRefId}`;

const findRecordByContentRef = (
    records: GraphMaterialListRecord[],
    contentRef: GraphContentRefRecord
) =>
    records.find(
        (record) =>
            record.source.contentRef.contentType === contentRef.contentType &&
            record.source.contentRef.contentRefId === contentRef.contentRefId
    );

const toExtractionResults = (
    records: GraphMaterialListRecord[],
    result: GraphBatchExtractionResultRecord
): MaterialBatchActionResult[] =>
    result.materials.map((item) => {
        const record = findRecordByContentRef(records, item.contentRef);
        return {
            contentRef: item.contentRef,
            failureMessage: item.failureMessage,
            key: toContentRefKey(item.contentRef),
            statusText: item.success
                ? `任务已创建${item.result?.id ? ` #${item.result.id}` : ""}`
                : "提取失败",
            success: item.success,
            title: record?.source.title ?? toContentRefKey(item.contentRef)
        };
    });

const toPublicationResults = (
    records: GraphMaterialListRecord[],
    results: readonly GraphMaterialBatchPublicationResult[]
): MaterialBatchActionResult[] =>
    records.map((record) => {
        const result = results.find((item) => item.materialId === record.material?.id);
        const contentRef = record.source.contentRef;
        if (!record.material) {
            return {
                contentRef,
                failureMessage: "素材尚未初始化，无法发布。",
                key: toContentRefKey(contentRef),
                statusText: "发布失败",
                success: false,
                title: record.source.title
            };
        }
        return {
            contentRef,
            failureMessage: result?.failureReason ?? (result ? null : "未返回发布结果。"),
            key: toContentRefKey(contentRef),
            statusText: result?.status === "PUBLISHED" ? "已发布" : "发布失败",
            success: result?.status === "PUBLISHED",
            title: record.source.title
        };
    });

const toWithdrawalResults = (
    records: GraphMaterialListRecord[],
    result: GraphBatchWithdrawalResultRecord
): MaterialBatchActionResult[] =>
    records.map((record) => {
        const contentRef = record.source.contentRef;
        const item = result.materials.find(
            (materialResult) =>
                toContentRefKey(materialResult.contentRef) === toContentRefKey(contentRef)
        );
        if (!record.material?.lockVersion) {
            return {
                contentRef,
                failureMessage: "素材尚未初始化或缺少锁版本，无法撤回。",
                key: toContentRefKey(contentRef),
                statusText: "撤回失败",
                success: false,
                title: record.source.title
            };
        }
        return {
            contentRef,
            failureMessage: item?.failureMessage ?? (item ? null : "未返回撤回结果。"),
            key: toContentRefKey(contentRef),
            statusText: item?.success ? "已撤回" : "撤回失败",
            success: Boolean(item?.success),
            title: record.source.title
        };
    });

const hasFailedResult = (results: MaterialBatchActionResult[]) =>
    results.some((result) => !result.success);

export const MaterialBatchActions = ({
    canApplyGraph,
    extracting = false,
    onExtract,
    onPublish,
    onViewTasks,
    onWithdraw,
    publishing = false,
    selectedRecords,
    withdrawing = false
}: MaterialBatchActionsProps) => {
    const [activeKind, setActiveKind] = useState<MaterialBatchActionKind | null>(null);
    const [results, setResults] = useState<MaterialBatchActionResult[]>([]);

    if (selectedRecords.length === 0) {
        return null;
    }

    const contentRefs = selectedRecords.map((record) => record.source.contentRef);
    const selectedCount = selectedRecords.length;
    const isBusy = extracting || publishing || withdrawing;

    const extractMaterials = async () => {
        setActiveKind("EXTRACT");
        const result = await onExtract(contentRefs);
        setResults(toExtractionResults(selectedRecords, result));
    };

    const publishMaterials = async () => {
        setActiveKind("PUBLISH");
        const result = await onPublish(selectedRecords);
        setResults(toPublicationResults(selectedRecords, result));
    };

    const withdrawMaterials = async () => {
        setActiveKind("WITHDRAW");
        const result = await onWithdraw(selectedRecords);
        setResults(toWithdrawalResults(selectedRecords, result));
    };

    return (
        <KuzhambuCard
            title={`批量动作（${selectedCount}）`}
            data-testid="knowledge-graph-material-batch-actions"
        >
            <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                <KuzhambuSpace wrap>
                    <KuzhambuButton
                        disabled={!canApplyGraph || isBusy}
                        loading={extracting}
                        testId="knowledge-graph-material-batch-extract-button"
                        type="primary"
                        onClick={() => void extractMaterials()}
                    >
                        <KuzhambuSpace size={6}>
                            <RobotOutlined />
                            批量提取
                        </KuzhambuSpace>
                    </KuzhambuButton>
                    <KuzhambuButton
                        disabled={!canApplyGraph || isBusy}
                        loading={publishing}
                        testId="knowledge-graph-material-batch-publish-button"
                        onClick={() => void publishMaterials()}
                    >
                        <KuzhambuSpace size={6}>
                            <ExportOutlined />
                            批量发布
                        </KuzhambuSpace>
                    </KuzhambuButton>
                    <KuzhambuButton
                        disabled={!canApplyGraph || isBusy}
                        loading={withdrawing}
                        testId="knowledge-graph-material-batch-withdraw-button"
                        danger
                        onClick={() => void withdrawMaterials()}
                    >
                        <KuzhambuSpace size={6}>
                            <ImportOutlined />
                            批量撤回
                        </KuzhambuSpace>
                    </KuzhambuButton>
                    <KuzhambuButton
                        disabled={isBusy}
                        testId="knowledge-graph-material-batch-view-tasks-button"
                        onClick={() => onViewTasks(contentRefs)}
                    >
                        <KuzhambuSpace size={6}>
                            <EyeOutlined />
                            查看任务
                        </KuzhambuSpace>
                    </KuzhambuButton>
                </KuzhambuSpace>
                {results.length > 0 ? (
                    <KuzhambuSpace orientation="vertical" size={8} style={{ width: "100%" }}>
                        {hasFailedResult(results) ? (
                            <KuzhambuAlert
                                title="部分素材处理失败，其余逐素材结果已保留。"
                                type="warning"
                                showIcon
                            />
                        ) : null}
                        <KuzhambuSpace orientation="vertical" size={6} style={{ width: "100%" }}>
                            {results.map((result) => (
                                <div
                                    key={`${activeKind ?? "RESULT"}:${result.key}`}
                                    data-testid={`knowledge-graph-material-batch-result-${result.key}`}
                                >
                                    <KuzhambuSpace wrap>
                                        <Text strong>{result.title}</Text>
                                        <KuzhambuTag type={result.success ? "success" : "danger"}>
                                            {result.statusText}
                                        </KuzhambuTag>
                                        {result.failureMessage ? (
                                            <Text type="danger">{result.failureMessage}</Text>
                                        ) : null}
                                    </KuzhambuSpace>
                                </div>
                            ))}
                        </KuzhambuSpace>
                    </KuzhambuSpace>
                ) : null}
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};
