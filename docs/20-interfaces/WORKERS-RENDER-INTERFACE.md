# Workers Render Interface

## Purpose

本文档定义 Java 业务域与 Python workers 之间的内部文件渲染接口。

Render 接口只表达“业务域请求 workers 基于已授权内容快照生成一次临时文件产物”。权限、业务状态、私有内容风险确认、导出记录、文件对象创建、文件引用、业务审计和任务状态仍归 Java servers。

## Transport

- 协议：HTTP。
- 编码：UTF-8 JSON。
- 同步响应：`application/json`。
- 流式响应：`text/event-stream`，使用 Server-Sent Events。
- 调用方：允许的 Java 业务域。
- 被调用方：Python workers。
- 认证：内部服务认证，不接收用户 access token。

## Internal Access Control

Render 接口是内部接口，只允许明确授权的 Java 业务域调用。

允许的服务和路径：

| 服务名 | 允许路径 | 用途 |
| --- | --- | --- |
| `kuzhambu-classics` | `/internal/render/classics-export` | Classics 导出 |
| `kuzhambu-classics` | `/internal/render/sancai-showcase` | 三才图会静态展示页 |
| `kuzhambu-operations` | `/internal/render/operations-report` | Operations 报表 |

访问控制分三层：

1. 网络层：workers 不暴露公网入口，只允许 Java servers 所在内网、容器网络或服务网格访问。
2. 服务身份层：workers 必须校验调用方服务身份。
3. 请求完整性层：workers 必须校验请求时间、签名或内部令牌。

Render 接口使用与 AI workers 一致的内部 HMAC 签名。

必需请求头：

- `X-Kuzhambu-Service`：调用方服务名。
- `X-Kuzhambu-Request-Id`：单次请求标识，必须与请求体 `requestId` 一致。
- `X-Kuzhambu-Trace-Id`：链路标识，必须与请求体 `traceId` 一致。
- `X-Kuzhambu-Timestamp`：请求发起时间，Unix milliseconds。
- `X-Kuzhambu-Signature`：请求签名。

签名输入：

```text
METHOD + "\n" +
PATH + "\n" +
X-Kuzhambu-Timestamp + "\n" +
X-Kuzhambu-Request-Id + "\n" +
sha256(requestBody)
```

签名算法：

```text
hex(hmac_sha256(internalWorkerSecret, signingInput))
```

校验规则：

- `X-Kuzhambu-Service` 必须在 worker 允许列表中。
- 服务名必须被授权访问对应 render 路径。
- 时间戳与 worker 当前时间偏差不得超过 5 分钟。
- 请求头 `requestId` 和 `traceId` 必须与请求体一致。
- 签名不匹配时必须返回 `401`。
- 服务名不允许访问该路径时必须返回 `403`。
- 请求体不合法时返回 `400`。
- worker 内部异常返回 `500`，但不得暴露完整业务输入或临时文件路径。

## Endpoints

Render 接口：

- `POST /internal/render/classics-export`
- `POST /internal/render/sancai-showcase`
- `POST /internal/render/operations-report`

流式 render 接口：

- `POST /internal/render/classics-export/stream`
- `POST /internal/render/sancai-showcase/stream`
- `POST /internal/render/operations-report/stream`
- `GET /internal/artifacts/{artifactToken}`

同步接口适合小型文件。流式接口适合长时间 HTML、ZIP 或 PDF 生成进度展示。`GET /internal/artifacts/{artifactToken}` 用于下载当前请求生成的大型产物。无论同步、流式还是 artifact 下载，最终业务落库都必须由 Java servers 以最终响应或 `completed` 事件为准。

健康和能力发现接口：

- `GET /internal/health`
- `GET /internal/capabilities`

Health 和 capabilities 响应模型见 [`WORKERS-AI-INTERFACE.md`](./WORKERS-AI-INTERFACE.md)。

## Common Request

所有 render 接口使用同一基础请求模型。

```json
{
  "requestId": "req_20260601_000101",
  "traceId": "trace_20260601_000101",
  "callerDomain": "CLASSICS",
  "operation": "CLASSICS_EXPORT",
  "renderType": "CLASSICS_EXPORT",
  "template": {
    "templateId": "classics-export-default",
    "templateVersion": "2026.06.01"
  },
  "output": {
    "format": "ZIP",
    "filenameHint": "sancai-export.zip",
    "locale": "zh-CN"
  },
  "input": {
    "snapshotId": "export_20260601_0001",
    "contentType": "CLASSICS_EXPORT_SNAPSHOT",
    "payload": {
      "title": "三才图会导出",
      "items": []
    }
  },
  "options": {
    "stream": false,
    "includeMetadata": true
  }
}
```

字段规则：

- `requestId`：Java servers 生成的单次 worker 请求标识。
- `traceId`：跨服务链路标识。
- `callerDomain`：调用来源域，例如 `CLASSICS` 或 `OPERATIONS`。
- `operation`：调用方业务动作，用于日志和排查。
- `renderType`：渲染能力类型。
- `template`：随系统发布的模板标识和版本，不得要求 workers 读取业务数据库。
- `output.format`：输出格式。
- `output.filenameHint`：文件名建议，workers 可以做安全化处理。
- `input.payload`：已完成权限过滤和风险确认的完整内容快照。
- `options.stream`：调用流式路径时必须为 `true`。

Workers 不得根据 `snapshotId`、业务 ID、文件 ID 或模板 ID 回查 Java servers、数据库、Redis 或 MQ。

## Common Response

同步响应：

```json
{
  "requestId": "req_20260601_000101",
  "traceId": "trace_20260601_000101",
  "status": "SUCCEEDED",
  "renderType": "CLASSICS_EXPORT",
  "artifact": {
    "format": "ZIP",
    "filename": "sancai-export.zip",
    "contentType": "application/zip",
    "encoding": "BASE64",
    "content": "UEsDB...",
    "sizeBytes": 4096,
    "sha256": "sha256:..."
  },
  "summary": {
    "itemCount": 20,
    "warnings": []
  },
  "usage": {
    "latencyMs": 1800
  },
  "error": null
}
```

失败响应：

```json
{
  "requestId": "req_20260601_000101",
  "traceId": "trace_20260601_000101",
  "status": "FAILED",
  "renderType": "CLASSICS_EXPORT",
  "artifact": null,
  "summary": {
    "itemCount": 0,
    "warnings": []
  },
  "usage": {
    "latencyMs": 320
  },
  "error": {
    "type": "RENDER_INPUT_FAILURE",
    "code": "UNSUPPORTED_OUTPUT_FORMAT",
    "message": "不支持的导出格式。",
    "retryable": false,
    "detail": {
      "format": "EXE"
    }
  }
}
```

`status` 取值：

- `SUCCEEDED`
- `FAILED`
- `PARTIAL`

`artifact.format` 取值：

- `CSV`
- `JSON`
- `HTML`
- `ZIP`
- `PDF`

`artifact.encoding` 取值：

- `BASE64`
- `TEXT`
- `STREAM`
- `ARTIFACT_TOKEN`

同步 JSON 响应使用 `BASE64` 或 `TEXT` 返回小型产物。大型产物使用 `ARTIFACT_TOKEN`，由 Java servers 通过 `GET /internal/artifacts/{artifactToken}` 读取二进制内容。

## Artifact Download

`GET /internal/artifacts/{artifactToken}`

用于读取 render 请求生成的大型临时产物。

请求头必须包含：

- `X-Kuzhambu-Service`
- `X-Kuzhambu-Request-Id`
- `X-Kuzhambu-Trace-Id`
- `X-Kuzhambu-Timestamp`
- `X-Kuzhambu-Signature`

下载响应：

- HTTP `200`。
- `Content-Type` 使用产物内容类型。
- `Content-Disposition` 使用安全化后的文件名。
- `X-Kuzhambu-Artifact-Sha256` 返回文件摘要。
- `X-Kuzhambu-Artifact-Size` 返回文件大小。

下载规则：

- `artifactToken` 必须由 workers 为当前 render 请求生成。
- `artifactToken` 不得包含业务 ID、用户 ID、文件路径或可猜测序列。
- `artifactToken` 必须有过期时间。
- 过期、签名不匹配、服务不匹配或产物不存在时返回 `404` 或 `403`。
- artifact 下载只用于 Java servers 取回产物并交给 Storage 保存，不面向 Admin Web 或 Portal Web。

## Classics Export

`POST /internal/render/classics-export`

用于 Classics 导出 CSV、JSON、HTML、ZIP 或静态展示所需文件包。

`renderType` 固定为 `CLASSICS_EXPORT`。

调用前 Classics 必须完成：

- 用户权限校验。
- 导出范围确认。
- 内容可见性过滤。
- 私有内容风险二次确认。
- 导出记录创建或任务状态准备。
- 内容快照组装。

Workers 只负责生成文件内容和摘要，不负责导出记录状态、不创建 Storage 文件对象、不建立业务引用。

## Sancai Showcase

`POST /internal/render/sancai-showcase`

用于生成三才图会静态展示页面。

`renderType` 固定为 `SANCAI_SHOWCASE`。

输入快照应包含：

- 数据集元信息。
- 目录结构。
- 条目正文。
- 图片或视觉资产引用的临时可读内容。
- 是否包含私有内容的确认结果。

模板必须支持离线打开、浏览器打印和 PDF 生成。

## Operations Report

`POST /internal/render/operations-report`

用于生成 Operations 周报或月报 HTML/PDF 产物。

`renderType` 固定为 `OPERATIONS_REPORT`。

调用前 Operations 必须完成：

- admin 权限校验。
- 统计数据聚合。
- 报表范围确认。
- 报表快照组装。

Workers 不拥有维护记录、备份恢复记录、长任务状态或看板聚合事实。

## SSE Stream

流式 render 接口返回 SSE。

每个事件必须包含 JSON data：

```text
event: progress
data: {"eventId":"evt_0002","requestId":"req_20260601_000101","traceId":"trace_20260601_000101","stage":"render_html","timestamp":"2026-06-01T10:00:01.123Z","progress":{"current":10,"total":20,"message":"rendering"}}
```

事件类型：

- `started`：执行开始。
- `progress`：阶段进度。
- `artifact`：产物元信息。
- `warning`：非阻断问题。
- `error`：失败信息。
- `completed`：最终完成。

`completed` 示例：

```json
{
  "eventId": "evt_0099",
  "requestId": "req_20260601_000101",
  "traceId": "trace_20260601_000101",
  "stage": "completed",
  "timestamp": "2026-06-01T10:00:05.000Z",
  "status": "SUCCEEDED",
  "artifact": {
    "format": "HTML",
    "filename": "report.html",
    "contentType": "text/html; charset=utf-8",
    "encoding": "TEXT",
    "content": "<!doctype html>...",
    "sizeBytes": 8192,
    "sha256": "sha256:..."
  },
  "usage": {
    "latencyMs": 5000
  }
}
```

流式规则：

- Java servers 可以把 `progress` 转发给前端展示。
- Java servers 只能以 `completed.artifact` 或同步最终响应创建 Storage 文件对象和更新导出记录。
- workers 不负责 stream 恢复。
- HTTP 连接中断但未收到 `completed` 时，Java servers 必须按失败或部分失败处理。
- `error` 事件后 workers 应结束流；如无法发送 `error`，Java servers 按连接中断处理。

## Error Types

稳定错误类型：

- `WORKER_PROTOCOL_FAILURE`：请求字段缺失、响应格式不合法或 SSE 协议异常。
- `WORKER_TIMEOUT`：worker 调用超时。
- `WORKER_UNAVAILABLE`：worker 不可达。
- `RENDER_INPUT_FAILURE`：输入快照、输出格式、模板参数或文件内容不满足要求。
- `RENDER_TEMPLATE_FAILURE`：模板缺失、模板渲染失败或模板版本不支持。
- `RENDER_OUTPUT_FAILURE`：产物编码、压缩、文件大小或摘要生成失败。
- `UNSUPPORTED_CAPABILITY`：workers 不支持请求的 render 类型。
- `INTERNAL_FAILURE`：未分类内部错误。

重试规则：

- `WORKER_UNAVAILABLE` 和 `WORKER_TIMEOUT` 可以由调用方决定重试。
- `RENDER_INPUT_FAILURE` 不应自动重试，必须修正输入或格式。
- `RENDER_TEMPLATE_FAILURE` 需要按模板发布或版本配置问题处理。

## Security

- 用户 access token 不得转发给 workers。
- workers 不调用 System 校验用户权限。
- workers 不根据用户身份改变输出。
- workers 不回调业务域写入导出记录、报表记录或文件对象。
- workers 不保存 `input.payload`、生成文件或临时文件。
- workers 不得把完整业务输入、私有内容全文、签名、临时文件路径写入日志、错误或响应。
- Java servers 必须在调用 workers 前完成用户认证、权限、业务状态和内容可见性校验。
- workers 的内部服务认证只证明调用方服务可信，不证明最终用户有业务权限。

## Related Documents

- [WORKERS-REQUIREMENTS.md](../10-requirements/WORKERS-REQUIREMENTS.md)：workers 无状态执行、render 能力和跨域边界。
- [WORKERS-DESIGN.md](../30-designs/WORKERS-DESIGN.md)：workers 工程结构和 render 模块设计。
- [CLASSICS-REQUIREMENTS.md](../10-requirements/CLASSICS-REQUIREMENTS.md)：Classics 导出、静态展示和分享边界。
- [OPERATIONS-REQUIREMENTS.md](../10-requirements/OPERATIONS-REQUIREMENTS.md)：Operations 报表和运维统计边界。
