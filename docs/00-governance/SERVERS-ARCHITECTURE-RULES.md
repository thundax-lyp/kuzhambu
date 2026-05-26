# Servers Architecture Rules

## Purpose

本文档固定 kuzhambu Java servers 的可门禁架构细则、命名和文件归属规则，避免新增代码时产生多套口径。

新增规则必须先判定能否稳定门禁；可以稳定门禁的规则沉淀为 Hard Rule，暂时依赖 AI 或人工判断的规则保留为 Review Rule。

## Hard Rules

Hard Rules 必须使用 ArchUnit、Maven reactor、Checkstyle、脚本或测试稳定验证；暂时没有门禁的规则不得放入 Hard Rules。

### Module And Dependency

- `kuzhambu-servers/biz/` 下的业务模块必须命名为 `kuzhambu-biz-<domain>`。
- `kuzhambu-servers/infra/` 下的技术实现模块必须命名为 `kuzhambu-infra-<domain>`。
- `kuzhambu-biz-<domain>` 的 Java 包必须位于 `com.thundax.kuzhambu.biz.<domain>`。
- `kuzhambu-infra-<domain>` 的 Java 包必须位于 `com.thundax.kuzhambu.infra.<domain>`。
- `kuzhambu-infra-<domain>` 只能依赖对应的 `kuzhambu-biz-<domain>`，不得依赖其他 `kuzhambu-biz-*`。
- `kuzhambu-infra-<domain>` 不得依赖任何 `kuzhambu-infra-*`。
- `kuzhambu-biz-*` 子工程之间不得相互依赖。

### Application Service API

- `*ApplicationService` 的公开用例方法入参只能是无参、单个 `*Command`、单个 `*Query`、单个 `*PageQuery`，或少量 Java plain type 参数。
- `*ApplicationService` 的公开用例方法返回值只能是 `void`、`*Result`、`*DTO`、`List<*DTO>`、`PageResult<*DTO>` 或 Java plain type。
- Java plain type 指 JDK 基础类型、包装类型、`String`、`Instant`、`BigDecimal`、枚举，以及这些类型的集合。

### Persistence Boundary

- `*Repository` 接口必须位于对应业务域的 `domain/repository/` 包。
- `*RepositoryImpl` 必须位于对应业务域 infra 模块的 `repository/impl/` 包。
- `*Mapper` 必须位于对应业务域 infra 模块的 `mapper/` 包。
- `*Mapper` 只能被本业务域 infra 模块的 `repository/impl/` 包调用。
- `*DO` 必须位于对应业务域 infra 模块的 `dataobject/` 包。
- `*PersistenceAssembler` 必须位于对应业务域 infra 模块的 `assembler/` 包。

### Interface Boundary

- `kuzhambu-admin-api` 和 `kuzhambu-portal-api` 不得依赖 `domain.repository`、`infra.mapper`、`infra.dataobject`、`infra.repository.impl` 包。

### Annotation Boundary

- `@RestController` 只能标注在接口服务 `controller/` 包内的 `*Controller` 类。
- `@Mapper` 只能标注在对应业务域 infra 模块 `mapper/` 包内的 `*Mapper` 接口。
- `@TableName`、`@TableId`、`@TableField` 只能出现在对应业务域 infra 模块 `dataobject/` 包内的 `*DO` 类或字段。

### Transaction Boundary

- `@Transactional` 只能标注在 `*ApplicationService` 类或其公开用例方法上。

### Exception Boundary

- domain 层业务规则异常只能使用 `DomainException` 或其子类。
- application 层业务流程异常只能使用 `BizException` 或其子类。
- interfaces 层 HTTP 出口异常只能使用 `ApiException` 或其子类。
- `application` 和 `infra.repository.impl` 不得抛出 `IllegalArgumentException` 作为业务异常出口。

### JSON Boundary

- HTTP API、接口模型、配置对象和项目内通用 JSON 读写必须使用 Spring Boot 默认 Jackson。
- 生产源码不得直接引入或调用非 Jackson JSON 包作为项目 JSON 处理能力。

## Review Rules

当前无。新增 Review Rule 必须说明为什么不能门禁。
