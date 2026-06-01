import { Space, Tag, Typography } from "antd";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import type { SubmissionStatus } from "../submission-service";
import type { SubmissionRecord } from "../submission-types";

const { Text, Paragraph } = Typography;

interface SubmissionDetailProps {
    submission?: SubmissionRecord | null;
    statusLabels: Record<SubmissionStatus, string>;
    onClose: () => void;
}

const readStatusLabel = (
    statusLabels: Record<SubmissionStatus, string>,
    status?: string | null
) => {
    return status && status in statusLabels
        ? statusLabels[status as SubmissionStatus]
        : status || "未知";
};

export const SubmissionDetail = ({ submission, statusLabels, onClose }: SubmissionDetailProps) => (
    <SandwishDrawer title="提交详情" open={Boolean(submission)} size="middle" onClose={onClose}>
        {submission ? (
            <div className="submission-detail-content">
                <Text type="secondary">标题</Text>
                <Paragraph strong>{submission.title}</Paragraph>
                <Text type="secondary">正文</Text>
                <Paragraph>{submission.content}</Paragraph>
                <Text type="secondary">状态</Text>
                <Paragraph>{readStatusLabel(statusLabels, submission.status)}</Paragraph>
                <Text type="secondary">图片对象</Text>
                {submission.imageObjectIds?.length ? (
                    <Space wrap>
                        {submission.imageObjectIds.map((id) => (
                            <Tag key={id}>{id}</Tag>
                        ))}
                    </Space>
                ) : (
                    <Paragraph type="secondary">未上传</Paragraph>
                )}
            </div>
        ) : null}
    </SandwishDrawer>
);
