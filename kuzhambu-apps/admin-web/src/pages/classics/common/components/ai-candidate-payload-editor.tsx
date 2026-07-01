import { Button, Input } from "antd";
import { useEffect, useState } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuSpace } from "@/components/kuzhambu-space";

type AiCandidateCapability = "translate" | "summary" | "tags" | "qa" | "image_analysis";

const TEXT_CAPABILITY_ARIA_LABEL: Record<"translate" | "summary" | "image_analysis", string> = {
    translate: "候选译文内容",
    summary: "候选摘要内容",
    image_analysis: "候选图片理解内容"
};

interface AiCandidatePayloadEditorProps {
    candidateId: number;
    capability: AiCandidateCapability;
    initialPayload?: string | null;
    onPayloadChange: (candidateId: number, payload: string) => void;
    onSubmitEnabledChange: (candidateId: number, canSubmit: boolean) => void;
}

interface AiCandidateQaPair {
    answer: string;
    question: string;
}

const parseTagsPayload = (payload?: string | null): string[] => {
    if (!payload?.trim()) {
        return [""];
    }

    try {
        const parsed = JSON.parse(payload);
        if (Array.isArray(parsed)) {
            return parsed.map((tag: unknown) => String(tag ?? ""));
        }
        if (Array.isArray(parsed?.tags)) {
            return parsed.tags.map((tag: unknown) => String(tag ?? ""));
        }
    } catch {
        return payload.split("\n");
    }

    return [""];
};

const parseQaPayload = (payload?: string | null): AiCandidateQaPair[] => {
    if (!payload?.trim()) {
        return [{ question: "", answer: "" }];
    }

    try {
        const parsed = JSON.parse(payload);
        const qaPairs = Array.isArray(parsed) ? parsed : parsed?.qaPairs;
        if (Array.isArray(qaPairs)) {
            return qaPairs.map((pair: { question?: unknown; answer?: unknown }) => ({
                question: String(pair?.question ?? ""),
                answer: String(pair?.answer ?? "")
            }));
        }
    } catch {
        return [{ question: payload, answer: "" }];
    }

    return [{ question: "", answer: "" }];
};

const stringifyTagsPayload = (tags: string[]): string => {
    return JSON.stringify({
        tags: tags.map((tag) => tag.trim()).filter(Boolean)
    });
};

const stringifyQaPayload = (qaPairs: AiCandidateQaPair[]): string => {
    return JSON.stringify({
        qaPairs: qaPairs
            .map((pair) => ({
                question: pair.question.trim(),
                answer: pair.answer.trim()
            }))
            .filter((pair) => pair.question && pair.answer)
    });
};

const buildTextPayload = (payload?: string | null): string => {
    return payload ?? "";
};

export const AiCandidatePayloadEditor = ({
    candidateId,
    capability,
    initialPayload,
    onPayloadChange,
    onSubmitEnabledChange
}: AiCandidatePayloadEditorProps) => {
    const [textPayload, setTextPayload] = useState(() => buildTextPayload(initialPayload));
    const [tagsPayload, setTagsPayload] = useState(() => parseTagsPayload(initialPayload));
    const [qaPayload, setQaPayload] = useState(() => parseQaPayload(initialPayload));

    useEffect(() => {
        if (
            capability === "summary" ||
            capability === "translate" ||
            capability === "image_analysis"
        ) {
            const payload = textPayload.trim();
            onPayloadChange(candidateId, payload);
            onSubmitEnabledChange(candidateId, payload.length > 0);
            return;
        }

        if (capability === "tags") {
            const payload = stringifyTagsPayload(tagsPayload);
            const canSubmit = tagsPayload.some((tag) => tag.trim());
            onPayloadChange(candidateId, payload);
            onSubmitEnabledChange(candidateId, canSubmit);
            return;
        }

        const payload = stringifyQaPayload(qaPayload);
        const canSubmit = qaPayload.some(
            (pair) => pair.question.trim().length > 0 && pair.answer.trim().length > 0
        );
        onPayloadChange(candidateId, payload);
        onSubmitEnabledChange(candidateId, canSubmit);
    }, [
        candidateId,
        capability,
        onPayloadChange,
        onSubmitEnabledChange,
        qaPayload,
        tagsPayload,
        textPayload
    ]);

    const updateTag = (index: number, value: string) => {
        setTagsPayload((current) => {
            const next = [...current];
            next[index] = value;
            return next;
        });
    };

    const appendTag = () => {
        setTagsPayload((current) => [...current, ""]);
    };

    const removeTag = (index: number) => {
        setTagsPayload((current: string[]) => {
            const next = current.filter(
                (_: string, currentIndex: number) => currentIndex !== index
            );
            return next.length ? next : [""];
        });
    };

    const updateQaPair = (index: number, field: "question" | "answer", value: string) => {
        setQaPayload((current: AiCandidateQaPair[]) => {
            const next = [...current];
            next[index] = {
                ...next[index],
                [field]: value
            };
            return next;
        });
    };

    const appendQaPair = () => {
        setQaPayload((current) => [...current, { question: "", answer: "" }]);
    };

    const removeQaPair = (index: number) => {
        setQaPayload((current: AiCandidateQaPair[]) => {
            const next = current.filter(
                (_: AiCandidateQaPair, currentIndex: number) => currentIndex !== index
            );
            return next.length ? next : [{ question: "", answer: "" }];
        });
    };

    if (capability === "summary" || capability === "translate" || capability === "image_analysis") {
        return (
            <Input.TextArea
                aria-label={TEXT_CAPABILITY_ARIA_LABEL[capability]}
                autoSize={resolveTextAreaAutoSize({ minRows: 4, maxRows: 10 })}
                value={textPayload}
                onChange={(event) => setTextPayload(event.target.value)}
            />
        );
    }

    if (capability === "tags") {
        return (
            <div>
                {tagsPayload.map((tag: string, index: number) => (
                    <KuzhambuSpace
                        key={`${index}-${tag}`}
                        style={{ display: "flex", marginBottom: 8 }}
                    >
                        <Input
                            aria-label={`候选标签 ${index + 1}`}
                            placeholder="标签"
                            value={tag}
                            onChange={(event) => updateTag(index, event.target.value)}
                        />
                        <Button
                            aria-label={`删除候选标签 ${index + 1}`}
                            onClick={() => removeTag(index)}
                        >
                            删除
                        </Button>
                    </KuzhambuSpace>
                ))}
                <Button aria-label="新增候选标签" onClick={appendTag}>
                    新增标签
                </Button>
            </div>
        );
    }

    return (
        <div>
            {qaPayload.map((pair: AiCandidateQaPair, index: number) => (
                <div key={`${pair.question}-${pair.answer}-${index}`} style={{ marginBottom: 12 }}>
                    <Input.TextArea
                        aria-label={`问答问题 ${index + 1}`}
                        placeholder="问题"
                        autoSize={resolveTextAreaAutoSize({ minRows: 2, maxRows: 4 })}
                        value={pair.question}
                        onChange={(event) => updateQaPair(index, "question", event.target.value)}
                    />
                    <Input.TextArea
                        aria-label={`问答答案 ${index + 1}`}
                        placeholder="答案"
                        autoSize={resolveTextAreaAutoSize({ minRows: 2, maxRows: 4 })}
                        style={{ marginTop: 8 }}
                        value={pair.answer}
                        onChange={(event) => updateQaPair(index, "answer", event.target.value)}
                    />
                    <Button
                        aria-label={`删除候选问答 ${index + 1}`}
                        style={{ marginTop: 8 }}
                        onClick={() => removeQaPair(index)}
                    >
                        删除问答
                    </Button>
                </div>
            ))}
            <Button aria-label="新增候选问答" onClick={appendQaPair}>
                新增问答
            </Button>
        </div>
    );
};
