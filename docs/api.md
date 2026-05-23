# 接口摘要

所有接口统一以 `/api` 开头，返回结构如下：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 认证接口

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/auth/login` | 登录，返回 token、用户名、角色、关联业务 ID |
| `GET` | `/api/auth/info` | 获取当前用户信息 |
| `PUT` | `/api/auth/password` | 修改当前用户密码 |

## 仪表盘接口

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/api/dashboard/overview` | 统计卡片数据，按当前角色自动收敛范围 |
| `GET` | `/api/dashboard/department-students` | 院系学生分布，教师/学生仅返回关联院系 |
| `GET` | `/api/dashboard/score-distribution` | 成绩分布，按当前角色自动收敛范围 |

## 业务接口

| 模块 | 列表 | 详情 | 新增 | 修改 | 删除 |
|---|---|---|---|---|---|
| 院系 | `GET /api/departments` | `GET /api/departments/{id}` | `POST /api/departments` | `PUT /api/departments/{id}` | `DELETE /api/departments/{id}` |
| 专业 | `GET /api/majors` | `GET /api/majors/{id}` | `POST /api/majors` | `PUT /api/majors/{id}` | `DELETE /api/majors/{id}` |
| 班级 | `GET /api/classes` | `GET /api/classes/{id}` | `POST /api/classes` | `PUT /api/classes/{id}` | `DELETE /api/classes/{id}` |
| 学生 | `GET /api/students` | `GET /api/students/{id}` | `POST /api/students` | `PUT /api/students/{id}` | `DELETE /api/students/{id}` |
| 教师 | `GET /api/teachers` | `GET /api/teachers/{id}` | `POST /api/teachers` | `PUT /api/teachers/{id}` | `DELETE /api/teachers/{id}` |
| 课程 | `GET /api/courses` | `GET /api/courses/{id}` | `POST /api/courses` | `PUT /api/courses/{id}` | `DELETE /api/courses/{id}` |
| 成绩 | `GET /api/scores` | `GET /api/scores/{id}` | `POST /api/scores` | `PUT /api/scores/{id}` | `DELETE /api/scores/{id}` |

## 成绩模块说明

- `GET /api/scores/statistics`
  用于查询课程成绩统计。

- 成绩写接口会额外校验：
  - 成绩范围 `0-100`
  - 学生和课程必须存在
  - 同一学生、同一课程、同一学期成绩唯一
  - 教师只能维护本人授课课程的成绩
