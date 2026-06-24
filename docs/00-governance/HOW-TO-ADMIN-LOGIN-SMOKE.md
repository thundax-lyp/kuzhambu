# HOW-TO Admin Login Smoke

本文档用于本地调试 admin API 时快速取得可用账号、密码和验证码，避免每次重新排查认证链路。

## 适用范围

- 基于仓库根目录 `.env.example` 复制得到的本地 `dev.env` 环境。
- `kuzhambu-servers/starter/kuzhambu-admin-starter` 启动的 admin API。
- 仅用于开发和冒烟测试，不用于生产环境。

## 本地环境准备

如本地还没有 `dev.env`，先在仓库根目录创建：

```sh
cp .env.example dev.env
```

## 默认账号来源

默认账号来自 [`db/data/system.sql`](../../../db/data/system.sql)：

- `admin` / `admin`
- `developer` / `Q1w2e3r$`

说明：

- `system_auth_principal_identity.identity_value` 是登录名。
- `system_auth_principal_credential.credential_value` 是密码凭据；`{noop}admin` 表示本地明文占位密码为 `admin`。
- `system.sql` 中注释已说明这些都是占位凭据，生产使用前必须轮换。
- 优先使用 `developer` 做日常调试；需要全量后台权限时再使用 `admin`。

## 从 dev.env 查询账号

```sh
set -a
source dev.env
set +a

mysql --protocol=TCP \
  -h"$MYSQL_HOST" \
  -P"$MYSQL_PORT" \
  -u"$MYSQL_USER" \
  -p"$MYSQL_PASSWORD" \
  "$MYSQL_DATABASE" \
  -e "
SELECT
  i.identity_value AS login_name,
  c.credential_value,
  i.status AS identity_status,
  c.status AS credential_status,
  c.need_change_password
FROM system_auth_principal_identity i
JOIN system_auth_principal_credential c ON c.identity_id = i.id
WHERE i.identity_type = 'USER_ACCOUNT'
ORDER BY i.id
LIMIT 10;"
```

可登录账号必须同时满足：

- `identity_status = ENABLED`
- `credential_status = ACTIVE`

## 验证码

本地 `dev.env` 已启用验证码白名单：

```sh
KUZHAMBU_AUTH_CAPTCHA_WHITELIST_ENABLED=true
KUZHAMBU_AUTH_CAPTCHA_WHITELIST_VALUES=6666
```

因此本地冒烟测试可直接使用验证码 `6666`。如果关闭白名单，需要先创建登录前会话，再访问验证码图片：

```text
GET /kuzhambu-admin-api/api/auth/captcha?loginToken={loginToken}&width=150&height=40
```

## 登录流程

admin 登录不是直接提交明文密码。流程是：

1. `POST /kuzhambu-admin-api/api/auth/session/pre-auth-session`
2. 从响应 `data` 读取 `loginToken`、`refreshToken`、`publicKey`
3. 使用 `publicKey` 对明文密码做 SM2 加密，模式为 `0`
4. `POST /kuzhambu-admin-api/api/auth/session/login`

登录请求字段是：

```json
{
  "loginToken": "...",
  "userName": "developer",
  "password": "...SM2 encrypted password...",
  "captcha": "6666"
}
```

注意字段名是 `userName`，不是 `username`。

## curl 冒烟示例

先启动 admin starter：

```sh
set -a
source dev.env
set +a

cd kuzhambu-servers
mvn -pl starter/kuzhambu-admin-starter -am -DskipTests install
cd starter/kuzhambu-admin-starter
mvn spring-boot:run
```

另开终端创建登录前会话：

```sh
PRE_AUTH_JSON=$(curl -fsS -X POST \
  http://127.0.0.1:20010/kuzhambu-admin-api/api/auth/session/pre-auth-session \
  -H 'Content-Type: application/json' \
  -d '{}')

LOGIN_TOKEN=$(printf '%s' "$PRE_AUTH_JSON" | jq -r '.data.loginToken')
PUBLIC_KEY=$(printf '%s' "$PRE_AUTH_JSON" | jq -r '.data.publicKey')
```

使用 admin-web 同款 `sm-crypto` 加密密码：

```sh
cd kuzhambu-apps/admin-web
PLAIN_PASSWORD='Q1w2e3r$'
ENCRYPTED_PASSWORD=$(PUBLIC_KEY="$PUBLIC_KEY" PLAIN_PASSWORD="$PLAIN_PASSWORD" \
  node -e 'const { sm2 } = require("sm-crypto"); console.log(sm2.doEncrypt(process.env.PLAIN_PASSWORD, process.env.PUBLIC_KEY, 0));')
```

提交登录：

```sh
curl -fsS -X POST \
  http://127.0.0.1:20010/kuzhambu-admin-api/api/auth/session/login \
  -H 'Content-Type: application/json' \
  -d "{
    \"loginToken\":\"$LOGIN_TOKEN\",
    \"userName\":\"developer\",
    \"password\":\"$ENCRYPTED_PASSWORD\",
    \"captcha\":\"6666\"
  }"
```

成功响应的 `data.token` 是后续 admin API 的 `Access-Token`。

## 常见问题

- 如果 `node -e` 找不到 `sm-crypto`，先在 `kuzhambu-apps/` 安装前端依赖，或直接通过 admin-web 登录页验证。
- 如果验证码失败，确认当前 shell 已加载 `dev.env`，并确认 starter 进程使用的是同一份环境变量。
- 如果返回用户名或密码错误，先用上面的 SQL 确认账号启用、凭据激活，并注意 `Q1w2e3r$` 在 shell 中必须用单引号保护 `$`。
- 如果接口路径 404，确认 admin starter 的路径前缀是 `/kuzhambu-admin-api/api/...`。
