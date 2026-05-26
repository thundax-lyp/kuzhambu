# Document Rules

## Purpose

本文档定义 kuzhambu 内需求文档、设计文档和 AI 输入文档的统一写作要求，让文档适合 AI 读取、工程实现和持续维护。

## File Naming

- 文档文件名使用大写英文，可用 `-` 连接单词。
- 文件名不得使用中文或空格。
- 治理文档固定放在 `docs/00-governance/`。
- `RUNBOOK` 文档固定放在 `docs/30-designs/`，命名为 `RUNBOOK-XXX.md`。
- `HOW-TO` 文档仅在用户明确要求沉淀稳定流程时新增，命名为 `HOW-TO-XXX.md`。

## Governance Entry Map

- 架构和模块边界：[`ARCHITECTURE.md`](./ARCHITECTURE.md)
- 命名、目录和文件归属：[`NAMING-AND-PLACEMENT-RULES.md`](./NAMING-AND-PLACEMENT-RULES.md)
- UI 风格和交互原则：[`UI-RULES.md`](./UI-RULES.md)
- TODO 协作、删除、测试检查和提交收口：[`TODO-RULES.md`](./TODO-RULES.md)
- 文档写作、路由和提交口径：本文档

## Language Rules

- 文档说明内容使用中文。
- 代码定义、模块名、类名、接口名、字段名、工具名和协议名保留英文原文。
- 不为了“纯中文”翻译代码概念，也不为了“纯英文”改写业务说明。

## Content Principles

- 文档必须清晰、明确、可执行。
- `docs/10-requirements/` 只写已确认交付范围内的最终需求。
- 稳定规则进入 `00-governance/`；任务讨论进入 `TODO.md` 或 `RUNBOOK-*`。
- 同一规则不得在多处重复且表述不一致。
- 治理文档只沉淀稳定规则，不记录任务执行过程。
- `50-prompts/` 只保存人工触发的提示词模板。

## RUNBOOK Boundary

`RUNBOOK` 是复杂任务的执行手册，适用于跨模块清理、迁移、删除或重构。任务关闭时，应清理 RUNBOOK 及残留引用。
