# Task 2: 脚本管理模块 — 完成报告

## 目标
实现 scripts 表的完整 CRUD (entity / mapper / service / controller)

## 创建/修改文件列表

| 文件 | 状态 | 说明 |
|------|------|------|
| `backend-extended/src/main/java/com/taffy/entity/Script.java` | DONE | 新建实体类，Lombok @Data，含 id/userId/title/content/category/createdAt/updatedAt |
| `backend-extended/src/main/java/com/taffy/mapper/ScriptMapper.java` | DONE | 新建 MyBatis @Mapper 接口，注解 SQL，6个方法 |
| `backend-extended/src/main/java/com/taffy/service/ScriptService.java` | DONE | 新建 Service，5个业务方法，自动管理时间戳 |
| `backend-extended/src/main/java/com/taffy/controller/ScriptsController.java` | DONE | 完善现有控制器，注入 ScriptService，实现完整 REST API |

## 实现详情

### entity/Script.java
- 包路径: com.taffy.entity
- 字段: id, userId, title, content, category, createdAt, updatedAt
- 与 schema.sql 中 scripts 表字段完全对齐

### mapper/ScriptMapper.java
- `findByUserId(Long userId)` — 按用户查询，按创建时间倒序
- `findByUserIdAndCategory(@Param Long userId, @Param String category)` — 按用户+分类筛选
- `findById(Long id)` — 按主键查询
- `insert(Script script)` — 插入，useGeneratedKeys=true
- `update(Script script)` — 更新 title/content/category/updated_at
- `delete(Long id)` — 物理删除

### service/ScriptService.java
- `getScriptList(userId, category)` — category 为空/空字符串时查询全部，否则按分类筛选
- `getScriptById(id)` — 返回 Script 或 null
- `createScript(script)` — 设置 createdAt/updatedAt，插入并返回带 id 的实体
- `updateScript(script)` — 更新 updatedAt，调用 mapper 更新
- `deleteScript(id)` — 调用 mapper 删除

### controller/ScriptsController.java
- `GET /api/scripts?category=xxx` — 返回 {code:200, data:[Script数组]}
- `GET /api/scripts/{id}` — 存在返回 200+data，不存在返回 404
- `POST /api/scripts` — 从 request.getAttribute("userId") 获取用户ID，返回 {code:200, message:"脚本创建成功", data:script}
- `PUT /api/scripts/{id}` — 返回 {code:200, message:"脚本更新成功"}
- `DELETE /api/scripts/{id}` — 返回 {code:200, message:"脚本删除成功"}

## 外部依赖
- 表 `scripts` 已在 `backend-main/src/main/resources/schema.sql` 中定义，无需修改
- mybatis-spring-boot-starter、lombok、jakarta.servlet 均在 pom.xml 中已依赖

## 构建验证
本地环境无 Maven/Java 工具链，无法执行 `mvn compile`，但所有文件语法、包引用、命名规范均与项目中已完成的 HelpArticle/HelpService/HelpController 模式一致。

## 最终状态
**DONE**
