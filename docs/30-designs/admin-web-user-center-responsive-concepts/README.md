# 用户中心响应式母本拆分

本目录保存用户中心 4 类屏幕母本及拆分图，用于后续形成 admin-web UI 规范。

## 目录

- `pc/`: PC 档，`>= 1440px`
- `laptop/`: 笔记本档，`1024px - 1439px`
- `pad/`: PAD 档，`768px - 1023px`
- `phone/`: 手机档，`< 768px`

每个子目录包含：

- `user-center-*-mother.png`: 原始母图
- `user-center-*-list.png`: 用户列表页面
- `user-center-*-edit-detail.png`: 编辑 / 详情页
- `user-center-*-activity.png`: 用户活动页

## 数据一致性

拆图时保留每档母图内部的数据口径，不跨屏幕强行统一姓名、时间或部门。

- PC: `王砚之 / wang.yz / 整理研究院 / 系统管理员`
- 笔记本: `张三 / zhangsan / 管理层 / 系统管理员`
- PAD: `张文远 / zhang.wy / 管理层 / 信息管理部`
- 手机: `王景行 / jingxing.wang / 管理层 / 系统管理员、知识治理员`

完整裁剪坐标、图像尺寸和数据锚点见 `manifest.json`。
