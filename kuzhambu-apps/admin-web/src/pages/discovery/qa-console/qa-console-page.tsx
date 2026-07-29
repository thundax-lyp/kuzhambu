import { useMutation, useQuery } from "@tanstack/react-query";
import { Segmented, Typography } from "antd";
import type { Dayjs } from "dayjs";
import { useCallback, useEffect, useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuSpace } from "@/components";
import { QaDiagnosticsPanel } from "./qa-diagnostics-panel";
import { QaHealthPanel } from "./qa-health-panel";
import { QaSessionDetailDrawer } from "./qa-session-detail-drawer";
import { QaSessionTable } from "./qa-session-table";
import { QaSyncTable } from "./qa-sync-table";
import * as service from "./qa-console-service";
import type { DiscoveryQaSessionDetailRecord, KnowledgeSyncItemRecord } from "./qa-console-types";
import "./qa-console-page.css";

const { Text, Title } = Typography;

const DEFAULT_PAGE_SIZE = 10;

type QaConsolePanel = "health" | "sync" | "sessions" | "diagnostics";

const parseString = (value?: string | null) => {
    const trimmed = value?.trim() ?? "";
    return trimmed.length ? trimmed : null;
};

export const QaConsolePage = () => {
    const confirm = useKuzhambuConfirm();
    const [activePanel, setActivePanel] = useState<QaConsolePanel>("health");
    const requesterUserId = "1001";
    const fastGptConsoleUrl = parseString(import.meta.env.VITE_FASTGPT_CONSOLE_URL);
    const [sessionTitle, setSessionTitle] = useState("");
    const [sessionOpenedRange, setSessionOpenedRange] = useState<
        [Dayjs | null, Dayjs | null] | null
    >(null);
    const [sessionPageNo, setSessionPageNo] = useState(1);
    const [contentType, setContentType] = useState<string | undefined>("SANCAI_ENTRY");
    const [syncStatus, setSyncStatus] = useState<string | undefined>();
    const [syncPageNo, setSyncPageNo] = useState(1);
    const [sessionDrawerOpen, setSessionDrawerOpen] = useState(false);
    const [sessionDetail, setSessionDetail] = useState<DiscoveryQaSessionDetailRecord | null>(null);
    const [sessionOperationText, setSessionOperationText] = useState<string | null>(null);

    const qaConsoleHealthQuery = useQuery({
        queryFn: service.getKnowledgeHealth,
        queryKey: ["discovery-qa-console-knowledge-health"]
    });

    const rebuildMutation = useMutation({
        mutationFn: service.rebuildKnowledge
    });
    const syncKnowledgeMutation = useMutation({
        mutationFn: service.createKnowledgeSync
    });
    const syncRecordPageMutation = useMutation({
        mutationFn: service.pageKnowledgeSyncItems
    });
    const {
        data: syncPageData,
        isPending: isSyncPagePending,
        mutate: mutateSyncPage
    } = syncRecordPageMutation;
    const sessionDetailMutation = useMutation({
        mutationFn: service.getQaSession,
        onSuccess: (nextDetail) => {
            setSessionDetail(nextDetail);
            setSessionDrawerOpen(true);
            setSessionOperationText(null);
        }
    });
    const sessionRecordPageMutation = useMutation({
        mutationFn: service.pageQaSessions
    });
    const {
        data: sessionPageData,
        isPending: isSessionPagePending,
        mutate: mutateSessionPage
    } = sessionRecordPageMutation;
    const syncItems = syncPageData?.records ?? [];

    const loadSyncItems = useCallback(
        (pageNo = syncPageNo) => {
            mutateSyncPage({
                contentType: parseString(contentType),
                pageNo,
                pageSize: DEFAULT_PAGE_SIZE,
                syncStatus: parseString(syncStatus)
            });
        },
        [contentType, mutateSyncPage, syncPageNo, syncStatus]
    );

    useEffect(() => {
        if (activePanel === "sync" && !syncPageData && !isSyncPagePending) {
            loadSyncItems();
        }
    }, [activePanel, isSyncPagePending, loadSyncItems, syncPageData]);

    const buildSessionPageQuery = useCallback(
        (pageNo = sessionPageNo) => ({
            openedAtEnd: sessionOpenedRange?.[1]?.endOf("day").toISOString() ?? null,
            openedAtStart: sessionOpenedRange?.[0]?.startOf("day").toISOString() ?? null,
            pageNo,
            pageSize: DEFAULT_PAGE_SIZE,
            title: parseString(sessionTitle)
        }),
        [sessionOpenedRange, sessionPageNo, sessionTitle]
    );

    const loadSessions = useCallback(
        (pageNo = sessionPageNo) => {
            mutateSessionPage(buildSessionPageQuery(pageNo));
            setSessionDrawerOpen(false);
        },
        [buildSessionPageQuery, mutateSessionPage, sessionPageNo]
    );

    const deleteSessionMutation = useMutation({
        mutationFn: service.deleteQaSession,
        onSuccess: (_, variables) => {
            const deletedSessionId = parseString(variables.sessionId);
            setSessionDetail((current) => ({
                ...(current ?? {}),
                sessionId: current?.sessionId ?? deletedSessionId,
                status: "REMOVED"
            }));
            setSessionDrawerOpen(false);
            setSessionOperationText(`会话 ${deletedSessionId ?? "-"} 已删除`);
            mutateSessionPage(buildSessionPageQuery());
        },
        onError: (error) => {
            setSessionOperationText(error instanceof Error ? error.message : "会话删除失败");
        }
    });

    const deleteCurrentSession = (targetSessionId: string) => {
        const nextSessionId = parseString(targetSessionId);
        if (nextSessionId === null) {
            return;
        }

        deleteSessionMutation.mutate({
            requesterUserId,
            sessionId: nextSessionId
        });
    };

    const confirmDeleteSession = (targetSessionId: string) => {
        const nextSessionId = parseString(targetSessionId);
        if (nextSessionId === null) {
            return;
        }

        confirm.danger({
            title: "删除问答会话",
            message: `确认删除会话 ${nextSessionId}？`,
            description: "删除后 Portal 不再展示该会话，Admin 仍保留审计查看和导出入口。",
            okText: "删除",
            onConfirm: () => deleteCurrentSession(nextSessionId)
        });
    };

    const exportSessionMutation = useMutation({
        mutationFn: service.createQaSessionExport,
        onSuccess: (record, variables) => {
            const exportName = record.filename ?? record.exportStatus ?? "-";
            setSessionOperationText(`会话 ${variables.sessionId} 导出已创建：${exportName}`);
        },
        onError: (error) => {
            setSessionOperationText(error instanceof Error ? error.message : "会话导出失败");
        }
    });

    const exportCurrentSession = (targetSessionId: string) => {
        const nextSessionId = parseString(targetSessionId);
        if (nextSessionId === null) {
            return;
        }

        exportSessionMutation.mutate({
            format: "CSV",
            requesterUserId,
            sessionId: nextSessionId
        });
    };

    useEffect(() => {
        if (activePanel === "sessions" && !sessionPageData && !isSessionPagePending) {
            mutateSessionPage(buildSessionPageQuery());
        }
    }, [
        activePanel,
        buildSessionPageQuery,
        isSessionPagePending,
        mutateSessionPage,
        sessionPageData
    ]);

    const sessionRows = sessionPageData?.records ?? [];

    return (
        <main className="kuzhambu-page discovery-admin-page qa-console-page">
            <section>
                <header className="kuzhambu-page-header">
                    <div>
                        <Title level={2}>问答运维</Title>
                        <Text type="secondary">查看知识库健康、知识文档和问答会话。</Text>
                    </div>
                </header>

                <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                    <Segmented
                        className="qa-console-segmented"
                        options={[
                            { label: "健康状态", value: "health" },
                            { label: "知识文档", value: "sync" },
                            { label: "会话管理", value: "sessions" },
                            { label: "问答诊断", value: "diagnostics" }
                        ]}
                        value={activePanel}
                        onChange={(value) => setActivePanel(value as QaConsolePanel)}
                    />

                    {activePanel === "health" ? (
                        <QaHealthPanel
                            data={qaConsoleHealthQuery.data}
                            loading={qaConsoleHealthQuery.isFetching}
                            onRefresh={() => void qaConsoleHealthQuery.refetch()}
                        />
                    ) : null}

                    {activePanel === "sync" ? (
                        <QaSyncTable
                            contentType={contentType}
                            loading={isSyncPagePending}
                            onContentTypeChange={setContentType}
                            onPageChange={(nextPageNo) => {
                                setSyncPageNo(nextPageNo);
                                loadSyncItems(nextPageNo);
                            }}
                            onQuery={() => {
                                setSyncPageNo(1);
                                loadSyncItems(1);
                            }}
                            onRebuild={() => rebuildMutation.mutate({})}
                            onSyncItem={(record: KnowledgeSyncItemRecord) =>
                                syncKnowledgeMutation.mutate({
                                    contentId: record.contentId ?? "",
                                    contentType: record.contentType ?? "",
                                    currentVersionNo: record.currentVersionNo ?? null
                                })
                            }
                            onSyncStatusChange={setSyncStatus}
                            pageData={syncPageData}
                            pageNo={syncPageNo}
                            pageSize={DEFAULT_PAGE_SIZE}
                            rebuildLoading={rebuildMutation.isPending}
                            syncItems={syncItems}
                            syncLoading={syncKnowledgeMutation.isPending}
                            syncStatus={syncStatus}
                        />
                    ) : null}

                    {activePanel === "sessions" ? (
                        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                            <QaSessionTable
                                deleteLoading={deleteSessionMutation.isPending}
                                exportLoading={exportSessionMutation.isPending}
                                loading={isSessionPagePending}
                                onDelete={confirmDeleteSession}
                                onExport={exportCurrentSession}
                                onLoad={() => {
                                    setSessionPageNo(1);
                                    loadSessions(1);
                                }}
                                onOpen={(sessionId) => {
                                    const nextSessionId = parseString(sessionId);
                                    if (nextSessionId) {
                                        sessionDetailMutation.mutate({ sessionId: nextSessionId });
                                    }
                                }}
                                onPageChange={(nextPageNo) => {
                                    setSessionPageNo(nextPageNo);
                                    loadSessions(nextPageNo);
                                }}
                                onRangeChange={setSessionOpenedRange}
                                onTitleChange={setSessionTitle}
                                operationText={sessionOperationText}
                                pageData={sessionPageData}
                                pageNo={sessionPageNo}
                                pageSize={DEFAULT_PAGE_SIZE}
                                range={sessionOpenedRange}
                                rows={sessionRows}
                                sessionLoading={sessionDetailMutation.isPending}
                                title={sessionTitle}
                            />
                            <QaSessionDetailDrawer
                                onClose={() => setSessionDrawerOpen(false)}
                                open={sessionDrawerOpen}
                                sessionDetail={sessionDetail}
                            />
                        </KuzhambuSpace>
                    ) : null}

                    {activePanel === "diagnostics" ? (
                        <QaDiagnosticsPanel fastGptConsoleUrl={fastGptConsoleUrl} />
                    ) : null}
                </KuzhambuSpace>
            </section>
        </main>
    );
};
