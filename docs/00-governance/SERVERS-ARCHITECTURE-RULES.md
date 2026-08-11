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
- `SERVERS_INTERFACE_POM_DEPENDENCY_WHITELIST`：`kuzhambu-<domain>-interface` 在 Maven POM 层只能依赖 `kuzhambu-<domain>-application`、任意 `kuzhambu-*-facade` 与 `kuzhambu-common-*`；不得直接依赖任何 `*-domain`、他域 `*-application`、`*-infra` 或 `starter` 模块。
- `SERVERS_APPLICATION_POM_DEPENDENCY_WHITELIST`：`kuzhambu-<domain>-application` 在 Maven POM 层只能依赖 `kuzhambu-<domain>-domain`、任意 `kuzhambu-*-facade` 与 `kuzhambu-common-*`；不得直接依赖任何 `*-application`、他域 `*-domain`、`*-interface`、`*-infra` 或 `starter` 模块。
- `SERVERS_POM_LEGACY_ALLOWLIST_SHRINK_ONLY`：测试中的 legacy allowlist 只用于登记存量跨域债务；一旦完成某条 facade 迁移，必须在同一轮删除对应 allowlist，允许项只能收缩不能新增。
- `SERVERS_TEST_POM_COMMON_TEST_ONLY`：除 `kuzhambu-common-test` 外，任何模块的 `pom.xml` 都不得直接声明 `spring-boot-starter-test`；测试基建必须统一通过 `kuzhambu-common-test` 间接获得。
- `SERVERS_TEST_POM_COMMON_TEST_SCOPE_ONLY`：业务模块如需依赖 `kuzhambu-common-test`，只能声明为 `test` 作用域；不得把测试基建作为生产依赖引入 `application`、`domain`、`interface` 或 `infra`。
- `SERVERS_MYBATIS_POM_COMMON_MYBATIS_ONLY`：除仓库根 `dependencyManagement` 与 `kuzhambu-common-mybatis` 外，任何模块的 `pom.xml` 都不得直接声明 `mybatis-plus-spring-boot3-starter`；MyBatis-Plus 基建必须统一通过 `kuzhambu-common-mybatis` 间接获得。
- `SERVERS_FACADE_POM_DEPENDENCY_WHITELIST`：`kuzhambu-<domain>-facade` 在 Maven POM 层只能依赖 `kuzhambu-common-*`；不得依赖任何 `*-application`、`*-domain`、`*-interface`、`*-infra` 或 `starter` 模块。
- `SERVERS_DOMAIN_POM_DEPENDENCY_WHITELIST`：`kuzhambu-<domain>-domain` 在 Maven POM 层只能依赖 `kuzhambu-common-*`；不得依赖任何 `*-application`、`*-facade`、他域 `*-domain`、`*-interface`、`*-infra` 或 `starter` 模块。
- `SERVERS_INTERFACE_NO_INFRA_DEPENDENCY`：`interface` 不得依赖任何业务域 `infra`。
- `SERVERS_INFRA_POM_DEPENDENCY_WHITELIST`：`kuzhambu-<domain>-infra` 在 Maven POM 层只能依赖 `kuzhambu-<domain>-domain` 与 `kuzhambu-common-*`；不得直接依赖任何 `*-application`、`*-facade`、他域 `*-domain`、`*-interface`、他域 `*-infra` 或 `starter` 模块。
- `SERVERS_INFRA_NO_INTERFACE_DEPENDENCY`：`infra` 不得依赖任何业务域 `interfaces` 或 `starter`。
- `SERVERS_NO_STARTER_DEPENDENCY_OUTSIDE_STARTER`：除 `starter` 模块外，任何模块不得依赖 `kuzhambu-admin-starter` 或 `kuzhambu-portal-starter`。
- `SERVERS_CROSS_DOMAIN_NO_INFRA_DEPENDENCY`：跨业务域依赖不得指向对端 `infra`、`infra.mapper`、`infra.dataobject` 或 `infra.repository.impl`。

### Naming And File Ownership

- `SERVERS_NAMING_CONTROLLER`：HTTP 入口类必须以 `Controller` 结尾，并位于 `interfaces/<entry>/<subdomain>/controller/` 包。
- `SERVERS_NAMING_INTERFACE_ASSEMBLER`：接口层协议转换类必须以 `InterfaceAssembler` 结尾，并位于 `interfaces/<entry>/<subdomain>/assembler/` 包。
- `SERVERS_NAMING_REQUEST_RESPONSE`：接口层请求模型必须以 `Request` 结尾并位于 `request/` 包；响应模型必须以 `Response` 结尾并位于 `response/` 包。
- `SERVERS_NAMING_APPLICATION_SERVICE`：用例编排入口必须以 `ApplicationService` 结尾；接口位于 `application/{subdomain}/`，实现位于对应的 `application/{subdomain}/impl/`；接口和实现均不得命名为通用 `Manager`、`Processor` 或 `Handler`。
- `SERVERS_APPLICATION_SERVICE_SUFFIX_ONLY`：业务域 `application` 层内以 `Service` 或 `ServiceImpl` 结尾的类型必须分别以 `ApplicationService` 或 `ApplicationServiceImpl` 结尾，内部辅助组件不得使用泛化 `*Service` 命名。
- `SERVERS_IMPL_CONTRACT`：生产代码中命名为 `XxxImpl` 的类必须实现对应的 `Xxx` 接口；生产代码不得在字段、构造器参数、方法参数或泛型依赖参数中直接使用 `XxxImpl` 类型。仅内部协作组件不得使用 `Impl` 后缀；存量违规只能通过架构测试 allowlist 暂存，并且 allowlist 只能收缩。
- `SERVERS_NAMING_APPLICATION_INPUT`：应用层写入输入模型必须以 `Command` 结尾；读取输入模型必须以 `Query` 结尾；分页输入统一使用 common-core 的全局 `PageQuery`，业务域不得定义 `XxxPageQuery` / `PageXxxQuery`。
- `SERVERS_APPLICATION_PAGE_QUERY_SINGLETON`：`PageQuery` 是全局唯一分页契约，负责分页默认值、边界和归一化语义；除 `com.thundax.kuzhambu.common.core.page.PageQuery` 外不得定义其他 `*PageQuery` / `Page*Query` 类型。
- `SERVERS_APPLICATION_QUERY_NO_PAGE_STATE`：application 层业务 `*Query` 不得声明 `pageNo`、`pageSize`、`pageNum`、`offset`、`limit` 等分页字段，也不得内嵌 `PageQuery`；分页用例必须以 `BusinessQuery + PageQuery` 或单个 `PageQuery` 进入 application service。
- `SERVERS_APPLICATION_COMMAND_PACKAGE`：`*-application` 模块中的 `*Command` 必须位于 `application/**/command/`。
- `SERVERS_APPLICATION_QUERY_PACKAGE`：`*-application` 模块中的 `*Query` 必须位于 `application/**/query/`。
- `SERVERS_APPLICATION_RESULT_PACKAGE`：`*-application` 模块中的 `*Result` 必须位于 `application/**/result/`。
- `SERVERS_NAMING_DOMAIN_ID`：强类型业务 ID 必须以 `Id` 结尾，必须是 `final class`，必须继承 common 基础 ID 类型，并位于对应业务域 `{module}-domain` 模块下的 `com.thundax.kuzhambu.{module}.domain.{domain}.model.valueobject`。
- `SERVERS_VALUE_OBJECT_ID_NO_STATIC_METHODS`：`valueobject` 包下以 `Id` 结尾的值对象不得声明 `static` 方法；基础类型创建、nullable 处理和字符串转换必须放入对应 `*Codec`；每个包含 `valueobject/*Id.java` 的 domain 模块必须在架构测试中挂载该 source scan。
- `SERVERS_VALUE_OBJECT_DOMAIN_ONLY`：`valueobject` 包只能出现在对应业务域 `{module}-domain` 模块下的 `com.thundax.kuzhambu.{module}.domain.{domain}.model.valueobject`；`infra`、`application`、`interfaces` 不得定义 `valueobject` 包。
- `SERVERS_ENTITY_DOMAIN_ONLY`：`entity` 包只能出现在对应业务域 `{module}-domain` 模块下的 `com.thundax.kuzhambu.{module}.domain.{domain}.model.entity`；`infra`、`application`、`interfaces` 不得定义 `entity` 包。
- `SERVERS_ENTITY_CLASS_ANNOTATIONS`：领域实体类必须且只能声明 `@Getter`、`@Setter`、`@NoArgsConstructor`、`@AllArgsConstructor` 四个类级 Lombok 注解。
- `SERVERS_DOMAIN_ENUM_MODEL_PACKAGE`：对应业务域 `{module}-domain` 模块内所有 enum 必须位于 `com.thundax.kuzhambu.{module}.domain.{domain}.model.enums`。
- `SERVERS_NAMING_DOMAIN_SERVICE`：领域服务接口必须以 `DomainService` 结尾并位于 `domain/service/` 包；领域服务实现必须以 `DomainServiceImpl` 结尾并位于 `domain/service/impl/` 包。具体 `DomainService` / `DomainServiceImpl` 必须依赖本域 `Repository`，用于协同 repository-backed 聚合状态的领域规则；不触碰 `Repository` 的纯计算、默认值、归一化、对象构造、factory、policy、normalizer、support/helper 逻辑不得命名为 `DomainService`，也不得放入 `domain/service` 包。
- `SERVERS_NAMING_REPOSITORY`：领域仓储端口必须以 `Repository` 结尾，并位于 `domain/{domain}/repository/` 包；仓储实现必须以 `RepositoryImpl` 结尾，并位于 `infra/{domain}/repository/impl/` 包。
- `SERVERS_REPOSITORY_METHOD_VERB_WHITELIST`：`Repository` 接口方法名必须使用稳定仓储动作白名单；通用读写继续使用 `getBy*`、`list*`、`page*`、`count*`、`insert*`、`update*`、`deleteBy*`、`batch*`，内容仓储端口允许使用精确动作 `save`、`exists`、`open`、`delete`。
- `SERVERS_NAMING_FACADE`：跨域 facade 协议接口必须以 `Facade` 结尾，并位于独立 `*-facade` 模块的 `facade/` 包；facade 协议对象继续使用 `FacadeRequest`、`FacadeResponse`、`FacadeDto` 后缀与对应子包。
- `SERVERS_NAMING_MAPPER_DO`：MyBatis Mapper 必须以 `Mapper` 结尾并位于 `infra/{domain}/persistence/mapper/` 包；持久化对象必须以 `DO` 结尾并位于 `infra/{domain}/persistence/dataobject/` 包。
- `SERVERS_NAMING_PERSISTENCE_ASSEMBLER`：持久化转换类必须以 `PersistenceAssembler` 结尾，并位于 `infra/{domain}/persistence/assembler/` 包。
- `SERVERS_NAMING_CODEC`：基础类型和值对象互转类必须以 `Codec` 结尾，并位于对应业务域 `{module}-domain` 模块下的 `com.thundax.kuzhambu.{module}.domain.{domain}.codec`；通用基础 codec 必须位于明确的 common 基础能力包。
- `SERVERS_NO_MISC_PACKAGE`：业务域生产代码不得新增 `misc`、`util`、`utils`、`helper` 顶层包；确需辅助类时必须放入所属层已有职责包，例如 `application/support/`、`application/helper/` 或 common 专用能力模块。

### Application Service API

- `SERVERS_APP_SERVICE_INPUT_SHAPE`：`*ApplicationService` 的公开用例方法入参只能是无参、单个 `*Command`、单个 `*Query`、单个全局 `PageQuery`、业务 `*Query` + 全局 `PageQuery`、或流式 `*Command` / `*Query` + handler；`*Command` / `*Query` 字段可以持有本域 domain entity 或强类型值对象，但不得持有分页状态。
- `SERVERS_APP_SERVICE_RETURN_SHAPE`：`*ApplicationService` 的公开用例方法返回值只能是 `void`、本域 domain entity、强类型值对象、`*Result`、`*DTO`、`List<本域 domain entity>`、`List<*Result>`、`List<*DTO>`、`PageResult<本域 domain entity>`、`PageResult<*Result>`、`PageResult<*DTO>` 或 Java primitive / 明确允许的基础类型。
- `SERVERS_APP_SERVICE_COUNT_RETURN_LONG`：`*ApplicationService` 中以 `count` 命名的公开查询方法必须返回 primitive `long`，不得返回 `Long`、`*Result` 或其他包装对象。
- `SERVERS_APP_SERVICE_PLAIN_TYPE_SET`：Application service 允许的基础类型指 Java primitive、`String`、`Instant`、`BigDecimal`、枚举，以及这些类型的集合；计数类返回必须使用 primitive `long`。

### Persistence Boundary

- `SERVERS_PERSISTENCE_REPOSITORY_PORT_PATH`：`*Repository` 接口必须位于对应业务域的 `domain/{domain}/repository/` 包。
- `SERVERS_PERSISTENCE_REPOSITORY_IMPL_PATH`：`*RepositoryImpl` 必须位于对应业务域 `infra/{domain}/repository/impl/` 包。
- `SERVERS_PERSISTENCE_MAPPER_PATH`：`*Mapper` 必须位于对应业务域 `infra/{domain}/persistence/mapper/` 包。
- `SERVERS_PERSISTENCE_MAPPER_ANNOTATIONS`：`*Mapper` 必须且只能装配 `@Mapper`。
- `SERVERS_PERSISTENCE_MAPPER_CALLER`：`*Mapper` 只能被本业务域 `infra/{domain}/repository/impl/` 包调用。
- `SERVERS_PERSISTENCE_DATA_OBJECT_PATH`：`*DO` 必须位于对应业务域 `infra/{domain}/persistence/dataobject/` 包。
- `SERVERS_PERSISTENCE_DATA_OBJECT_LOMBOK`：`*DO` 必须且只能装配 `@Data`、`@NoArgsConstructor`、`@AllArgsConstructor` 三个类级 Lombok 注解。
- `SERVERS_PERSISTENCE_ASSEMBLER_PATH`：`*PersistenceAssembler` 必须位于对应业务域 `infra/{domain}/persistence/assembler/` 包。
- `SERVERS_PERSISTENCE_ASSEMBLER_METHODS`：`*PersistenceAssembler` 必须具备 `public static` 的 `toObject` 和 `toDomain` 转换方法。

### Interface Boundary

- `SERVERS_INTERFACE_NO_PERSISTENCE_DEPENDENCY`：业务域 `interface` 模块不得依赖 `domain.repository`、`infra.mapper`、`infra.dataobject`、`infra.repository.impl` 包。
- `SERVERS_INTERFACE_ONLY_APPLICATION_SERVICE_DEPENDENCY`：业务域 `interface` 模块调用 application 用例时，只能依赖 `*ApplicationService`，不得依赖泛化 `*Service`。
- `SERVERS_INTERFACE_ENTRY_PACKAGE`：后台 HTTP 入口必须位于 `interfaces/admin/<subdomain>/controller/` 包，前台 HTTP 入口必须位于 `interfaces/portal/<subdomain>/controller/` 包。
- `SERVERS_INTERFACE_REQUEST_RESPONSE_LOCAL`：`interfaces.admin.<subdomain>` 下的 Controller 只能使用同一子域的 `controller.request`、`controller.response` 和 `assembler` 模型；`interfaces.portal.<subdomain>` 同理，公共协议模型必须先提升到 application 或 common 明确包。
- `SERVERS_INTERFACE_NO_DOMAIN_MODEL_EXPOSE`：接口层 `Controller` 方法签名和 `Response` 字段不得直接暴露 `domain.{domain}.model` 类型。
- `SERVERS_INTERFACE_NO_PRIORITY_PROTOCOL_FIELD`：业务域 `*-interface` 模块内 `*Request` / `*Response` 协议模型不得声明或序列化 `priority` 字段；`priority` 是后端内部排序权重，不属于对外 API 契约。
- `SERVERS_INTERFACE_SORT_REQUEST_ORDERED_IDS_ONLY`：接口层 `*SortRequest` 只能接收 `orderedIds`；排序接口不得接收 `priority`、`sortDirection` 或业务作用域字段，后端根据 ID 列表读取当前集合并交换内部 `priority`。
- `SERVERS_STARTER_NO_CONTROLLER`：`starter` 模块不得定义业务 `Controller`。
- `SERVERS_INTERFACE_OPENAPI_MODULE_GROUP`：OpenAPI 文档必须按业务 module 注册 `GroupedOpenApi` 分组，并使用 `addOpenApiMethodFilter` 根据接口方法声明类所属包归组；不得把多个业务 module 混入同一个默认分组。

### Annotation Boundary

- `SERVERS_ANNOTATION_REST_CONTROLLER_PATH`：`@RestController` 只能标注在业务域 `interfaces/admin/<subdomain>/controller/` 或 `interfaces/portal/<subdomain>/controller/` 包内的 `*Controller` 类。
- `SERVERS_ANNOTATION_MAPPER_PATH`：`@Mapper` 只能标注在对应业务域 `infra/{domain}/persistence/mapper/` 包内的 `*Mapper` 接口。
- `SERVERS_ANNOTATION_TABLE_PATH`：`@TableName`、`@TableId`、`@TableField` 只能出现在对应业务域 `infra/{domain}/persistence/dataobject/` 包内的 `*DO` 类或字段。
- `SERVERS_ANNOTATION_DOMAIN_SPRING_FREE`：`domain` 层不得使用 Spring MVC、Spring Transaction、MyBatis 或持久化框架注解。
- `SERVERS_ANNOTATION_APPLICATION_NO_HTTP`：`application` 层不得使用 Spring MVC 注解或 OpenAPI 注解。
- `SERVERS_ANNOTATION_INFRA_NO_HTTP`：`infra` 层不得使用 Spring MVC 注解或 OpenAPI 注解。
- `SERVERS_ANNOTATION_REST_CLASS_BASE_REQUIRED`：接口服务 REST API 入口类必须声明 `@WrappedApiController` 或显式 `@IgnoreWrappedApiController`，并声明类级 `@RequestMapping`；类级路径必须使用 `/api/{domain}/{resource}`。
- `SERVERS_ANNOTATION_REST_CLASS_TAG_REQUIRED`：接口服务 REST API 入口类必须声明 OpenAPI 3 `@Tag`，且必须填写 `description` 说明业务子域。
- `SERVERS_ANNOTATION_REST_CLASS_SYS_LOGGER_REQUIRED`：后台 REST API 入口类必须声明 `@SysLogger` 或显式 `@IgnoreSysLogger`。
- `SERVERS_ANNOTATION_REST_CLASS_TAG_BUSINESS_NAME`：`@Tag` 必须使用稳定业务分组名，不得使用数字排序前缀。
- `SERVERS_ANNOTATION_REST_METHOD_MAPPING_REQUIRED`：REST API 入口类中的公开 HTTP 方法必须且只能声明一个方法级 HTTP 映射；不得使用方法级 `@RequestMapping`、`@PutMapping`、`@DeleteMapping` 或 `@PatchMapping`。
- `SERVERS_ANNOTATION_REST_ACTION_AMBIGUOUS_VERB_FORBIDDEN`：REST API 入口类中的公开 HTTP 方法名，以及 `@PostMapping` 方法级路径最后一个非路径变量片段，不得使用模糊动作 `save`、`do`、`handle`、`process`、`operate`、`action`、`manage`。该规则由 ArchUnit 稳定门禁；新增和更新应使用能表达业务规则的动作，不得以泛化动词掩盖实际语义。
- `SERVERS_ANNOTATION_REST_METHOD_OPERATION_REQUIRED`：REST API 入口类中的公开 HTTP 方法必须声明 OpenAPI 3 `@Operation`；认证公开入口和文件流入口也必须声明。
- `SERVERS_ANNOTATION_REST_METHOD_ACCESS_MARK_REQUIRED`：声明 `@Operation` 的 REST API 方法必须由方法级或类级 `@HasPermission` / `@PublicApi` 表达访问口径；公开认证入口必须使用 `@PublicApi`。
- `SERVERS_ANNOTATION_REST_METHOD_DOC_AND_LOG_REQUIRED`：后台 REST API 方法必须声明 `@ApiImplicitParams`，并声明 `@SysLogger` 或显式 `@IgnoreSysLogger`。
- `SERVERS_ANNOTATION_REST_METHOD_POST_JSON_DEFAULT`：全局 API 接口默认必须使用 `HTTP POST + JSON body`：方法必须使用 `@PostMapping`，入参允许为空、单个 `@Valid @RequestBody *Request` 或单个 `@Valid @RequestBody List<*Request>`，且不得使用 `@PathVariable` 或 `@RequestParam`。验证码、头像与文件内容直链、`multipart/form-data` 上传、SSE 等非 JSON 传输允许使用 `@PostJsonApiExempt(reason = "...")` 豁免；豁免必须填写具体传输原因。出参允许 `void`、`Boolean`、`String`、`Long`、`Integer`、`SseEmitter`、`*Response`、`List<*Response>` 或 `PageResponse<*Response>`。
- `SERVERS_ANNOTATION_REST_METHOD_GET_NON_JSON_REQUIRED`：`@GetMapping` 只能用于验证码、头像、文件内容等非 JSON 响应，方法返回类型必须为 `void`；声明 `@PostJsonApiExempt(reason = "...")` 的 SSE 事件流可返回 `SseEmitter`。
- `SERVERS_ANNOTATION_REQUEST_BODY_VALID_REQUIRED`：REST API 方法中使用 `@RequestBody` 的 `*Request` 参数必须同时声明 `@Valid`。
- `SERVERS_ANNOTATION_REQUEST_MODEL_CLASS_REQUIRED`：接口层 `request/` 包内 `*Request` 类级注解必须固定为 `@Getter`、`@Setter`、`@Schema`、`@JsonInclude(JsonInclude.Include.NON_NULL)`、`@JsonIgnoreProperties(ignoreUnknown = true)`。
- `SERVERS_ANNOTATION_RESPONSE_MODEL_CLASS_REQUIRED`：接口层 `response/` 包内 `*Response` 类级注解必须固定为 `@Getter`、`@Builder`、`@Schema`、`@JsonInclude(JsonInclude.Include.NON_NULL)`、`@JsonIgnoreProperties(ignoreUnknown = true)`。
- `SERVERS_ANNOTATION_CONTROLLER_RESPONSE_ASSEMBLER_REQUIRED`：Controller 必须通过对应 `*InterfaceAssembler` 或 `PageResponseHelper` 创建业务 `*Response` / `PageResponse`，不得直接 `new *Response()`。

### Transaction Boundary

- `SERVERS_TRANSACTION_APPLICATION_SERVICE_ONLY`：全 servers 生产源码中的 `@Transactional` 只能标注在 application 层 `*ApplicationServiceImpl` 或 `*FacadeImpl` 类，或其公开用例方法上；interface、domain、infra、starter 和 common 模块不得声明该注解。

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
- `SERVERS_SPRING_BEAN_SINGLE_CONSTRUCTOR`：直接标注 `@Component`、`@Service`、`@Repository`、`@Controller`、`@RestController` 或 `@Configuration` 的 Spring 管理类必须有且仅有一个构造器；不得在生产 Bean 中保留简化构造器或测试专用构造器。

## Review Rules

- `SERVERS_REVIEW_MODEL_FIELD_DESCRIPTION`：API Request / Response 对外字段应声明 OpenAPI 3 `@Schema` 说明和稳定 JSON 字段名；字段说明质量依赖语义审阅，不放入 Hard Rules。
- `SERVERS_REVIEW_CROSS_DOMAIN_USE_CASE`：单体内跨业务域协作不按微服务远程调用口径强制经过对端 application 公开用例；复杂跨域业务表达为稳定 repository-backed `DomainService` 语义，不为了复用内部查询而直接穿透对端 infra、mapper、dataobject 或 repository implementation。
- `SERVERS_REVIEW_COMMON_EXTRACTION`：提取 common 能力前应确认至少两个业务域存在稳定复用需求，避免把业务概念过早沉淀到 common。
- `SERVERS_REVIEW_SERVICE_GRANULARITY`：ApplicationService 应按用例聚合，不按数据库表机械拆分，也不把无关用例堆入单个巨型服务。
- `SERVERS_REVIEW_FACADE_EXTERNAL_BOUNDARY`：`*Facade` 是提供方业务域给外域暴露的统一跨域边界，只供外域调用；提供方本域内部默认继续使用本域 application/domain 分层对象，不把 facade 当成本域内部复用入口。
- `SERVERS_REVIEW_ASSEMBLER_COMPLEXITY`：InterfaceAssembler 和 PersistenceAssembler 只做模型转换；出现业务分支、权限判断或持久化访问时应回收到 application、domain 或 infra 对应职责内。
- `SERVERS_REVIEW_SPRING_META_BEAN_SINGLE_CONSTRUCTOR`：通过派生注解、组合注解或其他 Spring 语义间接注册的类级 Bean 也应有且仅有一个构造器；如框架绑定类或特殊装配方式存在约束，按框架约定单独评审。
- `SERVERS_REVIEW_TEST_CONSTRUCTION_EXPLICIT`：当生产类收敛为单构造器后，测试应通过 mock、stub 或测试工厂显式补齐依赖，不得为了测试便利重新引入第二构造器。
- `SERVERS_REVIEW_ACTION_BUSINESS_SEMANTICS`：Service、Controller 方法与 action URL 应使用准确表达业务规则和操作语义的动作；不要求跨层使用同一个动词，也不使用封闭允许词表。评审应结合调用语义、权限和状态变化判断名称是否准确。
- `SERVERS_REVIEW_APPLICATION_OUTPUT_SEMANTICS`：ApplicationService 默认应返回本域 domain entity 或强类型值对象；`*Result` 仅用于不存在自然 domain entity 的复合结果、跨资源聚合结果或明确的非领域输出，`*DTO` 仅用于稳定通用传输对象。该选择依赖业务模型语义，由评审确认。
- `SERVERS_REVIEW_CONFIGURATION_BUSINESS_BOUNDARY`：`*Configuration`、`*Properties` 不应承载业务判断、业务查询或业务状态变更。配置绑定、默认值与框架装配边界需结合运行时语义评审，不以控制流关键字作硬门禁。
- `SERVERS_REVIEW_STARTER_BUSINESS_BOUNDARY`：`starter` 应只承载启动、扫描范围和运行时装配，不应承载业务规则、业务查询聚合、持久化实现或 HTTP 业务入口。是否构成业务聚合需结合调用链和职责语义评审。
