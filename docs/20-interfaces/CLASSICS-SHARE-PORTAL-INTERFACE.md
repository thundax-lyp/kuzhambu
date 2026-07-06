# Classics Share Portal Interface

## Purpose

本文档固定古籍公开分享 Portal API 的稳定响应契约。Portal 入口不需要登录。

## Routes

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/portal/classics/shares` | 公开分享列表 |
| `GET` | `/api/portal/classics/shares/{shareToken}` | 公开分享详情；私有分享返回登录引导 |
| `GET` | `/api/portal/classics/private-shares/{shareToken}` | 私有分享详情 |
| `GET` | `/api/portal/classics/private-shares/{shareToken}/resources/{storageObjectId}/content` | 私有分享资源读取 |

## ShareToken

- `shareToken` 是后端生成的公开分享短码，不是登录认证 token。
- Admin 创建分享和 Portal 分享列表可以返回 `shareToken`，用于拼接 `/share/{shareToken}`。
- 数据库同时保存 `share_token` 和 `token_hash`：`share_token` 用于公开展示和列表跳转，`token_hash` 用于详情查询索引。
- Portal 详情接收明文 `shareToken`，后端计算 hash 后查询 `token_hash`。
- 私有分享详情和资源读取必须携带后台登录态，允许创建者或具备 `classics:sharing:view` 权限的管理员访问。

## List Query

列表支持以下查询参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `contentType` | string/null | 内容分类 |
| `title` | string/null | 分享标题或内容标题快照 |
| `issuedAfter` | string/null | 分享时间下限，ISO-8601 |
| `issuedBefore` | string/null | 分享时间上限，ISO-8601 |
| `pageNo` | number/null | 页码 |
| `pageSize` | number/null | 每页数量 |

## List Response

```json
{
  "pageNo": 1,
  "pageSize": 20,
  "totalCount": 1,
  "totalPage": 1,
  "records": [
    {
      "shareLinkId": 10,
      "shareToken": "abc123_-",
      "shareTitle": "公开分享",
      "issuedAt": "2026-06-20T10:00:00.000+00:00",
      "expiresAt": null,
      "contentType": "SANCAI_ENTRY",
      "contentId": 300000003360,
      "contentVersionId": 1,
      "contentVersionNo": 1,
      "titleSnapshot": "天地",
      "contentVisibilitySnapshot": "PUBLIC",
      "targetStatus": "AVAILABLE",
      "priority": 1
    }
  ]
}
```

列表项不得返回完整 `contentSnapshotJson`，但必须返回 `shareToken`，让 Portal Web 可以从列表进入详情页。

## Detail Response

```json
{
  "title": "公开分享",
  "visibility": "PUBLIC",
  "status": "ACTIVE",
  "issuedAt": "2026-06-20T10:00:00.000+00:00",
  "expiresAt": null,
  "targets": [
    {
      "contentType": "SANCAI_ENTRY",
      "contentId": 300000003360,
      "contentVersionId": 1,
      "contentVersionNo": 1,
      "titleSnapshot": "天地",
      "contentSnapshotJson": "{\"contentType\":\"SANCAI_ENTRY\"}",
      "contentVisibilitySnapshot": "PUBLIC",
      "targetStatus": "AVAILABLE",
      "priority": 1
    }
  ]
}
```

详情响应不返回 `shareToken` 或 `tokenHash`。

私有分享通过公开详情入口访问时，不返回 `targets` 内容，只返回登录引导：

```json
{
  "visibility": "PRIVATE",
  "loginRequired": true,
  "targets": []
}
```

## Not Found Rule

Portal 侧对过期、撤销、不存在的 `shareToken` 统一返回 404，不区分原因，避免泄露分享链接存在性。
