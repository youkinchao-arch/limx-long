# 虹桥试验室 LIMS / Hongqiao Lab LIMS

实验室信息管理系统（LIMS），满足 ISO/IEC 17025 体系资源管理要求。
Laboratory Information Management System for the Hongqiao testing lab.

> 本仓库当前为**第一阶段：搭建可运行基座 + 铺出全部模块（核心模块已可用，其余为可扩展的 CRUD 占位）**。
> Stage 1: a runnable foundation with all modules laid out — core modules are functional, the rest are extensible CRUD scaffolds.

## 技术栈 / Tech Stack

- **前端 Frontend**: React 18 + TypeScript + Vite + Ant Design 5, react-router, i18next（中英双语）
- **后端 Backend**: Java 17 + Spring Boot 3.3 (Web / Security / Data JPA) + PostgreSQL, Flyway 数据库迁移, JWT 认证 (jjwt), 基于角色的权限控制 (RBAC), ZXing 设备二维码, springdoc-openapi (Swagger UI)
- **部署 Deploy**: Docker Compose（Postgres + backend + nginx 前端）

## 功能模块 / Modules

| 模块 Module | 状态 Status | 说明 |
|------|------|------|
| 运营看板 Dashboard | ✔ 可用 | 资源统计、到期/预警提醒、设备状态分布 |
| 人员管理 Personnel | ✔ 可用 | 组织架构、人员台账、培训/考核记录、资质到期提醒 |
| 设备管理 Equipment | ✔ 可用 | 设备台账、唯一标识二维码、校准计划与周期提醒、状态管理 |
| 资源排程 Resource | ◐ CRUD 占位 | 资源预约记录（人员/设备/物资），优先级与冲突状态 |
| 仓库物资 Warehouse | ◐ CRUD 占位 | 供应商、物资台账（多级库房、安全库存、有效期） |
| 文件管理 Documents | ◐ CRUD 占位 | 标识/批准/发布/变更/废止状态流转 |
| 环境管理 Environment | ◐ CRUD 占位 | 温湿度记录、范围监控告警 |
| 方法管理 Methods | ◐ CRUD 占位 | 检测方法、限值标准、方法证实状态 |
| 报告管理 Reports | ◐ CRUD 占位 | 多模板报告、审批/归档状态 |
| 系统管理 System | ✔ 可用 | 用户、角色、权限分配 |

✔ 完整业务逻辑；◐ 通用增删改查 + 搜索分页，按需逐步扩展专属逻辑。

## 本地开发 / Local Development

### 后端 Backend

```bash
cd backend
# 默认连接 jdbc:postgresql://localhost:5432/lims（lims/lims），可用环境变量覆盖
# 见 .env.example：SPRING_DATASOURCE_URL / SECRET_KEY / CORS_ORIGINS 等
mvn spring-boot:run
```

首次启动会自动运行 Flyway 迁移建表，并创建超级管理员 `admin / admin123`（可用环境变量修改）。
API 文档 (Swagger UI)：http://localhost:8000/api/docs

### 前端 Frontend

```bash
cd frontend
npm install
npm run dev   # http://localhost:5173 （已代理 /api 到 http://localhost:8000）
```

## 一键启动 / Docker Compose

```bash
docker compose up --build
# 前端 http://localhost:3000 ，后端 http://localhost:8000
```

## 测试与检查 / Tests & Checks

```bash
# 后端（JUnit 5 + MockMvc + H2 内存库）
cd backend && mvn verify
# 前端
cd frontend && npm run lint && npm run build
```

## 默认账号 / Default Account

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin  | admin123 | 系统管理员（全部权限）|

内置角色：`admin`（全部）、`manager`（读写全部模块）、`operator`（只读）。

## 目录结构 / Layout

```
backend/
  src/main/java/com/hongqiao/lims/
    config/     # 安全、CORS、数据初始化、配置属性
    security/   # JWT 过滤器、权限校验
    common/     # 基础实体、分页、异常处理、通用 CRUD 基类
    user/       # 用户/角色、认证控制器
    personnel/  # 人员、部门、培训记录
    equipment/  # 设备 + 二维码
    modules/    # 仓库/文件/环境/方法/报告/资源 CRUD
    dashboard/  # 运营看板汇总
  src/main/resources/db/migration/  # Flyway 迁移 (V1__init.sql)
  src/test/java/  # JUnit 5 + MockMvc 测试
frontend/
  src/
    api/        # axios 客户端
    auth/       # 登录态与权限
    components/ # 布局、通用 CRUD 表格
    i18n/       # 中英文资源
    pages/      # 各模块页面
```
