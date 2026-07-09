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
KUZHAMBU_KNOWLEDGE_FASTGPT_TIMEOUT=10s
```

`KUZHAMBU_FASTGPT_API_KEY` 和 `KUZHAMBU_KNOWLEDGE_FASTGPT_API_KEY` 使用同一个 FastGPT OpenAPI key。

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
