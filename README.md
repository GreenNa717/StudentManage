# 学生管理系统

基于 `Spring Boot 3 + MyBatis-Plus + Spring Security + JWT` 和 `Vue 3 + Vite + Element Plus` 的前后端分离学生管理系统，覆盖院系、专业、班级、学生、教师、课程、成绩、登录认证与仪表盘统计。

## 当前状态

- 后端提供基础 CRUD、JWT 登录认证、统一返回结构与权限校验。
- 前端提供登录页、仪表盘、基础资料管理页和通用 CRUD 组件。
- 本次整理补齐了成绩/课程/学生数据范围控制、前端角色行为修正、测试用例与项目文档。

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
- [接口摘要](./docs/api.md)
- [权限说明](./docs/permission.md)
- [测试与验收](./docs/test-cases.md)
- [项目规划](./Document/学生管理系统_优化版项目规划.md)
