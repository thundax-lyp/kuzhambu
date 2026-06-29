import { Button, Progress, Typography } from "antd";
import { StorageUploadTaskRecord } from "../storage-object-types";
import "./storage-upload-task-card.css";

const { Text } = Typography;

interface StorageUploadTaskCardProps {
    task: StorageUploadTaskRecord;
    onCancel?: () => void;
}

const toUploadStageText = (stage: StorageUploadTaskRecord["stage"]) => {
    if (stage === "uploading-single") {
        return "单文件上传中";
    }
    if (stage === "initiating-multipart") {
        return "初始化分片上传";
    }
    if (stage === "uploading-parts") {
        return "上传分片中";
    }
    if (stage === "completing-multipart") {
        return "合并分片中";
    }
    if (stage === "success") {
        return "上传完成";
    }
    if (stage === "error") {
        return "上传失败";
    }
    if (stage === "aborting") {
        return "正在取消";
    }
    if (stage === "aborted") {
        return "已取消";
    }
    return "待上传";
};

const bytesRate = (uploadedBytes: number, totalBytes: number) => {
    if (!Number.isFinite(uploadedBytes) || !Number.isFinite(totalBytes) || totalBytes <= 0) {
        return "0 / 0 B";
    }

    return `${uploadedBytes} / ${totalBytes} B`;
};

export const StorageUploadTaskCard = ({ task, onCancel }: StorageUploadTaskCardProps) => {
    const percent = task.totalBytes <= 0 ? 0 : Math.round((task.uploadedBytes / task.totalBytes) * 100);
    const canCancel = task.canCancel && ["uploading-single", "initiating-multipart", "uploading-parts", "completing-multipart"].includes(task.stage);

    return (
        <div className="storage-upload-task-card">
            <div className="storage-upload-task-card-header">
                <Text className="storage-upload-task-card-title" strong>
                    {task.fileName}
                </Text>
                <Button
                    size="small"
                    danger
                    disabled={!canCancel}
                    onClick={() => {
                        if (!task.canCancel || !onCancel) {
                            return;
                        }
                        onCancel();
                    }}
                >
                    取消
                </Button>
            </div>
            <Text className="storage-upload-task-card-stage">{toUploadStageText(task.stage)}</Text>
            <Progress
                className="storage-upload-task-card-progress"
                percent={percent}
                size="small"
                status={task.stage === "error" ? "exception" : task.stage === "success" ? "success" : "active"}
                showInfo={false}
            />
            <Text className="storage-upload-task-card-meta" type="secondary">
                {bytesRate(task.uploadedBytes, task.totalBytes)}
            </Text>
            {task.totalPartCount > 0 ? (
                <Text className="storage-upload-task-card-meta" type="secondary">
                    {`已上传分片：${task.uploadedPartCount} / ${task.totalPartCount}`}
                </Text>
            ) : null}
            {task.errorMessage ? <Text type="danger">{task.errorMessage}</Text> : null}
        </div>
    );
};
