import { CheckCircleOutlined, ExclamationCircleOutlined, LoadingOutlined } from "@ant-design/icons";
import { Bubble, Sender, type BubbleItemType } from "@ant-design/x";
import { Input, Tag, Tooltip, Typography } from "antd";
import { forwardRef, useMemo, type ComponentProps, type ElementRef, type ReactNode } from "react";
import { KuzhambuSpace } from "@/components";
import ancientReaderAvatar from "@/assets/ancient-reader-avatar-face.jpg";
import ancientScholarAvatar from "@/assets/ancient-scholar-avatar-face.jpg";
import type {
    DiscoveryQaSessionRecord,
    DiscoveryQaSourceRecord
} from "@/pages/discovery/qa/qa-types";

const { Text, Title } = Typography;

const QaSenderInput = forwardRef<
    ElementRef<typeof Input.TextArea>,
    ComponentProps<typeof Input.TextArea>
>((props, ref) => <Input.TextArea {...props} ref={ref} aria-label="问题" />);
QaSenderInput.displayName = "QaSenderInput";
const QA_SENDER_COMPONENTS = { input: QaSenderInput };

export interface QaTimelineMessage {
    content: string;
    id: string;
    role: "assistant" | "user";
    sources?: DiscoveryQaSourceRecord[];
    status: "failed" | "loading" | "succeeded";
}

const formatTime = (value?: number | null) => {
    if (!value) {
        return "-";
    }

    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium"
    }).format(new Date(value));
};

const sessionTitle = (session?: DiscoveryQaSessionRecord) => {
    if (session?.title) {
        return session.title;
    }
    return session?.id ? `会话 ${session.id}` : "未命名会话";
};

const toSourceKey = (source: DiscoveryQaSourceRecord, index: number) => {
    return source.sourceId ?? `${source.contentType ?? "SOURCE"}-${source.contentId ?? index}`;
};

const toBubbleStatus = (status: QaTimelineMessage["status"]) => {
    if (status === "failed") {
        return "error";
    }
    return undefined;
};

const toBubbleStatusLabel = (status: QaTimelineMessage["status"]) => {
    if (status === "failed") {
        return "回答失败";
    }
    if (status === "loading") {
        return "正在生成";
    }
    return "已完成";
};

const toBubbleStatusIcon = (status: QaTimelineMessage["status"]): ReactNode => {
    if (status === "failed") {
        return <ExclamationCircleOutlined />;
    }
    if (status === "loading") {
        return <LoadingOutlined spin />;
    }
    return <CheckCircleOutlined />;
};

interface QaMessagePanelProps {
    inputValue: string;
    loading: boolean;
    messages: QaTimelineMessage[];
    onDetailOpen: () => void;
    onInputChange: (value: string) => void;
    onSubmit: (message?: string) => void;
    operationMessage: string | null;
    selectedSession?: DiscoveryQaSessionRecord;
}

export const QaMessagePanel = ({
    inputValue,
    loading,
    messages,
    onDetailOpen,
    onInputChange,
    onSubmit,
    operationMessage,
    selectedSession
}: QaMessagePanelProps) => {
    const latestAssistantMessage = [...messages]
        .reverse()
        .find((message) => message.role === "assistant" && message.sources?.length);
    const bubbleItems = useMemo<BubbleItemType[]>(() => {
        return messages.map((message) => {
            const content =
                message.content ||
                (message.status === "loading" ? "正在检索知识库..." : "未返回回答内容");

            return {
                avatar: (
                    <span
                        aria-label={message.role === "user" ? "用户" : "古籍助手"}
                        className={`discovery-qa-page__avatar discovery-qa-page__avatar--${message.role}`}
                        role="img"
                    >
                        <img
                            alt=""
                            aria-hidden="true"
                            src={
                                message.role === "user" ? ancientReaderAvatar : ancientScholarAvatar
                            }
                        />
                    </span>
                ),
                content:
                    message.role === "assistant" ? (
                        <span className="discovery-qa-page__assistant-content">
                            {content}
                            {message.status === "loading" && message.content ? (
                                <span
                                    aria-hidden="true"
                                    className="discovery-qa-page__stream-caret"
                                />
                            ) : null}
                        </span>
                    ) : (
                        content
                    ),
                footer: (
                    <KuzhambuSpace size={8}>
                        <Tooltip title={toBubbleStatusLabel(message.status)}>
                            <span
                                aria-label={toBubbleStatusLabel(message.status)}
                                className={`discovery-qa-page__message-status discovery-qa-page__message-status--${message.status}`}
                                role="img"
                            >
                                {toBubbleStatusIcon(message.status)}
                            </span>
                        </Tooltip>
                        {message.sources?.length ? (
                            <Text type="secondary">{message.sources.length} 个来源</Text>
                        ) : null}
                    </KuzhambuSpace>
                ),
                key: message.id,
                loading: false,
                role: message.role === "user" ? "user" : "ai",
                status: toBubbleStatus(message.status),
                variant: message.role === "user" ? "filled" : "outlined"
            };
        });
    }, [messages]);

    return (
        <section className="discovery-qa-page__chat">
            <header className="discovery-qa-page__chat-header">
                <div>
                    <Title level={2}>知识助手</Title>
                    <Text type="secondary">提问后，我会在知识库中查询并附上来源。</Text>
                </div>
                {selectedSession ? (
                    <button
                        className="discovery-qa-page__session-detail-trigger"
                        type="button"
                        onClick={onDetailOpen}
                    >
                        {sessionTitle(selectedSession)} · {formatTime(selectedSession.openedAt)}
                    </button>
                ) : null}
            </header>

            {operationMessage ? (
                <Text className="discovery-qa-page__notice" type="secondary">
                    {operationMessage}
                </Text>
            ) : null}

            <div className="discovery-qa-page__messages" aria-label="问答消息">
                {bubbleItems.length ? (
                    <Bubble.List
                        autoScroll
                        items={bubbleItems}
                        role={{
                            ai: {
                                placement: "start",
                                shape: "corner"
                            },
                            user: {
                                placement: "end",
                                shape: "corner"
                            }
                        }}
                    />
                ) : (
                    <div className="discovery-qa-page__empty">
                        <Title level={3}>我能帮你解答什么？</Title>
                        <Text type="secondary">问一个问题，开始新的对话。</Text>
                    </div>
                )}
            </div>

            <div className="discovery-qa-page__sources" aria-label="回答来源">
                {latestAssistantMessage?.sources?.length
                    ? latestAssistantMessage.sources.map((source, index) => (
                          <article
                              key={toSourceKey(source, index)}
                              className="discovery-qa-page__source"
                          >
                              <div className="discovery-qa-page__source-header">
                                  <Text strong>{source.titleSnapshot ?? source.sourceId}</Text>
                                  <Tag>{source.knowledgeBase ?? source.contentType ?? "-"}</Tag>
                              </div>
                              <Text type="secondary">
                                  {source.snippet ??
                                      source.locationLabel ??
                                      source.sourcePath ??
                                      "-"}
                              </Text>
                          </article>
                      ))
                    : null}
            </div>

            <Sender
                autoSize={false}
                className="discovery-qa-page__composer"
                components={QA_SENDER_COMPONENTS}
                loading={loading}
                placeholder="发送消息"
                submitType="enter"
                value={inputValue}
                onChange={onInputChange}
                onSubmit={(message) => onSubmit(message)}
                suffix={(_, { components }) => (
                    <components.SendButton
                        aria-label="发送问题"
                        data-testid="discovery-qa-send-question-button"
                        onClickCapture={(event) => {
                            event.preventDefault();
                            event.stopPropagation();
                            onSubmit();
                        }}
                    />
                )}
            />
        </section>
    );
};
