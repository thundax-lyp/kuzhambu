# HOW-TO FastGPT

本文档用于 Discovery QA 冒烟前确认 FastGPT 管理入口、管理员密码获取方式、开发环境配置和当前模型配置边界。

## 适用范围

- Docker 部署的 FastGPT 服务。
- 本地开发环境 `../kuzhambu/dev.env`。
- Discovery QA 来源跳转冒烟涉及的 Common Knowledge FastGPT Provider。

## 管理入口

FastGPT 管理页面地址由实际部署环境决定，不写死到通用文档。开发环境从 `../kuzhambu/dev.env` 读取：

```sh
KUZHAMBU_FASTGPT_BASE_URL=...
```

管理员账号：

```text
root
```

管理员密码不写入仓库。需要时从 FastGPT Docker 容器环境变量读取。

## 管理员密码获取

FastGPT Docker 部署的 root 密码来自 `DEFAULT_ROOT_PSW`。在运行 FastGPT 的 Docker 主机上执行：

```sh
docker inspect fastgpt-app --format '{{range .Config.Env}}{{println .}}{{end}}' \
  | awk -F= '$1=="DEFAULT_ROOT_PSW"{print substr($0,index($0,"=")+1)}'
```

如果容器名不是 `fastgpt-app`，先确认容器名：

```sh
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Ports}}'
```

说明：

- 正常重启容器不会随机改变密码。
- 如果重新执行部署脚本、重建 compose 环境或手工修改 `DEFAULT_ROOT_PSW`，密码会随该环境变量变化。
- FastGPT 登录接口使用 SHA-256 后的密码值；页面登录直接输入上面获取到的明文密码即可。

## 本地 dev.env 配置

当前开发环境配置写入 `../kuzhambu/dev.env`，不要提交。`KUZHAMBU_FASTGPT_BASE_URL` 按当前部署环境填写：

```sh
KUZHAMBU_FASTGPT_BASE_URL=...
KUZHAMBU_FASTGPT_LOGIN_USERNAME=root
KUZHAMBU_FASTGPT_LOGIN_PASSWORD=...
KUZHAMBU_FASTGPT_API_KEY_NAME=kuzhambu-dev
KUZHAMBU_FASTGPT_API_KEY=...

KUZHAMBU_KNOWLEDGE_ENABLED=true
KUZHAMBU_KNOWLEDGE_PROVIDER=fastgpt
KUZHAMBU_KNOWLEDGE_FASTGPT_BASE_URL=${KUZHAMBU_FASTGPT_BASE_URL}
KUZHAMBU_KNOWLEDGE_FASTGPT_API_KEY=...
KUZHAMBU_KNOWLEDGE_FASTGPT_APP_ID=...
KUZHAMBU_KNOWLEDGE_FASTGPT_APPID=...
KUZHAMBU_KNOWLEDGE_FASTGPT_TIMEOUT=180s
```

`KUZHAMBU_FASTGPT_API_KEY` 和 `KUZHAMBU_KNOWLEDGE_FASTGPT_API_KEY` 使用同一个 FastGPT OpenAPI key。
`KUZHAMBU_KNOWLEDGE_FASTGPT_APPID` 是 `appId` 绑定兼容变量；保留 `KUZHAMBU_KNOWLEDGE_FASTGPT_APP_ID` 作为可读配置，避免不同 binder 对 `appId` 的环境变量归一化差异。

## 模型配置边界

当前没有可用 embedding 模型，因此 FastGPT 暂时只配置：

- LLM：使用 `../kuzhambu/dev.env` 中的主 LLM 供应商参数。
- 分词/文本切分：使用 FastGPT 内置文本处理能力。

不要为了通过冒烟临时配置虚假的 embedding 模型。涉及向量化、语义召回或依赖 embedding 的知识库能力时，应在证据中标记为当前环境限制，而不是用错误模型兜底。

## 配置顺序

1. 从 Docker 主机读取 `DEFAULT_ROOT_PSW`。
2. 登录 FastGPT 或调用 OpenAPI，确认 `kuzhambu-dev` API key 存在。
3. 将 FastGPT 管理配置和 Knowledge Provider 配置写入 `../kuzhambu/dev.env`。
4. 用 `../kuzhambu/dev.env` 中的主 LLM 参数写入 FastGPT LLM 模型配置。
5. 不配置 embedding；仅保留 FastGPT 默认分词/文本切分能力。
6. 重启本地 admin/portal starter 后执行 Discovery QA 冒烟。

## Docker 配置步骤

在 Docker 主机上先确认容器名：

```sh
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Ports}}'
```

读取 FastGPT Mongo 连接串时不要写死账号密码，从 app 容器环境变量取：

```sh
MONGODB_URI=$(docker inspect fastgpt-app --format '{{range .Config.Env}}{{println .}}{{end}}' \
  | sed -n 's/^MONGODB_URI=//p')
```

确认当前 app 配置：

```sh
docker exec fastgpt-mongo mongosh "$MONGODB_URI" --quiet --eval '
db.apps.find({ name: /kuzhambu-qa/ }, { name: 1, type: 1, version: 1, modules: 1, edges: 1 }).forEach(doc => printjson(doc));
'
```

确认 LLM 模型配置，不输出 API key 到日志或提交记录：

```sh
docker exec fastgpt-mongo mongosh "$MONGODB_URI" --quiet --eval '
db.system_models.find(
  { "metadata.type": "llm" },
  { model: 1, "metadata.name": 1, "metadata.provider": 1, "metadata.isActive": 1, "metadata.isDefault": 1 }
).forEach(doc => printjson(doc));
'
```

如果没有 app，可先用 OpenAPI 创建壳：

```sh
curl -fsS -X POST "$KUZHAMBU_FASTGPT_BASE_URL/api/core/app/create" \
  -H "Authorization: Bearer $KUZHAMBU_FASTGPT_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"name":"kuzhambu-qa-dev","type":"advanced","intro":"Kuzhambu dev QA app"}'
```

将返回的 app id 同步到 `../kuzhambu/dev.env`：

```sh
KUZHAMBU_KNOWLEDGE_FASTGPT_APP_ID=<app-id>
KUZHAMBU_KNOWLEDGE_FASTGPT_APPID=<app-id>
```

当前开发环境没有 embedding 模型时，FastGPT app 只能作为 LLM 对话预检；依赖 dataset、collection、向量召回或来源引用的 QA 冒烟必须记录为未通过，不能伪造 embedding 或来源。

## API Key 同步

先在 FastGPT 中创建或确认 OpenAPI key，再把 key 同步到本地开发环境：

```sh
KUZHAMBU_FASTGPT_API_KEY=<fastgpt-openapi-key>
KUZHAMBU_KNOWLEDGE_FASTGPT_API_KEY=<fastgpt-openapi-key>
```

如果直接调用 FastGPT OpenAI-compatible chat 接口验证 app，优先使用 FastGPT 官方兼容写法：

```sh
curl -fsS -X POST "$KUZHAMBU_FASTGPT_BASE_URL/api/v1/chat/completions" \
  -H "Authorization: Bearer ${KUZHAMBU_FASTGPT_API_KEY}-${KUZHAMBU_KNOWLEDGE_FASTGPT_APP_ID}" \
  -H "Content-Type: application/json" \
  -d '{"stream":false,"messages":[{"role":"user","content":"请回复 OK"}]}'
```

Discovery Common Knowledge adapter 使用独立配置项向 FastGPT 发送请求；改动 `../kuzhambu/dev.env` 后必须重启 Portal/Admin starter。

## 冒烟前确认

FastGPT OpenAPI key 健康检查：

```sh
curl -fsS \
  "${KUZHAMBU_FASTGPT_BASE_URL}/api/support/openapi/health?apiKey=${KUZHAMBU_FASTGPT_API_KEY}"
```

本地服务启动前确认：

```sh
set -a
source ../kuzhambu/dev.env
set +a

test "$KUZHAMBU_KNOWLEDGE_ENABLED" = "true"
test -n "$KUZHAMBU_KNOWLEDGE_FASTGPT_API_KEY"
```
