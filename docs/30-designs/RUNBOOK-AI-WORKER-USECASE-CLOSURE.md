# RUNBOOK AI Worker Usecase Closure

## 1. 目标

本 RUNBOOK 只解决一件事：

- 让 `kuzhambu-servers/biz/ai/` 中 **Classics 同步候选型 AI 精修调用** 不再走 workers 调试接口 `/internal/ai/invoke`，而是走 workers 已存在的 **稳定 usecase path**。

本 RUNBOOK 交付后，Java AI 域对下列 Classics 精修能力必须使用稳定 usecase path：

- `translate`
- `summary`
- `tags`
- `qa`
- `visual`
- `split`

本 RUNBOOK 不处理以下能力：

- `image_analysis`
- `image_gen`
- `fusion`
- Discovery usecase
- Knowledge usecase
- Platform AI usecase
- admin-web 页面接线

本 RUNBOOK 完成后，必须额外形成两份实现覆盖清单文档：

- `docs/30-designs/AI-IMPLEMENTATION-COVERAGE.md`
- `docs/30-designs/WORKERS-IMPLEMENTATION-COVERAGE.md`

## 2. 既定事实

### 2.1 当前 Java AI 调用链

当前 Java AI 调用链事实如下：

- `AiRefinementController` 提供同步候选型精修入口：
  - [AiRefinementController.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/controller/AiRefinementController.java)
- `AiRefinementApplicationServiceImpl` 将请求转换为 `AiInvokeCommand` 后调用 `AiWorkerInvocationApplicationService`：
  - [AiRefinementApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImpl.java)
- `AiInvokeCommand` 当前只有 `operation`，没有 `workerPath`：
  - [AiInvokeCommand.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java)
- `WorkerAiHttpClient` 当前固定使用：
  - 同步：`/internal/ai/invoke`
  - 流式：`/internal/ai/stream`
  - 文件：[WorkerAiHttpClient.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClient.java)

### 2.2 当前 workers 侧已经存在稳定 usecase path

workers 已经存在 Classics AI usecase path，定义在：

- [usecase_registry.py](/Volumes/storage/workspace/kuzhambu/kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py)
- [service_paths.py](/Volumes/storage/workspace/kuzhambu/kuzhambu-workers/src/kuzhambu_workers/core/service_paths.py)
- [WORKERS-AI-USECASE-INTERFACE.md](/Volumes/storage/workspace/kuzhambu/docs/20-interfaces/WORKERS-AI-USECASE-INTERFACE.md)

当前已存在且本 RUNBOOK 会用到的 usecase path 与 operation 固定为：

| contentType | capability | operation | workerPath |
| --- | --- | --- | --- |
| `SANCAI_ENTRY` | `translate` | `CLASSICS_SANCAI_TRANSLATE` | `/internal/ai/classics/sancai/translate` |
| `SANCAI_ENTRY` | `summary` | `CLASSICS_SANCAI_SUMMARY` | `/internal/ai/classics/sancai/summary` |
| `SANCAI_ENTRY` | `tags` | `CLASSICS_SANCAI_TAGS` | `/internal/ai/classics/sancai/tags` |
| `SANCAI_ENTRY` | `qa` | `CLASSICS_SANCAI_QA` | `/internal/ai/classics/sancai/qa` |
| `SANCAI_ENTRY` | `visual` | `CLASSICS_SANCAI_VISUAL_DESCRIPTION` | `/internal/ai/classics/sancai/visual-description` |
| `SANCAI_ENTRY` | `split` | `CLASSICS_SANCAI_SPLIT` | `/internal/ai/classics/sancai/split` |
| `WANGQI_DOCUMENT` | `summary` | `CLASSICS_WANGQI_SUMMARY` | `/internal/ai/classics/wangqi/summary` |
| `WANGQI_DOCUMENT` | `tags` | `CLASSICS_WANGQI_TAGS` | `/internal/ai/classics/wangqi/tags` |
| `WANGQI_DOCUMENT` | `qa` | `CLASSICS_WANGQI_QA` | `/internal/ai/classics/wangqi/qa` |
| `MING_CUSTOMS` | `summary` | `CLASSICS_MING_CUSTOMS_SUMMARY` | `/internal/ai/classics/ming-customs/summary` |
| `MING_CUSTOMS` | `tags` | `CLASSICS_MING_CUSTOMS_TAGS` | `/internal/ai/classics/ming-customs/tags` |
| `MING_CUSTOMS` | `qa` | `CLASSICS_MING_CUSTOMS_QA` | `/internal/ai/classics/ming-customs/qa` |

### 2.3 本轮明确不接的能力

以下能力本轮不进入 usecase 化闭环：

| capability | 原因 |
| --- | --- |
| `image_analysis` | workers 合同固定 `stream=true`，当前 `AiRefinementController.analyzeImage()` 是同步 `CandidateResultResponse` |
| `image_gen` | 当前 `AiRefinementController` 没有对应入口 |
| `fusion` | 当前 `AiRefinementController` 没有对应入口 |

这三类能力本轮保持现状：

- 不新增 admin 接口
- 不改 workers 路由
- 不改 SSE 语义
- 不改 AI 候选确认闭环

## 3. 目标状态

本 RUNBOOK 完成后，系统状态固定为：

1. `AiRefinementApplicationServiceImpl` 对 **Classics 同步候选型精修能力** 生成 canonical `operation` 与 `workerPath`。
2. `WorkerAiHttpClient` 优先使用 `AiInvokeCommand.workerPath` 发起请求。
3. `WorkerAiHttpClient` 仅在 `workerPath` 为空时，才回退到：
   - `/internal/ai/invoke`
   - `/internal/ai/stream`
4. `AiRefinementRequests.RefinementRequest.operation` 继续保留在接口模型中，但 **本轮不再作为 Classics 同步候选型精修路径选择的真相源**。
5. workers 侧 **不改任何生产代码**；本轮只消费现有 workers usecase 合同。

## 4. 执行章节

下列章节已经是最终执行粒度。**不得继续拆分。**

---

## 4.1 章节一：给 `AiInvokeCommand` 增加显式 `workerPath`

### 文件范围

- [AiInvokeCommand.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/command/AiInvokeCommand.java)

### 必做改动

在 `AiInvokeCommand` 中新增字段：

```java
private String workerPath;
```

要求：

- 只新增字段、getter、setter、构造函数参与字段。
- `toRunningCallRecord()` 不写入 `workerPath`。
- 不新增其他字段。
- 不改现有字段名。

### 数据结构结果

`AiInvokeCommand` 完成后必须同时拥有：

- `operation`
- `workerPath`

语义固定为：

- `operation`：workers 请求体中的 canonical operation 编码。
- `workerPath`：workers HTTP path。

### 验收点

- `AiInvokeCommand` 编译通过。
- 没有任何 repository、domain model、DO 因此新增字段被动改动。

---

## 4.2 章节二：新增 Classics AI worker usecase 解析器

### 文件范围

- 新增 [ClassicsAiWorkerUsecaseSpec.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/support/ClassicsAiWorkerUsecaseSpec.java)
- 新增 [ClassicsAiWorkerUsecaseResolver.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/support/ClassicsAiWorkerUsecaseResolver.java)
- 新增 [ClassicsAiWorkerUsecaseResolverTest.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/support/ClassicsAiWorkerUsecaseResolverTest.java)

### 必做改动

#### 4.2.1 `ClassicsAiWorkerUsecaseSpec`

创建不可变数据结构，固定只有两个字段：

```java
private final String operation;
private final String workerPath;
```

允许实现形式：

- Java `record`
- 或普通不可变类

不得增加第三个字段。

#### 4.2.2 `ClassicsAiWorkerUsecaseResolver`

创建 Spring `@Component`。

只暴露一个公开方法：

```java
public ClassicsAiWorkerUsecaseSpec resolve(String contentType, String capability)
```

解析规则固定按本 RUNBOOK 第 2.2 节的表实现，不允许额外扩展。

不支持的组合必须抛出 `BizException`，错误信息必须表达：

- `contentType`
- `capability`
- unsupported

不得静默回退到 generic path。

#### 4.2.3 `ClassicsAiWorkerUsecaseResolverTest`

必须覆盖：

1. `SANCAI_ENTRY + summary` -> `CLASSICS_SANCAI_SUMMARY` + `/internal/ai/classics/sancai/summary`
2. `WANGQI_DOCUMENT + qa` -> `CLASSICS_WANGQI_QA` + `/internal/ai/classics/wangqi/qa`
3. `MING_CUSTOMS + tags` -> `CLASSICS_MING_CUSTOMS_TAGS` + `/internal/ai/classics/ming-customs/tags`
4. `WANGQI_DOCUMENT + translate` 抛出 `BizException`
5. `MING_CUSTOMS + visual` 抛出 `BizException`
6. `SANCAI_ENTRY + image_analysis` 抛出 `BizException`

### 验收点

- 解析器文件中不出现 `/internal/ai/invoke`、`/internal/ai/stream`。
- 解析器只表达 supported usecase mapping。
- 不支持的组合没有 fallback。

---

## 4.3 章节三：在 AI refinement application 层使用 canonical usecase 解析器

### 文件范围

- [AiRefinementApplicationServiceImpl.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImpl.java)
- 新增 [AiRefinementApplicationServiceImplTest.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/refinement/service/impl/AiRefinementApplicationServiceImplTest.java)

### 必做改动

#### 4.3.1 修改 `AiRefinementApplicationServiceImpl`

构造函数新增依赖：

```java
private final ClassicsAiWorkerUsecaseResolver classicsAiWorkerUsecaseResolver;
```

对以下方法，统一改为：

- `translate`
- `summarize`
- `generateTags`
- `generateQa`
- `describeVisual`
- `splitEntry`

执行流程固定为：

1. `validateCommand(command)`
2. `ClassicsAiWorkerUsecaseSpec spec = classicsAiWorkerUsecaseResolver.resolve(command.getContentType(), capability)`
3. `AiInvokeCommand invokeCommand = command.toInvokeCommand(capability)`
4. `invokeCommand.setOperation(spec.operation())`
5. `invokeCommand.setWorkerPath(spec.workerPath())`
6. `AiInvokeResult result = invocationApplicationService.invoke(invokeCommand)`
7. `return AiCandidateResult.from(result)`

`analyzeImage` 不使用解析器，保持现状：

1. 继续 `validateCommand(command)`
2. 继续 `command.toInvokeCommand(CAPABILITY_IMAGE_ANALYSIS)`
3. 不设置 `workerPath`
4. 不覆盖 `operation`
5. 继续调用 `invocationApplicationService.invoke(invokeCommand)`

不得把 `analyzeImage` 纳入解析器。

#### 4.3.2 新增 `AiRefinementApplicationServiceImplTest`

测试必须使用 stub `AiWorkerInvocationApplicationService` 捕获 `AiInvokeCommand`。

必须覆盖：

1. `summarize` + `SANCAI_ENTRY`
   - `invokeCommand.capability == "summary"`
   - `invokeCommand.operation == "CLASSICS_SANCAI_SUMMARY"`
   - `invokeCommand.workerPath == "/internal/ai/classics/sancai/summary"`
2. `generateTags` + `WANGQI_DOCUMENT`
   - `invokeCommand.operation == "CLASSICS_WANGQI_TAGS"`
   - `invokeCommand.workerPath == "/internal/ai/classics/wangqi/tags"`
3. `generateQa` + `MING_CUSTOMS`
   - `invokeCommand.operation == "CLASSICS_MING_CUSTOMS_QA"`
   - `invokeCommand.workerPath == "/internal/ai/classics/ming-customs/qa"`
4. `describeVisual` + `SANCAI_ENTRY`
   - `invokeCommand.operation == "CLASSICS_SANCAI_VISUAL_DESCRIPTION"`
   - `invokeCommand.workerPath == "/internal/ai/classics/sancai/visual-description"`
5. `splitEntry` + `SANCAI_ENTRY`
   - `invokeCommand.operation == "CLASSICS_SANCAI_SPLIT"`
   - `invokeCommand.workerPath == "/internal/ai/classics/sancai/split"`
6. `analyzeImage` + `SANCAI_ENTRY`
   - `invokeCommand.capability == "image_analysis"`
   - `invokeCommand.workerPath == null`
   - `invokeCommand.operation` 保持 `AiRefinementRequestCommand.operation` 原值

### 验收点

- `AiRefinementApplicationServiceImpl` 内部没有手写 usecase path 字符串；全部来自 `ClassicsAiWorkerUsecaseResolver`。
- `analyzeImage` 仍是 legacy generic path 分支。

---

## 4.4 章节四：让 `WorkerAiHttpClient` 优先使用 `workerPath`

### 文件范围

- [WorkerAiHttpClient.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClient.java)
- [WorkerAiHttpClientTest.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/client/WorkerAiHttpClientTest.java)

### 必做改动

#### 4.4.1 修改 `WorkerAiHttpClient`

保留现有两个 generic 常量：

```java
private static final String INVOKE_PATH = "/internal/ai/invoke";
private static final String STREAM_PATH = "/internal/ai/stream";
```

新增一个私有方法：

```java
private String resolvePath(AiInvokeCommand command, boolean stream)
```

规则固定为：

1. `command.getWorkerPath()` 非空白时，返回 `command.getWorkerPath()`
2. `command.getWorkerPath()` 为空白且 `stream == true` 时，返回 `STREAM_PATH`
3. `command.getWorkerPath()` 为空白且 `stream == false` 时，返回 `INVOKE_PATH`

`invoke()` 与 `stream()` 都必须改为通过 `resolvePath()` 取 path。

签名计算必须使用实际发送 path，不得继续写死 `INVOKE_PATH` 或 `STREAM_PATH`。

#### 4.4.2 修改 `WorkerAiHttpClientTest`

现有测试拆成两个事实：

1. `invokeShouldSendSignedWorkerRequestToUsecasePath`
   - `AiInvokeCommand.workerPath = "/internal/ai/classics/sancai/summary"`
   - HTTP server context 固定监听 `/internal/ai/classics/sancai/summary`
   - 断言实际请求 path 与签名 path 都是 `/internal/ai/classics/sancai/summary`
2. `invokeShouldFallbackToGenericInvokePathWhenWorkerPathMissing`
   - `AiInvokeCommand.workerPath == null`
   - HTTP server context 固定监听 `/internal/ai/invoke`
   - 断言实际请求 path 为 `/internal/ai/invoke`

保留现有 `invokeShouldNormalizeWorkerHttpFailure`。

### 验收点

- `WorkerAiHttpClient` 不再默认只会发 `/internal/ai/invoke`。
- usecase path 场景与 legacy generic 场景都被测试覆盖。

---

## 4.5 章节五：补 invocation 层最小回归测试

### 文件范围

- [AiWorkerInvocationApplicationServiceTest.java](/Volumes/storage/workspace/kuzhambu/kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/invocation/AiWorkerInvocationApplicationServiceTest.java)

### 必做改动

保留现有 `streamShouldFailWhenWorkerEndsWithoutCompletedEvent`。

新增一个同步调用测试，名称固定表达：

- `invokeShouldKeepResolvedWorkerPathAndPersistCandidate`

测试结构固定为：

1. 构造 `AiInvokeCommand`
   - `scope = "classics"`
   - `capability = "summary"`
   - `operation = "CLASSICS_SANCAI_SUMMARY"`
   - `workerPath = "/internal/ai/classics/sancai/summary"`
   - `createCandidate = true`
2. stub `WorkerAiClient.invoke(command)`，断言收到的 `command.workerPath` 与 `command.operation` 不变
3. 返回成功 `AiInvokeResult`
4. 断言：
   - 返回结果 `status == "SUCCEEDED"`
   - `candidateId` 被写回
   - repository `saveCandidate()` 被调用

该测试只验证 invocation 层不会覆盖已解析好的 `workerPath` 与 `operation`。

### 验收点

- `AiWorkerInvocationApplicationServiceImpl` 不重写 `workerPath`
- `AiWorkerInvocationApplicationServiceImpl` 不重写 canonical `operation`

## 5. 不允许的实现

以下实现全部禁止：

1. 在 `WorkerAiHttpClient` 里硬编码 `operation -> path` 映射
2. 在 `AiRefinementController` 里直接硬编码 `workerPath`
3. 把 workers usecase path 常量复制到 admin-web
4. 让 admin-web 直接传 `workerPath`
5. 把 `image_analysis` 强行接入 usecase path 并保持同步调用
6. 删除 `/internal/ai/invoke` 或 `/internal/ai/stream`
7. 修改 workers 生产代码以配合本轮 Java 改动

## 6. 验证命令

本 RUNBOOK 完成后，固定执行以下验证：

### 6.1 Java AI 模块最小验证

```sh
cd kuzhambu-servers
mvn -q -pl biz/ai/kuzhambu-ai-application -am test
mvn -q -pl biz/ai/kuzhambu-ai-infra -am test
mvn -q -pl biz/ai/kuzhambu-ai-interface -am test
```

### 6.2 workers 合同回归

```sh
cd kuzhambu-workers
.venv/bin/python -m pytest tests/test_ai_usecase_routes_classics.py tests/test_openapi.py -p no:capture
```

### 6.3 全量 PR 入口

```sh
cd kuzhambu-servers
mvn -q spotless:check
mvn -q checkstyle:check
mvn -q test

cd ../kuzhambu-apps
npm run format:check
npm run lint
npm run test

cd ../kuzhambu-workers
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture
```

## 7. 补充交付物一：AI Implementation Coverage

### 文件范围

- 新增 [AI-IMPLEMENTATION-COVERAGE.md](/Volumes/storage/workspace/kuzhambu/docs/30-designs/AI-IMPLEMENTATION-COVERAGE.md)

### 必做内容

该文档必须只记录 **Java AI 模块对 workers AI usecase 的接入覆盖事实**。

文档固定分成 3 个章节：

1. `已实现并已接入 usecase path`
2. `AI 有入口但本轮明确排除`
3. `workers 已存在但 Java AI 未接入`

### 固定表头

3 个章节下都必须使用同一张表，表头固定为：

| domain | contentType | capability | javaEntry | operation | workerPath | status | note |

字段含义固定为：

- `domain`：固定写 `classics`、`discovery`、`knowledge`、`platform`
- `contentType`：没有业务 contentType 时写 `-`
- `capability`：workers capability 编码
- `javaEntry`：Java 入口方法，格式固定为 `ClassName#methodName`
- `operation`：workers usecase operation 编码
- `workerPath`：workers HTTP path
- `status`：只能使用 `implemented`、`not_implemented`、`excluded`
- `note`：一句事实说明，不写计划

### 本轮文档内容固定要求

#### 7.1 `已实现并已接入 usecase path`

必须列出本 RUNBOOK 第 2.2 节中纳入本轮的 6 个 Classics 同步候选型能力：

- `SANCAI_ENTRY + translate`
- `SANCAI_ENTRY + summary`
- `SANCAI_ENTRY + tags`
- `SANCAI_ENTRY + qa`
- `SANCAI_ENTRY + visual`
- `SANCAI_ENTRY + split`
- `WANGQI_DOCUMENT + summary`
- `WANGQI_DOCUMENT + tags`
- `WANGQI_DOCUMENT + qa`
- `MING_CUSTOMS + summary`
- `MING_CUSTOMS + tags`
- `MING_CUSTOMS + qa`

对应 `javaEntry` 固定写为：

- `AiRefinementController#translate`
- `AiRefinementController#summarize`
- `AiRefinementController#generateTags`
- `AiRefinementController#generateQa`
- `AiRefinementController#describeVisual`
- `AiRefinementController#splitEntry`

#### 7.2 `AI 有入口但本轮明确排除`

必须列出：

- `SANCAI_ENTRY + image_analysis`

这一行的 `status` 固定写 `excluded`，`note` 固定说明：

- `workers contract requires stream=true but current Java refinement entry is synchronous`

#### 7.3 `workers 已存在但 Java AI 未接入`

必须列出至少以下项：

- `/internal/ai/classics/sancai/image-analysis`
- `/internal/ai/classics/sancai/fusion`
- `/internal/ai/classics/sancai/image-gen`
- 全部 Discovery usecase
- 全部 Knowledge usecase
- 全部 Platform usecase

这些行的 `status` 固定写 `not_implemented`。

### 验收点

- `AI-IMPLEMENTATION-COVERAGE.md` 不写“后续考虑”“待完善”“建议”等模糊措辞。
- 所有行都能在 `AiRefinementController.java`、`usecase_registry.py`、`service_paths.py` 或本 RUNBOOK 范围中找到事实依据。
- 该文档只表达 Java AI 接入覆盖事实，不表达 workers 代码内部实现细节。

## 8. 补充交付物二：Workers Implementation Coverage

### 文件范围

- 新增 [WORKERS-IMPLEMENTATION-COVERAGE.md](/Volumes/storage/workspace/kuzhambu/docs/30-designs/WORKERS-IMPLEMENTATION-COVERAGE.md)

### 必做内容

该文档必须只记录 **workers 模块已经实现并注册的 AI usecase 覆盖事实**。

文档固定分成 4 个章节：

1. `Classics usecase`
2. `Discovery usecase`
3. `Knowledge usecase`
4. `Platform usecase`

### 固定表头

4 个章节下都必须使用同一张表，表头固定为：

| domain | contentType | capability | operation | workerPath | stream | workerEntry | status | note |

字段含义固定为：

- `domain`：固定写 `classics`、`discovery`、`knowledge`、`platform`
- `contentType`：没有业务 contentType 时写 `-`
- `capability`：workers capability 编码
- `operation`：workers usecase operation 编码
- `workerPath`：workers HTTP path
- `stream`：固定写 `true` 或 `false`
- `workerEntry`：workers 实现入口，格式固定为 `module_path:function_name`
- `status`：只能使用 `implemented`
- `note`：一句事实说明，不写计划

### 本轮文档内容固定要求

#### 8.1 `Classics usecase`

必须完整列出 `usecase_registry.py` 中所有 `classics` usecase。至少包括：

- `/internal/ai/classics/sancai/translate`
- `/internal/ai/classics/sancai/summary`
- `/internal/ai/classics/sancai/tags`
- `/internal/ai/classics/sancai/qa`
- `/internal/ai/classics/sancai/image-analysis`
- `/internal/ai/classics/sancai/fusion`
- `/internal/ai/classics/sancai/visual-description`
- `/internal/ai/classics/sancai/image-gen`
- `/internal/ai/classics/sancai/split`
- `/internal/ai/classics/wangqi/summary`
- `/internal/ai/classics/wangqi/tags`
- `/internal/ai/classics/wangqi/qa`
- `/internal/ai/classics/ming-customs/summary`
- `/internal/ai/classics/ming-customs/tags`
- `/internal/ai/classics/ming-customs/qa`

#### 8.2 `Discovery usecase`

必须完整列出 `usecase_registry.py` 中所有 `discovery` usecase。

#### 8.3 `Knowledge usecase`

必须完整列出 `usecase_registry.py` 中所有 `knowledge` usecase。

#### 8.4 `Platform usecase`

必须完整列出 `usecase_registry.py` 中所有 `platform` usecase。

### 取值来源固定要求

- `operation`、`workerPath`、`stream` 必须从 `usecase_registry.py` 与 `service_paths.py` 提取。
- `workerEntry` 必须指向 workers 实际实现函数，不得写抽象描述。
- `status` 所有行固定写 `implemented`。

### 验收点

- `WORKERS-IMPLEMENTATION-COVERAGE.md` 不写“后续考虑”“待完善”“建议”等模糊措辞。
- 所有行都能在 `usecase_registry.py`、`service_paths.py` 与 workers 实现文件中找到事实依据。
- 该文档只表达 workers 已实现覆盖事实，不表达 Java AI 是否已接入。

## 9. 交付完成定义

本 RUNBOOK 对应改动完成后，必须同时满足：

1. `translate/summary/tags/qa/visual/split` 六类 Classics 同步候选型精修能力使用 workers usecase path。
2. `image_analysis` 仍保持 legacy generic path，未被误改。
3. Java AI 域没有新增对 workers 代码目录的编译期依赖。
4. workers 生产代码零改动。
5. `docs/30-designs/AI-IMPLEMENTATION-COVERAGE.md` 已创建并完成事实覆盖登记。
6. `docs/30-designs/WORKERS-IMPLEMENTATION-COVERAGE.md` 已创建并完成事实覆盖登记。
7. 本 RUNBOOK 中列出的验证命令全部通过。
