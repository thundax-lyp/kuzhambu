import {
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuGraph,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";
import type { GraphMaterialDraftObject, GraphMaterialRecord } from "../graph-material-types";

interface MaterialDraftCanvasProps {
    material: GraphMaterialRecord;
    onClose: () => void;
    onOpenObject: (objectId: string) => void;
}

const DRAFT_OBJECTS: GraphMaterialDraftObject[] = [
    { id: "draft-object-li-bai", name: "李白", type: "人物", sourceText: "字太白，号青莲居士。" },
    { id: "draft-object-tang-poetry", name: "唐诗", type: "作品分类", sourceText: "唐代诗歌总集。" }
];

export const MaterialDraftCanvas = ({
    material,
    onClose,
    onOpenObject
}: MaterialDraftCanvasProps) => {
    const isDraft = material.status === "DRAFT";
    return (
        <KuzhambuCard
            title={`素材画布：${material.title}`}
            extra={<KuzhambuTag>{material.status}</KuzhambuTag>}
        >
            <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                <p>正文摘要：本素材用于演示图谱对象、证据和关系草稿的本地 Mock 交互。</p>
                <KuzhambuGraph
                    height={300}
                    spoList={[{ subject: "李白", predicate: "归属", object: "唐诗" }]}
                />
                <KuzhambuSpace wrap>
                    {DRAFT_OBJECTS.map((object) => (
                        <KuzhambuButton
                            key={object.id}
                            testId={`knowledge-graph-material-open-object-${object.id}-button`}
                            onClick={() => onOpenObject(object.id)}
                        >
                            对象：{object.name}
                        </KuzhambuButton>
                    ))}
                </KuzhambuSpace>
                {isDraft ? (
                    <KuzhambuSpace wrap>
                        <KuzhambuButton testId="knowledge-graph-material-create-draft-object-button">
                            新增对象
                        </KuzhambuButton>
                        <KuzhambuButton testId="knowledge-graph-material-extract-draft-button">
                            抽取草稿
                        </KuzhambuButton>
                        <KuzhambuButton testId="knowledge-graph-material-import-draft-button">
                            导入草稿
                        </KuzhambuButton>
                    </KuzhambuSpace>
                ) : null}
                {material.status === "PUBLISHED" ? (
                    <KuzhambuSpace>
                        <KuzhambuTag type="success">发布结果：已成功发布</KuzhambuTag>
                        <KuzhambuButton testId="knowledge-graph-material-withdraw-button">
                            撤回素材
                        </KuzhambuButton>
                    </KuzhambuSpace>
                ) : null}
                <KuzhambuButton
                    testId="knowledge-graph-material-close-draft-canvas-button"
                    onClick={onClose}
                >
                    关闭素材画布
                </KuzhambuButton>
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};
