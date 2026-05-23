# 学生管理系统

基于 `Spring Boot 3 + MyBatis-Plus + Spring Security + JWT` 和 `Vue 3 + Vite + Element Plus` 的前后端分离学生管理系统，覆盖院系、专业、班级、学生、教师、课程、成绩、登录认证与仪表盘统计。

## 当前状态

- 项目已经可以作为本地演示版运行，具备数据库初始化、登录认证、角色菜单、基础资料维护、成绩管理和统计展示能力。
- 后端已具备统一返回结构、全局异常处理、JWT 认证、接口级角色权限和部分数据范围控制。
- 前端已具备登录页、主布局、角色菜单、主题切换、仪表盘、个人资料与多个 CRUD 页面。
- 当前更接近“可演示、可继续开发”的阶段，还不是完整的生产可用版本。

## 功能总览

### 已实现功能

- 登录认证：用户名密码登录、JWT 鉴权、获取当前用户资料、修改本人密码。
- 角色体系：支持 `ADMIN`、`TEACHER`、`STUDENT` 三类角色。
- 后端数据模块：院系、专业、班级、教师、学生、课程、成绩、系统用户的分页查询、详情、新增、修改、删除接口。
- 前端业务页面：院系管理、专业管理、班级管理、教师管理、学生管理、课程管理、成绩管理、用户管理、首页仪表盘、个人资料。
- 成绩统计：支持成绩分布统计和按课程聚合的成绩统计。
- 数据保护：学生只能看本人信息和成绩；教师只能看本人课程，并且只能维护本人课程成绩。
- 用户管理：管理员可管理系统账号、启用状态、关联教师/学生档案，并可重置密码。
- 基础约束校验：学号唯一、工号唯一、课程号唯一、成绩范围校验、课程/院系/专业/班级删除前关联校验。

### 当前未实现或未完善

- 没有导入导出、批量操作、操作日志、消息通知、选课、考勤等增强能力。
- 没有刷新令牌、注销接口黑名单、审计字段自动追踪等更完整的安全能力。
- 没有 403 / 404 独立错误页，也没有完整的前端深层路由异常处理。
- 自动化测试目前集中在关键权限逻辑，尚未覆盖完整接口联调和前端交互。

## 当前阶段判断

- 适合：课程作业、毕业设计雏形、作品集演示、继续迭代的基础项目。
- 不适合：未经补强直接投入生产环境或多人协作长期维护。

## 技术栈

- 后端：`Java 21`、`Spring Boot 3.4`、`Spring Security`、`MyBatis-Plus`、`MySQL`
- 前端：`Vue 3`、`TypeScript`、`Vite`、`Pinia`、`Vue Router`、`Element Plus`、`ECharts`

## 仓库结构

```text
StudentManage/
├── Backend/      # Spring Boot 后端
├── Frontend/     # Vue 3 前端
├── SQLFile/      # 数据库初始化脚本
├── docs/         # 启动、权限、接口、测试文档
└── Document/     # 项目规划文档
```

## 快速开始

### 1. 初始化数据库

执行 [SQLFile/init.sql](./SQLFile/init.sql) 创建库表并导入演示数据。

### 2. 启动后端

默认端口 `8080`。建议通过环境变量注入数据库和 JWT 配置：

```powershell
$env:STUDENT_MANAGE_DB_URL="jdbc:mysql://localhost:3306/student_manage?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
$env:STUDENT_MANAGE_DB_USERNAME="root"
$env:STUDENT_MANAGE_DB_PASSWORD="你的数据库密码"
$env:STUDENT_MANAGE_JWT_SECRET="替换成足够长的随机密钥"
cd Backend
.\mvnw.cmd spring-boot:run
```

### 3. 启动前端

```powershell
cd Frontend
npm install
npm run dev
```

前端默认访问 `http://localhost:5173`。

## 演示账号

初始化脚本内置以下账号，默认密码均为 `123456`：

- 管理员：`admin`
- 教师：`liuzhiqiang`、`chenxiaodong`
- 学生：`wanghaoran`、`lisiqi`

## 常用命令

```powershell
# 后端测试
cd Backend
.\mvnw.cmd test

# 前端静态检查
cd Frontend
npm run lint

# 前端生产构建
cd Frontend
npm run build
```

## 文档索引

- [部署说明](./docs/deploy.md)
- [当前状态说明](./docs/current-status.md)
- [接口摘要](./docs/api.md)
- [权限说明](./docs/permission.md)
- [测试与验收](./docs/test-cases.md)
- [项目规划](./Document/学生管理系统_优化版项目规划.md)
