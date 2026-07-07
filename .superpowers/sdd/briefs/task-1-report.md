# Task 1 Report: 基础设施 + 帮助中心模块

## 状态: DONE

## 创建的文件列表

| # | 文件 | 操作 | 说明 |
|---|------|------|------|
| 1 | `config/JwtUtil.java` | 新建 | JWT工具类，从application.yml读取secret/expiration，提供getUserIdFromToken、validateToken方法 |
| 2 | `config/AuthInterceptor.java` | 新建 | 认证拦截器，检查Authorization: Bearer <token>，OPTIONS放行，无效token返回401 JSON |
| 3 | `config/WebConfig.java` | 新建 | Web配置，注册拦截器到/api/**，排除帮助中心和评价评分接口；CORS允许所有来源 |
| 4 | `entity/HelpArticle.java` | 新建 | 帮助文章实体，包含id/title/content/category/sortOrder/createdAt，使用Lombok @Data |
| 5 | `mapper/HelpArticleMapper.java` | 新建 | MyBatis注解SQL映射，提供findAll/findByCategory/findById/search/insert |
| 6 | `service/HelpService.java` | 新建 | 业务层，封装getArticles/getArticleById/searchArticles |
| 7 | `controller/HelpController.java` | 修改 | 注入HelpService，实现三个端点的真实数据返回逻辑（含404处理） |
| 8 | `application.yml` | 修改 | 新增jwt.secret和jwt.expiration配置项 |
| 9 | `pom.xml` | 修改 | 新增jjwt-api/jjwt-impl/jjwt-jackson三个JWT依赖（版本0.12.3） |

## 关键实现说明

### JWT认证链路
- `application.yml` -> `JwtUtil` (读取配置) -> `AuthInterceptor` (校验token) -> `WebConfig` (注册拦截器)
- 与 backend-main 完全一致: 同样的JWT库版本(0.12.3)、同样的解析方式(Keys.hmacShaKeyFor + verifyWith)、同样的错误响应格式

### 排除认证的公开接口
- `GET /api/help/articles` — 文章列表(支持?category=可选)
- `GET /api/help/articles/*` — 文章详情
- `GET /api/help/search?keyword=xxx` — 文章搜索
- `GET /api/feedback/rating/*` — 评价评分查询

### 数据库
- `help_articles` 表结构已在 backend-main 的 schema.sql 中定义（两个服务共享同一个SQLite数据库: `./data/taffy.db`）
- MyBatis自动驼峰映射: `sort_order` -> `sortOrder`, `created_at` -> `createdAt`

### 返回格式
- 成功: `{code:200, data: [...]}`
- 文章不存在: `{code:404, message: "文章不存在", data: null}`
- 认证失败: `{code:401, message: "..."}` (由拦截器直接返回)

## 遇到的问题

无

## 备注

- 未配置schema.sql初始化 — help_articles表由backend-main的schema.sql负责创建，两个服务共享同一SQLite文件
- 未添加spring-boot-starter-security依赖 — 认证仅通过自定义HandlerInterceptor实现，无需Spring Security
