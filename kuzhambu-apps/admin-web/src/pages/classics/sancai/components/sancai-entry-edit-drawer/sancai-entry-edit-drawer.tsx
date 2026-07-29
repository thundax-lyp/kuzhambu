import { App, Empty } from "antd";
import { useState } from "react";
import type { ReactNode } from "react";
import { KuzhambuSegmentedDrawer, KuzhambuButton } from "@/components";

import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import { useSancaiEntryVisualPreviewState } from "@/pages/classics/common/hooks/use-sancai-entry-visual-preview-state";

import { SancaiEntryBasicSection } from "./sancai-entry-basic-section";
import { openSancaiEntryPreviewWindow } from "./sancai-entry-preview-window";
import { toEntryFormValues, type SancaiEntryFormValues } from "./sancai-entry-form-values";
import type { SancaiEntryRecord } from "@/pages/classics/sancai/sancai-types";
import "./sancai-entry-edit-drawer.css";

type SancaiEntryEditDrawerSection = "basic" | "tags" | "qa" | "versions";

interface SancaiEntryBasicPreviewState {
    imageUrl?: string;
}

interface SancaiEntryEditDrawerProps {
    qaContent?: ReactNode;
    tagContent?: ReactNode;
    versionContent?: ReactNode;
    categoryOptions?: Array<{ label: string; value: string }>;
    entry: SancaiEntryRecord | undefined;
    initialCategoryId?: string | null;
    initialVolumeId?: string | null;
    isSubmitting: boolean;
    mode?: "create" | "edit";
    open: boolean;
    onCancel: () => void;
    onSubmit: (values: SancaiEntryFormValues) => void;
    onCreateTranslationTask?: (draft: SancaiEntryFormValues) => void;
    onCreateSummaryTask?: (draft: SancaiEntryFormValues) => void;
    isCreatingTranslationTask?: boolean;
    isCreatingSummaryTask?: boolean;
    translationTasks?: AiRefinementTaskRecord[];
    summaryTasks?: AiRefinementTaskRecord[];
    volumes?: Array<{ categoryId?: string | null; id: string; title?: string | null }>;
}

export const SancaiEntryEditDrawer = ({
    qaContent,
    tagContent,
    versionContent,
    categoryOptions = [],
    entry,
    initialCategoryId = null,
    initialVolumeId = null,
    isSubmitting,
    mode = "edit",
    open,
    onCancel,
    onSubmit,
    onCreateTranslationTask,
    onCreateSummaryTask,
    isCreatingTranslationTask = false,
    isCreatingSummaryTask = false,
    translationTasks = [],
    summaryTasks = [],
    volumes = []
}: SancaiEntryEditDrawerProps) => {
    const { message: messageApi } = App.useApp();
    const [entryDraft, setEntryDraft] = useState<SancaiEntryFormValues>(() =>
        toEntryFormValues(entry, volumes, initialCategoryId, initialVolumeId)
    );
    const [activeSection, setActiveSection] = useState<SancaiEntryEditDrawerSection>("basic");
    const [basicPreviewState, setBasicPreviewState] = useState<SancaiEntryBasicPreviewState>({});
    const entryId = mode === "edit" ? entry?.id : undefined;
    const visualPreviewState = useSancaiEntryVisualPreviewState(entryId);

    const submitForm = () => {
        if (!entryDraft.volumeId) {
            messageApi.warning("请选择卷");
            return;
        }
        onSubmit(entryDraft);
    };

    if (!entry && mode !== "create") {
        return null;
    }

    const basicContent = (
        <SancaiEntryBasicSection
            categoryOptions={categoryOptions}
            entryId={entryId}
            isCreatingSummaryTask={isCreatingSummaryTask}
            isCreatingTranslationTask={isCreatingTranslationTask}
            mode={mode}
            summaryTasks={summaryTasks}
            translationTasks={translationTasks}
            value={entryDraft}
            volumes={volumes}
            onChange={setEntryDraft}
            onPreviewStateChange={setBasicPreviewState}
            onRequestSummaryTask={onCreateSummaryTask}
            onRequestTranslationTask={onCreateTranslationTask}
        />
    );

    const openPreviewWindow = () => {
        openSancaiEntryPreviewWindow({
            currentVisualAsset: visualPreviewState.currentVisualAsset,
            form: entryDraft,
            imageUrl: basicPreviewState.imageUrl,
            visualDescription: visualPreviewState.visualDescription,
            visualUrl: visualPreviewState.generatedPreviewUrl
        });
    };

    const sections = [
        {
            label: "基础信息",
            value: "basic",
            content: basicContent
        },
        {
            label: "标签",
            value: "tags",
            content: tagContent || (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无标签" />
            ),
            visible: mode === "edit"
        },
        {
            label: "问答",
            value: "qa",
            content: qaContent || (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无问答" />
            ),
            visible: mode === "edit"
        },
        {
            label: "版本",
            value: "versions",
            content: versionContent || (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无版本" />
            ),
            visible: mode === "edit"
        }
    ] satisfies Array<{
        content: ReactNode;
        label: string;
        value: SancaiEntryEditDrawerSection;
        visible?: boolean;
    }>;

    return (
        <KuzhambuSegmentedDrawer
            activeSection={activeSection}
            extraClassName="sancai-entry-edit-drawer-header-actions"
            headerExtra={
                mode === "edit" ? (
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-preview-sancai-entry-button"
                        onClick={openPreviewWindow}
                    >
                        预览
                    </KuzhambuButton>
                ) : undefined
            }
            sectionClassName="sancai-entry-edit-drawer-section"
            sections={sections}
            segmentedClassName="sancai-entry-edit-drawer-header-sections"
            showSegmented={mode === "edit"}
            testId="classics-sancai-sancai-entry-editor-drawer"
            title={mode === "create" ? "新增条目" : "编辑条目"}
            open={open}
            size="large"
            destroyOnHidden
            footerActions={[
                {
                    testId: "classics-sancai-sancai-entry-cancel-button",
                    title: "取消",
                    action: onCancel
                },
                {
                    testId: "classics-sancai-sancai-entry-create-button",
                    title: "保存",
                    type: "primary",
                    loading: isSubmitting,
                    action: submitForm
                }
            ]}
            onClose={onCancel}
            onSectionChange={setActiveSection}
        />
    );
};
