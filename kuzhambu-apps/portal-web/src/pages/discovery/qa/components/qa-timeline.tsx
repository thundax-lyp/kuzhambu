import { Button } from "@/components/ui/button";
import ancientReaderAvatar from "@/assets/discovery/ancient-reader-avatar-face.jpg";
import ancientScholarAvatar from "@/assets/discovery/ancient-scholar-avatar-face.jpg";
import {
    isUnavailableSource,
    toChatCompletionSourceKey,
    toChatCompletionSourceLabel,
    type QaTimelineMessage
} from "@/pages/discovery/qa/qa-utils";

interface QaTimelineProps {
    messages: QaTimelineMessage[];
    onRetry: (messageId: string) => void;
}

export const QaTimeline = ({ messages, onRetry }: QaTimelineProps) => {
    if (!messages.length) {
        return <div className="portal-empty">输入问题后，回答会显示在这里。</div>;
    }

    return (
        <div className="portal-qa-timeline">
            {messages.map((message) => (
                <article
                    className={`portal-qa-message portal-qa-message-${message.role}`}
                    key={message.id}
                >
                    <span
                        aria-label={message.role === "user" ? "用户" : "古籍助手"}
                        className="portal-qa-avatar"
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
                    <div className="portal-qa-bubble">
                        {message.role === "assistant" ? (
                            <AssistantMessage message={message} onRetry={onRetry} />
                        ) : (
                            <p>{message.content}</p>
                        )}
                    </div>
                </article>
            ))}
        </div>
    );
};

const AssistantMessage = ({
    message,
    onRetry
}: {
    message: QaTimelineMessage;
    onRetry: (messageId: string) => void;
}) => {
    return (
        <div>
            <p>{message.content}</p>
            {message.status === "loading" ? <p>正在生成回答…</p> : null}
            {message.status === "failed" ? (
                <p>{message.failureReason ?? "回答失败，请稍后重试。"}</p>
            ) : null}
            {message.sources?.length ? <QaSourceList message={message} /> : null}
            {message.status === "failed" ? (
                <Button
                    size="sm"
                    type="button"
                    variant="outline"
                    onClick={() => onRetry(message.id)}
                >
                    重试
                </Button>
            ) : null}
        </div>
    );
};

const QaSourceList = ({ message }: { message: QaTimelineMessage }) => {
    return (
        <div className="portal-qa-source-list">
            {message.sources?.map((source, sourceIndex) => {
                const key = toChatCompletionSourceKey(source, sourceIndex);
                const hasSourcePath = source.sourcePath !== null && source.sourcePath !== undefined;
                const sourcePath = source.sourcePath ?? "";
                const sourceLabel = toChatCompletionSourceLabel(source);
                const unavailable = isUnavailableSource(source) || !hasSourcePath;
                return (
                    <div key={key}>
                        <p>
                            {unavailable ? (
                                <span>{sourceLabel}</span>
                            ) : (
                                <a href={sourcePath}>{sourceLabel}</a>
                            )}
                        </p>
                        <p>来源状态：{source.sourceStatus ?? "-"}</p>
                        <p>相关度：{source.score ?? "-"}</p>
                    </div>
                );
            })}
        </div>
    );
};
