# Kuzhambu Workers

Python 技术支撑工程，用于承载无状态 AI graph 执行、流式输出转发、图片理解、内容生成、文件渲染和格式加工。

Workers 不承载核心业务规则，不直接写入正式业务数据，不连接数据库、Redis 或 MQ，不保存任务状态。

## Environment

- Python 版本固定为 3.10。
- 本地虚拟环境使用 `kuzhambu-workers/.venv/`。

## Technology Baseline

- FastAPI 提供内部 HTTP API。
- Pydantic 定义请求和响应模型。
- LangGraph 承载 AI 执行图。
- LangChain 负责 prompt、message、model adapter 和 structured output 基础能力。
- httpx 负责内部 HTTP、临时 URL 和 OpenAI-compatible 模型访问。
- Pillow 负责图片尺寸、格式和基础转换处理。
- Playwright/Chromium print 负责 HTML 到 PDF 渲染，Chromium 通过 Browser Pool 复用。
- Python 标准库负责 CSV、JSON、HTML、ZIP 和临时文件处理。
- Ruff 统一负责 Python lint 和 formatter。

## System Dependencies

- Python package 固定使用 `playwright==1.59.0`。
- PDF 渲染使用 Playwright-managed Chromium，不使用系统 `chromium` 包。
- Linux 容器基线使用 `mcr.microsoft.com/playwright/python:v1.59.0-jammy`。
- 非官方镜像构建时必须执行：

```sh
python -m playwright install --with-deps chromium
python -m playwright install --list
```

`install --list` 输出必须保留在构建日志中，用于确认 Chromium revision。

Browser Pool 运行参数：

- `KUZHAMBU_WORKER_BROWSER_POOL_SIZE`：Chromium browser 进程数量，默认 `1`。
- `KUZHAMBU_WORKER_BROWSER_MAX_PAGES`：最大并发 page 数，默认 `4`。
- `KUZHAMBU_WORKER_BROWSER_PAGE_TIMEOUT_MS`：单页渲染超时，默认 `30000` 毫秒。

PDF 渲染通过 Browser Pool 复用 Chromium。每次渲染创建独立 context/page，请求完成、超时或异常后关闭 context。

## Invocation

- Java 主系统通过内部 HTTP request 调用 Python workers。
- Workers 只提供能力计算、AI 执行和文件渲染接口。
- 权限、任务状态、审计、候选结果确认和最终数据写入由 Java 主系统负责。
- AI 能力只能由 AI 域调用 workers；Classics、Knowledge 和 Discovery 不得绕过 AI 域直接调用 AI workers。
- Classics 和 Operations 可以在完成权限过滤和内容快照准备后直接调用 render workers。

## Internal APIs

- `GET /internal/openapi.json`
- `GET /internal/docs`
- `GET /internal/redoc`
- `GET /internal/health`
- `GET /internal/capabilities`
- `POST /internal/ai/invoke`
- `POST /internal/ai/stream`
- `POST /internal/ai/classics/*`
- `POST /internal/ai/discovery/*`
- `POST /internal/ai/knowledge/*`
- `POST /internal/ai/platform/*`
- `POST /internal/render/classics-export`
- `POST /internal/render/sancai-showcase`
- `POST /internal/render/operations-report`

内部接口必须校验服务身份和请求签名，不接收用户 access token。

OpenAPI 和 Swagger UI 只暴露在 `/internal/*` 路径下，用于内网开发、联调和接口排查。

`/internal/ai/invoke` 和 `/internal/ai/stream` 是通用调试接口，仅用于平台联调和协议验证。真实业务应接入基于 usecase 定义的稳定接口。

AI usecase 路径只允许 `kuzhambu-ai` 服务身份调用；Classics、Discovery、Knowledge 等业务域不得绕过 AI 域直接访问 workers AI 接口。完整 usecase path、capability 和 stream 约束见 `docs/20-interfaces/WORKERS-AI-USECASE-INTERFACE.md`。

## Local Development

```sh
python3.10 -m venv .venv
.venv/bin/python -m pip install -e '.[dev]'
.venv/bin/uvicorn kuzhambu_workers.main:app --reload
```

## Documents

- `docs/10-requirements/WORKERS-REQUIREMENTS.md`
- `docs/20-interfaces/WORKERS-AI-INTERFACE.md`
- `docs/20-interfaces/WORKERS-AI-USECASE-INTERFACE.md`
- `docs/20-interfaces/WORKERS-RENDER-INTERFACE.md`
- `docs/30-designs/WORKERS-DESIGN.md`

## Local Checks

```sh
.venv/bin/ruff format --check .
.venv/bin/ruff check .
.venv/bin/pytest
```

格式化和自动修复：

```sh
.venv/bin/ruff format .
.venv/bin/ruff check --fix .
```
