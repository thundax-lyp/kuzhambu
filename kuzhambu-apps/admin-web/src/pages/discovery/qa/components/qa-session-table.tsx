import { CloseOutlined } from "@ant-design/icons";
import { Empty } from "antd";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import type { DiscoveryQaSessionRecord } from "../qa-types";

const toSessionId = (value?: string | null) => {
    return typeof value === "string" && value.trim().length ? value : null;
};

const sessionTitle = (session?: DiscoveryQaSessionRecord) => {
    if (session?.title) {
        return session.title;
    }
    return session?.sessionId ? `会话 ${session.sessionId}` : "未命名会话";
};

export interface QaSessionTableProps {
    deleting: boolean;
    exportDisabled: boolean;
    exporting: boolean;
    loading: boolean;
    onCreate: () => void;
    onDelete: (sessionId: string) => void;
    onExport: () => void;
    onSelect: (sessionId: string) => void;
    opening: boolean;
    selectedSessionId: string | null;
    sessions: DiscoveryQaSessionRecord[];
}

export const QaSessionTable = ({
    deleting,
    exportDisabled,
    exporting,
    loading,
    onCreate,
    onDelete,
    onExport,
    onSelect,
    opening,
    selectedSessionId,
    sessions
}: QaSessionTableProps) => {
    return (
        <aside className="discovery-qa-page__sidebar">
            <KuzhambuButton
                block
                testId="discovery-qa-create-session-button"
                loading={opening}
                type="primary"
                onClick={onCreate}
            >
                新建对话
            </KuzhambuButton>
            <div className="discovery-qa-page__session-list" aria-label="问答会话">
                {sessions.length ? (
                    sessions.map((session) => {
                        const sessionId = toSessionId(session.sessionId);
                        if (sessionId === null) {
                            return null;
                        }

                        return (
                            <div key={sessionId} className="discovery-qa-page__session-item">
                                <KuzhambuButton
                                    block
                                    className="discovery-qa-page__select-session"
                                    testId="discovery-qa-select-session-button"
                                    type={sessionId === selectedSessionId ? "primary" : "default"}
                                    onClick={() => onSelect(sessionId)}
                                >
                                    {sessionTitle(session)}
                                </KuzhambuButton>
                                <KuzhambuButton
                                    aria-label={`删除对话 ${sessionTitle(session)}`}
                                    className="discovery-qa-page__delete-session"
                                    disabled={deleting}
                                    testId="discovery-qa-delete-session-button"
                                    onClick={(event) => {
                                        event.stopPropagation();
                                        onDelete(sessionId);
                                    }}
                                >
                                    <CloseOutlined />
                                </KuzhambuButton>
                            </div>
                        );
                    })
                ) : (
                    <Empty
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        description={loading ? "正在加载对话" : "还没有对话"}
                    />
                )}
            </div>
            <KuzhambuButton
                testId="discovery-qa-export-session-button"
                disabled={exportDisabled}
                loading={exporting}
                onClick={onExport}
            >
                导出对话
            </KuzhambuButton>
        </aside>
    );
};
