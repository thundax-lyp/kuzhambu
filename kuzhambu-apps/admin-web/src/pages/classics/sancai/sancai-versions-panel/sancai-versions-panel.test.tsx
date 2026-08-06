import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { SancaiVersionsPanel } from "./sancai-versions-panel";

describe("SancaiVersionsPanel", () => {
    it("renders text changes from the historical snapshot to the current entry", () => {
        render(
            <SancaiVersionsPanel
                currentEntry={{
                    id: "entry-1",
                    originalText: "天地玄妙",
                    translationText: "当前译文",
                    summary: "当前摘要"
                }}
                selectedVersion={{
                    id: "version-1",
                    versionNo: 1,
                    snapshotJson: JSON.stringify({
                        originalText: "天地玄黄",
                        translationText: "历史译文",
                        summary: "历史摘要"
                    })
                }}
                versions={[]}
                onResetVersion={vi.fn()}
                onSelectVersion={vi.fn()}
            />
        );

        const originalTextCompare = screen.getByTestId(
            "classics-sancai-version-originalText-compare"
        );
        expect(originalTextCompare).toHaveAccessibleName("原文差异（历史 → 当前）");
        expect(originalTextCompare.querySelector(".is-removed")).toHaveTextContent("黄");
        expect(originalTextCompare.querySelector(".is-added")).toHaveTextContent("妙");
    });
});
