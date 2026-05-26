# Human Materials

## Purpose

本目录保存人类阅读材料、项目叙事和临时参考材料。这里的内容不作为 AI 默认执行规则。

## Current Materials

- `redesign/`: 临时参考材料目录。该目录保留原始文件名和原始结构，后续可整体删除。

## Reading Rules

- 只有用户明确要求使用本目录材料时才读取。
- 读取 `redesign/` 时，先读 `redesign/INDEX.md`，再按索引选择相关文档。
- 不要默认全量加载 `redesign/`。
- `redesign/Question.md` 可用于后续审查参考，不直接等同于当前 TODO。

## Promotion Rules

从本目录抽取正式工程材料时，按目标用途迁移：

- 稳定治理规则 -> `docs/00-governance/`
- 产品或模块需求 -> `docs/10-requirements/`
- API、协议和配置契约 -> `docs/20-interfaces/`
- 专项方案或一次性执行计划 -> `docs/30-designs/`
- 上线、发布和验收准备 -> `docs/40-readiness/`

抽取后的正式文档必须遵守 `docs/00-governance/DOCUMENT-RULES.md`。
