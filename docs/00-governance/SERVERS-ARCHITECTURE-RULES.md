# Servers Architecture Rules

## Purpose

本文档固定 kuzhambu Java servers 的可门禁架构细则、命名和文件归属规则，避免新增代码时产生多套口径。

新增规则必须先判定能否稳定门禁；可以稳定门禁的规则沉淀为 Hard Rule，暂时依赖 AI 或人工判断的规则保留为 Review Rule。

## Hard Rules

Hard Rules 必须使用 ArchUnit、Maven reactor、Checkstyle、脚本或测试稳定验证；暂时没有门禁的规则不得放入 Hard Rules。

### Module And Dependency

- `SERVERS_MODULE_GROUPS`：`kuzhambu-servers/` 下后端工程组固定为 `common/`、`biz/`、`starter/`。
- `SERVERS_COMMON_MODULE_NAME`：`common/` 下通用模块必须命名为 `kuzhambu-common-<capability>`，`<capability>` 使用小写短横线命名。
- `SERVERS_COMMON_PACKAGE`：通用模块 Java 包必须位于 `com.thundax.kuzhambu.common.<capability>`，不得放入任何业务域包。
- `SERVERS_COMMON_NO_BIZ_DEPENDENCY`：`common` 模块不得依赖 `biz` 或 `starter` 模块。
- `SERVERS_DOMAIN_DIRECTORY_NAME`：`kuzhambu-servers/biz/` 下业务域目录固定使用业务域名，例如 `system`、`classics`。
- `SERVERS_DOMAIN_LAYER_MODULE_NAME`：业务域层模块必须命名为 `kuzhambu-<domain>-interface`、`kuzhambu-<domain>-application`、`kuzhambu-<domain>-domain`、`kuzhambu-<domain>-infra`。
- `SERVERS_STARTER_MODULE_NAME`：启动应用模块必须命名为 `kuzhambu-admin-starter` 或 `kuzhambu-portal-starter`。
- `SERVERS_DOMAIN_PACKAGE`：业务域 Java 包必须位于 `com.thundax.kuzhambu.<domain>`。
- `SERVERS_INTERFACE_PACKAGE`：业务域接口层 Java 包必须位于 `com.thundax.kuzhambu.<domain>.interfaces`，并使用 `admin` 和 `portal` 子包区分入口。
- `SERVERS_APPLICATION_PACKAGE`：业务域应用层 Java 包必须位于 `com.thundax.kuzhambu.<domain>.application`。
- `SERVERS_DOMAIN_PACKAGE_LAYER`：业务域领域层 Java 包必须位于 `com.thundax.kuzhambu.<domain>.domain`。
- `SERVERS_INFRA_PACKAGE`：业务域基础设施层 Java 包必须位于 `com.thundax.kuzhambu.<domain>.infra`。
- `SERVERS_STARTER_PACKAGE`：启动应用 Java 包必须位于 `com.thundax.kuzhambu.starter.admin` 或 `com.thundax.kuzhambu.starter.portal`。
- `SERVERS_DOMAIN_LAYER_DEPENDENCY`：同一业务域内依赖方向固定为 `interface -> application -> domain`、`infra -> domain`；`starter` 只做运行时装配。
- `SERVERS_DOMAIN_NO_OUTER_DEPENDENCY`：`domain` 不得依赖 `application`、`interfaces`、`infra` 或 `starter`。
- `SERVERS_APPLICATION_NO_OUTER_DEPENDENCY`：`application` 不得依赖 `interfaces`、`infra` 或 `starter`。
- `SERVERS_INTERFACE_NO_INFRA_DEPENDENCY`：`interface` 不得依赖任何业务域 `infra`。
- `SERVERS_INFRA_NO_INTERFACE_DEPENDENCY`：`infra` 不得依赖任何业务域 `interfaces` 或 `starter`。
- `SERVERS_NO_STARTER_DEPENDENCY_OUTSIDE_STARTER`：除 `starter` 模块外，任何模块不得依赖 `kuzhambu-admin-starter` 或 `kuzhambu-portal-starter`。
- `SERVERS_CROSS_DOMAIN_NO_INFRA_DEPENDENCY`：跨业务域依赖不得指向对端 `infra`、`infra.mapper`、`infra.dataobject` 或 `infra.repository.impl`。
- `SERVERS_CROSS_DOMAIN_NO_REPOSITORY_PORT_DEPENDENCY`：跨业务域依赖不得直接指向对端 `domain.repository`，跨域读写必须通过对端 application 层公开用例或后续明确的防腐接口表达。
- `SERVERS_STARTER_NO_BUSINESS_CODE`：`starter` 不承载业务规则、业务查询聚合、持久化实现或 HTTP 业务入口。

### Naming And File Ownership

- `SERVERS_NAMING_CONTROLLER`：HTTP 入口类必须以 `Controller` 结尾，并位于 `interfaces/<entry>/controller/` 包。
- `SERVERS_NAMING_INTERFACE_ASSEMBLER`：接口层协议转换类必须以 `InterfaceAssembler` 结尾，并位于 `interfaces/<entry>/assembler/` 包。
- `SERVERS_NAMING_REQUEST_RESPONSE`：接口层请求模型必须以 `Request` 结尾并位于 `request/` 包；响应模型必须以 `Response` 结尾并位于 `response/` 包。
- `SERVERS_NAMING_APPLICATION_SERVICE`：用例编排入口必须以 `ApplicationService` 结尾，接口和实现均不得命名为通用 `Manager`、`Processor` 或 `Handler`。
- `SERVERS_NAMING_APPLICATION_INPUT`：应用层写入输入模型必须以 `Command` 结尾；读取输入模型必须以 `Query` 或 `PageQuery` 结尾。
- `SERVERS_NAMING_APPLICATION_OUTPUT`：应用层输出模型必须以 `Result`、`DTO` 或 `PageResult` 结尾。
- `SERVERS_NAMING_DOMAIN_ID`：强类型业务 ID 必须以 `Id` 结尾，必须是 `final class`，必须继承 common 基础 ID 类型，并位于对应业务域 `{module}-domain` 模块下的 `com.thundax.kuzhambu.{module}.domain.model.valueobject`。
- `SERVERS_VALUE_OBJECT_DOMAIN_ONLY`：`valueobject` 包只能出现在对应业务域 `{module}-domain` 模块下的 `com.thundax.kuzhambu.{module}.domain.model.valueobject`；`infra`、`application`、`interfaces` 不得定义 `valueobject` 包。
- `SERVERS_ENTITY_DOMAIN_ONLY`：`entity` 包只能出现在对应业务域 `{module}-domain` 模块下的 `com.thundax.kuzhambu.{module}.domain.model.entity`；`infra`、`application`、`interfaces` 不得定义 `entity` 包。
- `SERVERS_ENTITY_CLASS_ANNOTATIONS`：领域实体类必须且只能声明 `@Getter`、`@Setter`、`@NoArgsConstructor`、`@AllArgsConstructor` 四个类级 Lombok 注解。
- `SERVERS_DOMAIN_ENUM_MODEL_PACKAGE`：对应业务域 `{module}-domain` 模块内所有 enum 必须位于 `com.thundax.kuzhambu.{module}.domain.model.enums`。
- `SERVERS_NAMING_DOMAIN_SERVICE`：领域服务必须以 `DomainService` 结尾，并位于 `domain/service/` 包。
- `SERVERS_NAMING_REPOSITORY`：领域仓储端口必须以 `Repository` 结尾，并位于 `domain/repository/` 包；仓储实现必须以 `RepositoryImpl` 结尾，并位于 `infra/repository/impl/` 包。
- `SERVERS_NAMING_MAPPER_DO`：MyBatis Mapper 必须以 `Mapper` 结尾并位于 `infra/mapper/` 包；持久化对象必须以 `DO` 结尾并位于 `infra/dataobject/` 包。
- `SERVERS_NAMING_PERSISTENCE_ASSEMBLER`：持久化转换类必须以 `PersistenceAssembler` 结尾，并位于 `infra/assembler/` 包。
- `SERVERS_NAMING_CODEC`：基础类型和值对象互转类必须以 `Codec` 结尾，并位于对应业务域 `{module}-domain` 模块下的 `com.thundax.kuzhambu.{module}.domain.{domain}.codec`；通用基础 codec 必须位于明确的 common 基础能力包。
- `SERVERS_NO_MISC_PACKAGE`：业务域生产代码不得新增 `misc`、`util`、`utils`、`helper` 顶层包；确需辅助类时必须放入所属层已有职责包，例如 `application/support/`、`application/helper/` 或 common 专用能力模块。

### Application Service API

- `SERVERS_APP_SERVICE_INPUT_SHAPE`：`*ApplicationService` 的公开用例方法入参只能是无参、单个 `*Command`、单个 `*Query`、单个 `*PageQuery`，或少量 Java plain type 参数。
- `SERVERS_APP_SERVICE_RETURN_SHAPE`：`*ApplicationService` 的公开用例方法返回值只能是 `void`、`*Result`、`*DTO`、`List<*DTO>`、`PageResult<*DTO>` 或 Java plain type。
- `SERVERS_APP_SERVICE_PLAIN_TYPE_SET`：Java plain type 指 JDK 基础类型、包装类型、`String`、`Instant`、`BigDecimal`、枚举，以及这些类型的集合。

### Persistence Boundary

- `SERVERS_PERSISTENCE_REPOSITORY_PORT_PATH`：`*Repository` 接口必须位于对应业务域的 `domain/repository/` 包。
- `SERVERS_PERSISTENCE_REPOSITORY_IMPL_PATH`：`*RepositoryImpl` 必须位于对应业务域 `infra/repository/impl/` 包。
- `SERVERS_PERSISTENCE_MAPPER_PATH`：`*Mapper` 必须位于对应业务域 `infra/mapper/` 包。
- `SERVERS_PERSISTENCE_MAPPER_CALLER`：`*Mapper` 只能被本业务域 `infra/repository/impl/` 包调用。
- `SERVERS_PERSISTENCE_DATA_OBJECT_PATH`：`*DO` 必须位于对应业务域 `infra/dataobject/` 包。
- `SERVERS_PERSISTENCE_ASSEMBLER_PATH`：`*PersistenceAssembler` 必须位于对应业务域 `infra/assembler/` 包。

### Interface Boundary

- `SERVERS_INTERFACE_NO_PERSISTENCE_DEPENDENCY`：业务域 `interface` 模块不得依赖 `domain.repository`、`infra.mapper`、`infra.dataobject`、`infra.repository.impl` 包。
- `SERVERS_INTERFACE_ENTRY_PACKAGE`：后台 HTTP 入口必须位于 `interfaces/admin/controller/` 包，前台 HTTP 入口必须位于 `interfaces/portal/controller/` 包。
- `SERVERS_INTERFACE_REQUEST_RESPONSE_LOCAL`：`interfaces.admin` 下的 Controller 只能使用 `interfaces.admin.request/response/assembler` 模型；`interfaces.portal` 下的 Controller 只能使用 `interfaces.portal.request/response/assembler` 模型，公共协议模型必须先提升到 application 或 common 明确包。
- `SERVERS_INTERFACE_NO_DOMAIN_MODEL_EXPOSE`：接口层 `Controller` 方法签名和 `Response` 字段不得直接暴露 `domain.model` 类型。
- `SERVERS_STARTER_NO_CONTROLLER`：`starter` 模块不得定义业务 `Controller`。

### Annotation Boundary

- `SERVERS_ANNOTATION_REST_CONTROLLER_PATH`：`@RestController` 只能标注在业务域 `interfaces/admin/controller/` 或 `interfaces/portal/controller/` 包内的 `*Controller` 类。
- `SERVERS_ANNOTATION_MAPPER_PATH`：`@Mapper` 只能标注在对应业务域 `infra/mapper/` 包内的 `*Mapper` 接口。
- `SERVERS_ANNOTATION_TABLE_PATH`：`@TableName`、`@TableId`、`@TableField` 只能出现在对应业务域 `infra/dataobject/` 包内的 `*DO` 类或字段。
- `SERVERS_ANNOTATION_DOMAIN_SPRING_FREE`：`domain` 层不得使用 Spring MVC、Spring Transaction、MyBatis 或持久化框架注解。
- `SERVERS_ANNOTATION_APPLICATION_NO_HTTP`：`application` 层不得使用 Spring MVC 注解或 OpenAPI 注解。
- `SERVERS_ANNOTATION_INFRA_NO_HTTP`：`infra` 层不得使用 Spring MVC 注解或 OpenAPI 注解。
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

### Runtime Assembly Boundary

- `SERVERS_STARTER_APPLICATION_CLASS_ONLY`：`starter` 模块中 `*Application` 启动类只能负责启动、扫描范围和运行时装配，不得声明业务用例方法。
- `SERVERS_STARTER_CONFIGURATION_PACKAGE`：`starter` 运行时专属配置必须位于 `com.thundax.kuzhambu.starter.admin` 或 `com.thundax.kuzhambu.starter.portal` 包下。
- `SERVERS_CONFIGURATION_NO_BUSINESS_RULE`：`*Configuration`、`*Properties` 类不得承载业务判断、业务查询或业务状态变更。

## Review Rules

- `SERVERS_REVIEW_MODEL_FIELD_DESCRIPTION`：API Request / Response 对外字段应声明 OpenAPI 3 `@Schema` 说明和稳定 JSON 字段名；字段说明质量依赖语义审阅，不放入 Hard Rules。
- `SERVERS_REVIEW_CROSS_DOMAIN_USE_CASE`：跨业务域调用应优先表达为稳定业务用例，不应为了复用内部查询而直接穿透对端层次。
- `SERVERS_REVIEW_COMMON_EXTRACTION`：提取 common 能力前应确认至少两个业务域存在稳定复用需求，避免把业务概念过早沉淀到 common。
- `SERVERS_REVIEW_SERVICE_GRANULARITY`：ApplicationService 应按用例聚合，不按数据库表机械拆分，也不把无关用例堆入单个巨型服务。
- `SERVERS_REVIEW_ASSEMBLER_COMPLEXITY`：InterfaceAssembler 和 PersistenceAssembler 只做模型转换；出现业务分支、权限判断或持久化访问时应回收到 application、domain 或 infra 对应职责内。
