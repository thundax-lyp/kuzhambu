import { App, Empty } from "antd";
import { useMemo, useState } from "react";
import type { ReactNode } from "react";
import { KuzhambuSegmentedDrawer, KuzhambuButton } from "@/components";

import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";

import { SancaiEntryBasicSection } from "./sancai-entry-basic-section";
import { openSancaiEntryPreviewWindow } from "./sancai-entry-preview-window";
import {
    SancaiEntryVisualSection,
    type SancaiEntryVisualPreviewState
} from "./sancai-entry-visual-section";
import { toEntryFormValues, type SancaiEntryFormValues } from "./sancai-entry-form-values";
import type {
    SancaiEntryRecord,
    SancaiVisualAssetRecord
} from "@/pages/classics/sancai/sancai-types";
import "./sancai-entry-edit-drawer.css";

type SancaiEntryEditDrawerSection = "basic" | "visual" | "tags" | "qa" | "versions";

interface SancaiEntryBasicPreviewState {
    imageUrl?: string;
}

interface SancaiEntryEditDrawerProps {
    imageContent?: ReactNode;
    qaContent?: ReactNode;
    tagContent?: ReactNode;
    versionContent?: ReactNode;
    categoryOptions?: Array<{ label: string; value: number }>;
    currentUserId?: number | string | null;
    entry: SancaiEntryRecord | undefined;
    initialCategoryId?: number | null;
    initialVolumeId?: number | null;
    isSubmitting: boolean;
    isUpdatingVisualAsset?: boolean;
    mode?: "create" | "edit";
    open: boolean;
    onCancel: () => void;
    onSubmit: (values: SancaiEntryFormValues) => void;
    onUseVisualAsset?: (asset: SancaiVisualAssetRecord) => void;
    onUpdateVisualAsset?: (asset: SancaiVisualAssetRecord) => void;
    onVisualRefinementChanged?: () => Promise<void> | void;
    onCreateTranslationTask?: (draft: SancaiEntryFormValues) => void;
    onCreateSummaryTask?: (draft: SancaiEntryFormValues) => void;
    isCreatingTranslationTask?: boolean;
    isCreatingSummaryTask?: boolean;
    translationTasks?: AiRefinementTaskRecord[];
    summaryTasks?: AiRefinementTaskRecord[];
    volumes?: Array<{ categoryId?: number | null; id: number; title?: string | null }>;
}

export const SancaiEntryEditDrawer = ({
    imageContent,
    qaContent,
    tagContent,
    versionContent,
    categoryOptions = [],
    currentUserId = null,
    entry,
    initialCategoryId = null,
    initialVolumeId = null,
    isSubmitting,
    isUpdatingVisualAsset = false,
    mode = "edit",
    open,
    onCancel,
    onSubmit,
    onUseVisualAsset,
    onUpdateVisualAsset,
    onVisualRefinementChanged = () => {},
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
    const [visualPreviewState, setVisualPreviewState] = useState<SancaiEntryVisualPreviewState>({
        currentVisualAsset: null
    });
    const entryId = mode === "edit" ? entry?.id : undefined;
    const volumeOptions = useMemo(
        () =>
            volumes
                .filter((volume) => volume.categoryId === entryDraft.categoryId)
                .map((volume) => ({
                    label: volume.title?.trim() || `卷 ${volume.id}`,
                    value: volume.id
                })),
        [entryDraft.categoryId, volumes]
    );
    const changeCategory = (categoryId: number | null) => {
        setEntryDraft((currentForm) => {
            const currentVolume = volumes.find((volume) => volume.id === currentForm.volumeId);
            const volumeStillMatches = currentVolume?.categoryId === categoryId;
            return {
                ...currentForm,
                categoryId,
                volumeId: volumeStillMatches ? currentForm.volumeId : null
            };
        });
    };
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
            form={entryDraft}
            imageContent={imageContent}
            isCreatingSummaryTask={isCreatingSummaryTask}
            isCreatingTranslationTask={isCreatingTranslationTask}
            mode={mode}
            setForm={setEntryDraft}
            summaryTasks={summaryTasks}
            translationTasks={translationTasks}
            volumeOptions={volumeOptions}
            onChangeCategory={changeCategory}
            onPreviewStateChange={setBasicPreviewState}
            onRequestSummaryTask={onCreateSummaryTask}
            onRequestTranslationTask={onCreateTranslationTask}
        />
    );

    const visualAssetContent =
        entryId && entry ? (
            <SancaiEntryVisualSection
                currentUserId={currentUserId}
                entry={entry}
                isUpdatingVisualAsset={isUpdatingVisualAsset}
                onRefinementChanged={onVisualRefinementChanged}
                onPreviewStateChange={setVisualPreviewState}
                onUpdateVisualAsset={onUpdateVisualAsset}
                onUseVisualAsset={onUseVisualAsset}
            />
        ) : null;

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
            label: "视觉处理",
            value: "visual",
            content: visualAssetContent,
            visible: mode === "edit"
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
