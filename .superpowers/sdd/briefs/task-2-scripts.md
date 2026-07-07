# Task 2: 脚本管理模块

**目标**: 创建 scripts 表的完整 CRUD 实现

## 数据库表 (schema.sql)
```sql
CREATE TABLE IF NOT EXISTS scripts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    category VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## 需要创建的文件

### 1. entity/Script.java
```java
@Data
public class Script {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 2. mapper/ScriptMapper.java
- @Mapper, 注解SQL
- findByUserId(Long userId) — WHERE user_id = #{userId} ORDER BY created_at DESC
- findByUserIdAndCategory(@Param userId, @Param category) — 支持分类筛选
- findById(Long id)
- insert(Script) — @Options(useGeneratedKeys=true, keyProperty="id")
- update(Script) — SET title/content/category/updated_at
- delete(Long id)

### 3. service/ScriptService.java
- getScriptList(userId, category): category为空→findByUserId, 不为空→findByUserIdAndCategory
- getScriptById(id)
- createScript(script): 设 createdAt/updatedAt=now()，调用 insert
- updateScript(script): 设 updatedAt=now()，调用 update
- deleteScript(id)

### 4. controller/ScriptsController.java（完善现有）
- GET /api/scripts?category=xxx → {code:200, data:[Script数组]}
- GET /api/scripts/{id} → {code:200, data:Script}
- POST /api/scripts → {code:200, message:"脚本创建成功", data:script}
- PUT /api/scripts/{id} → {code:200, message:"脚本更新成功"}
- DELETE /api/scripts/{id} → {code:200, message:"脚本删除成功"}
- 从 request.getAttribute("userId") 获取用户ID

## 参考模式
- Mapper: `/home/ldl/桌面/claude/小学期项目/taffy/backend-main/.../mapper/VoiceModelMapper.java`
- Service: `/home/ldl/桌面/claude/小学期项目/taffy/backend-main/.../service/VoiceModelService.java`

## 全局约束
- com.taffy 包
- Map<String,Object> 返回 {code, data, message}
- MyBatis 注解SQL
- Spring Boot 3.2 / jakarta.servlet
