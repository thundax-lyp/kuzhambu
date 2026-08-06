import { useMutation, useQuery } from "@tanstack/react-query";
import { App } from "antd";
import { useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { SancaiVersionsPanel } from "@/pages/classics/sancai/sancai-versions-panel";
import * as entryService from "@/pages/classics/sancai/sancai-entry-service";
import type {
    SancaiContentVersionRecord,
    SancaiEntryRecord
} from "@/pages/classics/sancai/sancai-types";
import { isSameId } from "@/types/id";

interface SancaiEntryVersionSectionProps {
    currentEntry: SancaiEntryRecord | undefined;
    isCreating: boolean;
    readOnly?: boolean;
    volumeOptions?: Array<{ label: string; value: string }>;
    onChanged: () => void | Promise<void>;
}

export const SancaiEntryVersionSection = ({
    currentEntry,
    isCreating,
    readOnly = false,
    volumeOptions = [],
    onChanged
}: SancaiEntryVersionSectionProps) => {
    const { message: messageApi, modal: modalApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const [selectedVersionKey, setSelectedVersionKey] = useState<{
        entryId: string;
        versionId: string;
    } | null>(null);
    const selectedVersionId =
        selectedVersionKey && selectedVersionKey.entryId === currentEntry?.id
            ? selectedVersionKey.versionId
            : null;

    const versionsQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "versions", currentEntry?.id],
        queryFn: () => entryService.listVersions(currentEntry?.id ?? ""),
        enabled: !isCreating && Boolean(currentEntry?.id),
        retry: false
    });
    const versions = versionsQuery.data || [];
    const versionDetailQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "version", currentEntry?.id, selectedVersionId],
        queryFn: () => entryService.getVersion(currentEntry?.id ?? "", selectedVersionId ?? ""),
        enabled: Boolean(currentEntry?.id && selectedVersionId),
        retry: false
    });
    const selectedVersion =
        versionDetailQuery.data ||
        versions.find((version) => isSameId(version.id, selectedVersionId)) ||
        null;
    const resetVersionMutation = useMutation({
        mutationFn: ({ entryId, versionId }: { entryId: string; versionId: string }) =>
            entryService.resetVersion(entryId, versionId),
        onSuccess: async () => {
            setSelectedVersionKey(null);
            await onChanged();
            modalApi.success({
                title: "三才图会版本已恢复",
                content: "已生成新的正式版本，并已将条目移动到恢复快照所在卷目的末尾。"
            });
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "版本恢复失败");
        }
    });

    if (isCreating || !currentEntry) {
        return null;
    }

    const resetVersion = (version: SancaiContentVersionRecord) => {
        confirm.danger({
            title: "恢复三才图会版本",
            message: `确认恢复版本 ${version.versionNo ?? version.id}？`,
            description: "恢复后会产生新的正式版本，并刷新条目详情、列表和版本历史。",
            okText: "恢复",
            onConfirm: () =>
                resetVersionMutation.mutateAsync({
                    entryId: currentEntry.id,
                    versionId: version.id
                })
        });
    };

    return (
        <SancaiVersionsPanel
            currentEntry={currentEntry}
            detailLoading={versionDetailQuery.isLoading}
            listLoading={versionsQuery.isLoading}
            resetting={resetVersionMutation.isPending}
            readOnly={readOnly}
            selectedVersion={selectedVersion}
            volumeOptions={volumeOptions}
            versions={versions}
            onSelectVersion={(version) =>
                setSelectedVersionKey({
                    entryId: currentEntry.id,
                    versionId: version.id
                })
            }
            onResetVersion={resetVersion}
        />
    );
};
