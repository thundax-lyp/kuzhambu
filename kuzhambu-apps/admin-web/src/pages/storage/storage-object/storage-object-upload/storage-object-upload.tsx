import { UploadOutlined } from "@ant-design/icons";
import { useMutation } from "@tanstack/react-query";
import { App, Popover } from "antd";
import { useEffect, useRef, useState } from "react";
import type { ChangeEvent } from "react";
import { KuzhambuButton } from "@/components";
import * as service from "../storage-object-service";
import { StorageUploadTaskCard } from "../storage-upload-task-card";
import type { StorageUploadTaskRecord } from "../storage-object-types";

import "./storage-object-upload.css";

const UPLOAD_ACCEPT =
    ".jpg,.jpeg,.png,.gif,.webp,.pdf,.txt,.md,.csv,.json,.html,.zip,.docx,.xlsx,.pptx";

const UPLOAD_ACTIVE_STAGES: StorageUploadTaskRecord["stage"][] = [
    "uploading-single",
    "initiating-multipart",
    "uploading-parts",
    "completing-multipart"
];

interface StorageObjectUploadProps {
    canUpload: boolean;
    onUploaded: () => Promise<void>;
}

export const StorageObjectUpload = ({ canUpload, onUploaded }: StorageObjectUploadProps) => {
    const { message: messageApi } = App.useApp();
    const [uploadTask, setUploadTask] = useState<StorageUploadTaskRecord | null>(null);
    const uploadInputRef = useRef<HTMLInputElement>(null);
    const uploadAbortControllerRef = useRef<AbortController | null>(null);

    useEffect(() => {
        return () => uploadAbortControllerRef.current?.abort();
    }, []);

    const uploadMutation = useMutation({
        mutationFn: (file: File) => {
            uploadAbortControllerRef.current?.abort();
            uploadAbortControllerRef.current = new AbortController();
            return service.uploadStorageFile({
                file,
                signal: uploadAbortControllerRef.current.signal,
                onTaskUpdate: setUploadTask
            });
        },
        onSuccess: async () => {
            await onUploaded();
            messageApi.success("文件已上传");
        },
        onError: (error) => {
            if (error instanceof Error && error.message === "Request was aborted") {
                return;
            }
            messageApi.error(error instanceof Error ? error.message : "上传失败");
        },
        onSettled: () => {
            uploadAbortControllerRef.current = null;
        }
    });

    const uploadSelectedFile = (event: ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        event.target.value = "";
        if (file) {
            setUploadTask(null);
            uploadMutation.mutate(file);
        }
    };

    const cancelUpload = () => {
        if (uploadTask?.canCancel) {
            uploadAbortControllerRef.current?.abort();
        }
    };

    const isUploadInProgress = Boolean(
        uploadMutation.isPending || (uploadTask && UPLOAD_ACTIVE_STAGES.includes(uploadTask.stage))
    );

    const uploadButton = (
        <>
            <input
                ref={uploadInputRef}
                aria-label="选择上传文件"
                className="storage-object-upload-input"
                type="file"
                accept={UPLOAD_ACCEPT}
                onChange={uploadSelectedFile}
            />
            <KuzhambuButton
                testId="storage-storage-object-storage-object-upload-button"
                icon={<UploadOutlined />}
                disabled={!canUpload || isUploadInProgress}
                loading={isUploadInProgress}
                onClick={() => uploadInputRef.current?.click()}
            >
                上传
            </KuzhambuButton>
        </>
    );

    if (!uploadTask) {
        return uploadButton;
    }

    return (
        <Popover
            content={<StorageUploadTaskCard task={uploadTask} onCancel={cancelUpload} />}
            open
            placement="bottomRight"
        >
            <span>{uploadButton}</span>
        </Popover>
    );
};
