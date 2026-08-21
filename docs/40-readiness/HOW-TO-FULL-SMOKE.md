# How to Run Full Smoke

## Purpose

本手册说明如何持续执行和排查全域冒烟。它服务于每次 CI 或隔离 Docker 验收，不是一次性
RUNBOOK。验收标准以
[`FULL-SMOKE-REQUIREMENTS.md`](../10-requirements/FULL-SMOKE-REQUIREMENTS.md) 为准。

## Prerequisites

- Docker、Docker Compose、`curl`、`jq`、Node.js 已安装。
- `deploy/.env`、`deploy/fastgpt/.env` 已按目标环境配置，且不提交凭据。
- 若使用离线 image archives，已在 build host 生成本次代码对应的业务镜像，并在 deploy host load 后
  强制重建受影响服务；`docker load` 本身不会更新既有容器。
- admin-web 依赖已安装，以便 smoke 账户按正式 SM2 登录流程取得 token。
- 仓库内固定的 `scripts/smoke/full-smoke-api-runner.mjs` 可执行正式 API 流程；它接收 run ID、evidence 路径、seed 环境及 Admin/Portal base URL，并在发布、图谱提取/合并/发布和 Portal 校验完成后写入 evidence。
- Sancai Portal 列表/详情接口、图谱 Portal 接口及受控图谱提取输出已经部署；缺任一接口时
  full smoke 必须失败，不可降级为静态页面检查。王圻与明代习俗的内容 Portal 列表/详情暂不验收。

## Execute

在仓库根目录执行：

```sh
KUZHAMBU_SMOKE_BUILD_IMAGES=true \
KUZHAMBU_SMOKE_EVIDENCE_FILE="$PWD/build/full-smoke/evidence.json" \
scripts/smoke/full-smoke.sh deploy/.env deploy/fastgpt/.env
```

脚本使用独立 Docker project 和卷；不要把 production `.env` 或 production 数据库传给它。
运行结束后，单独复核 evidence：

```sh
scripts/smoke/verify-full-smoke-evidence.sh build/full-smoke/evidence.json
```

只有该命令成功，才可接受 `Docker full smoke passed`。

## Incremental image rollout

生产或回归 Docker 环境更新业务镜像时，先以标准镜像归档交付，再重建服务。以下示例仅更新 Workers：

```sh
scripts/smoke/load-image-files.sh deploy/image-files
scripts/deploy/recreate-image-services.sh --env deploy/.env workers
```

脚本会输出运行容器的 image ID；将其与构建产物记录一起保存。随后执行与本次变更相符的 smoke。
不要用 `docker cp` 或 `docker commit` 代替正式交付；这类操作无法提供可复现的镜像来源。

## Evidence contract

执行器在发布、图谱和 Portal 校验完成后写入下列最小结构。数组元素为字符串 ID；一个 ID
只能出现一次。脚本比较每个数组与 `expected` 的集合相等性。

```json
{
  "smokeRunId": "由 full-smoke.sh 注入的本次 run ID",
  "generatedAt": 1787227200000,
  "mode": "fresh-full",
  "parameters": { "batchSize": 20, "pollIntervalSeconds": 5, "deadlineSeconds": 900 },
  "expected": { "SANCAI_ENTRY": ["1"], "WANGQI_DOCUMENT": ["2"], "MING_CUSTOMS": ["3"] },
  "accepted": { "SANCAI_ENTRY": ["1"], "WANGQI_DOCUMENT": ["2"], "MING_CUSTOMS": ["3"] },
  "successfulJobs": { "SANCAI_ENTRY": ["1"], "WANGQI_DOCUMENT": ["2"], "MING_CUSTOMS": ["3"] },
  "publishedContents": { "SANCAI_ENTRY": ["1"], "WANGQI_DOCUMENT": ["2"], "MING_CUSTOMS": ["3"] },
  "readyDocuments": { "SANCAI_ENTRY": ["1"], "WANGQI_DOCUMENT": ["2"], "MING_CUSTOMS": ["3"] },
  "portalList": { "SANCAI_ENTRY": ["1"] },
  "portalDetails": { "SANCAI_ENTRY": ["1"] },
  "extractionTasks": { "SANCAI_ENTRY": ["1"], "WANGQI_DOCUMENT": ["2"], "MING_CUSTOMS": ["3"] },
  "adoptedTasks": { "SANCAI_ENTRY": ["1"], "WANGQI_DOCUMENT": ["2"], "MING_CUSTOMS": ["3"] },
  "publishedMaterials": { "SANCAI_ENTRY": ["1"], "WANGQI_DOCUMENT": ["2"], "MING_CUSTOMS": ["3"] },
  "visibleGraphs": { "SANCAI_ENTRY": ["1"], "WANGQI_DOCUMENT": ["2"], "MING_CUSTOMS": ["3"] },
  "failures": []
}
```

除 `portalList` 与 `portalDetails` 目前只填充 `SANCAI_ENTRY` 外，每个阶段均须填充三类
contentType 的 ID 数组；不能用计数替代。`smokeRunId` 必须使用入口注入的值，`generatedAt` 为
生成 evidence 的 epoch milliseconds。失败时在 `failures` 写入脱敏诊断，包括 contentRef、job/task
ID、step、lastStatus、reason、waitedSeconds 和 deadlineSeconds。

## Failure handling

- evidence 校验失败：保留 evidence 和容器日志；按脚本提示定位缺失或不相等集合。
- job 或 extraction 超时：记录最后状态与 deadline；不要直接改数据库、ES、FastGPT 或图谱表。
- 图谱任务 `WORKER_STREAM` 失败：先检查 Workers 容器是否已从本次镜像重建，再检查 provider SSE
  兼容性和 `KUZHAMBU_KNOWLEDGE_GRAPH_EXECUTOR_*` 的限流配置；不要以 HTTP 200 代替最终事件校验。
- 图谱任务 `OUTPUT_FORMAT_FAILURE` 或“模型输出不是合法 JSON”：保留脱敏失败原因和任务 ID，修复
  模型结构化输出后用新任务验证；不得放宽图谱 JSON schema 或修改失败任务状态来通过冒烟。
- Portal 失败：确认请求没有管理端 token/Cookie，再检查 Discovery 的 READY 文档和版本。
- publication job 失败：保留 job 的失败步骤和原因；本冒烟不对 FastGPT 作独立探测。

修复后必须使用新 `smokeRunId` 和新隔离环境重新运行，不得修改旧 evidence 后重验。
