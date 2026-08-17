import { Empty, Spin, Typography } from "antd";
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
    const sections: Array<KuzhambuSegmentedDrawerSection<GraphMaterialDrawerSection>> = [
        {
            content: <MaterialOverviewPanel detail={detail} />,
            label: "概览",
            value: "OVERVIEW"
        },
        {
            content: (
                <MaterialDetailPlaceholder testId="knowledge-graph-material-detail-draft-graph-section">
                    草稿图谱待接入。
                </MaterialDetailPlaceholder>
            ),
            label: "草稿图谱",
            value: "DRAFT_GRAPH"
        },
        {
            content: (
                <MaterialDetailPlaceholder testId="knowledge-graph-material-detail-tasks-section">
                    任务摘要待接入。
                </MaterialDetailPlaceholder>
            ),
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
