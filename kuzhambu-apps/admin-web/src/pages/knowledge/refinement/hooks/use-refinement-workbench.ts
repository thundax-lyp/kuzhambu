import { useMemo, useState } from "react";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import type {
    QualityAnnotationTarget,
    RefinementApplyRecord,
    RefinementDetailRecord,
    RefinementEntityRecord,
    RefinementRelationRecord,
    RefinementTaskPageQuery
} from "../refinement-types";

export const readRefinementDetailTaskId = (detail: RefinementDetailRecord | null) =>
    detail?.refinementTaskId ?? 0;

export const useRefinementWorkbench = () => {
    const [taskQuery, setTaskQuery] = useState<RefinementTaskPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [detail, setDetail] = useState<RefinementDetailRecord | null>(null);
    const [entityEditorOpen, setEntityEditorOpen] = useState(false);
    const [editingEntity, setEditingEntity] = useState<RefinementEntityRecord | null>(null);
    const [deletingEntity, setDeletingEntity] = useState<RefinementEntityRecord | null>(null);
    const [relationEditorOpen, setRelationEditorOpen] = useState(false);
    const [editingRelation, setEditingRelation] = useState<RefinementRelationRecord | null>(null);
    const [deletingRelation, setDeletingRelation] = useState<RefinementRelationRecord | null>(null);
    const [annotationTarget, setAnnotationTarget] = useState<QualityAnnotationTarget | null>(null);
    const [applyFollowUp, setApplyFollowUp] = useState<RefinementApplyRecord | null>(null);

    const detailEyebrow = useMemo(() => {
        if (!detail) {
            return "先从左侧任务列表打开一条精修任务";
        }
        return `${detail.taskType || "GRAPH"} / ${detail.sourceCategoryName || detail.sourceCategoryCode || "-"}`;
    }, [detail]);

    return {
        annotationTarget,
        applyFollowUp,
        deletingEntity,
        deletingRelation,
        detail,
        detailEyebrow,
        detailReady: detail !== null,
        editingEntity,
        editingRelation,
        entityEditorOpen,
        relationEditorOpen,
        setAnnotationTarget,
        setApplyFollowUp,
        setDeletingEntity,
        setDeletingRelation,
        setDetail,
        setEditingEntity,
        setEditingRelation,
        setEntityEditorOpen,
        setRelationEditorOpen,
        setTaskQuery,
        taskQuery
    };
};
