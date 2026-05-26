# Servers Architecture Rules

## Purpose

本文档固定 kuzhambu Java servers 的可门禁架构细则、命名和文件归属规则，避免新增代码时产生多套口径。

新增规则必须先判定能否稳定门禁；可以稳定门禁的规则沉淀为 Hard Rule，暂时依赖 AI 或人工判断的规则保留为 Review Rule。

## Hard Rules

Hard Rules 必须使用 ArchUnit、Maven reactor、Checkstyle、脚本或测试稳定验证；暂时没有门禁的规则不得放入 Hard Rules。

### Module And Dependency

- `SERVERS_MODULE_BIZ_ARTIFACT_NAME`：`kuzhambu-servers/biz/` 下的业务模块必须命名为 `kuzhambu-biz-<domain>`。
- `SERVERS_MODULE_INFRA_ARTIFACT_NAME`：`kuzhambu-servers/infra/` 下的技术实现模块必须命名为 `kuzhambu-infra-<domain>`。
- `SERVERS_MODULE_BIZ_PACKAGE`：`kuzhambu-biz-<domain>` 的 Java 包必须位于 `com.thundax.kuzhambu.biz.<domain>`。
- `SERVERS_MODULE_INFRA_PACKAGE`：`kuzhambu-infra-<domain>` 的 Java 包必须位于 `com.thundax.kuzhambu.infra.<domain>`。
- `SERVERS_MODULE_INFRA_DEPENDS_OWN_BIZ`：`kuzhambu-infra-<domain>` 只能依赖对应的 `kuzhambu-biz-<domain>`，不得依赖其他 `kuzhambu-biz-*`。
- `SERVERS_MODULE_INFRA_NO_INFRA_DEPENDENCY`：`kuzhambu-infra-<domain>` 不得依赖任何 `kuzhambu-infra-*`。
- `SERVERS_MODULE_BIZ_NO_BIZ_DEPENDENCY`：`kuzhambu-biz-*` 子工程之间不得相互依赖。

### Application Service API

- `SERVERS_APP_SERVICE_INPUT_SHAPE`：`*ApplicationService` 的公开用例方法入参只能是无参、单个 `*Command`、单个 `*Query`、单个 `*PageQuery`，或少量 Java plain type 参数。
- `SERVERS_APP_SERVICE_RETURN_SHAPE`：`*ApplicationService` 的公开用例方法返回值只能是 `void`、`*Result`、`*DTO`、`List<*DTO>`、`PageResult<*DTO>` 或 Java plain type。
- `SERVERS_APP_SERVICE_PLAIN_TYPE_SET`：Java plain type 指 JDK 基础类型、包装类型、`String`、`Instant`、`BigDecimal`、枚举，以及这些类型的集合。

### Persistence Boundary

- `SERVERS_PERSISTENCE_REPOSITORY_PORT_PATH`：`*Repository` 接口必须位于对应业务域的 `domain/repository/` 包。
- `SERVERS_PERSISTENCE_REPOSITORY_IMPL_PATH`：`*RepositoryImpl` 必须位于对应业务域 infra 模块的 `repository/impl/` 包。
- `SERVERS_PERSISTENCE_MAPPER_PATH`：`*Mapper` 必须位于对应业务域 infra 模块的 `mapper/` 包。
- `SERVERS_PERSISTENCE_MAPPER_CALLER`：`*Mapper` 只能被本业务域 infra 模块的 `repository/impl/` 包调用。
- `SERVERS_PERSISTENCE_DATA_OBJECT_PATH`：`*DO` 必须位于对应业务域 infra 模块的 `dataobject/` 包。
- `SERVERS_PERSISTENCE_ASSEMBLER_PATH`：`*PersistenceAssembler` 必须位于对应业务域 infra 模块的 `assembler/` 包。

### Interface Boundary

- `SERVERS_INTERFACE_NO_PERSISTENCE_DEPENDENCY`：`kuzhambu-admin-api` 和 `kuzhambu-portal-api` 不得依赖 `domain.repository`、`infra.mapper`、`infra.dataobject`、`infra.repository.impl` 包。

### Annotation Boundary

- `SERVERS_ANNOTATION_REST_CONTROLLER_PATH`：`@RestController` 只能标注在接口服务 `controller/` 包内的 `*Controller` 类。
- `SERVERS_ANNOTATION_MAPPER_PATH`：`@Mapper` 只能标注在对应业务域 infra 模块 `mapper/` 包内的 `*Mapper` 接口。
- `SERVERS_ANNOTATION_TABLE_PATH`：`@TableName`、`@TableId`、`@TableField` 只能出现在对应业务域 infra 模块 `dataobject/` 包内的 `*DO` 类或字段。
- `SERVERS_ANNOTATION_REST_CLASS_BASE_REQUIRED`：接口服务 REST API 入口类必须声明 `@RestController` 或 `@WrappedApiController`，并声明类级 `@RequestMapping`；类级路径必须使用 `/api/{domain}/{resource}`。
- `SERVERS_ANNOTATION_REST_CLASS_TAG_REQUIRED`：接口服务 REST API 入口类必须声明 OpenAPI 3 `@Tag`。
- `SERVERS_ANNOTATION_REST_CLASS_TAG_BUSINESS_NAME`：`@Tag` 必须使用稳定业务分组名，不得使用数字排序前缀。
- `SERVERS_ANNOTATION_REST_METHOD_MAPPING_REQUIRED`：REST API 入口类中的公开 HTTP 方法必须且只能声明一个方法级 HTTP 映射；JSON 请求必须使用 `@PostMapping`，非 JSON 读取必须使用 `@GetMapping`；不得使用方法级 `@RequestMapping`、`@PutMapping`、`@DeleteMapping` 或 `@PatchMapping`。
- `SERVERS_ANNOTATION_REST_METHOD_OPERATION_REQUIRED`：REST API 入口类中的公开 HTTP 方法必须声明 OpenAPI 3 `@Operation`；认证公开入口和文件流入口也必须声明。
- `SERVERS_ANNOTATION_REST_METHOD_ACCESS_MARK_REQUIRED`：声明 `@Operation` 的 REST API 方法必须由方法级或类级 `@HasPermission` / `@PublicApi` 表达访问口径；公开认证入口必须使用 `@PublicApi`。
- `SERVERS_ANNOTATION_REST_METHOD_JSON_POST_REQUIRED`：REST API 方法中使用 `@RequestBody` 或返回 JSON 包装的接口必须使用 `@PostMapping`。
- `SERVERS_ANNOTATION_REST_METHOD_GET_NON_JSON_REQUIRED`：`@GetMapping` 只能用于验证码、头像、文件内容等非 JSON 响应，方法返回类型必须为 `void`。
- `SERVERS_ANNOTATION_REQUEST_BODY_VALID_REQUIRED`：REST API 方法中使用 `@RequestBody` 的 `*Request` 参数必须同时声明 `@Valid`。
- `SERVERS_ANNOTATION_REQUEST_MODEL_CLASS_REQUIRED`：接口层 `request/` 包内 `*Request` 类级注解必须固定为 `@Getter`、`@Setter`、`@Schema`、`@JsonInclude(JsonInclude.Include.NON_NULL)`、`@JsonIgnoreProperties(ignoreUnknown = true)`。
- `SERVERS_ANNOTATION_RESPONSE_MODEL_CLASS_REQUIRED`：接口层 `response/` 包内 `*Response` 类级注解必须固定为 `@Getter`、`@Builder`、`@Schema`、`@JsonInclude(JsonInclude.Include.NON_NULL)`、`@JsonIgnoreProperties(ignoreUnknown = true)`。
- `SERVERS_ANNOTATION_CONTROLLER_RESPONSE_ASSEMBLER_REQUIRED`：Controller 必须通过对应 `*InterfaceAssembler` 或 `PageResponseHelper` 创建业务 `*Response` / `PageResponse`，不得直接 `new *Response()`。

### Transaction Boundary

- `SERVERS_TRANSACTION_APPLICATION_SERVICE_ONLY`：`@Transactional` 只能标注在 `*ApplicationService` 类或其公开用例方法上。

### Exception Boundary

- `SERVERS_EXCEPTION_DOMAIN_TYPE`：domain 层业务规则异常只能使用 `DomainException` 或其子类。
- `SERVERS_EXCEPTION_APPLICATION_TYPE`：application 层业务流程异常只能使用 `BizException` 或其子类。
- `SERVERS_EXCEPTION_INTERFACE_TYPE`：interfaces 层 HTTP 出口异常只能使用 `ApiException` 或其子类。
- `SERVERS_EXCEPTION_NO_ILLEGAL_ARGUMENT_EXIT`：`application` 和 `infra.repository.impl` 不得抛出 `IllegalArgumentException` 作为业务异常出口。

### JSON Boundary

- `SERVERS_JSON_JACKSON_DEFAULT`：HTTP API、接口模型、配置对象和项目内通用 JSON 读写必须使用 Spring Boot 默认 Jackson。
- `SERVERS_JSON_NO_NON_JACKSON_DIRECT_USE`：生产源码不得直接引入或调用非 Jackson JSON 包作为项目 JSON 处理能力。

## Review Rules

- `SERVERS_REVIEW_MODEL_FIELD_DESCRIPTION`：API Request / Response 对外字段应声明 OpenAPI 3 `@Schema` 说明和稳定 JSON 字段名；字段说明质量依赖语义审阅，不放入 Hard Rules。
