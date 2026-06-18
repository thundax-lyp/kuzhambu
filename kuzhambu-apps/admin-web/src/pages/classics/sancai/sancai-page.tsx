import { Typography } from "antd";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import "./sancai-page.css";

const { Text } = Typography;

export const SancaiPage = () => {
    return (
        <KuzhambuPage
            className="sancai-page"
            title="三才图会"
            description="后台条目治理页面正在接入。"
        >
            <Text type="secondary">三才图会目录、条目和编辑闭环将在本页承载。</Text>
        </KuzhambuPage>
    );
};
