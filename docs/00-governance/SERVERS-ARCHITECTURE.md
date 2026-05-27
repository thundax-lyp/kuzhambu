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
- `kuzhambu-servers/common/kuzhambu-common-mq/`：RocketMQ 消息基础能力。
- `kuzhambu-servers/common/kuzhambu-common-elasticsearch/`：Elasticsearch 客户端和索引基础能力。
- `kuzhambu-servers/common/kuzhambu-common-oss/`：对象存储通用客户端抽象和基础适配。
- `kuzhambu-servers/common/kuzhambu-common-test/`：测试基建、架构测试和集成测试辅助。
- `kuzhambu-servers/biz/`：业务域模块组，按业务域组织 `interface`、`application`、`domain`、`infra` 四层。
- `kuzhambu-servers/biz/system/`：系统基础域，承载用户、角色、权限、认证和业务审计。
- `kuzhambu-servers/biz/storage/`：文件存储域，承载文件对象、引用、读取和上传。
- `kuzhambu-servers/biz/classics/`：古籍域，承载三才图会、王圻文档、明代习俗和分享。
- `kuzhambu-servers/biz/ai/`：AI 生产域，承载 AI 配置、提示词和 AI 内容精修。
- `kuzhambu-servers/biz/knowledge/`：知识组织域，承载标签、同义词、实体关系精修和知识图谱。
- `kuzhambu-servers/biz/discovery/`：知识发现域，承载搜索和智能问答。
- `kuzhambu-servers/biz/operations/`：运营运维域，承载看板、报表、任务台账和维护操作记录。
- `kuzhambu-servers/starter/kuzhambu-admin-starter/`：后台启动应用，只负责后台运行时装配。
- `kuzhambu-servers/starter/kuzhambu-portal-starter/`：前台启动应用，只负责前台运行时装配。

## Fast Choice

- HTTP 外部入口：`Controller`
- 接口层请求响应模型：`Request` / `Response`
- 接口层协议转换：`InterfaceAssembler`
- 用例编排入口：`ApplicationService`
- 应用层输入：`Command` / `Query` / `PageQuery`
- 应用层输出：`Result` / `PageResult`
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
          command/
          query/
          assembler/
          support/
      kuzhambu-<domain>-domain/
        src/main/java/com/thundax/kuzhambu/<domain>/domain/
          <subdomain>/
            codec/
            valueobject/
          model/
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
        controller/
        request/
        response/
        assembler/

com/thundax/kuzhambu/<domain>/interfaces/portal/
    controller/
    request/
    response/
    assembler/
```

路径用途：

- `application/`：用例编排、事务边界、跨域协调、命令、查询和结果对象。
- `application/command/`：写入用例输入模型。
- `application/query/`：读取用例输入模型。
- `application/assembler/`：application 内部模型装配，不处理 HTTP 或持久化细节。
- `domain/<subdomain>/codec/`：基础类型和值对象互转，`<subdomain>` 使用业务子域名，例如 `core`、`auth`、`audit`、`object`。
- `application/support/`：仅服务本业务域 application 层的辅助实现。
- `domain/model/`：聚合、实体、值对象和领域枚举。
- `domain/service/`：无法自然归入单个领域对象的领域规则。
- `domain/repository/`：业务域持久化端口，只表达聚合读写语义。
- `domain/event/`：领域事件。
- `infra/repository/impl/`：`domain.repository` 的持久化实现。
- `infra/mapper/`：MyBatis 数据库访问对象。
- `infra/dataobject/`：数据库表映射对象。
- `infra/assembler/`：domain 与 dataobject 的持久化转换。
- `infra/client/`：外部系统、对象存储、搜索、worker 等技术客户端。
- `interfaces/admin/controller/`：后台 HTTP API 入口。
- `interfaces/admin/request/`：后台 HTTP 请求模型。
- `interfaces/admin/response/`：后台 HTTP 响应模型。
- `interfaces/admin/assembler/`：后台 HTTP 协议模型与 application 契约的转换。
- `interfaces/portal/controller/`：前台 HTTP API 入口。
- `interfaces/portal/request/`：前台 HTTP 请求模型。
- `interfaces/portal/response/`：前台 HTTP 响应模型。
- `interfaces/portal/assembler/`：前台 HTTP 协议模型与 application 契约的转换。
- `starter/*/`：Spring Boot 启动类、运行时配置装配、扫描范围和应用依赖选择。
