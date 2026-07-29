import { SwapOutlined } from "@ant-design/icons";
import { Empty } from "antd";
import { KuzhambuButton } from "@/components";
import type {
    SancaiEntryRecord,
    SancaiVisualAssetRecord
} from "@/pages/classics/sancai-visual/sancai-visual-types";
import { SancaiEntryVisualSection } from "../sancai-entry-visual-section";
import { SancaiVisualEntryContext } from "../sancai-visual-entry-context";

interface SancaiVisualWorkbenchProps {
    entry: SancaiEntryRecord | null;
    isUpdatingVisualAsset: boolean;
    onRefinementChanged: () => Promise<void> | void;
    onSelectEntry: () => void;
    onUpdateVisualAsset: (
        asset: SancaiVisualAssetRecord
    ) => Promise<SancaiVisualAssetRecord | void>;
    onUseVisualAsset: (asset: SancaiVisualAssetRecord) => void;
}

export const SancaiVisualWorkbench = ({
    entry,
    isUpdatingVisualAsset,
    onRefinementChanged,
    onSelectEntry,
    onUpdateVisualAsset,
    onUseVisualAsset
}: SancaiVisualWorkbenchProps) => {
    return (
        <section className="sancai-visual-main-panel" aria-label="三才图会视觉处理工作台">
            {entry ? (
                <>
                    <SancaiVisualEntryContext entry={entry} onSelectEntry={onSelectEntry} />
                    <SancaiEntryVisualSection
                        entry={entry}
                        isUpdatingVisualAsset={isUpdatingVisualAsset}
                        onPreviewStateChange={() => undefined}
                        onRefinementChanged={onRefinementChanged}
                        onUpdateVisualAsset={onUpdateVisualAsset}
                        onUseVisualAsset={onUseVisualAsset}
                    />
                </>
            ) : (
                <Empty
                    className="sancai-visual-empty"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                    description="请先选择要处理的稿件"
                >
                    <KuzhambuButton
                        testId="classics-sancai-visual-empty-entry-picker-button"
                        type="primary"
                        icon={<SwapOutlined />}
                        onClick={onSelectEntry}
                    >
                        选择稿件
                    </KuzhambuButton>
                </Empty>
            )}
        </section>
    );
};
