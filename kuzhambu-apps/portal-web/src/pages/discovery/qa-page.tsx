import { useMemo, useState, type FormEvent } from "react";
import { useMutation } from "@tanstack/react-query";
import { MessageSquareQuote, Play, Sparkles, Workflow } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import * as qaService from "./qa-service";
import type {
    DiscoveryQaAskQuestionRequest,
    DiscoveryQaAskQuestionResponse,
    DiscoveryQaOpenSessionRequest
} from "./qa-types";

interface QaFormState {
    contextContentId: string;
    contextContentType: string;
    contextMode: string;
    contextTurnCount: string;
    ownerUserId: string;
    operatorId: string;
    operatorType: string;
    question: string;
    requestId: string;
    scope: string;
    sessionId: string;
    sessionTitle: string;
    traceId: string;
}

const INITIAL_FORM_STATE: QaFormState = {
    contextContentId: "",
    contextContentType: "",
    contextMode: "",
    contextTurnCount: "3",
    ownerUserId: "1",
    operatorId: "portal-user",
    operatorType: "PORTAL",
    question: "",
    requestId: "",
    scope: "",
    sessionId: "",
    sessionTitle: "知识中心问答",
    traceId: ""
};

const toOptionalNumber = (value: string) => {
    if (!value.trim()) {
        return null;
    }

    const parsed = Number.parseInt(value, 10);
    return Number.isNaN(parsed) ? null : parsed;
};

const toOptionalString = (value: string) => {
    const trimmed = value.trim();
    return trimmed.length ? trimmed : null;
};

const toOpenSessionRequest = (form: QaFormState): DiscoveryQaOpenSessionRequest => {
    return {
        contextContentId: toOptionalNumber(form.contextContentId),
        contextContentType: toOptionalString(form.contextContentType),
        contextMode: toOptionalString(form.contextMode),
        ownerUserId: Number.parseInt(form.ownerUserId, 10) || 1,
        requestId: toOptionalString(form.requestId),
        scope: toOptionalString(form.scope),
        title: toOptionalString(form.sessionTitle),
        traceId: toOptionalString(form.traceId)
    };
};

const toAskQuestionRequest = (
    form: QaFormState,
    sessionId: number
): DiscoveryQaAskQuestionRequest => {
    return {
        contextTurnCount: toOptionalNumber(form.contextTurnCount),
        operatorId: toOptionalString(form.operatorId),
        operatorType: toOptionalString(form.operatorType),
        requestId: toOptionalString(form.requestId),
        question: form.question.trim(),
        sessionId,
        traceId: toOptionalString(form.traceId)
    };
};

const formatTimestamp = (value?: number | null) => {
    if (!value) {
        return "未设置";
    }

    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
};

const formatSourceScore = (value?: number | string | null) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }

    return typeof value === "number" ? value.toFixed(3) : value;
};

const sourceSummary = (response?: DiscoveryQaAskQuestionResponse | null) => {
    const count = response?.sources?.length ?? 0;
    const status = response?.answerStatus || "待回答";
    return `答案状态 ${status} · 来源 ${count} 条`;
};

export const DiscoveryQaPage = () => {
    const [form, setForm] = useState<QaFormState>(INITIAL_FORM_STATE);
    const [sessionId, setSessionId] = useState<number | null>(null);
    const [answerResult, setAnswerResult] = useState<DiscoveryQaAskQuestionResponse | null>(null);

    const openSessionMutation = useMutation({
        mutationFn: (request: DiscoveryQaOpenSessionRequest) => qaService.openQaSession(request)
    });
    const askQuestionMutation = useMutation({
        mutationFn: (request: DiscoveryQaAskQuestionRequest) => qaService.askQaQuestion(request)
    });

    const summaryText = useMemo(() => {
        if (askQuestionMutation.isPending) {
            return "正在生成回答";
        }
        if (askQuestionMutation.isError) {
            return "回答生成失败";
        }
        if (answerResult) {
            return sourceSummary(answerResult);
        }
        if (sessionId) {
            return `会话 ${sessionId} 已就绪，可以直接提问`;
        }
        return "先创建会话，再发送问题";
    }, [answerResult, askQuestionMutation.isError, askQuestionMutation.isPending, sessionId]);
    const visibleSessionId = sessionId ?? toOptionalNumber(form.sessionId);

    const updateField = (key: keyof QaFormState, value: string) => {
        setForm((current) => ({
            ...current,
            [key]: value
        }));
    };

    const handleOpenSession = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const response = await openSessionMutation.mutateAsync(toOpenSessionRequest(form));
        setSessionId(response.sessionId ?? null);
        if (response.title && !form.sessionTitle.trim()) {
            setForm((current) => ({
                ...current,
                sessionTitle: response.title ?? current.sessionTitle
            }));
        }
    };

    const handleAskQuestion = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const activeSessionId = sessionId ?? toOptionalNumber(form.sessionId);

        if (!activeSessionId) {
            return;
        }

        const response = await askQuestionMutation.mutateAsync(
            toAskQuestionRequest(form, activeSessionId)
        );
        setSessionId(response.sessionId ?? activeSessionId);
        setAnswerResult(response);
    };

    const sources = answerResult?.sources ?? [];

    return (
        <main className="portal-shell">
            <header className="portal-header">
                <div>
                    <p className="portal-kicker">知识中心 · Discovery</p>
                    <h1>问答工作台</h1>
                </div>
                <Button asChild className="portal-action" size="lg" variant="outline">
                    <Link to="/">返回首页</Link>
                </Button>
            </header>

            <section className="portal-qa-hero">
                <div className="portal-qa-copy">
                    <p className="portal-qa-tag">
                        <MessageSquareQuote aria-hidden="true" size={16} />
                        会话、来源、轨迹三者一起看
                    </p>
                    <h2>最小问答入口，直连 Discovery 与 AI</h2>
                    <p>
                        先创建一个问答会话，再围绕知识库内容发问。回答返回后会同时展示来源列表和检索摘要，方便验证知识增强是否真正进入了回答链路。
                    </p>
                </div>
                <div className="portal-qa-stat">
                    <span>当前状态</span>
                    <strong>{summaryText}</strong>
                    <small>
                        会话 {visibleSessionId ?? "未创建"} · 提问
                        {answerResult?.questionMessageId ?? "-"}
                    </small>
                </div>
            </section>

            <section className="portal-qa-grid">
                <Card className="portal-qa-panel">
                    <div className="portal-qa-panel-header">
                        <div>
                            <p className="portal-kicker">步骤 1</p>
                            <h2>创建会话</h2>
                        </div>
                        <Workflow aria-hidden="true" size={18} />
                    </div>

                    <form className="portal-qa-form" onSubmit={handleOpenSession}>
                        <div className="portal-qa-form-grid">
                            <Label className="portal-filter-field">
                                <span>拥有者用户号</span>
                                <Input
                                    name="ownerUserId"
                                    type="number"
                                    value={form.ownerUserId}
                                    onChange={(event) =>
                                        updateField("ownerUserId", event.target.value)
                                    }
                                />
                            </Label>
                            <Label className="portal-filter-field">
                                <span>会话标题</span>
                                <Input
                                    name="sessionTitle"
                                    value={form.sessionTitle}
                                    onChange={(event) =>
                                        updateField("sessionTitle", event.target.value)
                                    }
                                />
                            </Label>
                            <Label className="portal-filter-field">
                                <span>作用域</span>
                                <Input
                                    name="scope"
                                    value={form.scope}
                                    onChange={(event) => updateField("scope", event.target.value)}
                                />
                            </Label>
                            <Label className="portal-filter-field">
                                <span>上下文模式</span>
                                <Input
                                    name="contextMode"
                                    value={form.contextMode}
                                    onChange={(event) =>
                                        updateField("contextMode", event.target.value)
                                    }
                                />
                            </Label>
                            <Label className="portal-filter-field">
                                <span>上下文内容类型</span>
                                <Input
                                    name="contextContentType"
                                    value={form.contextContentType}
                                    onChange={(event) =>
                                        updateField("contextContentType", event.target.value)
                                    }
                                />
                            </Label>
                            <Label className="portal-filter-field">
                                <span>上下文内容标识</span>
                                <Input
                                    name="contextContentId"
                                    type="number"
                                    value={form.contextContentId}
                                    onChange={(event) =>
                                        updateField("contextContentId", event.target.value)
                                    }
                                />
                            </Label>
                            <Label className="portal-filter-field">
                                <span>请求号</span>
                                <Input
                                    name="requestId"
                                    value={form.requestId}
                                    onChange={(event) =>
                                        updateField("requestId", event.target.value)
                                    }
                                />
                            </Label>
                            <Label className="portal-filter-field">
                                <span>链路号</span>
                                <Input
                                    name="traceId"
                                    value={form.traceId}
                                    onChange={(event) => updateField("traceId", event.target.value)}
                                />
                            </Label>
                        </div>

                        <div className="portal-qa-actions">
                            <Button disabled={openSessionMutation.isPending} type="submit">
                                {openSessionMutation.isPending ? "创建中..." : "创建会话"}
                            </Button>
                        </div>
                    </form>

                    <dl className="portal-qa-session-meta">
                        <div>
                            <dt>会话号</dt>
                            <dd>{sessionId ?? "-"}</dd>
                        </div>
                        <div>
                            <dt>状态</dt>
                            <dd>{openSessionMutation.data?.status ?? "未创建"}</dd>
                        </div>
                        <div>
                            <dt>创建时间</dt>
                            <dd>{formatTimestamp(openSessionMutation.data?.openedAt)}</dd>
                        </div>
                        <div>
                            <dt>最后消息</dt>
                            <dd>{formatTimestamp(openSessionMutation.data?.lastMessageAt)}</dd>
                        </div>
                    </dl>
                </Card>

                <Card className="portal-qa-panel">
                    <div className="portal-qa-panel-header">
                        <div>
                            <p className="portal-kicker">步骤 2</p>
                            <h2>发送问题</h2>
                        </div>
                        <Play aria-hidden="true" size={18} />
                    </div>

                    <form className="portal-qa-form" onSubmit={handleAskQuestion}>
                        <div className="portal-qa-form-grid">
                            <Label className="portal-filter-field">
                                <span>会话号</span>
                                <Input
                                    name="sessionId"
                                    type="number"
                                    value={form.sessionId || sessionId?.toString() || ""}
                                    onChange={(event) =>
                                        updateField("sessionId", event.target.value)
                                    }
                                />
                            </Label>
                            <Label className="portal-filter-field">
                                <span>上下文轮次</span>
                                <Input
                                    name="contextTurnCount"
                                    type="number"
                                    value={form.contextTurnCount}
                                    onChange={(event) =>
                                        updateField("contextTurnCount", event.target.value)
                                    }
                                />
                            </Label>
                            <Label className="portal-filter-field">
                                <span>操作者类型</span>
                                <Input
                                    name="operatorType"
                                    value={form.operatorType}
                                    onChange={(event) =>
                                        updateField("operatorType", event.target.value)
                                    }
                                />
                            </Label>
                            <Label className="portal-filter-field">
                                <span>操作者号</span>
                                <Input
                                    name="operatorId"
                                    value={form.operatorId}
                                    onChange={(event) =>
                                        updateField("operatorId", event.target.value)
                                    }
                                />
                            </Label>
                            <Label className="portal-filter-field">
                                <span>请求号</span>
                                <Input
                                    name="questionRequestId"
                                    value={form.requestId}
                                    onChange={(event) =>
                                        updateField("requestId", event.target.value)
                                    }
                                />
                            </Label>
                            <Label className="portal-filter-field">
                                <span>链路号</span>
                                <Input
                                    name="questionTraceId"
                                    value={form.traceId}
                                    onChange={(event) => updateField("traceId", event.target.value)}
                                />
                            </Label>
                        </div>

                        <Label className="portal-filter-field portal-qa-question">
                            <span>问题</span>
                            <Textarea
                                name="question"
                                placeholder="例如：这类古籍中的礼器常见在哪些章节？"
                                rows={7}
                                value={form.question}
                                onChange={(event) => updateField("question", event.target.value)}
                            />
                        </Label>

                        <div className="portal-qa-actions">
                            <Button disabled={askQuestionMutation.isPending} type="submit">
                                {askQuestionMutation.isPending ? "回答中..." : "发送问题"}
                            </Button>
                        </div>
                    </form>

                    <div className="portal-qa-answer">
                        <div>
                            <p className="portal-kicker">回答</p>
                            <h2>{answerResult?.answerStatus || "等待提问"}</h2>
                        </div>
                        <p>{answerResult?.answer || answerResult?.failureReason || "尚无回答。"}</p>
                    </div>
                </Card>
            </section>

            <section className="portal-qa-results">
                <Card className="portal-qa-panel">
                    <div className="portal-qa-panel-header">
                        <div>
                            <p className="portal-kicker">来源</p>
                            <h2>引用证据</h2>
                        </div>
                        <Sparkles aria-hidden="true" size={18} />
                    </div>

                    {sources.length ? (
                        <div className="portal-qa-source-list">
                            {sources.map((source) => (
                                <article
                                    key={
                                        source.sourceId ??
                                        `${source.contentType}-${source.contentId}`
                                    }
                                >
                                    <div className="portal-qa-source-title">
                                        <h3>
                                            {source.titleSnapshot ||
                                                `来源 ${source.sourceId ?? "-"}`}
                                        </h3>
                                        <span>
                                            {source.knowledgeBase || source.contentType || "-"}
                                        </span>
                                    </div>
                                    <p>{source.snippet || "暂无摘录。"}</p>
                                    <dl>
                                        <div>
                                            <dt>位置</dt>
                                            <dd>{source.locationLabel || "-"}</dd>
                                        </div>
                                        <div>
                                            <dt>排序</dt>
                                            <dd>{source.sourceRank ?? "-"}</dd>
                                        </div>
                                        <div>
                                            <dt>得分</dt>
                                            <dd>{formatSourceScore(source.score)}</dd>
                                        </div>
                                        <div>
                                            <dt>状态</dt>
                                            <dd>{source.sourceStatus || "-"}</dd>
                                        </div>
                                    </dl>
                                </article>
                            ))}
                        </div>
                    ) : (
                        <div className="portal-empty">回答生成后，这里会列出 cited sources。</div>
                    )}
                </Card>

                <Card className="portal-qa-panel">
                    <div className="portal-qa-panel-header">
                        <div>
                            <p className="portal-kicker">轨迹</p>
                            <h2>Trace Summary</h2>
                        </div>
                        <MessageSquareQuote aria-hidden="true" size={18} />
                    </div>

                    {answerResult?.traceSummary ? (
                        <dl className="portal-qa-trace">
                            <div>
                                <dt>Trace ID</dt>
                                <dd>{answerResult.traceSummary.traceId ?? "-"}</dd>
                            </div>
                            <div>
                                <dt>改写问题</dt>
                                <dd>{answerResult.traceSummary.rewrittenQuestion || "-"}</dd>
                            </div>
                            <div>
                                <dt>候选数</dt>
                                <dd>{answerResult.traceSummary.candidateCount ?? "-"}</dd>
                            </div>
                            <div>
                                <dt>扩展词</dt>
                                <dd>{answerResult.traceSummary.expandedTermsJson || "-"}</dd>
                            </div>
                            <div>
                                <dt>关联实体</dt>
                                <dd>{answerResult.traceSummary.linkedEntitiesJson || "-"}</dd>
                            </div>
                        </dl>
                    ) : (
                        <div className="portal-empty">生成回答后，这里会展示检索摘要。</div>
                    )}
                </Card>
            </section>
        </main>
    );
};
