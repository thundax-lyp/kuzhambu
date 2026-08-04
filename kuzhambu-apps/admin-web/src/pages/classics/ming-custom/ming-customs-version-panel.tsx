import { MingCustomsVersionHistoryPanel } from "./ming-customs-version-history-panel";
import type { MingCustomsContentVersionRecord, MingCustomsRecord } from "./ming-custom-types";

interface MingCustomsVersionPanelProps {
    currentEntry: MingCustomsRecord;
    detailLoading: boolean;
    listLoading: boolean;
    onResetVersion: (version: MingCustomsContentVersionRecord) => void;
    onSelectVersion: (version: MingCustomsContentVersionRecord) => void;
    resetting: boolean;
    selectedVersion: MingCustomsContentVersionRecord | null;
    versions: MingCustomsContentVersionRecord[];
}

export const MingCustomsVersionPanel = ({
    currentEntry,
    detailLoading,
    listLoading,
    onResetVersion,
    onSelectVersion,
    resetting,
    selectedVersion,
    versions
}: MingCustomsVersionPanelProps) => {
    return (
        <MingCustomsVersionHistoryPanel
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
