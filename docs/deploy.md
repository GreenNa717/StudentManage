# 部署与本地启动

## 环境要求

- `Java 21`
- `Node.js 20+`
- `MySQL 8.x`
- `npm`

## 数据库初始化

1. 创建本地数据库并导入 [SQLFile/init.sql](../SQLFile/init.sql)。
2. 默认库名为 `student_manage`。

## 后端启动

后端位于 `Backend/`，默认读取以下环境变量：

- `STUDENT_MANAGE_DB_URL`
- `STUDENT_MANAGE_DB_USERNAME`
- `STUDENT_MANAGE_DB_PASSWORD`
- `STUDENT_MANAGE_JWT_SECRET`
- `STUDENT_MANAGE_JWT_EXPIRATION`
- `STUDENT_MANAGE_LOG_LEVEL`

PowerShell 示例：

```powershell
$env:STUDENT_MANAGE_DB_URL="jdbc:mysql://localhost:3306/student_manage?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
$env:STUDENT_MANAGE_DB_USERNAME="root"
$env:STUDENT_MANAGE_DB_PASSWORD="你的数据库密码"
$env:STUDENT_MANAGE_JWT_SECRET="请替换为足够长的随机密钥"
cd Backend
.\mvnw.cmd spring-boot:run
```

启动后接口地址为 `http://localhost:8080`。

## 前端启动

```powershell
cd Frontend
npm install
npm run dev
```

默认访问地址为 `http://localhost:5173`。

## 构建与验证

```powershell
cd Backend
.\mvnw.cmd test

cd ..\Frontend
npm run lint
npm run build
```

## 生产部署建议

- 数据库密码与 JWT 密钥必须通过环境变量注入，不要写回仓库。
- Nginx 或同类反向代理统一转发前端静态资源和 `/api` 请求。
- 发布前至少执行一次完整的后端测试与前端构建。
