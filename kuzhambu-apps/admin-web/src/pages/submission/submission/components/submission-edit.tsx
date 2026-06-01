import { FileImageOutlined, UploadOutlined } from "@ant-design/icons";
import { useMutation } from "@tanstack/react-query";
import { App, Button, Form, Input, Space, Typography, Upload } from "antd";
import { useEffect, useState } from "react";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import * as service from "../submission-service";
import type { SubmissionSaveCommand } from "../submission-service";
import type { StorageUploadRecord } from "../submission-types";

const { Text } = Typography;
const { TextArea } = Input;

interface SubmissionEditProps {
    open?: boolean;
    saving?: boolean;
    onClose: () => void;
    onSave: (request: SubmissionSaveCommand) => void;
}

interface SubmissionFormValues {
    title: string;
    content: string;
    imageObjectIds?: string[];
}

interface UploadedSubmissionImage {
    id: string;
    name: string;
}

const readFormRequest = (values: SubmissionFormValues): SubmissionSaveCommand => {
    return {
        title: values.title.trim(),
        content: values.content.trim(),
        imageObjectIds: values.imageObjectIds?.filter(Boolean) || []
    };
};

const readUploadedImageName = (response: StorageUploadRecord, fallbackName: string) => {
    return response.originalFilename || fallbackName || response.id || "图片";
};

export const SubmissionEdit = ({ open, saving, onClose, onSave }: SubmissionEditProps) => {
    const { message: messageApi } = App.useApp();
    const [form] = Form.useForm<SubmissionFormValues>();
    const [uploadedImages, setUploadedImages] = useState<UploadedSubmissionImage[]>([]);

    const uploadMutation = useMutation({
        mutationFn: service.uploadSubmissionImage,
        onSuccess: (response, file) => {
            if (response.error) {
                messageApi.error(response.error);
                return;
            }
            if (!response.id) {
                messageApi.error("上传失败");
                return;
            }

            const currentIds = form.getFieldValue("imageObjectIds") || [];
            form.setFieldValue("imageObjectIds", [...currentIds, response.id]);
            setUploadedImages((currentImages) => [
                ...currentImages,
                {
                    id: response.id as string,
                    name: readUploadedImageName(response, file.name)
                }
            ]);
            messageApi.success("图片已上传");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "上传失败");
        }
    });

    useEffect(() => {
        if (!open) {
            return;
        }
        form.resetFields();
    }, [form, open]);

    const saveSubmission = async () => {
        const values = await form.validateFields();
        onSave(
            readFormRequest({
                ...values,
                imageObjectIds: form.getFieldValue("imageObjectIds")
            })
        );
    };

    const closeEditor = () => {
        if (saving || uploadMutation.isPending) {
            return;
        }
        onClose();
    };

    const removeUploadedImage = (id: string) => {
        setUploadedImages((currentImages) => currentImages.filter((image) => image.id !== id));
        const currentIds = form.getFieldValue("imageObjectIds") || [];
        form.setFieldValue(
            "imageObjectIds",
            currentIds.filter((currentId: string) => currentId !== id)
        );
    };

    return (
        <SandwishDrawer
            title="新增提交"
            open={Boolean(open)}
            size="middle"
            onClose={closeEditor}
            footer={
                <div className="submission-editor-footer">
                    <Button onClick={closeEditor}>取消</Button>
                    <Button type="primary" loading={saving} onClick={saveSubmission}>
                        保存
                    </Button>
                </div>
            }
        >
            <Form<SubmissionFormValues>
                form={form}
                layout="vertical"
                className="submission-editor-form"
            >
                <Form.Item
                    name="title"
                    label="标题"
                    rules={[
                        { required: true, message: "请输入标题" },
                        { max: 200, message: "标题不能超过 200 个字符" }
                    ]}
                >
                    <Input placeholder="请输入标题" maxLength={200} showCount />
                </Form.Item>
                <Form.Item
                    name="content"
                    label="正文"
                    rules={[{ required: true, message: "请输入正文" }]}
                >
                    <TextArea placeholder="请输入正文" rows={8} />
                </Form.Item>
                <Form.Item label="图片">
                    <div className="submission-upload-field">
                        <Upload
                            accept="image/*"
                            beforeUpload={(file) => {
                                uploadMutation.mutate(file);
                                return false;
                            }}
                            disabled={uploadMutation.isPending}
                            showUploadList={false}
                        >
                            <Button icon={<UploadOutlined />} loading={uploadMutation.isPending}>
                                上传图片
                            </Button>
                        </Upload>
                        {uploadedImages.length ? (
                            <div className="submission-upload-list">
                                {uploadedImages.map((image) => (
                                    <div key={image.id} className="submission-upload-item">
                                        <Space size={8} className="submission-upload-item-name">
                                            <FileImageOutlined />
                                            <Text ellipsis>{image.name}</Text>
                                        </Space>
                                        <Button
                                            type="text"
                                            danger
                                            size="small"
                                            onClick={() => removeUploadedImage(image.id)}
                                        >
                                            移除
                                        </Button>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <Text type="secondary">可上传多张图片，保存后绑定到提交内容。</Text>
                        )}
                    </div>
                </Form.Item>
            </Form>
        </SandwishDrawer>
    );
};
