import { ArrowLeftOutlined, ArrowRightOutlined } from "@ant-design/icons";
import { Col, Input, Row, Tag, Typography } from "antd";
import type { FormInstance } from "antd";
import type { ReactNode } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuSelect,
    KuzhambuSpace,
    KuzhambuSteps
} from "@/components";
import type {
    SancaiEntryImageRecord,
    SancaiVisualAssetRecord
} from "@/pages/classics/sancai-visual/sancai-visual-types";
import {
    readImageTitle,
    readVisualAssetStatusLabel,
    readVisualAssetStatusTagColor,
    readVisualAssetTitle
} from "./sancai-entry-visual-formatters";

const { Text } = Typography;

export type SancaiVisualWorkflowStepKey =
    "source" | "image_analysis" | "fusion" | "visual" | "image_gen" | "save";

export interface SancaiVisualWorkflowStep {
    blockedReason: string | null;
    buttonText: string;
    icon: ReactNode;
    key: SancaiVisualWorkflowStepKey;
    loading: boolean;
    onClick: () => Promise<unknown> | unknown;
    status: {
        label: string;
        stepStatus: "error" | "finish" | "process" | "wait";
    };
    summary: string;
    testId: string;
    title: string;
}

interface SancaiVisualWorkflowCardProps {
    canGoToNextWorkflowStep: boolean;
    canGoToPreviousWorkflowStep: boolean;
    currentWorkflowStep: SancaiVisualWorkflowStep;
    currentWorkflowStepIndex: number;
    defaultSourceImage?: SancaiEntryImageRecord;
    entryImages: SancaiEntryImageRecord[];
    form: FormInstance<SancaiVisualAssetRecord>;
    isUpdatingVisualAsset: boolean;
    onGoToNextWorkflowStep: () => void;
    onGoToPreviousWorkflowStep: () => void;
    onGoToWorkflowStep: (stepIndex: number) => void;
    onSelectVisualSourceImageBySelectValue: (storageObjectId: string) => void;
    selectedVisualAsset: SancaiVisualAssetRecord;
    visualWorkflowSteps: SancaiVisualWorkflowStep[];
}

export const SancaiVisualWorkflowCard = ({
    canGoToNextWorkflowStep,
    canGoToPreviousWorkflowStep,
    currentWorkflowStep,
    currentWorkflowStepIndex,
    defaultSourceImage,
    entryImages,
    form,
    isUpdatingVisualAsset,
    onGoToNextWorkflowStep,
    onGoToPreviousWorkflowStep,
    onGoToWorkflowStep,
    onSelectVisualSourceImageBySelectValue,
    selectedVisualAsset,
    visualWorkflowSteps
}: SancaiVisualWorkflowCardProps) => {
    return (
        <KuzhambuForm
            form={form}
            className="sancai-entry-edit-drawer-form"
            colon={false}
            component="div"
        >
            <KuzhambuCard size="small" aria-label="图文生图工作流">
                <Row gutter={[12, 8]} align="top">
                    <Col xs={24} lg={9} xl={8}>
                        <KuzhambuSpace
                            className="sancai-visual-stepper-panel"
                            orientation="vertical"
                            size={10}
                        >
                            <KuzhambuSteps
                                testId="classics-sancai-visual-workflow-step"
                                className="sancai-visual-workflow-stepper"
                                current={currentWorkflowStepIndex}
                                orientation="vertical"
                                responsive
                                onChange={onGoToWorkflowStep}
                                items={visualWorkflowSteps.map((step) => ({
                                    status: step.status.stepStatus,
                                    title: step.title,
                                    content: step.status.label
                                }))}
                            />
                            <KuzhambuSpace className="sancai-visual-stepper-actions">
                                <KuzhambuButton
                                    testId="classics-sancai-visual-workflow-prev-button"
                                    ariaLabel="上一步"
                                    icon={<ArrowLeftOutlined />}
                                    disabled={!canGoToPreviousWorkflowStep}
                                    onClick={onGoToPreviousWorkflowStep}
                                />
                                <KuzhambuButton
                                    testId="classics-sancai-visual-workflow-next-button"
                                    ariaLabel="下一步"
                                    icon={<ArrowRightOutlined />}
                                    type="primary"
                                    disabled={!canGoToNextWorkflowStep}
                                    loading={
                                        currentWorkflowStep.key === "source" &&
                                        isUpdatingVisualAsset
                                    }
                                    onClick={onGoToNextWorkflowStep}
                                />
                            </KuzhambuSpace>
                        </KuzhambuSpace>
                    </Col>
                    <Col xs={24} lg={15} xl={16}>
                        <KuzhambuSpace orientation="vertical" size={8} style={{ width: "100%" }}>
                            <KuzhambuAlert
                                className="sancai-visual-workflow-alert"
                                showIcon
                                title={currentWorkflowStep.title}
                                description={
                                    currentWorkflowStep.blockedReason ?? currentWorkflowStep.summary
                                }
                                type={
                                    currentWorkflowStep.blockedReason
                                        ? "warning"
                                        : currentWorkflowStep.status.stepStatus === "finish"
                                          ? "success"
                                          : "info"
                                }
                            />
                            <section className="sancai-visual-workflow-step-form">
                                {currentWorkflowStep.key === "source" ? (
                                    <section className="sancai-visual-asset-picker">
                                        <KuzhambuFormItem
                                            name="sourceImageStorageObjectId"
                                            layoutSize="large"
                                        >
                                            <KuzhambuSelect
                                                aria-label="三才图会视觉处理来源图片"
                                                disabled={!defaultSourceImage}
                                                placeholder="选择来源图片"
                                                options={entryImages.map((image) => ({
                                                    disabled: !image.storageObjectId,
                                                    label: readImageTitle(image),
                                                    value:
                                                        image.storageObjectId ?? `image:${image.id}`
                                                }))}
                                                onChange={(value) =>
                                                    onSelectVisualSourceImageBySelectValue(value)
                                                }
                                            />
                                        </KuzhambuFormItem>
                                    </section>
                                ) : null}
                                {currentWorkflowStep.key === "image_analysis" ? (
                                    <KuzhambuFormItem
                                        name="imageAnalysisMarkdown"
                                        layoutSize="large"
                                        className="sancai-entry-edit-drawer-form-item-top"
                                    >
                                        <Input.TextArea
                                            aria-label="三才图会视觉处理图片理解"
                                            autoSize={resolveTextAreaAutoSize({
                                                minRows: 2,
                                                maxRows: 4
                                            })}
                                        />
                                    </KuzhambuFormItem>
                                ) : null}
                                {currentWorkflowStep.key === "fusion" ? (
                                    <>
                                        <Row gutter={[10, 10]}>
                                            <Col xs={24} sm={12}>
                                                <KuzhambuFormItem
                                                    name="textWeight"
                                                    label="文本权重"
                                                    layoutSize="middle"
                                                >
                                                    <Input aria-label="三才图会视觉处理文本权重" />
                                                </KuzhambuFormItem>
                                            </Col>
                                            <Col xs={24} sm={12}>
                                                <KuzhambuFormItem
                                                    name="imageWeight"
                                                    label="图片权重"
                                                    layoutSize="middle"
                                                >
                                                    <Input aria-label="三才图会视觉处理图片权重" />
                                                </KuzhambuFormItem>
                                            </Col>
                                        </Row>
                                        <KuzhambuFormItem
                                            name="fusionDescription"
                                            label="图文融合"
                                            layoutSize="large"
                                            className="sancai-entry-edit-drawer-form-item-top"
                                        >
                                            <Input.TextArea
                                                aria-label="三才图会视觉处理融合描述"
                                                autoSize={resolveTextAreaAutoSize({
                                                    minRows: 2,
                                                    maxRows: 4
                                                })}
                                            />
                                        </KuzhambuFormItem>
                                    </>
                                ) : null}
                                {currentWorkflowStep.key === "visual" ? (
                                    <KuzhambuFormItem
                                        name="visualDescription"
                                        label="视觉描述"
                                        layoutSize="large"
                                        className="sancai-entry-edit-drawer-form-item-top"
                                    >
                                        <Input.TextArea
                                            aria-label="三才图会视觉处理视觉描述"
                                            autoSize={resolveTextAreaAutoSize({
                                                minRows: 2,
                                                maxRows: 4
                                            })}
                                        />
                                    </KuzhambuFormItem>
                                ) : null}
                                {currentWorkflowStep.key === "image_gen" ? (
                                    <KuzhambuFormItem
                                        name="generationParamsJson"
                                        label="生成参数"
                                        layoutSize="large"
                                        className="sancai-entry-edit-drawer-form-item-top"
                                    >
                                        <Input.TextArea
                                            aria-label="三才图会视觉处理生成参数"
                                            autoSize={resolveTextAreaAutoSize({
                                                minRows: 2,
                                                maxRows: 4
                                            })}
                                        />
                                    </KuzhambuFormItem>
                                ) : null}
                                {currentWorkflowStep.key === "save" ? (
                                    <KuzhambuSpace
                                        className="sancai-visual-save-review"
                                        orientation="vertical"
                                        size={8}
                                    >
                                        <KuzhambuSpace wrap>
                                            <Tag
                                                color={readVisualAssetStatusTagColor(
                                                    selectedVisualAsset.status
                                                )}
                                            >
                                                {readVisualAssetStatusLabel(
                                                    selectedVisualAsset.status
                                                )}
                                            </Tag>
                                            <Text type="secondary">
                                                {readVisualAssetTitle(selectedVisualAsset)}
                                            </Text>
                                        </KuzhambuSpace>
                                        <Text type="secondary">
                                            保存当前来源图片、图片理解、融合描述、视觉描述、生成参数和生成图状态。
                                        </Text>
                                    </KuzhambuSpace>
                                ) : null}
                                {currentWorkflowStep.key !== "source" ? (
                                    <KuzhambuSpace className="sancai-visual-workflow-card-actions">
                                        <KuzhambuButton
                                            testId={currentWorkflowStep.testId}
                                            icon={currentWorkflowStep.icon}
                                            type="primary"
                                            loading={currentWorkflowStep.loading}
                                            disabled={Boolean(currentWorkflowStep.blockedReason)}
                                            onClick={currentWorkflowStep.onClick}
                                        >
                                            {currentWorkflowStep.buttonText}
                                        </KuzhambuButton>
                                    </KuzhambuSpace>
                                ) : null}
                            </section>
                        </KuzhambuSpace>
                    </Col>
                </Row>
            </KuzhambuCard>
        </KuzhambuForm>
    );
};
