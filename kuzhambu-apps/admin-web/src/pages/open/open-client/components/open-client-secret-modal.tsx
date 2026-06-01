import { Modal, Typography } from "antd";
import type { OpenClientSecretRecord } from "../open-client-types";
import { OpenClientSecretField } from "./open-client-secret-field";

const { Text } = Typography;

interface OpenClientSecretModalProps {
    secret?: OpenClientSecretRecord | null;
    onClose: () => void;
    onCopySecret: (label: string, value?: string | null) => void;
}

export const OpenClientSecretModal = ({
    secret,
    onClose,
    onCopySecret
}: OpenClientSecretModalProps) => (
    <Modal
        className="open-client-secret-modal"
        open={Boolean(secret)}
        width={680}
        title="API SECRET 已重置"
        okText="我已保存"
        cancelButtonProps={{ style: { display: "none" } }}
        onOk={onClose}
        onCancel={onClose}
    >
        <Text className="open-client-secret-note" type="secondary">
            API KEY 保持不变，新的 API SECRET 只在本次结果中显示。
        </Text>
        <div className="open-client-secret-panel">
            <OpenClientSecretField label="API KEY" value={secret?.apiKey} onCopy={onCopySecret} />
            <OpenClientSecretField
                label="API SECRET"
                value={secret?.apiSecret}
                onCopy={onCopySecret}
            />
        </div>
    </Modal>
);
