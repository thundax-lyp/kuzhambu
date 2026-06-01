# sandwish-admin-web

Kuzhambu 后台管理前端工程，对接 `kuzhambu-admin-api`。

## 技术栈

- React
- Vite
- TypeScript
- Ant Design
- @ant-design/icons
- react-router-dom
- TanStack Query
- Vitest
- React Testing Library

## 开发命令

```bash
npm install
npm run dev
```

```bash
npm run lint
npm run format:check
npm run test
npm run build
```

默认开发服务端口为 `5173`，后台接口代理目标通过 `.env` 中的 `VITE_ADMIN_API_BASE_URL` 配置。

## 路由规划

```text
/dashboard
/login
/system/users
/system/departments
/system/roles
/system/menus
/system/dictionaries
/system/logs
/storage/objects
```

## 目录说明

```text
public/
  kuzhambu-logo.svg    产品 Logo
src/
  App.tsx              后台管理端应用壳
  main.tsx             React 入口
  layouts/             后台布局
  router/              路由配置
  query/               服务端状态与请求缓存配置
  pages/               页面入口
  assets/              全局样式等静态资源
```
