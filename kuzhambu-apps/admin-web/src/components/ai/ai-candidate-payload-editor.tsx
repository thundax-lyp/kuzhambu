import { Button, Input, Space } from "antd";
import { useEffect, useState } from "react";

const { TextArea } = Input;

export type CandidateCapability = "summary" | "translate" | "tags" | "qa";

export interface AiCandidateQaPairPayload {
    question: string;
    answer: string;
}

interface AiCandidatePayloadEditorProps {
    candidateId: number;
    capability: CandidateCapability;
    initialPayload?: string | null;
    onPayloadChange?: (candidateId: number, payload: string) => void;
    onSubmitEnabledChange?: (candidateId: number, canSubmit: boolean) => void;
}

const parseTagsPayload = (payload?: string | null) => {
    if (!payload) {
        return [];
    }
    try {
        const parsed = JSON.parse(payload);
        if (Array.isArray(parsed)) {
            return parsed.filter((tag) => typeof tag === "string").map((tag) => tag.trim());
        }
        if (Array.isArray(parsed?.tags)) {
            return parsed.tags.filter((tag) => typeof tag === "string").map((tag) => tag.trim());
        }
    } catch {
        return [];
    }
    return [];
};

const parseQaPairsPayload = (payload?: string | null) => {
    if (!payload) {
        return [];
    }
    try {
        const parsed = JSON.parse(payload);
        let pairs: unknown[] = [];
        if (Array.isArray(parsed)) {
            pairs = parsed;
        } else if (Array.isArray(parsed?.qaPairs)) {
            pairs = parsed.qaPairs;
        }
        return pairs
            .filter((pair) => pair && typeof pair === "object")
            .map((pair) => {
                return {
                    answer: String(pair.answer ?? "").trim(),
                    question: String(pair.question ?? "").trim()
                };
            })
            .filter((pair) => pair.question && pair.answer);
    } catch {
        return [];
    }
};

const stringifyTagsPayload = (tags: string[]) => {
    const normalized = tags.map((tag) => tag.trim()).filter(Boolean);
    return JSON.stringify({ tags: normalized });
};

const stringifyQaPairsPayload = (pairs: AiCandidateQaPairPayload[]) => {
    const normalized = pairs
        .map((pair) => ({
            answer: pair.answer.trim(),
            question: pair.question.trim()
        }))
        .filter((pair) => pair.question && pair.answer);
    return JSON.stringify({ qaPairs: normalized });
};

export const AiCandidatePayloadEditor = ({
    capability,
    candidateId,
    initialPayload,
    onPayloadChange,
    onSubmitEnabledChange
}: AiCandidatePayloadEditorProps) => {
    const [textPayload, setTextPayload] = useState(() => {
        if (capability === "summary" || capability === "translate") {
            return (initialPayload || "").trim();
        }
        return "";
    });
    const [tagsPayload, setTagsPayload] = useState<string[]>(() => {
        if (capability === "tags") {
            return parseTagsPayload(initialPayload);
        }
        return [];
    });
    const [qaPayload, setQaPayload] = useState<AiCandidateQaPairPayload[]>(() => {
        if (capability === "qa") {
            return parseQaPairsPayload(initialPayload);
        }
        return [];
    });

    const payload = (() => {
        if (capability === "summary" || capability === "translate") {
            return textPayload;
        }
        if (capability === "tags") {
            return stringifyTagsPayload(tagsPayload);
        }
        return stringifyQaPairsPayload(qaPayload);
    })();

    const canSubmitPayload = (() => {
        if (capability === "summary" || capability === "translate") {
            return textPayload.trim().length > 0;
        }
        if (capability === "tags") {
            return tagsPayload.some((tag) => tag.trim().length > 0);
        }
        return qaPayload.some((pair) => pair.question.trim() && pair.answer.trim());
    })();

    useEffect(() => {
        if (onSubmitEnabledChange) {
            onSubmitEnabledChange(candidateId, canSubmitPayload);
        }
        if (onPayloadChange) {
            onPayloadChange(candidateId, payload);
        }
    }, [canSubmitPayload, candidateId, onPayloadChange, onSubmitEnabledChange, payload]);

    const updateTextPayload = (next: string) => {
        setTextPayload(next);
    };

    const addTag = () => {
        setTagsPayload((current) => {
            const next = [...current, ""];
            return next;
        });
    };

    const updateTag = (index: number, value: string) => {
        setTagsPayload((current) => {
            const next = [...current];
            next[index] = value;
            return next;
        });
    };

    const removeTag = (index: number) => {
        setTagsPayload((current) => {
            const next = current.filter((_, itemIndex) => itemIndex !== index);
            return next;
        });
    };

    const addQaPair = () => {
        setQaPayload((current) => {
            const next = [...current, { answer: "", question: "" }];
            return next;
        });
    };

    const updateQaPair = (
        index: number,
        field: "question" | "answer",
        value: string
    ) => {
        setQaPayload((current) => {
            const next = [...current];
            next[index] = {
                ...next[index],
                [field]: value
            };
            return next;
        });
    };

    const removeQaPair = (index: number) => {
        setQaPayload((current) => {
            const next = current.filter((_, itemIndex) => itemIndex !== index);
            return next;
        });
    };

    if (capability === "summary" || capability === "translate") {
        return (
            <TextArea
                aria-label={`${capability} 候选编辑`}
                autoSize={{ minRows: 3, maxRows: 8 }}
                showCount
                maxLength={4000}
                value={textPayload}
                onChange={(event) => updateTextPayload(event.target.value)}
            />
        );
    }

    if (capability === "tags") {
        return (
            <div>
                {tagsPayload.map((tag, index) => (
                    <Space key={`${tag}-${index}`} align="center" style={{ marginBottom: 8 }}>
                        <Input
                            aria-label={`标签 ${index + 1}`}
                            value={tag}
                            onChange={(event) => updateTag(index, event.target.value)}
                        />
                        <Button danger size="small" onClick={() => removeTag(index)}>
                            删除
                        </Button>
                    </Space>
                ))}
                <Button size="small" onClick={addTag}>
                    新增标签
                </Button>
                {!canSubmitPayload ? <p>请至少填写一个非空标签</p> : null}
            </div>
        );
    }

    return (
        <div>
            {qaPayload.map((pair, index) => (
                <div
                    key={`${pair.question}-${pair.answer}-${index}`}
                    style={{ marginBottom: 12 }}
                >
                    <Input.TextArea
                        aria-label={`问答问题 ${index + 1}`}
                        placeholder="问题"
                        autoSize={{ minRows: 1, maxRows: 3 }}
                        value={pair.question}
                        onChange={(event) => updateQaPair(index, "question", event.target.value)}
                        style={{ marginBottom: 8 }}
                    />
                    <Input.TextArea
                        aria-label={`问答答案 ${index + 1}`}
                        placeholder="答案"
                        autoSize={{ minRows: 1, maxRows: 3 }}
                        value={pair.answer}
                        onChange={(event) => updateQaPair(index, "answer", event.target.value)}
                        style={{ marginBottom: 8 }}
                    />
                    <Button danger size="small" onClick={() => removeQaPair(index)}>
                        删除
                    </Button>
                </div>
            ))}
            <Button size="small" onClick={addQaPair}>
                新增问答
            </Button>
            {!canSubmitPayload ? <p>请至少填写一组完整问题与答案</p> : null}
        </div>
    );
};
