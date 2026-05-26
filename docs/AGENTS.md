# Docs Agent

只给 AI / harness 读。目标：少读，快读，读对。

## Core Rules

- 只加载完成当前任务必需的文档。
- 先读治理文档，再读需求、接口或设计文档。
- `docs/` 不是默认全量输入目录。
- 工程级规则优先于模块文档。
- `50-prompts/` 与 `60-human/` 固定不进入默认读取路径。

## Mandatory Entry

实现、修改、评审代码前固定先读：

1. [`00-governance/ARCHITECTURE.md`](./00-governance/ARCHITECTURE.md)
2. [`00-governance/TODO-RULES.md`](./00-governance/TODO-RULES.md)

## Task Router

- 纯实现、修 bug、重构：
  读 `00-governance/ARCHITECTURE.md`，再按需读对应 `10-requirements/*-REQUIREMENTS.md`。
- 新增目录、改目录、判断文件归属：
  读 `00-governance/NAMING-AND-PLACEMENT-RULES.md`。
- 改文档、整理文档结构、创建 RUNBOOK：
  读 `00-governance/DOCUMENT-RULES.md`。
- TODO 协作、任务拆解、任务收口：
  读 `00-governance/TODO-RULES.md`。
- commit 粒度、提交信息、PR 收口和统一验证入口：
  读 `00-governance/TODO-RULES.md`
  再读 `40-readiness/PR-WORKFLOW.md`。
- 接口、协议、配置契约变化：
  按需读 `20-interfaces/`。
- UI 风格、前端体验、页面布局或交互原则变化：
  读 `00-governance/UI-RULES.md`。
- 专项方案、路线图、跨模块设计：
  按需读 `30-designs/`。
- 上线、发布、运维准备：
  按需读 `40-readiness/`。
- 用户明确要求使用 `60-human/redesign` 源材料时：
  先读 `60-human/redesign/INDEX.md`，再按索引读取对应文件；不要默认全量加载。

## Load Limits

- 单模块任务不默认加载其他模块文档。
- 跨模块任务只加载涉及的模块。
- 纯格式调整或无实现判断的机械修改不额外加载需求文档。
- 只有当前文档明确引用下一级文档时，才继续向下追。

## Directory Map

- `00-governance/`: 全局规则
- `10-requirements/`: 需求文档
- `20-interfaces/`: 接口、协议和契约
- `30-designs/`: 专项设计与 `RUNBOOK-*`
- `40-readiness/`: 上线、发布和运维准备
- `50-prompts/`: 人工触发的提示词模板
- `60-human/`: 人类阅读材料、项目叙事和导入源材料，默认不进入 AI 上下文
