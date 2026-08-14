import { Descriptions } from "antd";
import { KuzhambuDrawer } from "@/components";
import type { GraphMaterialDraftObject } from "../graph-material-types";

interface MaterialObjectDrawerProps {
    objectId: string | null;
    onClose: () => void;
    open: boolean;
}

const OBJECTS: GraphMaterialDraftObject[] = [
    { id: "draft-object-li-bai", name: "李白", type: "人物", sourceText: "字太白，号青莲居士。" },
    { id: "draft-object-tang-poetry", name: "唐诗", type: "作品分类", sourceText: "唐代诗歌总集。" }
];

export const MaterialObjectDrawer = ({ objectId, onClose, open }: MaterialObjectDrawerProps) => {
    const object = OBJECTS.find((item) => item.id === objectId);
    return (
        <KuzhambuDrawer
            open={open}
            onClose={onClose}
            title="素材对象详情"
            size="middle"
            testId="knowledge-graph-material-object-drawer"
        >
            <Descriptions bordered column={1} size="small">
                <Descriptions.Item label="对象名称">{object?.name ?? "-"}</Descriptions.Item>
                <Descriptions.Item label="对象类型">{object?.type ?? "-"}</Descriptions.Item>
                <Descriptions.Item label="证据摘录">{object?.sourceText ?? "-"}</Descriptions.Item>
            </Descriptions>
        </KuzhambuDrawer>
    );
};
