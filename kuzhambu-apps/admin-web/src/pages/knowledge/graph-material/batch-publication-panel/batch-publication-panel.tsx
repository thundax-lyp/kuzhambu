import { useState } from "react";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";
import type {
    GraphMaterialBatchPublicationResult,
    GraphMaterialRecord
} from "../graph-material-types";

interface BatchPublicationPanelProps {
    materials: GraphMaterialRecord[];
    onClose: () => void;
    open: boolean;
    results: readonly GraphMaterialBatchPublicationResult[];
}

export const BatchPublicationPanel = ({
    materials,
    onClose,
    open,
    results
}: BatchPublicationPanelProps) => {
    const [isConfirmed, setIsConfirmed] = useState(false);
    if (!open) {
        return null;
    }

    return (
        <KuzhambuCard title="批量发布预览" data-testid="knowledge-graph-material-batch-panel">
            <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                {materials.map((material) => {
                    const result = results.find((item) => item.materialId === material.id);
                    return (
                        <div key={material.id}>
                            <strong>{material.title}</strong>
                            {isConfirmed && result ? (
                                <KuzhambuSpace>
                                    <KuzhambuTag
                                        type={result.status === "FAILED" ? "danger" : "success"}
                                    >
                                        {result.status === "FAILED" ? "失败" : "成功"}
                                    </KuzhambuTag>
                                    {result.failureReason ? (
                                        <span>{result.failureReason}</span>
                                    ) : null}
                                </KuzhambuSpace>
                            ) : (
                                <span>等待确认</span>
                            )}
                        </div>
                    );
                })}
                {isConfirmed &&
                materials.some(
                    (material) =>
                        results.find((result) => result.materialId === material.id)?.status ===
                        "FAILED"
                ) ? (
                    <KuzhambuAlert
                        title="部分素材发布失败，其余结果已保留。"
                        type="warning"
                        showIcon
                    />
                ) : null}
                <KuzhambuSpace>
                    {!isConfirmed ? (
                        <KuzhambuButton
                            testId="knowledge-graph-material-confirm-batch-publication-button"
                            type="primary"
                            onClick={() => setIsConfirmed(true)}
                        >
                            确认批量发布
                        </KuzhambuButton>
                    ) : null}
                    <KuzhambuButton
                        testId="knowledge-graph-material-close-batch-panel-button"
                        onClick={onClose}
                    >
                        关闭
                    </KuzhambuButton>
                </KuzhambuSpace>
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};
