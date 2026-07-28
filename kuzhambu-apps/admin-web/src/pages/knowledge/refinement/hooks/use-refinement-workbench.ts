import { useMemo, useState } from "react";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import type { RefinementTaskPageQuery } from "../refinement-service";
import type {
    QualityAnnotationTarget,
    RefinementApplyRecord,
    RefinementDetailRecord,
    RefinementEntityRecord,
    RefinementRelationRecord
} from "../refinement-types";

export type RefinementWorkbenchSection =
    "entities" | "relations" | "lineageNodes" | "lineageRelations" | "annotations" | "followUp";

export const readRefinementDetailTaskId = (detail: RefinementDetailRecord | null) =>
    detail?.refinementTaskId ?? "";

export const useRefinementWorkbench = () => {
    const [taskQuery, setTaskQuery] = useState<RefinementTaskPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [detail, setDetail] = useState<RefinementDetailRecord | null>(null);
    const [entityEditModalOpen, setEntityEditModalOpen] = useState(false);
    const [editingEntity, setEditingEntity] = useState<RefinementEntityRecord | null>(null);
    const [deletingEntity, setDeletingEntity] = useState<RefinementEntityRecord | null>(null);
    const [relationEditModalOpen, setRelationEditModalOpen] = useState(false);
    const [editingRelation, setEditingRelation] = useState<RefinementRelationRecord | null>(null);
    const [deletingRelation, setDeletingRelation] = useState<RefinementRelationRecord | null>(null);
    const [annotationTarget, setAnnotationTarget] = useState<QualityAnnotationTarget | null>(null);
    const [applyFollowUp, setApplyFollowUp] = useState<RefinementApplyRecord | null>(null);
    const [activeSection, setActiveSection] = useState<RefinementWorkbenchSection>("entities");

    const detailEyebrow = useMemo(() => {
        if (!detail) {
            return "先从任务列表打开一条精修任务";
        }
        return `${detail.taskType || "GRAPH"} / ${detail.sourceCategoryName || detail.sourceCategoryCode || "-"}`;
    }, [detail]);

    return {
        activeSection,
        annotationTarget,
        applyFollowUp,
        deletingEntity,
        deletingRelation,
        detail,
        detailEyebrow,
        detailReady: detail !== null,
        editingEntity,
        editingRelation,
        entityEditModalOpen,
        relationEditModalOpen,
        setAnnotationTarget,
        setActiveSection,
        setApplyFollowUp,
        setDeletingEntity,
        setDeletingRelation,
        setDetail,
        setEditingEntity,
        setEditingRelation,
        setEntityEditModalOpen,
        setRelationEditModalOpen,
        setTaskQuery,
        taskQuery
    };
};
