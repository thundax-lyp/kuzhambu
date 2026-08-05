import { describe, expect, it } from "vitest";
import type {
    SancaiEntryRecord,
    SancaiVisualAssetRecord
} from "@/pages/classics/sancai-visual/sancai-visual-types";
import { buildInputPayloadJson } from "./hooks/use-sancai-entry-visual-refinement";

const entry: SancaiEntryRecord = {
    id: "3001",
    title: "三才图会序",
    originalText: "天地玄黄",
    translationText: "译文",
    summary: "摘要"
};

const asset: SancaiVisualAssetRecord = {
    id: "5002",
    visualAssetId: "5002",
    sourceImageStorageObjectId: "source-image-1",
    textWeight: 6,
    imageWeight: 4,
    imageAnalysisMarkdown: "图片理解结果",
    fusionDescription: "融合说明",
    visualDescription: "视觉描述",
    generationParamsJson: "写实古籍插画风格"
};

const parsePayload = (
    capability: Parameters<typeof buildInputPayloadJson>[0],
    capabilityCode: string
) =>
    JSON.parse(
        buildInputPayloadJson(capability, capabilityCode, entry, asset.visualAssetId ?? null, asset)
    ) as Record<string, unknown>;

describe("sancai entry visual refinement payload", () => {
    it("maps image analysis payload to seed prompt variables", () => {
        expect(parsePayload("image_analysis", "CLASSICS_IMAGE_DESCRIBE")).toMatchObject({
            capability: "CLASSICS_IMAGE_DESCRIBE",
            contentId: "3001",
            contentType: "SANCAI_ENTRY",
            contextText: "原文：天地玄黄\n\n译文：译文\n\n摘要：摘要",
            imageDescription: null,
            sourceImageStorageObjectId: "source-image-1",
            title: "三才图会序"
        });
    });

    it("maps fusion payload to seed prompt variables", () => {
        expect(parsePayload("fusion", "CLASSICS_IMAGE_PROMPT_FUSION")).toMatchObject({
            imageAnalysis: "图片理解结果",
            imageAnalysisMarkdown: "图片理解结果",
            sourceText: "天地玄黄",
            translationText: "译文"
        });
    });

    it("maps visual payload to seed prompt variables", () => {
        expect(parsePayload("visual", "CLASSICS_VISUAL_DESCRIBE")).toMatchObject({
            fusionDescription: "融合说明",
            fusionText: "融合说明",
            styleGuide: "写实古籍插画风格"
        });
    });

    it("maps image generation payload to seed prompt variables", () => {
        expect(parsePayload("image_gen", "CLASSICS_IMAGE_GENERATE")).toMatchObject({
            sourceText: "视觉描述",
            styleGuide: "写实古籍插画风格",
            visualDescription: "视觉描述"
        });
    });
});
