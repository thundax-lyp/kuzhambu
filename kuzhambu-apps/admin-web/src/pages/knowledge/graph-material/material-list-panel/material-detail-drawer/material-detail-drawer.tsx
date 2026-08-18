import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Spin } from "antd";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuSegmentedDrawer,
    KuzhambuSpace
} from "@/components";
import type { KuzhambuSegmentedDrawerSection } from "@/components";
import type {
    GraphMaterialDrawerSection,
    GraphMaterialListRecord
} from "@/pages/knowledge/graph-material/graph-material-types";
import * as service from "@/pages/knowledge/graph-material/graph-material-service";
import { MaterialDraftGraphSection } from "./material-draft-graph-section";
import { MaterialOverviewSection } from "./material-overview-section";
import { MaterialPublicationChangesSection } from "./material-publication-changes-section";
import { MaterialTaskSummarySection } from "./material-task-summary-section";
import "./material-detail-drawer.css";

interface MaterialDetailDrawerProps {
    record: GraphMaterialListRecord | null;
    onClose: () => void;
}

const getErrorMessage = (error: unknown) =>
    error instanceof Error ? error.message : "请稍后重试。";

export const MaterialDetailDrawer = ({ record, onClose }: MaterialDetailDrawerProps) => {
    const contentRef = record?.source.contentRef ?? null;
    const contentRefKey = contentRef
        ? `${contentRef.contentType}:${contentRef.contentRefId}`
        : null;
    const [activeSectionState, setActiveSectionState] = useState<{
        contentRefKey: string | null;
        section: GraphMaterialDrawerSection;
    }>({
        contentRefKey: null,
        section: "OVERVIEW"
    });
    const activeSection =
        activeSectionState.contentRefKey === contentRefKey
            ? activeSectionState.section
            : "OVERVIEW";
    const materialDetailQuery = useQuery({
        enabled: contentRef !== null,
        queryFn: () => {
            if (!contentRef) {
                throw new Error("未选择素材");
            }
            return service.getMaterial({ contentRef });
        },
        queryKey: [
            "knowledge",
            "graph-material",
            "detail",
            contentRef?.contentType,
            contentRef?.contentRefId
        ]
    });
    const detail = materialDetailQuery.data ?? null;
    const error = materialDetailQuery.error;
    const loading = materialDetailQuery.isFetching;
    const open = record !== null;
    const title =
        record?.material?.title ?? detail?.source.title ?? record?.source.title ?? "素材详情";
    const closeDrawer = () => {
        setActiveSectionState({ contentRefKey: null, section: "OVERVIEW" });
        onClose();
    };
    const changeSection = (section: GraphMaterialDrawerSection) => {
        setActiveSectionState({ contentRefKey, section });
    };

    const sections: Array<KuzhambuSegmentedDrawerSection<GraphMaterialDrawerSection>> = [
        {
            content: <MaterialOverviewSection detail={detail} />,
            label: "概览",
            value: "OVERVIEW"
        },
        {
            content: <MaterialDraftGraphSection detail={detail} />,
            label: "草稿图谱",
            value: "DRAFT_GRAPH"
        },
        {
            content: <MaterialTaskSummarySection detail={detail} />,
            label: "任务",
            value: "TASKS"
        },
        {
            content: <MaterialPublicationChangesSection detail={detail} />,
            label: "发布变更",
            value: "PUBLICATION_CHANGES"
        }
    ];

    return (
        <KuzhambuSegmentedDrawer<GraphMaterialDrawerSection>
            activeSection={activeSection}
            destroyOnHidden
            footerActions={[
                {
                    action: closeDrawer,
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
            onClose={closeDrawer}
            onSectionChange={changeSection}
        >
            <KuzhambuSpace
                className="knowledge-graph-material-detail-drawer-head"
                orientation="vertical"
                size={12}
            >
                {error ? (
                    <KuzhambuAlert
                        action={
                            <KuzhambuButton
                                testId="knowledge-graph-material-detail-retry-button"
                                size="small"
                                onClick={() => void materialDetailQuery.refetch()}
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
