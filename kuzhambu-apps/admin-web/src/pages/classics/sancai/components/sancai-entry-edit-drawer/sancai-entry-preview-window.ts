import type { SancaiEntryFormValues } from "../sancai-form-values";
import type { SancaiVisualAssetRecord } from "@/pages/classics/sancai/sancai-types";

interface OpenSancaiEntryPreviewWindowOptions {
    currentVisualAsset?: SancaiVisualAssetRecord | null;
    form: SancaiEntryFormValues;
    imageUrl?: string;
    visualDescription?: string | null;
    visualUrl?: string;
}

const escapeHtml = (value?: string | number | null) => {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
};

const readVisualAssetTitle = (asset: SancaiVisualAssetRecord | undefined | null) => {
    if (!asset) {
        return "未选择视觉处理";
    }
    return `处理记录 ${asset.versionNo ?? asset.visualAssetId ?? asset.id ?? "-"}`;
};

const toPreviewUrl = (url?: string) => {
    if (!url || typeof window === "undefined") {
        return "";
    }
    return new URL(url, window.location.origin).toString();
};

const buildPreviewImageSection = (imageUrl: string, visualUrl: string) => {
    if (!imageUrl && !visualUrl) {
        return "";
    }
    return `<h2>图像</h2><div class="grid">${imageUrl ? `<img src="${escapeHtml(imageUrl)}" alt="条目图片" />` : ""}${visualUrl ? `<img src="${escapeHtml(visualUrl)}" alt="视觉处理生成图" />` : ""}</div>`;
};

const buildVisualDescriptionSection = (visualDescription?: string | null) => {
    if (!visualDescription) {
        return "";
    }
    return `<h2>视觉描述</h2><p>${escapeHtml(visualDescription)}</p>`;
};

const buildPreviewHtml = ({
    currentVisualAsset,
    form,
    imageUrl,
    visualDescription,
    visualUrl
}: OpenSancaiEntryPreviewWindowOptions) => {
    const previewImageUrl = toPreviewUrl(imageUrl);
    const previewVisualUrl = toPreviewUrl(visualUrl);

    return `<!doctype html>
<html lang="zh-CN">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>${escapeHtml(form.title || "三才图会条目预览")}</title>
<style>
body{margin:0;background:#f7f1e6;color:#2f2418;font:16px/1.75 "Songti SC","STSong","Noto Serif CJK SC",serif;}
main{max-width:960px;margin:0 auto;padding:48px 28px 64px;}
h1{margin:0 0 10px;font-size:30px;line-height:1.3;font-weight:800;}
h2{margin:32px 0 10px;font-size:18px;border-bottom:1px solid rgba(124,93,59,.28);padding-bottom:8px;}
.meta{display:flex;gap:12px;flex-wrap:wrap;color:#7c5d3b;font-size:14px;}
.paper{margin-top:24px;padding:28px;background:#fffaf0;border:1px solid rgba(124,93,59,.26);box-shadow:0 18px 48px rgba(72,48,24,.08);}
p{white-space:pre-wrap;margin:0;}
img{display:block;max-width:100%;height:auto;border:1px solid rgba(124,93,59,.24);background:#fffaf0;}
.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:18px;}
</style>
</head>
<body>
<main>
<h1>${escapeHtml(form.title || "未命名条目")}</h1>
<div class="meta">
<span>可见性：${escapeHtml(form.visibility)}</span>
<span>当前视觉处理记录：${escapeHtml(readVisualAssetTitle(currentVisualAsset))}</span>
</div>
<section class="paper">
<h2>原文</h2><p>${escapeHtml(form.originalText || "-")}</p>
<h2>译文</h2><p>${escapeHtml(form.translationText || "-")}</p>
<h2>摘要</h2><p>${escapeHtml(form.summary || "-")}</p>
${buildPreviewImageSection(previewImageUrl, previewVisualUrl)}
${buildVisualDescriptionSection(visualDescription)}
</section>
</main>
</body>
</html>`;
};

export const openSancaiEntryPreviewWindow = (options: OpenSancaiEntryPreviewWindowOptions) => {
    if (typeof window === "undefined") {
        return;
    }
    const html = buildPreviewHtml(options);
    const blob = new Blob([html], { type: "text/html;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    window.open(url, "_blank", "noopener,noreferrer");
    window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
};
