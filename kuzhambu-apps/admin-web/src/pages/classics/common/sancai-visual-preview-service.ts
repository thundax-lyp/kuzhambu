import { postJson } from "@/api/http";
import type { SancaiVisualPreviewAssetRecord } from "./sancai-visual-preview-types";

export const listVisualAssets = (entryId: string) => {
    return postJson<SancaiVisualPreviewAssetRecord[], { entryId: string }>(
        "/classics/sancai/assets/visual-assets/list",
        {
            body: { entryId }
        }
    );
};
