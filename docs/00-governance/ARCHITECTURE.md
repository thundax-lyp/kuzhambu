# Architecture

## Purpose

本文档记录 kuzhambu 的稳定架构入口。本文只固定已经确认的工程边界、技术基线和模块方向，避免过早引入未确认的服务治理或未来设计。

## Scope

- 适用于新增代码、目录、依赖和构建系统时的架构判断。
- 不替代具体需求、接口契约或专项设计。
- 不记录一次性迁移步骤或完成清单。

## Current State

- 根目录保留项目说明、许可证、贡献入口和治理入口。
- `docs/` 承载文档治理、需求、接口、设计和上线准备材料。
- 后端采用 Java 17、Spring Boot 3.x 和 Maven 多模块。
- 前端应用放在 `kuzhambu-apps/`，后台和前台分离。
- Python 能力支撑代码放在 `kuzhambu-workers/`。

## Technology Stack

- 后端统一使用 Java 17。
- 后端构建系统使用 Maven 多模块。
- 后端采用 Spring Boot 3.x，不采用 Spring Cloud 微服务架构。
- 前端应用使用独立 `package.json` 管理，统一放在 `kuzhambu-apps/`。
- Python 代码用于能力支撑、生成工具、离线处理和 AI 辅助流程，统一放在 `kuzhambu-workers/`。

## Module Layout

- `kuzhambu-common/`：通用基础类型、工具和跨模块约定。
- `kuzhambu-biz/`：业务域规则和业务服务，包含 `auth`、`storage`、`audit` 等基础业务域。
- `kuzhambu-infra/`：数据库、对象存储、搜索引擎、AI 调用、调度等技术适配。
- `kuzhambu-server/`：Spring Boot 启动、后台 API、前台 API 和安全配置。
- `kuzhambu-apps/admin-web/`：后台管理前端。
- `kuzhambu-apps/public-web/`：前台 portal 和分享访问前端。
- `kuzhambu-workers/`：Python worker 能力支撑工程。

## Architecture Rules

- 先形成最小可运行模块，再沉淀更细分层。
- 新增目录必须有明确职责和归属，避免空壳结构。
- 新增依赖必须服务于当前任务，不为未来假设预留。
- 项目不设置 `platform` 目录或 `platform` Maven 模块。
- `auth`、`storage`、`audit` 是独立基础业务域，归入 `kuzhambu-biz/`。
- `kuzhambu-infra/` 只承载技术适配，不承载业务语义。
- 后端采用模块化单体，模块之间通过 Java 服务协作，不通过内部 HTTP 调用模拟微服务。
- 后台 API 使用 `/api/admin/**`，前台 API 使用 `/api/public/**`。
- `public-api` 表示前台访问边界，不等同于无认证；是否需要登录由资源访问策略决定。
- 行为、配置或开发流程发生变化时，同步更新相关文档。
- 具体模块建立后，模块内规则优先记录在模块自己的 `AGENTS.md` 或治理文档中。

## Open Items

无
