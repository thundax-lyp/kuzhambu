import { Empty, Spin, Typography } from "antd";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuSegmentedDrawer,
    KuzhambuSpace
} from "@/components";
import type { KuzhambuSegmentedDrawerSection } from "@/components";
import type {
    GraphMaterialDetailRecord,
    GraphMaterialDrawerSection,
    GraphMaterialRecord
} from "@/pages/knowledge/graph-material/graph-material-types";
import { MaterialOverviewPanel } from "@/pages/knowledge/graph-material/material-overview-panel";
import { MaterialTaskSummaryPanel } from "@/pages/knowledge/graph-material/material-task-summary-panel";
import { MaterialDraftCanvas } from "@/pages/knowledge/graph-material/material-draft-canvas";

const { Text } = Typography;

interface MaterialDetailDrawerProps {
    activeSection: GraphMaterialDrawerSection;
    detail: GraphMaterialDetailRecord | null;
    error?: unknown;
    loading?: boolean;
    material: GraphMaterialRecord | null;
    onClose: () => void;
    onRetry: () => void;
    onSectionChange: (section: GraphMaterialDrawerSection) => void;
    open: boolean;
}

const getErrorMessage = (error: unknown) =>
    error instanceof Error ? error.message : "请稍后重试。";

const MaterialDetailPlaceholder = ({ children, testId }: { children: string; testId: string }) => (
    <Empty data-testid={testId} description={children} />
);

export const MaterialDetailDrawer = ({
    activeSection,
    detail,
    error,
    loading = false,
    material,
    onClose,
    onRetry,
    onSectionChange,
    open
}: MaterialDetailDrawerProps) => {
    const canApplyGraph = hasPermission("knowledge:graph:apply");
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const activeMaterial = material ?? detail?.material ?? null;
    const sections: Array<KuzhambuSegmentedDrawerSection<GraphMaterialDrawerSection>> = [
        {
            content: <MaterialOverviewPanel detail={detail} />,
            label: "概览",
            value: "OVERVIEW"
        },
        {
            content: (
                <div data-testid="knowledge-graph-material-detail-draft-graph-section">
                    {activeMaterial ? (
                        <MaterialDraftCanvas
                            canApplyGraph={canApplyGraph}
                            canEditGraph={canEditGraph}
                            detail={detail}
                            material={activeMaterial}
                        />
                    ) : (
                        <MaterialDetailPlaceholder testId="knowledge-graph-material-detail-draft-graph-empty">
                            素材尚未初始化，暂无草稿图谱。
                        </MaterialDetailPlaceholder>
                    )}
                </div>
            ),
            label: "草稿图谱",
            value: "DRAFT_GRAPH"
        },
        {
            content: <MaterialTaskSummaryPanel detail={detail} />,
            label: "任务",
            value: "TASKS"
        },
        {
            content: (
                <MaterialDetailPlaceholder testId="knowledge-graph-material-detail-publication-changes-section">
                    发布变更待接入。
                </MaterialDetailPlaceholder>
            ),
            label: "发布变更",
            value: "PUBLICATION_CHANGES"
        }
    ];
    const title = material?.title ?? detail?.source.title ?? "素材详情";

    return (
        <KuzhambuSegmentedDrawer<GraphMaterialDrawerSection>
            activeSection={activeSection}
            destroyOnHidden
            footerActions={[
                {
                    action: onClose,
                    testId: "knowledge-graph-material-detail-close-button",
                    title: "关闭"
                }
            ]}
            open={open}
            sectionClassName="knowledge-graph-material-detail-drawer-section"
            sections={sections}
            size="large"
            testId="knowledge-graph-material-detail-drawer"
            title={title}
            onClose={onClose}
            onSectionChange={onSectionChange}
        >
            <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                <Text type="secondary">
                    {detail?.source.contentRef.contentType ?? material?.contentType ?? "-"} / #
                    {detail?.source.contentRef.contentRefId ??
                        material?.contentRef.contentRefId ??
                        "-"}
                </Text>
                {error ? (
                    <KuzhambuAlert
                        action={
                            <KuzhambuButton
                                testId="knowledge-graph-material-detail-retry-button"
                                size="small"
                                onClick={onRetry}
                            >
                                重试加载素材详情
                            </KuzhambuButton>
                        }
                        description={getErrorMessage(error)}
                        title="素材详情加载失败"
                        type="error"
                        showIcon
                    />
                ) : null}
                {loading ? <Spin tip="素材详情加载中" /> : null}
            </KuzhambuSpace>
        </KuzhambuSegmentedDrawer>
    );
};
