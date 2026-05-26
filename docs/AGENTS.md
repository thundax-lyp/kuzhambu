# Docs Agent

AI 文档路由入口。目标：少读、读准、只读取当前工程文档。

## Rules

- 只读取当前任务必需文档；不要默认全量加载 `docs/`。
- 治理文档优先于需求、接口和设计文档。
- 工程级规则优先于模块文档。
- `50-prompts/` 只保存人工触发提示词，默认不读。
- 实现、修改或评审代码前，先读 `00-governance/ARCHITECTURE.md`；涉及提交、TODO 或收口时再读 `00-governance/TODO-RULES.md`。

## Router

- 代码实现、修复、重构：`00-governance/ARCHITECTURE.md`，再按需读对应 `10-requirements/*-REQUIREMENTS.md`。
- 目录、命名、文件归属：`00-governance/NAMING-AND-PLACEMENT-RULES.md`。
- 文档结构、文档写作、RUNBOOK：`00-governance/DOCUMENT-RULES.md`。
- TODO、提交、PR、统一验证：`00-governance/TODO-RULES.md`；PR 收口再读 `40-readiness/PR-WORKFLOW.md`。
- 接口、协议、配置契约：按需读 `20-interfaces/`。
- UI 风格、前端体验、页面布局：`00-governance/UI-RULES.md`。
- 专项设计、模块数据结构、接口设计：按需读 `30-designs/`。
- 发布、上线、运维准备：按需读 `40-readiness/`。

## Load Limits

- 单模块任务不读其他模块文档，除非当前文档明确引用。
- 跨模块任务只读涉及模块。
- 机械格式调整不额外加载需求文档。

## Directory Map

- `00-governance/`: 稳定治理规则
- `10-requirements/`: 独立需求文档
- `20-interfaces/`: 接口、协议和配置契约
- `30-designs/`: 模块设计、专项设计和 RUNBOOK
- `40-readiness/`: PR、发布和运维准备
- `50-prompts/`: 人工触发提示词
