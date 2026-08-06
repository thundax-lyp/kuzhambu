import { App, Empty } from "antd";
import { EyeOutlined } from "@ant-design/icons";
import { useMemo, useState } from "react";
import type { ReactNode } from "react";
import { KuzhambuSegmentedDrawer, KuzhambuButton } from "@/components";

import { useSancaiEntryVisualPreviewState } from "@/pages/classics/common/hooks/use-sancai-entry-visual-preview-state";
import { isSameId } from "@/types/id";

import { SancaiEntryBasicSection } from "./sancai-entry-basic-section";
import { SancaiEntryQaSection } from "./sancai-entry-qa-section";
import { SancaiEntryTagSection } from "./sancai-entry-tag-section";
import { SancaiEntryVersionSection } from "./sancai-entry-version-section";
import { openSancaiEntryPreviewWindow } from "./open-sancai-entry-preview-window";
import {
    toEntryFormValues,
    type SancaiEntryFormValues
} from "./sancai-entry-edit-drawer-form-values";
import type { SancaiEntryRecord } from "@/pages/classics/sancai/sancai-types";
import "./sancai-entry-edit-drawer.css";

type SancaiEntryEditDrawerSection = "basic" | "tags" | "qa" | "versions";

interface SancaiEntryBasicPreviewState {
    imageUrl?: string;
}

interface SancaiEntryEditDrawerProps {
    categoryOptions?: Array<{ label: string; value: string }>;
    entry: SancaiEntryRecord | undefined;
    initialCategoryId?: string | null;
    initialVolumeId?: string | null;
    isSubmitting: boolean;
    mode?: "create" | "edit";
    open: boolean;
    readOnly?: boolean;
    onCancel: () => void;
    onSubmit: (values: SancaiEntryFormValues) => void;
    onEntryChanged: () => void | Promise<void>;
    volumes?: Array<{ categoryId?: string | null; id: string; title?: string | null }>;
}

export const SancaiEntryEditDrawer = ({
    categoryOptions = [],
    entry,
    initialCategoryId = null,
    initialVolumeId = null,
    isSubmitting,
    mode = "edit",
    open,
    readOnly = false,
    onCancel,
    onSubmit,
    onEntryChanged,
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
    const selectedEntryVolume = useMemo(
        () => volumes.find((volume) => isSameId(volume.id, entry?.volumeId)) ?? null,
        [entry?.volumeId, volumes]
    );
    const selectedEntryCategory = useMemo(
        () =>
            categoryOptions.find((category) =>
                isSameId(category.value, selectedEntryVolume?.categoryId)
            ) ?? null,
        [categoryOptions, selectedEntryVolume?.categoryId]
    );
    const categoryTitle = selectedEntryCategory?.label?.trim() || "未归类";
    const volumeTitle = selectedEntryVolume?.title?.trim() || "未选择卷目";
    const versionVolumeOptions = useMemo(
        () =>
            volumes.map((volume) => {
                const category = categoryOptions.find((option) =>
                    isSameId(option.value, volume.categoryId)
                );
                const volumeLabel = volume.title?.trim() || `卷 ${volume.id}`;
                return {
                    label: category?.label?.trim()
                        ? `${category.label.trim()} / ${volumeLabel}`
                        : volumeLabel,
                    value: volume.id
                };
            }),
        [categoryOptions, volumes]
    );

    const submitForm = () => {
        if (!entryDraft.volumeId) {
            messageApi.warning("请选择卷");
            return;
        }
        onSubmit(entryDraft);
    };
    const closeDrawer = () => {
        onCancel();
    };

    if (!entry && mode !== "create") {
        return null;
    }

    const basicContent = (
        <SancaiEntryBasicSection
            categoryOptions={categoryOptions}
            entryId={entryId}
            mode={mode}
            readOnly={readOnly}
            value={entryDraft}
            volumes={volumes}
            onChange={setEntryDraft}
            onPreviewStateChange={setBasicPreviewState}
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
            content: entry ? (
                <SancaiEntryTagSection
                    categoryTitle={categoryTitle}
                    entry={entry}
                    readOnly={readOnly}
                    volumeTitle={volumeTitle}
                    onChanged={onEntryChanged}
                />
            ) : (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无标签" />
            ),
            visible: mode === "edit"
        },
        {
            label: "问答",
            value: "qa",
            content: entry ? (
                <SancaiEntryQaSection
                    categoryTitle={categoryTitle}
                    entry={entry}
                    readOnly={readOnly}
                    volumeTitle={volumeTitle}
                    onChanged={onEntryChanged}
                />
            ) : (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无问答" />
            ),
            visible: mode === "edit"
        },
        {
            label: "版本",
            value: "versions",
            content: entry ? (
                <SancaiEntryVersionSection
                    currentEntry={entry}
                    isCreating={mode === "create"}
                    readOnly={readOnly}
                    volumeOptions={versionVolumeOptions}
                    onChanged={onEntryChanged}
                />
            ) : (
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
                        type="text"
                        icon={<EyeOutlined />}
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
            title={mode === "create" ? "新增条目" : readOnly ? "查看条目" : "编辑条目"}
            open={open}
            size="large"
            destroyOnHidden
            footerActions={
                readOnly
                    ? [
                          {
                              testId: "classics-sancai-sancai-entry-cancel-button",
                              title: "关闭",
                              action: closeDrawer
                          }
                      ]
                    : [
                          {
                              testId: "classics-sancai-sancai-entry-cancel-button",
                              title: "取消",
                              action: closeDrawer
                          },
                          {
                              testId: "classics-sancai-sancai-entry-create-button",
                              title: "保存",
                              type: "primary",
                              loading: isSubmitting,
                              action: submitForm
                          }
                      ]
            }
            onClose={closeDrawer}
            onSectionChange={setActiveSection}
        />
    );
};
