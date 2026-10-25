# XVI・搭（个人电子衣橱 · 电子橱窗微信小程序）

> 把每日穿搭像橱窗一样陈列：拍照建衣橱 → 搭配 → 打卡 → 按天气与风格推荐。

「XVI・搭」是一个个人电子衣橱管理微信小程序，核心概念是**电子橱窗**——首页以大图陈列的方式展示你的"今日穿搭"，让衣橱管理变成一种每日穿搭打卡的仪式感。

## 功能模块

| 模块 | 说明 |
|---|---|
| 衣物管理 | 拍照/相册上传衣物图片，打上分类/颜色/季节/品牌标签，支持搜索、筛选与换季归档 |
| 电子橱窗 / 穿搭日历 | 每日穿搭打卡，月历回看历史穿搭，首页"今日橱窗"陈列当日搭配 |
| 搭配器 | 从衣橱选上装+下装+鞋+配饰组成一套方案，可复用、可收藏 |
| 天气/场合推荐 | 根据实时天气与场合（通勤/休闲/运动/约会）从衣橱推荐单品组合 |
| 身材/风格推荐 | 结合身材特征与风格偏好的规则化推荐 |
| 个人中心 | 衣橱统计（分类占比、季节分布、穿着频次 Top、闲置提醒） |

## 技术栈

- **前端**：微信小程序原生框架（WXML / WXSS / JS）
- **后端**：Spring Boot 3.x（JDK 21）+ MyBatis-Plus + MySQL 8
- **图片存储**：服务端本地磁盘 + 静态资源映射（`FileStorageService` 接口化，可切换对象存储）
- **认证**：微信 `wx.login` + JWT
- **天气数据**：和风天气 API（可选，未配置 key 时自动降级为按季节推荐）

## 快速启动

1. 建库：执行 `db/schema.sql`（MySQL 8）
2. 配置：复制/编辑 `server/src/main/resources/application-local.yml`，填入数据库账号密码
3. 启动后端：`cd server && mvn spring-boot:run`（默认端口 8080）
4. 前端：用微信开发者工具打开 `miniprogram/` 目录，在「详情 → 本地设置」勾选**不校验合法域名**，并在 `project.config.json` 填入自己的 AppID

详细文档见 `docs/`。

## 原创声明

本项目为《开源框架应用》课程独立完成的原创作品。开发过程中仅参考了以下开源项目的**产品功能思路**（衣物数字化管理、试衣间搭配、穿搭日历、按天气推荐等），**全部代码为本人独立编写，未复制、未引用任何上述项目的源码**：

- https://github.com/czpGhost/AI-Virtual-Wardrobe
- https://github.com/SummitXY/closet-manager
- https://github.com/EXHIT01/wearwise
- https://github.com/ayrwy/outfit-assistant-miniprogram

本项目基于 MIT 协议开源，仅供学习交流使用。

## License

[MIT](./LICENSE) © 2026 窦雅涵
