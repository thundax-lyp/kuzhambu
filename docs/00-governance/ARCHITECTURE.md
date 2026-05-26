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
- 后端工程组放在 `kuzhambu-servers/`，采用 Java 17、Spring Boot 3.x 和 Maven 多模块。
- 前端应用放在 `kuzhambu-apps/`，后台和前台分离。
- Python 能力支撑代码放在 `kuzhambu-workers/`。

## Technology Stack

- 后端统一使用 Java 17。
- 后端构建系统使用 `kuzhambu-servers/pom.xml` 管理的 Maven 多模块。
- 后端采用 Spring Boot 3.x，不采用 Spring Cloud 微服务架构。
- 前端应用使用独立 `package.json` 管理，统一放在 `kuzhambu-apps/`。
- 后台前端 `kuzhambu-apps/admin-web/` 使用 React、Vite 和 Ant Design，参考 `sandwich` 的工程经验但不沿用其 UI 风格。
- 前台前端 `kuzhambu-apps/portal-web/` 使用 React、Vite、shadcn/ui、Tailwind CSS、Zustand、TanStack Query、TypeScript、ESLint 和 Prettier。
- Python 代码用于能力支撑、生成工具、离线处理和 AI 辅助流程，统一放在 `kuzhambu-workers/`，Python 版本固定为 3.10。

## Module Layout

- `kuzhambu-servers/common/`：后端通用能力模块组。
- `kuzhambu-servers/common/kuzhambu-common-core/`：通用基础类型、异常、值对象和基础工具。
- `kuzhambu-servers/common/kuzhambu-common-web/`：Web 请求响应、分页、异常处理和请求上下文。
- `kuzhambu-servers/common/kuzhambu-common-security/`：认证上下文、权限注解和安全基础工具。
- `kuzhambu-servers/common/kuzhambu-common-openapi/`：Springdoc OpenAPI 在线文档基础配置。
- `kuzhambu-servers/common/kuzhambu-common-mybatis/`：MyBatis 相关通用配置、类型处理和持久化基础能力。
- `kuzhambu-servers/common/kuzhambu-common-mysql/`：MySQL 驱动和 MySQL 连接基础能力。
- `kuzhambu-servers/common/kuzhambu-common-redis/`：Redis 连接和 Redis 基础能力。
- `kuzhambu-servers/common/kuzhambu-common-cache/`：缓存基础能力。
- `kuzhambu-servers/common/kuzhambu-common-mq/`：RocketMQ 消息基础能力。
- `kuzhambu-servers/common/kuzhambu-common-elasticsearch/`：Elasticsearch 客户端和索引基础能力。
- `kuzhambu-servers/common/kuzhambu-common-oss/`：对象存储通用客户端抽象和基础适配。
- `kuzhambu-servers/common/kuzhambu-common-test/`：测试基建、架构测试和集成测试辅助。
- `kuzhambu-servers/biz/`：业务域规则和业务服务，包含 `core`、`auth`、`storage`、`audit`、`knowledge-graph` 等业务域。
- `kuzhambu-servers/infra/`：各业务域的数据访问、外部调用和技术落地。
- `kuzhambu-servers/interfaces/kuzhambu-admin-api/`：后台接口入口服务。
- `kuzhambu-servers/interfaces/kuzhambu-portal-api/`：前台接口入口服务。
- `kuzhambu-apps/admin-web/`：后台管理前端。
- `kuzhambu-apps/portal-web/`：前台 portal 和分享访问前端。
- `kuzhambu-workers/`：Python worker 能力支撑工程。

## Architecture Rules

- 先形成最小可运行模块，再沉淀更细分层。
- 新增目录必须有明确职责和归属，避免空壳结构。
- 新增依赖必须服务于当前任务，不为未来假设预留。
- 项目不设置 `platform` 目录或 `platform` Maven 模块。
- `core`、`auth`、`storage`、`audit` 是独立基础业务域，归入 `kuzhambu-servers/biz/`。
- `core` 承载 menu、user、role 等框架业务，不承载认证登录态、文件存储或审计底座。
- `kuzhambu-servers/infra/` 只承载技术适配，不承载业务语义。
- `kuzhambu-servers/biz/` 和 `kuzhambu-servers/infra/` 内部必须按业务域分包，业务域名称应与需求文档和模块设计文档保持一致。
- 不设置独立 `application` Maven 模块；application 是每个业务域内部的分层包。
- `kuzhambu-servers/biz/kuzhambu-biz-<domain>/` 内部必须包含 `application/` 和 `domain/` 包。
- `application/` 承载用例编排、事务边界、命令、查询和结果对象；`domain/` 承载业务模型、领域服务和业务规则。
- `kuzhambu-servers/infra/<domain>/` 承载该业务域的数据访问、持久化转换和外部技术落地。
- `infra` 子工程内部按需使用 `persistence/`、`mapper/`、`repository/`、`assembler/`、`client/` 等包。
- `Repository` 表达业务域持久化端口，面向 application/domain 提供聚合读写能力，不等同于数据库表访问对象。
- `Mapper` 表达数据库访问实现细节，按 MyBatis 和表结构组织，只能被本业务域 infra 内的 Repository 实现调用。
- 持久化转换统一命名为 `*PersistenceAssembler`，归入对应 `infra-<domain>` 的 `assembler/` 包。
- `kuzhambu-servers/biz/` 和 `kuzhambu-servers/infra/` 必须按业务域拆分为 Maven 子工程。
- `infra-<domain>` 只能依赖对应的 `biz-<domain>` 和 `common-*`，不得依赖其他 `infra-*` 子工程。
- 各业务域 DAO、Mapper、Repository 只能放在对应 `infra-<domain>` 子工程内，跨域访问必须通过对应业务服务接口，不得直接调用其他业务域 DAO。
- `interfaces` 只能调用业务域 `application/` 暴露的用例服务，不得直接调用 `domain/`、DAO、Mapper 或 Repository。
- `interfaces` 子工程内部按业务域组织 Controller 和 DTO，不额外建立 `adminapi` 或 `portal` 包层级。
- `biz-*` 子工程之间暂时不允许相互依赖；跨域编排必须先形成明确需求和设计。
- 产出 jar 的 Maven 子工程 leaf 目录名必须与 `artifactId` 保持一致；分组聚合 POM 可以放在 `common/`、`biz/`、`infra/` 根部。
- 不设置 `infra/shared`；跨业务域技术能力应上收到 `common-*` 模块。
- 基础服务使用 MySQL、Redis、缓存、RocketMQ 和 Elasticsearch；不引入 Seata 或分布式事务基础模块。
- 数据库迁移使用 Flyway；当前只维护初始化脚本 `V1__init.sql`。
- Flyway 由 `kuzhambu-admin-api` 执行，初始化脚本放在 `kuzhambu-servers/interfaces/kuzhambu-admin-api/src/main/resources/db/migration/`；`kuzhambu-portal-api` 不执行数据库迁移。
- 配置管理使用单份 Spring Boot `application.yml`，环境差异只通过环境变量注入；不按 `local`、`test`、`docker` 拆分 profile 配置文件。
- 敏感信息不得写入仓库，应通过环境变量或部署注入。
- 业务对象对外标识统一使用 ULID，生成能力归属 `kuzhambu-common-core`。
- 跨模块引用、审计对象引用、导出引用、发布引用和 URL 参数不得使用数据库自增 ID。
- 数据库表可以保留自增 `bigint` 作为内部技术主键，但业务逻辑不得依赖其跨边界传播。
- 业务表不得设置通用审计字段 `created_at`、`updated_at`、`deleted_at`、`created_by`、`updated_by`、`deleted_by`；通用创建、更新、删除审计归属 `audit` 业务域。
- 业务确实需要表达时间、用户或生命周期时，必须使用业务语义命名，例如 `occurred_at`、`requested_at`、`completed_at`、`expires_at`、`owner_user_id`、`requester_user_id`，不得用审计字段伪装业务字段。
- 分享链接、访问凭证和下载凭证必须使用独立随机 token，不得直接暴露业务 ULID。
- Python worker 本地虚拟环境固定使用 `kuzhambu-workers/.venv/`，不得提交虚拟环境目录。
- Java 主系统原则上通过 HTTP request 调用 Python worker。
- Python worker 只提供能力计算接口，不承载核心业务规则，不直接写入正式业务数据。
- Java 主系统负责权限、任务状态、审计、候选结果确认和最终数据写入。
- API 在线文档使用 Springdoc OpenAPI，不使用旧版 Springfox Swagger；Swagger UI 只作为 OpenAPI 展示入口。
- 后端采用共享业务域的多接口服务形态，`admin-api` 和 `portal-api` 不通过内部 HTTP 调用对方能力。
- 后台 API 使用 `/api/admin/**`，前台 API 使用 `/api/portal/**`。
- `portal-api` 表示前台访问边界，不等同于无认证；是否需要登录由资源访问策略决定。
- 事务边界放在各业务域 `*ApplicationService` 上，不放在 Controller、Repository 或 domain 对象上。
- 异常分为 `DomainException`、`BizException`、`ApiException`；各业务域维护自己的 ErrorCode，Error 名称必须带业务域前缀。
- 日志分为运行日志、访问日志和审计日志；审计业务归属 `audit` 业务域。
- 运行部署采用 Docker 和 Docker Compose，部署样例只提供最小骨架，不在仓库内固化环境密钥。
- 行为、配置或开发流程发生变化时，同步更新相关文档。
- 具体模块建立后，模块内规则优先记录在模块自己的 `AGENTS.md` 或治理文档中。
