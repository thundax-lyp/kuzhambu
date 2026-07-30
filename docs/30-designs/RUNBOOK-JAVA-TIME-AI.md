# ai Java Time 迁移 RUNBOOK

## Purpose

独立完成 `ai` 本批旧时间类型迁移，闭合字段、签名、调用方、持久化与协议边界。本文件包含执行所需的完整上下文，不依赖其他迁移 RUNBOOK。

## Scope

- 本批包含 **6** 个生产文件。
- 每个 Task 包含 2–8 个生产文件。
- 字段、签名和操作行以当前分支 `HEAD` 为盘点基线。
- 仅可依据真实编译或调用关系补充遗漏文件，并在任务结果中记录证据。

## Non-goals

- 不机械迁移其他业务域。
- 不修改与时间类型无关的业务逻辑。
- 不把精确时间点降为 `LocalDate`。
- 不默认修改数据库列类型；发现无法无损映射时先停止并补充数据库决策。

## Migration Rules

- domain、application、facade 的时间点使用 `Instant`。
- interface 的自然日期使用 `LocalDate`；精确时间点使用 `Instant`，显式偏移协议使用 `OffsetDateTime`。
- infra 的时间戳列使用 `Instant`，SQL `DATE` 列使用 `LocalDate`。
- `new Date()` 改为 `Instant.now()`；关键规则需要可控时间时注入 `Clock`。
- `before/after` 改为 `isBefore/isAfter`；毫秒算术改为 `Duration` 或 `ChronoUnit`。
- 禁止通过系统默认时区做隐式转换。

## File Inventory

| 层 | 主题 | 文件 | 字段 | 签名与操作 |
|---|---|---|---|---|
| `facade` | `facade` | `kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/AiReportSummaryFacadeRequest.java` | `periodStart`(L14), `periodEnd`(L15) | — |
| `facade` | `facade` | `kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/AiReportSummaryFacadeResponse.java` | `periodStart`(L17), `periodEnd`(L18) | — |
| `application` | `invocation` | `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiReportSummaryResult.java` | `periodStart`(L18), `periodEnd`(L19) | — |
| `application` | `invocation` | `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiReportApplicationServiceImpl.java` | — | L82 `private Date toDate(Instant value) {`<br>操作行 L83 |
| `application` | `facade` | `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/assembler/AiFacadeAssembler.java` | — | L217–231：保持 result → facade response 的 `Instant` 直传 |
| `application` | `facade` | `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java` | — | L80–91：移除 facade request → query 的 `Date.toInstant()` 转换 |

## Plan

### Task 1: report facade 与 application 时间类型闭环

涉及生产文件：**6** 个。该 Task 必须一次完成；request、result、response、assembler 和实现属于同一编译契约链，不得拆开提交。

#### Files And Changes

- `kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/AiReportSummaryFacadeRequest.java`
  - L14 `periodStart`：`Date` → `Instant`。
  - L15 `periodEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/AiReportSummaryFacadeResponse.java`
  - L17 `periodStart`：`Date` → `Instant`。
  - L18 `periodEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiReportSummaryResult.java`
  - L18 `periodStart`：`Date` → `Instant`。
  - L19 `periodEnd`：`Date` → `Instant`。
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiReportApplicationServiceImpl.java`
  - L82：将签名 `private Date toDate(Instant value) {` 的 `Date` 与本调用链目标类型同步。
  - L71–83：构造 result 时直接传递 `periodStart`、`periodEnd`，删除 `toDate` 适配方法。
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/assembler/AiFacadeAssembler.java`
  - L217–231：确认 `periodStart`、`periodEnd` 从 `AiReportSummaryResult` 到 `AiReportSummaryFacadeResponse` 保持 `Instant`，不增加时区或旧类型转换。
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImpl.java`
  - L80–91：`AiReportSummaryFacadeRequest` 已为 `Instant` 后，直接传入 `AiReportSummaryQuery`，移除两处 `Date.toInstant()`。

#### Acceptance

- 逐项完成上述字段、签名与操作行，不留 `java.util.Date`。
- 接口与实现、模型与 assembler、repository 与 mapper 类型一致。
- 更新直接关联测试，覆盖空值、边界比较及适用的 JSON 或数据库往返。
- 最窄模块格式、静态检查、编译和测试通过。

## Associated Test Files

下列测试按本批主题匹配；执行时还必须根据编译和真实调用链补充测试。

- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/facade/impl/AiFacadeImplTest.java`
- `kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiReportApplicationServiceImplTest.java`

## Verification

- `rg 'java\.util\.Date|java\.sql\.(Date|Timestamp)' kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/request/AiReportSummaryFacadeRequest.java kuzhambu-servers/biz/ai/kuzhambu-ai-facade/src/main/java/com/thundax/kuzhambu/ai/facade/response/AiReportSummaryFacadeResponse.java kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/result/AiReportSummaryResult.java kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/service/impl/AiReportApplicationServiceImpl.java`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-ai-domain,:kuzhambu-ai-application,:kuzhambu-ai-facade,:kuzhambu-ai-interface,:kuzhambu-ai-infra spotless:apply`，随后检查 `git diff`。
- `cd kuzhambu-servers && mvn spotless:check`。
- `cd kuzhambu-servers && mvn checkstyle:check`。
- `cd kuzhambu-servers && mvn -pl :kuzhambu-ai-domain,:kuzhambu-ai-application,:kuzhambu-ai-facade,:kuzhambu-ai-interface,:kuzhambu-ai-infra -am -amd test`；确认 Reactor Build Order 包含上述 5 个叶子模块及依赖其 facade 的跨域消费者。
- 涉及 HTTP 时验证 JSON 精度、格式和时区；涉及 infra 时验证数据库往返。

## Closure

本批所有 Task 完成、验证通过并同步必要接口或数据库文档后删除本 RUNBOOK。未完成范围必须收窄为新的独立 RUNBOOK，不保留完成历史。
