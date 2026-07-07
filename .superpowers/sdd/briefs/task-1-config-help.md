# Task 1: 基础设施 + 帮助中心模块

**目标**: 创建JWT认证配置 + 帮助中心完整实现

## 需要创建的文件

### 1. config/JwtUtil.java
- 从 application.yml 读取 jwt.secret 和 jwt.expiration
- 方法: getUserIdFromToken(token), validateToken(token)
- 参考 backend-main/.../config/JwtUtil.java（完全一样的逻辑）
- 使用 io.jsonwebtoken，Keys.hmacShaKeyFor

### 2. config/AuthInterceptor.java
- 实现 HandlerInterceptor
- preHandle: 校验 Authorization: Bearer <token>，设置 request.setAttribute("userId", userId)
- OPTIONS直接放行；无token或token无效返回401 JSON

### 3. config/WebConfig.java
- 实现 WebMvcConfigurer
- 注册 AuthInterceptor，拦截 /api/**
- 排除: /api/help/articles, /api/help/articles/*, /api/help/search, /api/feedback/rating/*
- CORS 配置: allowedOriginPatterns("*")

### 4. entity/HelpArticle.java
```java
@Data
public class HelpArticle {
    private Long id;
    private String title;
    private String content;
    private String category;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
```

### 5. mapper/HelpArticleMapper.java
- @Mapper, 注解SQL
- findAll() — SELECT * FROM help_articles ORDER BY sort_order, created_at DESC
- findByCategory(category) — WHERE category = #{category}
- findById(id)
- search(keyword) — WHERE title LIKE '%' || #{keyword} || '%' OR content LIKE '%' || #{keyword} || '%'
- insert(HelpArticle) — @Options(useGeneratedKeys=true, keyProperty="id")

### 6. service/HelpService.java
- @Service, @Autowired HelpArticleMapper
- getArticles(category): category为空查全部，否则按category筛选
- getArticleById(id)
- searchArticles(keyword)

### 7. controller/HelpController.java（完善现有）
- GET /api/help/articles?category=xxx → {code:200, data:[...]}
- GET /api/help/articles/{id} → {code:200, data:{...}} or {code:404}
- GET /api/help/search?keyword=xxx → {code:200, data:[...]}
- 注入 HelpService

### 8. 修改 application.yml
- 添加 jwt.secret: taffy-live-assistant-jwt-secret-key-2026
- 添加 jwt.expiration: 86400000

## 全局约束
- 包名 com.taffy
- 返回格式 Map<String,Object>: {code:200, data:..., message:...}
- MyBatis 注解SQL，不用XML
- LocalDateTime 用 LocalDateTime.now()
- Spring Boot 3.2，jakarta.servlet
- 所有文件在 backend-extended/src/main/java/com/taffy/ 下
