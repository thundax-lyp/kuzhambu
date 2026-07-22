import { useState } from "react";
import { Download } from "lucide-react";
import { Button } from "@/components/ui/button";
import type {
    ClassicsSharePortalImage,
    ClassicsShareResource,
    ClassicsShareResourceContentUrlCommand
} from "@/pages/share-detail/share-detail-types";

interface SancaiImageGalleryProps {
    images?: ClassicsSharePortalImage[] | null;
    onResolveResourceUrl: (command: ClassicsShareResourceContentUrlCommand) => string;
    privateAccess: boolean;
    token: string;
}

export const SancaiImageGallery = ({
    images,
    onResolveResourceUrl,
    privateAccess,
    token
}: SancaiImageGalleryProps) => {
    const visibleImages = sortSancaiImages(
        (images || []).filter((image) => image.storageObject?.storageObjectId)
    );
    const defaultImage = visibleImages.find((image) => image.currentUsed) ?? visibleImages[0];
    const [selectedImageKey, setSelectedImageKey] = useState<string | null>(null);
    const selectedImage =
        visibleImages.find(
            (image, index) => getSancaiImageKey(image, index) === selectedImageKey
        ) ?? defaultImage;
    if (!visibleImages.length) {
        return null;
    }
    const selectedPreviewUrl = selectedImage
        ? resolveImageUrl(token, selectedImage, "preview", privateAccess, onResolveResourceUrl)
        : undefined;
    const selectedDownloadUrl = selectedImage
        ? resolveImageUrl(token, selectedImage, "download", privateAccess, onResolveResourceUrl)
        : undefined;
    const selectedTitle = selectedImage ? getSancaiImageTitle(selectedImage) : "三才图会图片";

    return (
        <section className="portal-share-images" aria-label="三才图会图片">
            <h3>三才图会图片</h3>
            <figure className="portal-share-image-featured">
                {selectedPreviewUrl ? (
                    <img src={selectedPreviewUrl} alt={selectedTitle} />
                ) : (
                    <div className="portal-share-image-placeholder">图片暂不可预览</div>
                )}
                <figcaption>
                    <div>
                        <strong>{selectedTitle}</strong>
                        <span>{formatFileSize(selectedImage?.size)}</span>
                    </div>
                    {selectedDownloadUrl ? (
                        <Button asChild size="sm" variant="outline">
                            <a href={selectedDownloadUrl} target="_blank" rel="noreferrer">
                                <Download aria-hidden="true" />
                                下载原图
                            </a>
                        </Button>
                    ) : null}
                </figcaption>
            </figure>
            <div className="portal-share-image-thumbnails" aria-label="三才图会图片缩略图">
                {visibleImages.map((image, index) => {
                    const imageKey = getSancaiImageKey(image, index);
                    const previewUrl = resolveImageUrl(
                        token,
                        image,
                        "preview",
                        privateAccess,
                        onResolveResourceUrl
                    );
                    const title = getSancaiImageTitle(image);
                    const selected = selectedImage === image;
                    return (
                        <button
                            aria-current={selected ? "true" : undefined}
                            aria-label={`切换图片 ${title}`}
                            className="portal-share-image-thumb"
                            key={imageKey}
                            type="button"
                            onClick={() => setSelectedImageKey(imageKey)}
                        >
                            {previewUrl ? <img src={previewUrl} alt="" /> : <span>无预览</span>}
                            <strong>{title}</strong>
                            {image.currentUsed ? <span>当前使用</span> : null}
                        </button>
                    );
                })}
            </div>
        </section>
    );
};

const resolveImageUrl = (
    token: string,
    image: ClassicsSharePortalImage,
    mode: "download" | "preview",
    privateAccess: boolean,
    onResolveResourceUrl: (command: ClassicsShareResourceContentUrlCommand) => string
) => {
    const directUrl = mode === "download" ? image.downloadUrl : image.previewUrl;
    if (directUrl && !privateAccess) {
        return directUrl;
    }
    const resourceUrl =
        mode === "download" ? image.storageObject?.downloadUrl : image.storageObject?.previewUrl;
    return !privateAccess && resourceUrl
        ? resourceUrl
        : resolveResourceUrl(token, image.storageObject, mode, privateAccess, onResolveResourceUrl);
};

const resolveResourceUrl = (
    token: string,
    resource: ClassicsShareResource | null | undefined,
    mode: "download" | "preview",
    privateAccess: boolean,
    onResolveResourceUrl: (command: ClassicsShareResourceContentUrlCommand) => string
) => {
    if (!resource?.storageObjectId) {
        return undefined;
    }
    return onResolveResourceUrl({
        mode,
        privateAccess,
        shareToken: token,
        storageObjectId: resource.storageObjectId
    });
};

const getSancaiImageKey = (image: ClassicsSharePortalImage, index: number) => {
    return `${image.imageId ?? "image"}-${image.storageObjectId ?? image.storageObject?.storageObjectId ?? index}`;
};

const getSancaiImageTitle = (image: ClassicsSharePortalImage) => {
    return image.title || image.originalFilename || `图片 ${image.imageId ?? "-"}`;
};

const sortSancaiImages = (images: ClassicsSharePortalImage[]) => {
    return [...images].sort((left, right) => {
        const leftPriority = left.priority ?? Number.MAX_SAFE_INTEGER;
        const rightPriority = right.priority ?? Number.MAX_SAFE_INTEGER;
        if (leftPriority !== rightPriority) {
            return leftPriority - rightPriority;
        }
        return (left.imageId ?? 0) - (right.imageId ?? 0);
    });
};

const formatFileSize = (size?: number | null) => {
    if (!size) {
        return "-";
    }
    if (size < 1024) {
        return `${size} B`;
    }
    if (size < 1024 * 1024) {
        return `${(size / 1024).toFixed(1)} KB`;
    }
    return `${(size / 1024 / 1024).toFixed(1)} MB`;
};
