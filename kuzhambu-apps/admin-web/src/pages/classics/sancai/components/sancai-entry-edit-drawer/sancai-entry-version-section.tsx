import { SancaiVersionsPanel } from "../sancai-versions-panel";
import type {
    SancaiContentVersionRecord,
    SancaiEntryRecord
} from "@/pages/classics/sancai/sancai-types";

interface SancaiEntryVersionSectionProps {
    currentEntry: SancaiEntryRecord | undefined;
    detailLoading: boolean;
    isCreating: boolean;
    listLoading: boolean;
    resetting: boolean;
    selectedVersion: SancaiContentVersionRecord | null;
    versions: SancaiContentVersionRecord[];
    onResetVersion: (version: SancaiContentVersionRecord) => void;
    onSelectVersion: (version: SancaiContentVersionRecord) => void;
}

export const SancaiEntryVersionSection = ({
    currentEntry,
    detailLoading,
    isCreating,
    listLoading,
    resetting,
    selectedVersion,
    versions,
    onResetVersion,
    onSelectVersion
}: SancaiEntryVersionSectionProps) => {
    if (isCreating || !currentEntry) {
        return null;
    }

    return (
        <SancaiVersionsPanel
            currentEntry={currentEntry}
            detailLoading={detailLoading}
            listLoading={listLoading}
            resetting={resetting}
            selectedVersion={selectedVersion}
            versions={versions}
            onSelectVersion={onSelectVersion}
            onResetVersion={onResetVersion}
        />
    );
};
