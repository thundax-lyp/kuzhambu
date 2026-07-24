import { CameraOutlined } from "@ant-design/icons";
import { Upload } from "antd";
import { useState } from "react";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuFormItem } from "@/components/kuzhambu-form";
import { UserAvatar } from "@/pages/system/user/components/user-avatar";
import type { UserRecord } from "@/pages/system/user/user-types";
import "./user-avatar-field.css";

interface UserAvatarFieldProps {
    user: UserRecord;
    onAvatarUpload?: (file: File) => Promise<unknown> | void;
}

export const UserAvatarField = ({ user, onAvatarUpload }: UserAvatarFieldProps) => {
    const [avatarUploading, setAvatarUploading] = useState(false);

    return (
        <KuzhambuFormItem endOfLine label="头像" layoutSize="large">
            <div className="user-avatar-field">
                <UserAvatar user={user} size={64} />
                <Upload
                    accept="image/*"
                    showUploadList={false}
                    beforeUpload={(file) => {
                        const uploadResult = onAvatarUpload?.(file);
                        if (uploadResult) {
                            setAvatarUploading(true);
                            Promise.resolve(uploadResult)
                                .finally(() => setAvatarUploading(false))
                                .catch(() => undefined);
                        }
                        return Upload.LIST_IGNORE;
                    }}
                >
                    <KuzhambuButton
                        testId="system-user-user-action-button-2"
                        size="small"
                        shape="circle"
                        icon={<CameraOutlined />}
                        loading={avatarUploading}
                    />
                </Upload>
            </div>
        </KuzhambuFormItem>
    );
};
