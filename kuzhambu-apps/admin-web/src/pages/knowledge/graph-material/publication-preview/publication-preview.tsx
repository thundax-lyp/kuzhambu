import { useState } from "react";
import { KuzhambuAlert, KuzhambuButton, KuzhambuSpace, KuzhambuTag } from "@/components";
import { graphMaterialMockData } from "../__mocks__/graph-mock-data";

const PREVIEW_TYPE_LABELS = {
    green: "新增对象",
    orange: "关联已有对象",
    red: "冲突待处理",
    blue: "已发布对象"
} as const;

const PREVIEW_TAG_TYPES = {
    green: "success",
    orange: "warning",
    red: "danger",
    blue: "info"
} as const;

export const PublicationPreview = () => {
    const [isConflictResolved, setIsConflictResolved] = useState(false);
    const [isFrozen, setIsFrozen] = useState(false);
    const [isWithdrawn, setIsWithdrawn] = useState(false);

    return (
        <KuzhambuSpace orientation="vertical" size={10} style={{ width: "100%" }}>
            <strong>发布预览</strong>
            {graphMaterialMockData.publicationPreview.map((item) => (
                <KuzhambuSpace key={item.id}>
                    <KuzhambuTag type={PREVIEW_TAG_TYPES[item.color]}>{item.color}</KuzhambuTag>
                    <span>{PREVIEW_TYPE_LABELS[item.color]}</span>
                    {item.color === "red" && !isConflictResolved ? (
                        <KuzhambuButton
                            size="small"
                            testId="knowledge-graph-material-resolve-conflict-button"
                            onClick={() => setIsConflictResolved(true)}
                        >
                            标记冲突已解决
                        </KuzhambuButton>
                    ) : null}
                </KuzhambuSpace>
            ))}
            {!isConflictResolved ? (
                <KuzhambuAlert title="存在未解决冲突，发布不可用。" type="error" showIcon />
            ) : null}
            {isFrozen ? <KuzhambuAlert title="发布已冻结" type="info" showIcon /> : null}
            {isWithdrawn ? <KuzhambuAlert title="素材已撤回" type="warning" showIcon /> : null}
            <KuzhambuSpace>
                <KuzhambuButton
                    disabled={!isConflictResolved || isFrozen || isWithdrawn}
                    testId="knowledge-graph-material-publish-preview-button"
                    type="primary"
                    onClick={() => setIsFrozen(true)}
                >
                    发布
                </KuzhambuButton>
                <KuzhambuButton
                    disabled={!isFrozen || isWithdrawn}
                    testId="knowledge-graph-material-withdraw-preview-button"
                    onClick={() => setIsWithdrawn(true)}
                >
                    撤回
                </KuzhambuButton>
            </KuzhambuSpace>
        </KuzhambuSpace>
    );
};
