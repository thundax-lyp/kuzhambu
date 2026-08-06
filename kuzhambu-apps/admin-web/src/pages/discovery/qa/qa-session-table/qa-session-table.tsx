import { CloseOutlined } from "@ant-design/icons";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Empty } from "antd";
import { KuzhambuButton } from "@/components";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import * as service from "@/pages/discovery/qa/qa-service";
import type { DiscoveryQaSessionRecord } from "@/pages/discovery/qa/qa-types";

const DEFAULT_PAGE_SIZE = 20;

const toSessionId = (value?: string | null) => {
    return typeof value === "string" && value.trim().length ? value : null;
};

const sessionTitle = (session?: DiscoveryQaSessionRecord) => {
    if (session?.title) {
        return session.title;
    }
    return session?.id ? `会话 ${session.id}` : "未命名会话";
};

interface QaSessionTableProps {
    onCreate: () => void;
    onDeleted: (sessionId: string) => void;
    onOperationMessage: (message: string | null) => void;
    onSelect: (sessionId: string) => void;
    ownerUserId: string | null;
    opening: boolean;
    selectedSessionId: string | null;
}

export const QaSessionTable = ({
    onCreate,
    onDeleted,
    onOperationMessage,
    onSelect,
    ownerUserId,
    opening,
    selectedSessionId
}: QaSessionTableProps) => {
    const confirm = useKuzhambuConfirm();
    const sessionsQuery = useQuery({
        queryFn: () =>
            service.pageQaSessions({
                ownerUserId,
                pageNo: 1,
                pageSize: DEFAULT_PAGE_SIZE,
                scope: "PORTAL"
            }),
        queryKey: ["discovery-qa", "session-page", ownerUserId],
        enabled: ownerUserId !== null
    });
    const deleteSessionMutation = useMutation({
        mutationFn: service.deleteQaSession,
        onSuccess: async (_, command) => {
            onDeleted(command.sessionId);
            await sessionsQuery.refetch();
        },
        onError: (error) => {
            onOperationMessage(error instanceof Error ? error.message : "删除对话失败");
        }
    });
    const exportSessionMutation = useMutation({
        mutationFn: service.createQaSessionExport,
        onSuccess: (result) => {
            if (result.exportStatus === "FAILED") {
                onOperationMessage(result.failureReason ?? "导出失败");
                return;
            }
            onOperationMessage(`导出完成：${result.filename ?? "问答会话.csv"}`);
        },
        onError: (error) => {
            onOperationMessage(error instanceof Error ? error.message : "导出失败");
        }
    });
    const page = sessionsQuery.data;
    const sessions = page?.records ?? [];

    const confirmDeleteSession = (session: DiscoveryQaSessionRecord, sessionId: string) => {
        if (ownerUserId === null) {
            return;
        }
        confirm.danger({
            title: "删除问答会话",
            message: `确认删除「${sessionTitle(session)}」？`,
            description: "删除后将无法继续查看、追问或导出该会话。",
            okText: "删除",
            onConfirm: () =>
                deleteSessionMutation.mutateAsync({
                    ownerUserId,
                    sessionId
                })
        });
    };

    const exportSelectedSession = () => {
        if (ownerUserId === null || selectedSessionId === null) {
            return;
        }
        onOperationMessage(null);
        exportSessionMutation.mutate({
            format: "CSV",
            ownerUserId,
            sessionId: selectedSessionId
        });
    };

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
                        const sessionId = toSessionId(session.id);
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
                                    disabled={deleteSessionMutation.isPending}
                                    testId="discovery-qa-delete-session-button"
                                    onClick={(event) => {
                                        event.stopPropagation();
                                        confirmDeleteSession(session, sessionId);
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
                        description={sessionsQuery.isPending ? "正在加载对话" : "还没有对话"}
                    />
                )}
            </div>
            <KuzhambuButton
                testId="discovery-qa-export-session-button"
                disabled={selectedSessionId === null}
                loading={exportSessionMutation.isPending}
                onClick={exportSelectedSession}
            >
                导出对话
            </KuzhambuButton>
        </aside>
    );
};
