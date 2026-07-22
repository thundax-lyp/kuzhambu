import type { FormEventHandler, KeyboardEventHandler } from "react";
import { SendHorizontal } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

interface QaComposerProps {
    disabled: boolean;
    question: string;
    onQuestionChange: (value: string) => void;
    onQuestionKeyDown: KeyboardEventHandler<HTMLTextAreaElement>;
    onSubmit: FormEventHandler<HTMLFormElement>;
}

export const QaComposer = ({
    disabled,
    question,
    onQuestionChange,
    onQuestionKeyDown,
    onSubmit
}: QaComposerProps) => {
    return (
        <form className="portal-qa-form" onSubmit={onSubmit}>
            <Label className="portal-filter-field portal-qa-question">
                <Textarea
                    aria-label="问题"
                    name="question"
                    placeholder="请输入问题"
                    rows={4}
                    value={question}
                    onKeyDown={onQuestionKeyDown}
                    onChange={(event) => onQuestionChange(event.target.value)}
                />
            </Label>
            <div className="portal-qa-actions">
                <Button disabled={disabled} type="submit">
                    <SendHorizontal aria-hidden="true" size={16} />
                    {disabled ? "回答中..." : "发送"}
                </Button>
            </div>
        </form>
    );
};
