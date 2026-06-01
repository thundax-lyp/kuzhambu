from dataclasses import dataclass
from enum import Enum

from kuzhambu_workers.schemas.ai import AiCapability, ResultFormat


class AiUsecaseDomain(str, Enum):
    CLASSICS = "classics"
    DISCOVERY = "discovery"
    KNOWLEDGE = "knowledge"
    PLATFORM = "platform"


@dataclass(frozen=True)
class AiUsecase:
    path: str
    domain: AiUsecaseDomain
    operation: str
    capability: AiCapability
    stream: bool
    output: ResultFormat
    summary: str
    description: str


USECASES: tuple[AiUsecase, ...] = (
    AiUsecase(
        "/internal/ai/classics/sancai/translate",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_SANCAI_TRANSLATE",
        AiCapability.TRANSLATE,
        False,
        ResultFormat.TEXT,
        "Classics Sancai translate",
        "三才图会古文翻译 usecase。输入必须包含条目标题、原文、门类、卷和上下文快照。",
    ),
    AiUsecase(
        "/internal/ai/classics/sancai/translate-batch-item",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM",
        AiCapability.TRANSLATE,
        False,
        ResultFormat.TEXT,
        "Classics Sancai translate batch item",
        "三才图会批量古文翻译单元 usecase。每次请求只处理一个条目快照。",
    ),
    AiUsecase(
        "/internal/ai/classics/sancai/summary",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_SANCAI_SUMMARY",
        AiCapability.SUMMARY,
        False,
        ResultFormat.TEXT,
        "Classics Sancai summary",
        "三才图会摘要生成 usecase。输入必须包含条目原文、译文、标签和已有摘要。",
    ),
    AiUsecase(
        "/internal/ai/classics/sancai/tags",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_SANCAI_TAGS",
        AiCapability.TAGS,
        False,
        ResultFormat.STRUCTURED,
        "Classics Sancai tags",
        "三才图会标签提取 usecase。输出为可进入候选区的结构化标签。",
    ),
    AiUsecase(
        "/internal/ai/classics/sancai/qa",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_SANCAI_QA",
        AiCapability.QA,
        False,
        ResultFormat.STRUCTURED,
        "Classics Sancai QA",
        "三才图会问答对生成 usecase。输出为可编辑的结构化问答对。",
    ),
    AiUsecase(
        "/internal/ai/classics/sancai/image-analysis",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_SANCAI_IMAGE_ANALYSIS",
        AiCapability.IMAGE_ANALYSIS,
        True,
        ResultFormat.MARKDOWN,
        "Classics Sancai image analysis",
        "三才图会图片理解 usecase。输入必须包含图片内容或临时可读资源和条目上下文。",
    ),
    AiUsecase(
        "/internal/ai/classics/sancai/fusion",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_SANCAI_FUSION",
        AiCapability.FUSION,
        False,
        ResultFormat.MARKDOWN,
        "Classics Sancai fusion",
        "三才图会视觉信息融合 usecase。输入必须包含原文、译文、图片理解结果和融合权重。",
    ),
    AiUsecase(
        "/internal/ai/classics/sancai/visual-description",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_SANCAI_VISUAL_DESCRIPTION",
        AiCapability.VISUAL,
        False,
        ResultFormat.TEXT,
        "Classics Sancai visual description",
        "三才图会视觉描述生成 usecase。输入必须包含融合说明、条目上下文和风格参数。",
    ),
    AiUsecase(
        "/internal/ai/classics/sancai/image-gen",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_SANCAI_IMAGE_GEN",
        AiCapability.IMAGE_GEN,
        True,
        ResultFormat.ARTIFACT,
        "Classics Sancai image generation",
        "三才图会图片生成 usecase。产物必须通过当前响应或 SSE artifact 返回。",
    ),
    AiUsecase(
        "/internal/ai/classics/sancai/split",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_SANCAI_SPLIT",
        AiCapability.SPLIT,
        False,
        ResultFormat.STRUCTURED,
        "Classics Sancai split",
        "三才图会条目拆分 usecase。输出为可预览和人工调整的结构化子条目。",
    ),
    AiUsecase(
        "/internal/ai/classics/wangqi/summary",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_WANGQI_SUMMARY",
        AiCapability.SUMMARY,
        False,
        ResultFormat.TEXT,
        "Classics Wangqi summary",
        "王圻文档摘要生成 usecase。输入必须包含文档标题、正文、时间线和已有摘要。",
    ),
    AiUsecase(
        "/internal/ai/classics/wangqi/tags",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_WANGQI_TAGS",
        AiCapability.TAGS,
        False,
        ResultFormat.STRUCTURED,
        "Classics Wangqi tags",
        "王圻文档标签提取 usecase。输出为可进入候选区的结构化标签。",
    ),
    AiUsecase(
        "/internal/ai/classics/wangqi/qa",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_WANGQI_QA",
        AiCapability.QA,
        False,
        ResultFormat.STRUCTURED,
        "Classics Wangqi QA",
        "王圻文档问答对生成 usecase。输出为可编辑的结构化问答对。",
    ),
    AiUsecase(
        "/internal/ai/classics/ming-customs/summary",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_MING_CUSTOMS_SUMMARY",
        AiCapability.SUMMARY,
        False,
        ResultFormat.TEXT,
        "Classics Ming customs summary",
        "明代习俗摘要生成 usecase。输入必须包含标题、概述、正文和原文摘录。",
    ),
    AiUsecase(
        "/internal/ai/classics/ming-customs/tags",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_MING_CUSTOMS_TAGS",
        AiCapability.TAGS,
        False,
        ResultFormat.STRUCTURED,
        "Classics Ming customs tags",
        "明代习俗标签提取 usecase。输出为可进入候选区的结构化标签。",
    ),
    AiUsecase(
        "/internal/ai/classics/ming-customs/qa",
        AiUsecaseDomain.CLASSICS,
        "CLASSICS_MING_CUSTOMS_QA",
        AiCapability.QA,
        False,
        ResultFormat.STRUCTURED,
        "Classics Ming customs QA",
        "明代习俗问答对生成 usecase。输出为可编辑的结构化问答对。",
    ),
    AiUsecase(
        "/internal/ai/discovery/query-understanding",
        AiUsecaseDomain.DISCOVERY,
        "DISCOVERY_QUERY_UNDERSTANDING",
        AiCapability.QUERY_UNDERSTANDING,
        False,
        ResultFormat.STRUCTURED,
        "Discovery query understanding",
        "Discovery 查询理解 usecase。输入必须包含查询词、可见范围、筛选条件和搜索上下文。",
    ),
    AiUsecase(
        "/internal/ai/discovery/query-rewrite",
        AiUsecaseDomain.DISCOVERY,
        "DISCOVERY_QUERY_REWRITE",
        AiCapability.QUERY_UNDERSTANDING,
        False,
        ResultFormat.STRUCTURED,
        "Discovery query rewrite",
        "Discovery 查询改写 usecase。输入必须包含同义词扩展和实体识别候选。",
    ),
    AiUsecase(
        "/internal/ai/discovery/answer-generation",
        AiUsecaseDomain.DISCOVERY,
        "DISCOVERY_ANSWER_GENERATION",
        AiCapability.ANSWER_GENERATION,
        False,
        ResultFormat.TEXT,
        "Discovery answer generation",
        "Discovery 回答生成 usecase。输入必须包含已完成权限过滤的来源和上下文片段。",
    ),
    AiUsecase(
        "/internal/ai/discovery/answer-generation/stream",
        AiUsecaseDomain.DISCOVERY,
        "DISCOVERY_ANSWER_GENERATION_STREAM",
        AiCapability.ANSWER_GENERATION,
        True,
        ResultFormat.TEXT,
        "Discovery answer generation stream",
        "Discovery 流式回答生成 usecase。stream 片段只用于展示，最终结果以 completed 为准。",
    ),
    AiUsecase(
        "/internal/ai/knowledge/relation-extraction",
        AiUsecaseDomain.KNOWLEDGE,
        "KNOWLEDGE_RELATION_EXTRACTION",
        AiCapability.RELATION_EXTRACTION,
        False,
        ResultFormat.STRUCTURED,
        "Knowledge relation extraction",
        "Knowledge 实体关系候选抽取 usecase。输出只作为候选结构。",
    ),
    AiUsecase(
        "/internal/ai/knowledge/graph-extraction",
        AiUsecaseDomain.KNOWLEDGE,
        "KNOWLEDGE_GRAPH_EXTRACTION",
        AiCapability.KNOWLEDGE_GRAPH,
        False,
        ResultFormat.STRUCTURED,
        "Knowledge graph extraction",
        "Knowledge 知识图谱候选抽取 usecase。输出实体、关系和来源片段。",
    ),
    AiUsecase(
        "/internal/ai/knowledge/lineage-extraction",
        AiUsecaseDomain.KNOWLEDGE,
        "KNOWLEDGE_LINEAGE_EXTRACTION",
        AiCapability.LINEAGE_EXTRACTION,
        False,
        ResultFormat.STRUCTURED,
        "Knowledge lineage extraction",
        "Knowledge 世系图候选抽取 usecase。输出节点、关系和来源片段。",
    ),
    AiUsecase(
        "/internal/ai/knowledge/tag-extraction",
        AiUsecaseDomain.KNOWLEDGE,
        "KNOWLEDGE_TAG_EXTRACTION",
        AiCapability.TAGS,
        False,
        ResultFormat.STRUCTURED,
        "Knowledge tag extraction",
        "Knowledge 标签候选抽取 usecase。输出必须进入标签治理流程。",
    ),
    AiUsecase(
        "/internal/ai/platform/prompt-suggestion",
        AiUsecaseDomain.PLATFORM,
        "PLATFORM_PROMPT_SUGGESTION",
        AiCapability.PROMPT_SUGGESTION,
        False,
        ResultFormat.STRUCTURED,
        "Platform prompt suggestion",
        "AI 域提示词优化建议 usecase。建议必须由用户确认后才可应用。",
    ),
    AiUsecase(
        "/internal/ai/platform/version-summary",
        AiUsecaseDomain.PLATFORM,
        "PLATFORM_VERSION_SUMMARY",
        AiCapability.VERSION_SUMMARY,
        False,
        ResultFormat.TEXT,
        "Platform version summary",
        "AI 域版本摘要 usecase。输入必须包含变更前后内容和操作上下文。",
    ),
)

USECASES_BY_PATH = {usecase.path: usecase for usecase in USECASES}
USECASE_PATHS = tuple(USECASES_BY_PATH)


def get_usecase(path: str) -> AiUsecase | None:
    return USECASES_BY_PATH.get(path)


def require_usecase(path: str) -> AiUsecase:
    usecase = get_usecase(path)
    if usecase is None:
        raise KeyError(path)
    return usecase
