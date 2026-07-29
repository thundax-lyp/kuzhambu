import { ArrowLeftOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App } from "antd";
import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { KuzhambuAlert, KuzhambuButton, KuzhambuPage, KuzhambuSpace } from "@/components";
import { SancaiVisualEntryPickerModal } from "./components/sancai-visual-entry-picker-modal";
import { SancaiVisualWorkbench } from "./components/sancai-visual-workbench";
import * as entryService from "./sancai-visual-service";
import type { SancaiEntryRecord, SancaiVisualAssetRecord } from "./sancai-visual-types";

import "@/pages/classics/sancai/sancai-page.css";
import "./sancai-visual-page.css";

export const SancaiVisualPage = () => {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const queryClient = useQueryClient();
    const { message: messageApi } = App.useApp();
    const selectedEntryId = searchParams.get("entryId");
    const [entryPickerOpen, setEntryPickerOpen] = useState(false);
    const entryDetailQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "detail", selectedEntryId],
        queryFn: () => entryService.get(selectedEntryId ?? ""),
        enabled: Boolean(selectedEntryId),
        retry: false
    });
    const selectedEntry = entryDetailQuery.data ?? null;
    const updateVisualAssetMutation = useMutation({
        mutationFn: entryService.updateVisualAsset,
        onSuccess: async () => {
            await refreshVisualPageData();
            messageApi.success("三才视觉处理已采纳");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "视觉处理采纳失败");
        }
    });
    const changeCurrentVisualAssetMutation = useMutation({
        mutationFn: entryService.changeCurrentVisualAsset,
        onSuccess: async () => {
            await refreshVisualPageData();
            messageApi.success("当前视觉处理版本已切换");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "视觉处理切换失败");
        }
    });

    const selectEntry = (entry: SancaiEntryRecord) => {
        setSearchParams({ entryId: entry.id });
        setEntryPickerOpen(false);
    };

    const refreshVisualPageData = async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "visual"] }),
            queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] })
        ]);
    };

    const updateVisualAsset = (asset: SancaiVisualAssetRecord) => {
        return updateVisualAssetMutation.mutateAsync({
            visualAssetId: asset.visualAssetId ?? asset.id ?? null,
            entryId: asset.entryId ?? selectedEntry?.id ?? null,
            versionNo: asset.versionNo,
            status: asset.status,
            sourceImageStorageObjectId: asset.sourceImageStorageObjectId,
            generatedImageStorageObjectId: asset.generatedImageStorageObjectId,
            currentUsed: asset.currentUsed,
            textWeight: asset.textWeight,
            imageWeight: asset.imageWeight,
            imageAnalysisMarkdown: asset.imageAnalysisMarkdown,
            fusionDescription: asset.fusionDescription,
            visualDescription: asset.visualDescription,
            generationParamsJson: asset.generationParamsJson
        });
    };

    const switchVisualAsset = (asset: SancaiVisualAssetRecord) => {
        const visualAssetId = asset.visualAssetId ?? asset.id;
        const entryId = asset.entryId ?? selectedEntry?.id;
        if (!visualAssetId || !entryId) {
            return;
        }
        changeCurrentVisualAssetMutation.mutate({ entryId, visualAssetId });
    };

    return (
        <KuzhambuPage
            className="sancai-page sancai-visual-page"
            title={
                <span className="sancai-visual-page-title">
                    三才图会 <small>视觉处理</small>
                </span>
            }
            description="选择稿件和来源图片，完成图片理解、图文融合、视觉描述和生图。"
            actions={
                <KuzhambuSpace className="sancai-page-actions">
                    <KuzhambuButton
                        testId="classics-sancai-visual-back-button"
                        icon={<ArrowLeftOutlined />}
                        onClick={() => navigate("/classics/sancai")}
                    >
                        返回
                    </KuzhambuButton>
                </KuzhambuSpace>
            }
        >
            {entryDetailQuery.isError ? (
                <KuzhambuAlert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    title="当前稿件加载失败"
                    description="请从稿件表重新进入，或选择其他稿件继续处理。"
                />
            ) : null}
            <SancaiVisualWorkbench
                entry={selectedEntry}
                isUpdatingVisualAsset={updateVisualAssetMutation.isPending}
                onRefinementChanged={refreshVisualPageData}
                onSelectEntry={() => setEntryPickerOpen(true)}
                onUpdateVisualAsset={updateVisualAsset}
                onUseVisualAsset={switchVisualAsset}
            />
            <SancaiVisualEntryPickerModal
                open={entryPickerOpen}
                onCancel={() => setEntryPickerOpen(false)}
                onSelect={selectEntry}
            />
        </KuzhambuPage>
    );
};
