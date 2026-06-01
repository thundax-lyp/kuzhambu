import { CopyOutlined } from "@ant-design/icons";
import { Button, Tooltip, Typography } from "antd";

const { Text } = Typography;

interface OpenClientSecretFieldProps {
    label: string;
    value?: string | null;
    onCopy: (label: string, value?: string | null) => void;
}

export const OpenClientSecretField = ({ label, value, onCopy }: OpenClientSecretFieldProps) => (
    <div className="open-client-secret-field">
        <div className="open-client-secret-field-header">
            <Text strong>{label}</Text>
        </div>
        <div className="open-client-secret-control">
            <div className="open-client-secret-value" title={value || undefined}>
                {value || "-"}
            </div>
            <Tooltip title={`复制 ${label}`}>
                <Button
                    className="open-client-secret-copy"
                    type="text"
                    icon={<CopyOutlined />}
                    aria-label={`复制 ${label}`}
                    disabled={!value}
                    onClick={() => onCopy(label, value)}
                >
                    复制
                </Button>
            </Tooltip>
        </div>
    </div>
);
