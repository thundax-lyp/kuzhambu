# Architecture

## Purpose

本文档记录 kuzhambu 的全局架构入口。本文只固定仓库级工程边界、技术分区和文档路由，不替代 Java servers、前端应用或 Python workers 的专项规则。

## Scope

- 适用于判断改动属于文档、Java servers、前端应用、Python workers 还是部署支撑。
- 适用于新增顶层目录、工程组或跨工程协作规则。
- 不记录 Java 代码分层细节；Java servers 规则见 [`SERVERS-ARCHITECTURE.md`](./SERVERS-ARCHITECTURE.md)。

## Project Layout

- 根目录保留项目说明、许可证、贡献入口和治理入口。
- `docs/` 承载文档治理、需求、接口、设计和上线准备材料。
- `kuzhambu-servers/` 承载 Java 后端工程组。
- `kuzhambu-apps/` 承载前端应用工程组。
- `kuzhambu-workers/` 承载 Python worker 能力支撑工程。

## Technology Baseline

- Java servers 使用 Java 17、Spring Boot 3.x 和 Maven 多模块。
- 前端应用使用独立 `package.json` 管理。
- 后台前端 `kuzhambu-apps/admin-web/` 使用 React、Vite 和 Ant Design。
- 前台前端 `kuzhambu-apps/portal-web/` 使用 React、Vite、shadcn/ui、Tailwind CSS、Zustand、TanStack Query、TypeScript、ESLint 和 Prettier。
- Python workers 使用 Python 3.10，本地虚拟环境固定为 `kuzhambu-workers/.venv/`。

## Global Rules

- 新增顶层工程组必须先明确职责、入口、构建命令和文档归属。
- 稳定治理规则放在 `docs/00-governance/`。
- 需求文档放在 `docs/10-requirements/`。
- 接口、协议和配置契约放在 `docs/20-interfaces/`。
- 专项设计和临时 RUNBOOK 放在 `docs/30-designs/`。
- 上线、发布和运维准备放在 `docs/40-readiness/`。
- `docs/50-prompts/` 只保存人工触发提示词，不作为默认 AI 上下文。
- `docs/60-human/` 只保存人类阅读叙事，不作为默认实现依据。
- 行为、配置或开发流程发生变化时，同步更新相关文档。

## Specialized Governance

- Java servers 架构和模块边界：[`SERVERS-ARCHITECTURE.md`](./SERVERS-ARCHITECTURE.md)。
- Java servers 架构细则、目录、命名和文件归属：[`SERVERS-ARCHITECTURE-RULES.md`](./SERVERS-ARCHITECTURE-RULES.md)。
- Java servers 数据库表、字段、索引和缓存真相源：[`SERVERS-DATABASE-RULES.md`](./SERVERS-DATABASE-RULES.md)。
- Java servers 统一业务标识、强类型 ID 和 token 边界：[`SERVERS-UNIFIED-ID-DESIGN.md`](./SERVERS-UNIFIED-ID-DESIGN.md)。
- UI 风格和交互原则：[`UI-RULES.md`](./UI-RULES.md)。
- TODO 协作、删除、测试检查和提交收口：[`TODO-RULES.md`](./TODO-RULES.md)。
