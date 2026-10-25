# crm-api

摄影培训机构顾问学员管理系统 · Java 后台服务。

## 技术栈

- Java 21
- Spring Boot 3.3
- Spring Security（Bearer JWT）
- Spring Data JPA
- Flyway（tidb / prod）
- TiDB Cloud / MySQL（tidb、prod）/ H2 文件库（h2）

## 配置结构

```
src/main/resources/
├── application.yml       # 公共配置 + spring.profiles.active 环境切换
├── application-tidb.yml  # 联调：端口 8080，TiDB/MySQL（环境变量）
├── application-h2.yml    # 本地：端口 8080，H2
├── application-prod.yml  # 生产：端口 8090，MySQL/TiDB（环境变量）
├── schema.sql            # 手工建库参考
└── db/migration/         # Flyway 迁移
```

环境由 `SPRING_PROFILES_ACTIVE` 控制（默认 `tidb`），或通过 `./start.sh tidb|h2|prod` 指定。

## 快速启动

```bash
cd crm-app/crm-api
chmod +x start.sh mvnw

# 本地密钥（勿提交）
cp crm.local.env.example crm.local.env
# 编辑 crm.local.env，填写 CRM_DB_URL / CRM_DB_USER / CRM_DB_PASSWORD

# TiDB / 联调（端口 8080）
./start.sh tidb

# 本地 H2
./start.sh h2

# 生产
export CRM_DB_URL='jdbc:mysql://host:4000/jdys-crm?...'
export CRM_DB_USER='user'
export CRM_DB_PASSWORD='password'
export CRM_JWT_SECRET='your-32-byte-secret-key!!!!!!'
./start.sh prod
```

也可直接使用 Maven Wrapper：

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=tidb
```

服务地址：

- **tidb / h2**：http://127.0.0.1:8080/api
- **prod**：http://127.0.0.1:8090/api

### 环境变量

| 变量 | 说明 |
|------|------|
| `CRM_DB_URL` | JDBC URL |
| `CRM_DB_USER` | 数据库用户名 |
| `CRM_DB_PASSWORD` | 数据库密码 |
| `CRM_JWT_SECRET` | JWT 密钥（prod 必填） |
| `CRM_CORS_ORIGINS` | 前端 Origin，逗号分隔 |
| `SERVER_PORT` | 覆盖默认端口 |
| `SPRING_PROFILES_ACTIVE` | 运行环境 |

### 端口被占用

```bash
lsof -i :8080
lsof -tiTCP:8080 -sTCP:LISTEN | xargs kill
SERVER_PORT=8081 ./start.sh tidb
```

## 主要接口

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/auth/health` | 健康检查 | 否 |
| POST | `/auth/login` | 登录 | 否 |
| GET | `/auth/me` | 当前用户 | 是 |
| GET/POST | `/users` | 账号列表 / 管理员新增账号 | 是 |
| GET/POST | `/leads` | 线索 | 是 |
| GET/POST | `/students` | 学员 | 是 |
| GET | `/checkins` | 报到名单 | 是 |
| GET/POST | `/pickups` | 接站看板 | 是 |
| GET/POST | `/dorms/**` | 宿舍地点 / 楼栋 / 楼层 / 房间 / 床位 | 是 |

### 鉴权方式

登录成功后返回 `token`，后续请求加 Header：

```
Authorization: Bearer <token>
```

### 响应格式

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

## 初始管理员

启动后若库中没有 `admin`，会自动创建：

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | 123456 | 管理员 |

个人账号由管理员在「系统管理 / 账号管理」里开通。

## 项目结构

```
src/main/java/com/brother/crm/
├── auth/          # 登录、JWT、鉴权过滤器
├── user/          # 账号管理
├── lead/          # 意向线索
├── student/       # 学员、报到
├── travel/        # 行程接站
├── dorm/          # 宿舍
├── entity/        # JPA 实体
├── repository/    # 数据访问
├── config/        # 安全、CORS、种子数据
└── common/        # 统一响应、异常处理
```

## 前端联调

网页默认 `http://127.0.0.1:5180`，开发代理 `/api` → `8080`。  
小程序 `BASE_URL` 指向 `http://127.0.0.1:8080/api`。

## H2 控制台（仅 h2 环境）

`./start.sh h2` 后访问：http://127.0.0.1:8080/api/h2

- JDBC URL: `jdbc:h2:file:./data/crm`
- 用户名: `sa`
- 密码: （空）

## 数据库

- tidb：Hibernate `ddl-auto: update`（TiDB Serverless 下暂不跑 Flyway 锁）
- prod：Flyway + Hibernate `ddl-auto: validate`
- 手工建库参考：`src/main/resources/schema.sql`
- 迁移脚本目录：`src/main/resources/db/migration/`
