import pytest

from kuzhambu_workers.ai.usecase_registry import (
    USECASE_PATHS,
    USECASES,
    AiUsecaseDomain,
    get_usecase,
    require_usecase,
)
from kuzhambu_workers.schemas.ai import AiCapability, ResultFormat

EXPECTED_PATHS = {
    "/internal/ai/classics/sancai/translate",
    "/internal/ai/classics/sancai/translate-batch-item",
    "/internal/ai/classics/sancai/summary",
    "/internal/ai/classics/sancai/tags",
    "/internal/ai/classics/sancai/qa",
    "/internal/ai/classics/sancai/image-analysis",
    "/internal/ai/classics/sancai/fusion",
    "/internal/ai/classics/sancai/visual-description",
    "/internal/ai/classics/sancai/image-gen",
    "/internal/ai/classics/sancai/split",
    "/internal/ai/classics/wangqi/summary",
    "/internal/ai/classics/wangqi/tags",
    "/internal/ai/classics/wangqi/qa",
    "/internal/ai/classics/ming-customs/summary",
    "/internal/ai/classics/ming-customs/tags",
    "/internal/ai/classics/ming-customs/qa",
    "/internal/ai/discovery/query-understanding",
    "/internal/ai/discovery/query-rewrite",
    "/internal/ai/discovery/answer-generation",
    "/internal/ai/discovery/answer-generation/stream",
    "/internal/ai/knowledge/relation-extraction",
    "/internal/ai/knowledge/graph-extraction",
    "/internal/ai/knowledge/lineage-extraction",
    "/internal/ai/knowledge/tag-extraction",
    "/internal/ai/platform/prompt-suggestion",
    "/internal/ai/platform/version-summary",
}


def test_usecase_registry_covers_interface_matrix() -> None:
    assert set(USECASE_PATHS) == EXPECTED_PATHS
    assert len(USECASES) == len(EXPECTED_PATHS)


def test_usecase_registry_exposes_metadata_by_path() -> None:
    usecase = require_usecase("/internal/ai/classics/sancai/image-analysis")

    assert usecase.domain == AiUsecaseDomain.CLASSICS
    assert usecase.capability == AiCapability.IMAGE_ANALYSIS
    assert usecase.stream is True
    assert usecase.output == ResultFormat.MARKDOWN
    assert "图片理解" in usecase.description


@pytest.mark.parametrize(
    ("path", "capability", "stream", "output"),
    [
        (
            "/internal/ai/discovery/answer-generation/stream",
            AiCapability.ANSWER_GENERATION,
            True,
            ResultFormat.TEXT,
        ),
        (
            "/internal/ai/knowledge/lineage-extraction",
            AiCapability.LINEAGE_EXTRACTION,
            False,
            ResultFormat.STRUCTURED,
        ),
        (
            "/internal/ai/platform/prompt-suggestion",
            AiCapability.PROMPT_SUGGESTION,
            False,
            ResultFormat.STRUCTURED,
        ),
    ],
)
def test_usecase_registry_has_expected_capability_stream_and_output(
    path: str,
    capability: AiCapability,
    stream: bool,
    output: ResultFormat,
) -> None:
    usecase = require_usecase(path)

    assert usecase.capability == capability
    assert usecase.stream is stream
    assert usecase.output == output


def test_usecase_registry_returns_none_for_unknown_path() -> None:
    assert get_usecase("/internal/ai/unknown") is None


def test_usecase_registry_requires_known_path() -> None:
    with pytest.raises(KeyError):
        require_usecase("/internal/ai/unknown")
