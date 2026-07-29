import { FileSearchOutlined } from "@ant-design/icons";
import { KuzhambuButton, KuzhambuSpace } from "@/components";

import type {
    ClassicsShareLinkStatus,
    ClassicsShareRecord
} from "@/pages/classics/common/classics-share-types";

const isExpiredAt = (expiresAt?: string | null) => {
    if (!expiresAt) {
        return false;
    }
    const timestamp = Date.parse(expiresAt);
    return Number.isNaN(timestamp) ? false : timestamp <= Date.now();
};

const canRestoreShare = (share?: ClassicsShareRecord | null) => {
    return share?.status === "REVOKED" && !isExpiredAt(share.expiresAt);
};

interface SharingAccessActionsProps {
    share: ClassicsShareRecord | undefined;
    onRefreshAccessRecords: () => void;
    onStatusChange: (share: ClassicsShareRecord, status: ClassicsShareLinkStatus | string) => void;
}

export const SharingAccessActions = ({
    share,
    onRefreshAccessRecords,
    onStatusChange
}: SharingAccessActionsProps) => {
    return (
        <KuzhambuSpace align="end">
            {share?.status === "ACTIVE" ? (
                <KuzhambuButton
                    testId="classics-sharing-sharing-action-button"
                    danger
                    onClick={() => onStatusChange(share, "REVOKED")}
                >
                    撤销
                </KuzhambuButton>
            ) : null}
            {canRestoreShare(share) ? (
                <KuzhambuButton
                    testId="classics-sharing-sharing-restore-button"
                    onClick={() => onStatusChange(share as ClassicsShareRecord, "ACTIVE")}
                >
                    恢复
                </KuzhambuButton>
            ) : null}
            <KuzhambuButton
                testId="classics-sharing-sharing-action-button-2"
                icon={<FileSearchOutlined />}
                onClick={onRefreshAccessRecords}
            >
                刷新访问记录
            </KuzhambuButton>
        </KuzhambuSpace>
    );
};
