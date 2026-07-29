# Servers Architecture

相关治理文档：

- 全局架构和工程组边界见 [`ARCHITECTURE.md`](./ARCHITECTURE.md)。
- 架构细则、目录、命名和文件归属见 [`SERVERS-ARCHITECTURE-RULES.md`](./SERVERS-ARCHITECTURE-RULES.md)。
- 数据库、字段、索引、缓存真相源见 [`SERVERS-DATABASE-RULES.md`](./SERVERS-DATABASE-RULES.md)。
- 统一业务标识、数据库主键、业务单号和随机 token 边界见 [`SERVERS-UNIFIED-ID-DESIGN.md`](./SERVERS-UNIFIED-ID-DESIGN.md)。
- 文档写作与维护见 [`DOCUMENT-RULES.md`](./DOCUMENT-RULES.md)。

## Purpose

本文档记录 kuzhambu Java servers 的稳定架构入口。本文只固定 Java 后端工程边界、技术基线和模块方向，不记录未纳入交付范围的设想。

## Scope

- 适用于 `kuzhambu-servers/` 内新增代码、目录、依赖和构建系统时的架构判断。
- 不替代具体需求、接口契约或专项设计。
- 不记录任务执行过程。

## Current State

- 后端工程组放在 `kuzhambu-servers/`，采用 Java 17、Spring Boot 3.x 和 Maven 多模块。

## Technology Stack

- 后端统一使用 Java 17。
- 后端构建系统使用 `kuzhambu-servers/pom.xml` 管理的 Maven 多模块。
- 后端采用 Spring Boot 3.x，不采用 Spring Cloud 微服务架构。

## Module Layout

- `kuzhambu-servers/common/`：后端通用能力模块组。
- `kuzhambu-servers/common/kuzhambu-common-core/`：通用基础类型、异常、值对象和基础工具。
- `kuzhambu-servers/common/kuzhambu-common-web/`：Web 请求响应、分页、异常处理和请求上下文。
- `kuzhambu-servers/common/kuzhambu-common-security/`：认证上下文、权限注解和安全基础工具。
- `kuzhambu-servers/common/kuzhambu-common-openapi/`：Springdoc OpenAPI 在线文档基础配置。
- `kuzhambu-servers/common/kuzhambu-common-mybatis/`：MyBatis 相关通用配置、类型处理和持久化基础能力。
- `kuzhambu-servers/common/kuzhambu-common-cache/`：缓存基础能力。
- `kuzhambu-servers/common/kuzhambu-common-rocketmq/`：RocketMQ 消息基础能力。
- `kuzhambu-servers/common/kuzhambu-common-elasticsearch/`：Elasticsearch 客户端和索引基础能力。
- `kuzhambu-servers/common/kuzhambu-common-knowledge/`：统一知识库客户端抽象和外部知识产品适配。
- `kuzhambu-servers/common/kuzhambu-common-oss/`：对象存储通用客户端抽象和基础适配。
- `kuzhambu-servers/common/kuzhambu-common-test/`：测试基建、架构测试和集成测试辅助。
- `kuzhambu-servers/biz/`：业务域模块组，按业务域组织 `interface`、`application`、`domain`、`infra` 四层。
- `kuzhambu-servers/biz/system/`：系统基础域，承载用户、角色、权限、认证和业务审计。
- `kuzhambu-servers/biz/storage/`：文件存储域，承载文件对象、引用、读取和上传。
- `kuzhambu-servers/biz/classics/`：古籍域，承载三才图会、王圻文档、明代习俗和分享。
- `kuzhambu-servers/biz/ai/`：AI 生产域，承载 AI 配置、提示词和 AI 内容精修。
- `kuzhambu-servers/biz/knowledge/`：知识组织域，承载标签、同义词、实体关系精修和知识图谱。
- `kuzhambu-servers/biz/discovery/`：知识发现域，承载知识检索和知识问答。
- `kuzhambu-servers/biz/operations/`：运营运维域，承载看板、报表、任务台账和维护操作记录。
- `kuzhambu-servers/starter/kuzhambu-admin-starter/`：后台启动应用，只负责后台运行时装配。
- `kuzhambu-servers/starter/kuzhambu-portal-starter/`：前台启动应用，只负责前台运行时装配。

## Fast Choice

- HTTP 外部入口：`Controller`
- 接口层请求响应模型：`Request` / `Response`
- 接口层协议转换：`InterfaceAssembler`
- 用例编排入口：`ApplicationService`
- 应用层输入：`Command` / `Query` / `PageQuery`
- 应用层输出：本域 domain entity / 强类型值对象 / `Result` / `DTO` / `PageResult`
- 应用层内部复用：`Helper` / `Factory` / `Resolver` / `Executor`
- 核心领域规则：`DomainService`
- 领域读写端口：`Repository`
- 仓储实现：`RepositoryImpl`
- 数据库访问：`Mapper`
- 持久化对象：`DO`
- 持久化桥接转换：`PersistenceAssembler`
- 基础类型和值对象互转：`Codec`
- 通用技术能力：`Service`
- 配置属性：`Properties`
- 自动配置或配置类：`Configuration`

## Cross-Domain Collaboration

- `ApplicationService` 是本业务域的用例入口，不作为其他业务域 application 层的直接依赖目标。
- 一个业务域的 `ApplicationService` 依赖本域内聚的 `*Service` 或 `*DomainService` 完成编排。
- 单体内跨业务域协作不按微服务远程调用口径强制经过对端 application 公开用例。
- 当某个业务域需要为其他业务域暴露稳定的单体内跨域接口时，应新增独立 `*-facade` 模块；该模块扮演微服务 `interface` 的等价物，对外提供统一 `*Facade` 边界。
- `*Facade` 只服务外域调用；提供方本域内部继续直接使用本域 `ApplicationService`、`DomainService`、`Repository` 等分层对象，不把 facade 当作本域内部默认入口。
- `*Facade` 按外域视角收敛成统一门面；不要把同一业务域对外边界机械拆成多个按内部 helper 或 use case 命名的 facade。
- 复杂跨域业务沉淀为稳定 `DomainService` 语义，由调用方 application 通过该 `DomainService` 完成读取、校验或状态变更。
- 跨域调用不得直接访问对端 `infra`、`mapper`、`dataobject`、`repository.impl` 或底层表。
- 对端 `ApplicationService` 不作为默认跨域防腐接口；复杂跨域业务定义明确业务语义的 `DomainService`，再由 application 编排事务和用户用例。

## Runtime Logging

- 启动模块使用 `logback-spring.xml` 配置运行时日志。
- 默认日志目录为 `logs/`，可通过 `KUZHAMBU_LOG_PATH` 调整。
- 默认根日志级别为 `INFO`，可通过 `KUZHAMBU_LOG_LEVEL` 调整。
- 默认业务包 `com.thundax.kuzhambu` 日志级别为 `INFO`，可通过 `KUZHAMBU_APP_LOG_LEVEL` 调整。
- 应用日志写入 `${spring.application.name}.log`，访问日志写入 `${spring.application.name}-access.log`。
- 访问日志 logger 固定为 `com.thundax.kuzhambu.access`，不得混入业务应用日志文件。
- 日志滚动默认单文件 `100MB`、保留 `30` 天、总量 `5GB`，可通过 `KUZHAMBU_LOG_MAX_FILE_SIZE`、`KUZHAMBU_LOG_MAX_HISTORY`、`KUZHAMBU_LOG_TOTAL_SIZE_CAP` 调整。

## Runtime Environment

- 仓库根目录 `.env.example` 是 Java servers 本地启动环境变量样例。
- 本地调试使用的 `dev.env` 必须由开发者基于 `.env.example` 自行创建，并保持未跟踪状态。
- `deploy/.env.example` 是部署支撑样例，必须包含基础设施变量和 Java starter 运行时变量。
- 新增 `application.yml` 环境变量占位符时，同步更新 `.env.example` 和 `deploy/.env.example`。
- 真实 `.env`、`dev.env` 和 `deploy/.env` 不得提交。

## Default Domain Structure

单个业务域默认目录结构如下，`<domain>` 使用业务域名，例如 `system`、`classics`。

```text
kuzhambu-servers/
  biz/
    <domain>/
      kuzhambu-<domain>-interface/
        src/main/java/com/thundax/kuzhambu/<domain>/interfaces/
          admin/
            controller/
            request/
            response/
            assembler/
          portal/
            controller/
            request/
            response/
            assembler/
      kuzhambu-<domain>-application/
        src/main/java/com/thundax/kuzhambu/<domain>/application/
          <subdomain>/
            service/
              impl/
            command/
            query/
            assembler/
            support/
      kuzhambu-<domain>-domain/
        src/main/java/com/thundax/kuzhambu/<domain>/domain/
          model/
            entity/
            enums/
            valueobject/
          <subdomain>/
            codec/
          service/
          repository/
          event/
      kuzhambu-<domain>-infra/
        src/main/java/com/thundax/kuzhambu/<domain>/infra/
          repository/
            impl/
          mapper/
          dataobject/
          assembler/
          client/

  starter/
    kuzhambu-admin-starter/
      src/main/java/com/thundax/kuzhambu/starter/admin/
    kuzhambu-portal-starter/
      src/main/java/com/thundax/kuzhambu/starter/portal/
```

`starter` 只负责运行时装配，不承载业务规则、业务查询聚合、持久化实现或 HTTP 业务入口。后台和前台 HTTP 入口放在各业务域 `interface` 模块内，并通过 `interfaces.admin` 和 `interfaces.portal` package 区分。

业务域 `interface` package 用途：

```text
com/thundax/kuzhambu/<domain>/interfaces/admin/
    <subdomain>/
        controller/
            request/
            response/
        assembler/

com/thundax/kuzhambu/<domain>/interfaces/portal/
    <subdomain>/
        controller/
            request/
            response/
        assembler/
```

路径用途：

- `application/`：用例编排、事务边界、跨域协调、命令、查询和结果对象。
- `application` 层公开方法输入默认使用 `*Command` / `*Query` / `PageQuery`。
- `application` 层 `*Command` / `*Query` 是纯契约对象，只定义字段；字段可以持有本域 domain entity 或强类型值对象；对象创建、值对象转换和领域模型装配放在 `*InterfaceAssembler`、application assembler 或应用服务编排代码中。
- `application` 层公开方法输出优先使用本域 domain entity 或强类型值对象；`*Result` 用于不存在自然 domain entity 的复合结果、跨资源聚合结果或明确的非领域输出；仅在稳定通用传输对象场景下使用 `*DTO`；分页输出统一使用 `PageResult<...>`。
- `application/<subdomain>/service/`：应用用例入口接口，命名为 `*ApplicationService`。
- `application/<subdomain>/service/impl/`：应用用例入口实现，命名为 `*ApplicationServiceImpl`。
- `application/<subdomain>/command/`：写入用例输入模型。
- `application/<subdomain>/query/`：读取用例输入模型。
- `application/<subdomain>/result/`：用例输出模型。
- 业务域 `*-application` 模块中的 `*Command`、`*Query`、`*Result` 必须分别位于 `application/**/command/`、`application/**/query/`、`application/**/result/` 职责包；该路径约束不扫描其他模块中的同名类型。
- `application/<subdomain>/assembler/`：application 内部模型装配，不处理 HTTP 或持久化细节。
- `domain/<subdomain>/codec/`：基础类型和值对象互转，`<subdomain>` 使用业务子域名，例如 `core`、`auth`、`audit`、`object`。
- `application/<subdomain>/support/`：仅服务本业务域 application 层的辅助实现。
- `domain/<subdomain>/model/entity/`：领域实体；实体类必须且只能声明 `@Getter`、`@Setter`、`@NoArgsConstructor`、`@AllArgsConstructor` 四个类级 Lombok 注解。
- `domain/<subdomain>/model/enums/`：领域层枚举；`{module}-domain` 内所有 enum 必须位于对应业务子域的此包。
- `domain/<subdomain>/model/valueobject/`：强类型 ID、Key、Code、Token、Ref、Snapshot 等领域值对象；`application`、`interfaces`、`infra` 不得定义 `valueobject` 包。
- `domain/<subdomain>/model/`：聚合、实体和领域模型。
- `domain/service/`：无法自然归入单个领域对象的领域规则。
- `domain/<subdomain>/repository/`：业务域持久化端口，只表达聚合读写语义。
- `domain/event/`：领域事件。
- `infra/<subdomain>/repository/impl/`：`domain.<subdomain>.repository` 的持久化实现。
- `infra/<subdomain>/persistence/mapper/`：MyBatis 数据库访问对象。
- `infra/<subdomain>/persistence/dataobject/`：数据库表映射对象。
- `infra/<subdomain>/persistence/assembler/`：domain 与 dataobject 的持久化转换。
- `infra/client/`：外部系统、对象存储、搜索、worker 等技术客户端。
- `interfaces/admin/<subdomain>/controller/`：后台 HTTP API 入口。
- `interfaces/admin/<subdomain>/controller/request/`：后台 HTTP 请求模型。
- `interfaces/admin/<subdomain>/controller/response/`：后台 HTTP 响应模型。
- `interfaces/admin/<subdomain>/assembler/`：后台 HTTP 协议模型与 application 契约的转换。
- `interfaces/portal/<subdomain>/controller/`：前台 HTTP API 入口。
- `interfaces/portal/<subdomain>/controller/request/`：前台 HTTP 请求模型。
- `interfaces/portal/<subdomain>/controller/response/`：前台 HTTP 响应模型。
- `interfaces/portal/<subdomain>/assembler/`：前台 HTTP 协议模型与 application 契约的转换。
- `starter/*/`：Spring Boot 启动类、运行时配置装配、扫描范围和应用依赖选择。
