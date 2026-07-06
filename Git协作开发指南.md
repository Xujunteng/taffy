# Git & GitHub 多人协作开发指南

> 适用项目：taffy（直播助手网站） | 团队规模：4人 | 最后更新：2026年7月7日

---

## 目录

- [一、Git 基础概念](#一git-基础概念)
- [二、Git 安装与配置](#二git-安装与配置)
- [三、Git 核心工作流程](#三git-核心工作流程)
- [四、Git 常用命令速查](#四git-常用命令速查)
- [五、分支管理策略](#五分支管理策略)
- [六、GitHub 协作工作流](#六github-协作工作流)
- [七、常见协作场景实操](#七常见协作场景实操)
- [八、冲突解决指南](#八冲突解决指南)
- [九、Git 提交规范](#九git-提交规范)
- [十、GitHub 项目管理](#十github-项目管理)
- [十一、最佳实践与注意事项](#十一最佳实践与注意事项)
- [十二、常见问题排查](#十二常见问题排查)

---

## 一、Git 基础概念

### 1.1 Git 是什么？

Git 是一个**分布式版本控制系统**，用于跟踪文件的更改，协调多人协作开发。

### 1.2 核心概念

| 概念 | 说明 |
|------|------|
| **仓库 (Repository)** | 存储代码和版本历史的地方，分为本地仓库和远程仓库（GitHub） |
| **工作区 (Working Directory)** | 你电脑上直观看到的项目文件夹，正在编辑的代码 |
| **暂存区 (Staging Area/Index)** | 临时存放准备提交的修改，通过 `git add` 添加 |
| **本地仓库 (Local Repository)** | 存储所有版本历史的本地 `.git` 目录 |
| **远程仓库 (Remote Repository)** | GitHub 上托管的仓库，团队成员共享 |
| **提交 (Commit)** | 一次代码变更的快照，有唯一标识（SHA-1哈希值） |
| **分支 (Branch)** | 独立的开发线，不同分支互不影响 |
| **合并 (Merge)** | 将一个分支的修改合并到另一个分支 |
| **拉取 (Pull)** | 从远程仓库获取最新代码并合并到本地 |
| **推送 (Push)** | 将本地提交推送到远程仓库 |
| **克隆 (Clone)** | 将远程仓库完整复制到本地 |

### 1.3 状态流转图

```
工作区（修改） --git add--> 暂存区 --git commit--> 本地仓库 --git push--> 远程仓库
     ↑                                                                    │
     └─────────────────── git pull / git fetch ◄──────────────────────────┘
```

---

## 二、Git 安装与配置

### 2.1 安装 Git

- **Windows**: 下载 [Git for Windows](https://git-scm.com/download/win) 安装
- **macOS**: `brew install git`
- **Linux**: `sudo apt install git` (Ubuntu/Debian) 或 `sudo yum install git` (CentOS)

安装完成后验证：

```bash
git --version
# 输出示例: git version 2.43.0
```

### 2.2 首次配置

```bash
# 设置用户名（提交时显示的名字，建议用真名或英文名）
git config --global user.name "你的名字"

# 设置邮箱（与GitHub注册邮箱一致）
git config --global user.email "your-email@example.com"

# 设置默认分支名为 main
git config --global init.defaultBranch main

# 查看配置
git config --list
```

### 2.3 配置 GitHub 认证

#### 方式一：SSH 密钥（推荐）

```bash
# 1. 生成 SSH 密钥（一路回车即可）
ssh-keygen -t ed25519 -C "your-email@example.com"

# 2. 复制公钥
cat ~/.ssh/id_ed25519.pub    # macOS/Linux
type %userprofile%\.ssh\id_ed25519.pub   # Windows

# 3. 打开 GitHub → Settings → SSH and GPG keys → New SSH key → 粘贴公钥

# 4. 测试连接
ssh -T git@github.com
# 成功输出: Hi username! You've successfully authenticated...
```

#### 方式二：Personal Access Token (PAT)

如果无法使用 SSH，可使用 Token 认证：

1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token → 勾选 `repo` 权限 → 生成
3. 在命令行中使用 Token 替代密码

### 2.4 克隆项目到本地

```bash
# taffy 项目的团队成员执行：
git clone git@github.com:Xujunteng/taffy.git
cd taffy
```

---

## 三、Git 核心工作流程

### 3.1 单人开发的日常循环

```bash
# 1. 拉取最新代码
git pull

# 2. 查看当前状态
git status

# 3. 将修改添加到暂存区
git add <文件名>        # 添加单个文件
git add .              # 添加所有修改

# 4. 提交到本地仓库
git commit -m "feat: 添加登录页面"

# 5. 推送到远程仓库
git push

# 6. 查看提交历史
git log --oneline
```

### 3.2 工作区状态解读

`git status` 的常见输出：

```
# 已修改但未暂存（红色）
Changes not staged for commit:
  modified:   src/App.vue

# 已暂存待提交（绿色）
Changes to be committed:
  new file:   src/views/Login.vue

# 工作区干净（无修改）
nothing to commit, working tree clean
```

---

## 四、Git 常用命令速查

### 4.1 基础操作

| 命令 | 说明 |
|------|------|
| `git init` | 初始化本地仓库 |
| `git clone <url>` | 克隆远程仓库 |
| `git status` | 查看工作区和暂存区状态 |
| `git add <file>` | 将文件加入暂存区 |
| `git add .` | 添加所有更改到暂存区 |
| `git commit -m "message"` | 提交暂存区的内容 |
| `git push` | 推送本地提交到远程 |
| `git pull` | 拉取远程更新并合并 |
| `git fetch` | 拉取远程更新但不合并 |
| `git log` | 查看提交历史 |
| `git log --oneline --graph --all` | 可视化查看所有分支历史 |

### 4.2 分支操作

| 命令 | 说明 |
|------|------|
| `git branch` | 查看本地分支列表 |
| `git branch -a` | 查看所有分支（含远程） |
| `git branch <branch-name>` | 创建新分支 |
| `git checkout <branch-name>` | 切换到指定分支 |
| `git checkout -b <branch-name>` | 创建并切换到新分支 |
| `git switch <branch-name>` | 切换分支（Git 2.23+，推荐） |
| `git switch -c <branch-name>` | 创建并切换分支（推荐） |
| `git merge <branch-name>` | 合并目标分支到当前分支 |
| `git branch -d <branch-name>` | 删除本地分支 |
| `git push origin --delete <branch-name>` | 删除远程分支 |

### 4.3 撤销与回退

| 命令 | 说明 |
|------|------|
| `git checkout -- <file>` | 撤销工作区的修改（未add的） |
| `git reset HEAD <file>` | 从暂存区移除文件（保留修改） |
| `git reset --soft HEAD~1` | 撤销最近一次commit（保留修改在暂存区） |
| `git reset --hard HEAD~1` | 撤销最近一次commit并丢弃修改（危险！） |
| `git revert <commit-hash>` | 创建一个新commit来撤销指定commit（安全） |
| `git stash` | 暂存当前修改，恢复干净工作区 |
| `git stash pop` | 恢复最近一次stash的修改 |

### 4.4 查看差异

| 命令 | 说明 |
|------|------|
| `git diff` | 查看工作区与暂存区的差异 |
| `git diff --staged` | 查看暂存区与最新commit的差异 |
| `git diff <branch1> <branch2>` | 查看两个分支的差异 |
| `git diff <commit1> <commit2> -- <file>` | 查看某文件在两个commit间的差异 |

---

## 五、分支管理策略

### 5.1 taffy 项目推荐分支模型（简化 Git Flow）

```
main ──────────────────────────────────────●──── (生产就绪代码)
  │                                         │
  └── develop ──────────────────────────●───┘    (开发主线)
        │                                │
        ├── member1/frontend ──────●─────┘        (成员1：前端)
        ├── member2/backend-main ──●───┘          (成员2：主后端)
        ├── member3/voice-ai ───────●───┘          (成员3：语音AI)
        └── member4/extended ────────●───┘          (成员4：拓展+部署)
```

### 5.2 各分支职责

| 分支 | 职责 | 保护规则 |
|------|------|----------|
| `main` | 稳定版本，可随时部署 | ✅ 禁止直接推送，仅通过 PR 合并 |
| `develop` | 开发主线，各成员合并目标 | ✅ 禁止直接推送，仅通过 PR 合并 |
| `member1/frontend` | 成员1的前端开发 | 个人分支，自由推送 |
| `member2/backend-main` | 成员2的主后端开发 | 个人分支，自由推送 |
| `member3/voice-ai` | 成员3的语音AI开发 | 个人分支，自由推送 |
| `member4/extended` | 成员4的拓展+部署开发 | 个人分支，自由推送 |

### 5.3 分支命名规范

```
# 功能分支
feature/<功能名>      例: feature/login-page

# 修复分支
fix/<问题描述>        例: fix/jwt-token-expired
bugfix/<编号>         例: bugfix/issue-12

# 热修复
hotfix/<问题描述>     例: hotfix/critical-login-bug

# 发布分支
release/<版本号>      例: release/v1.0.0

# 个人开发分支（taffy项目推荐）
<成员名>/<模块名>     例: member1/frontend
```

---

## 六、GitHub 协作工作流

### 6.1 标准协作流程（Fork + PR，适用于开源项目）

```
                      GitHub
┌─────────────────────────────────────────────┐
│  主仓库 (Xujunteng/taffy)                     │
│    ↑ PR merge                                 │
│  ┌──────────────────────────┐                │
│  │ Fork 仓库 (你的账号/taffy) │                │
│  └──────────────────────────┘                │
└─────────────────────────────────────────────┘
         ↑ git push              ↑ git clone
    ┌─────────┐             ┌─────────┐
    │ 本地仓库  │             │ 本地仓库  │
    │ (成员A)  │             │ (成员B)  │
    └─────────┘             └─────────┘
```

### 6.2 taffy 项目协作流程（同一仓库，推荐）

由于 taffy 项目成员都在同一团队，使用**单一仓库多分支**模式：

```
1. 仓库管理员创建仓库 + develop 分支
2. 每位成员从 develop 创建自己的功能分支
3. 在功能分支上开发
4. 开发完成后，在 GitHub 上提交 Pull Request（PR）到 develop
5. 其他成员 Code Review
6. 通过后合并到 develop
7. 定期将 develop 合并到 main 作为稳定版本
```

---

## 七、常见协作场景实操

### 场景一：新成员加入项目

```bash
# 1. 克隆项目
git clone git@github.com:Xujunteng/taffy.git
cd taffy

# 2. 切换到 develop 分支
git switch develop

# 3. 创建自己的功能分支（以成员1为例）
git switch -c member1/frontend

# 4. 开始开发...
# ... 写代码 ...

# 5. 首次推送自己的分支到远程
git push -u origin member1/frontend
# -u 参数会记住关联关系，下次直接 git push 即可
```

### 场景二：每日开发流程

```bash
# === 每天早上 ===
# 1. 切换到 develop 分支
git switch develop

# 2. 拉取最新代码
git pull

# 3. 将自己的功能分支更新到最新
git switch member1/frontend
git merge develop
# 如果有冲突，解决冲突（见第八节）

# === 开发过程中 ===
# 4. 写代码...

# 5. 添加修改
git add .

# 6. 提交（建议小步提交，一次功能一个提交）
git commit -m "feat(frontend): 完成登录页面UI"

# 7. 继续开发...

# 8. 再提交
git add .
git commit -m "feat(frontend): 添加登录表单验证"

# === 下班前 ===
# 9. 推送到远程（即使没完成也可以推送，做云端备份）
git push
```

### 场景三：提交 Pull Request (PR)

```bash
# 1. 确保功能分支代码已推送到 GitHub
git push

# 2. 打开 GitHub 仓库页面
#    → 点击 "Pull requests" 标签
#    → 点击 "New pull request"

# 3. 设置 PR
#    base: develop     ← 要合并到的目标分支
#    compare: member1/frontend  ← 你的功能分支

# 4. 填写 PR 标题和描述
#    标题示例: [前端] 完成登录注册页面开发
#    描述：
#    - 完成了哪些功能
#    - 测试了哪些接口
#    - 相关 Issue 编号（如有）

# 5. 点击 "Create pull request"

# 6. 通知团队成员进行 Code Review
```

### 场景四：Code Review 与修改

```bash
# 收到 Review 意见后

# 1. 直接在功能分支上修改
git switch member1/frontend

# 2. 修改代码...

# 3. 提交修改
git add .
git commit -m "fix: 根据Review意见修复XX问题"

# 4. 推送
git push

# PR 会自动更新，无需重新创建
# 在 PR 页面回复评论说明修改了哪些内容
```

### 场景五：同步上游变更

```bash
# 当 develop 分支有其他人的新合并时，同步到自己的分支

# === 方式一：merge（推荐，保留完整历史）===
git switch member1/frontend
git fetch origin
git merge origin/develop
# 解决冲突（如果有）
git push

# === 方式二：rebase（线性历史，适合个人分支）===
git switch member1/frontend
git fetch origin
git rebase origin/develop
# 解决冲突（如果有）
git push --force-with-lease  # ⚠️ 仅在个人分支使用！
```

---

## 八、冲突解决指南

### 8.1 冲突产生的原因

当两个分支修改了**同一个文件的同一行**时，Git 无法自动决定保留哪个版本，就会产生冲突。

### 8.2 冲突示例

```
<<<<<<< HEAD
console.log("这是 develop 分支的代码");
=======
console.log("这是 member1/frontend 分支的代码");
>>>>>>> member1/frontend
```

- `<<<<<<< HEAD` 到 `=======` 之间：当前分支的代码
- `=======` 到 `>>>>>>> branch-name` 之间：要合并进来的分支的代码

### 8.3 VS Code 中解决冲突（推荐）

VS Code 内置了冲突解决工具：

1. 打开有冲突的文件，会看到高亮标记
2. 点击以下选项之一：
   - **Accept Current Change** — 保留当前分支的代码
   - **Accept Incoming Change** — 采用合并分支的代码
   - **Accept Both Changes** — 两段代码都保留
   - **Compare Changes** — 对比查看差异
3. 解决完所有冲突后，保存文件
4. 执行：

```bash
git add .
git commit -m "merge: 合并 develop 分支，解决冲突"
git push
```

### 8.4 冲突预防建议

1. **频繁同步**：每天拉取 develop 最新代码并合并到自己的分支
2. **小步提交**：功能拆小，减少长时间脱离主线的风险
3. **模块清晰**：避免多人修改同一文件（taffy 项目已按模块划分目录）
4. **沟通优先**：如果要修改别人负责的文件，先沟通

---

## 九、Git 提交规范

### 9.1 提交信息格式（Conventional Commits）

```
<type>(<scope>): <subject>

<body>（可选）

<footer>（可选）
```

### 9.2 type 类型

| type | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(frontend): 添加声音上传功能` |
| `fix` | Bug 修复 | `fix(backend): 修复JWT过期判断错误` |
| `docs` | 文档修改 | `docs: 更新API接口文档` |
| `style` | 代码格式（不影响功能） | `style(frontend): 统一缩进为2空格` |
| `refactor` | 重构（不增功能，不修bug） | `refactor(backend): 提取公共认证逻辑` |
| `perf` | 性能优化 | `perf(backend): 优化数据库查询` |
| `test` | 测试相关 | `test: 添加登录接口单元测试` |
| `chore` | 构建/工具等杂项 | `chore: 更新依赖版本` |
| `ci` | CI/CD 相关 | `ci: 添加自动构建流程` |
| `merge` | 分支合并 | `merge: 合并 feature/login 到 develop` |

### 9.3 taffy 项目的 scope 建议

| scope | 对应模块 |
|-------|----------|
| `frontend` | 前端 Vue 3 |
| `backend-main` | 主后端（认证+声音管理） |
| `voice-ai` | 语音AI服务 |
| `extended` | 拓展功能 |
| `deploy` | Docker/部署 |
| `docs` | 文档 |
| `config` | 配置文件 |

### 9.4 提交信息示例

```bash
# 好的提交信息 ✅
git commit -m "feat(frontend): 完成登录页面UI和表单验证"
git commit -m "fix(backend-main): 修复注册时用户名重复未提示的问题"
git commit -m "docs: 添加Git协作开发指南"
git commit -m "refactor(voice-ai): 将TTS服务提取为独立模块"

# 不好的提交信息 ❌
git commit -m "修改"
git commit -m "update"
git commit -m "修了几个bug"
git commit -m "111"
git commit -m "下班了"
```

---

## 十、GitHub 项目管理

### 10.1 使用 Issues 跟踪任务

在 GitHub 仓库页面 → Issues 标签创建任务：

```markdown
标题: [前端] 完成 TTS 合成页面开发

描述:
## 任务描述
完成 TTS.vue 页面的功能开发，对接后端 TTS API

## 涉及接口
- POST /api/tts/convert
- GET /api/tts/history
- GET /api/tts/download/{taskId}

## 验收标准
- [ ] 输入文本可合成语音
- [ ] 语速/音调滑块可调节
- [ ] 音频可在线播放和下载
- [ ] 历史列表正确显示

标签: frontend, enhancement
指定人: @成员1
```

### 10.2 使用 Labels（标签）分类

建议设置以下标签：

| 标签 | 颜色 | 用途 |
|------|------|------|
| `frontend` | 🟢 绿色 | 前端相关 |
| `backend-main` | 🔵 蓝色 | 主后端相关 |
| `voice-ai` | 🟣 紫色 | 语音AI相关 |
| `extended` | 🟠 橙色 | 拓展功能相关 |
| `deploy` | ⚫ 黑色 | 部署相关 |
| `bug` | 🔴 红色 | Bug |
| `enhancement` | 🟡 黄色 | 功能增强 |
| `documentation` | ⚪ 灰色 | 文档 |
| `urgent` | 🔴 深红 | 紧急 |

### 10.3 使用 Projects（项目看板）

创建看板列：
- **待办 (To Do)** — 计划要做但还没开始
- **进行中 (In Progress)** — 正在开发
- **待审查 (Review)** — 已提交PR，等待Code Review
- **已完成 (Done)** — 已合并到 develop

### 10.4 关联 Issue 和 PR

在 PR 描述中使用关键字自动关闭 Issue：

```
Closes #12        # 合并后关闭 Issue #12
Fixes #15         # 合并后关闭 Issue #15
Resolves #20      # 合并后关闭 Issue #20
Ref #8            # 引用但不关闭 Issue #8
```

### 10.5 PR 模板

在项目根目录创建 `.github/pull_request_template.md`：

```markdown
## 变更描述
<!-- 简要说明做了什么变更 -->

## 变更类型
- [ ] 新功能 (feat)
- [ ] Bug修复 (fix)
- [ ] 文档更新 (docs)
- [ ] 重构 (refactor)
- [ ] 部署/配置 (deploy/chore)

## 测试情况
<!-- 描述如何测试你的变更 -->

## 关联 Issue
<!-- 例如: Closes #12 -->

## 检查清单
- [ ] 代码已自测通过
- [ ] 相关文档已更新
- [ ] 无冲突需解决
```

---

## 十一、最佳实践与注意事项

### 11.1 Do's ✅

| 实践 | 说明 |
|------|------|
| **提交前先 pull** | 每次 `commit` 前先 `pull` 最新代码，减少冲突 |
| **小步提交，频繁推送** | 一个功能点一个 commit，不要攒一大堆一起提交 |
| **写有意义的提交信息** | 遵循 Conventional Commits 规范 |
| **提交前检查变更** | 用 `git diff --staged` 确认要提交的内容 |
| **创建 .gitignore** | 排除不需要版本控制的文件 |
| **功能分支开发** | 不在 main/develop 分支上直接开发 |
| **合并前做 Code Review** | 至少 1 位成员审查代码 |
| **冲突立即解决** | 不要积压，越早解决越容易 |

### 11.2 Don'ts ❌

| 禁止事项 | 原因 |
|----------|------|
| **`git push --force` 在共享分支** | 会覆盖他人的提交，造成代码丢失 |
| **提交大文件（>50MB）** | 严重影响仓库性能，用 `.gitignore` 排除或用 Git LFS |
| **提交密钥/密码** | AccessKey、数据库密码等绝不能入库 |
| **提交编译产物** | `node_modules/`、`target/`、`.class`、`dist/` 等 |
| **提交前不测试** | 至少确保代码能编译/运行再提交 |
| **一个大PR包含太多改动** | PR 过大审查困难，拆成多个小PR |

### 11.3 .gitignore 配置

taffy 项目应包含以下 `.gitignore`：

```gitignore
# ===== IDE =====
.idea/
.vscode/
*.swp
*.swo
*~

# ===== Java =====
*.class
target/
*.jar
*.war

# ===== Node.js =====
node_modules/
dist/
.env
.env.local

# ===== 系统文件 =====
.DS_Store
Thumbs.db

# ===== 项目特定 =====
data/
*.db
*.db-journal

# ===== 密钥 =====
*.pem
*.key
application-local.yml
```

### 11.4 提交前检查清单

每次 `git commit` 前确认：

- [ ] 代码能正常编译/运行
- [ ] 没有包含调试代码（`console.log`、`System.out.println`）
- [ ] 没有包含敏感信息（密码、密钥、Token）
- [ ] 不必要的文件已在 `.gitignore` 中
- [ ] 提交信息清晰说明了"做了什么"
- [ ] 确认要提交的文件无误（`git status` + `git diff`）

---

## 十二、常见问题排查

### Q1: 推送被拒绝 `! [rejected]`

```
! [rejected]  main -> main (fetch first)
```

**原因**：远程有新的提交，本地落后了

**解决**：
```bash
git pull --rebase
# 解决冲突（如果有）
git push
```

### Q2: 提交到了错误的分支

```bash
# 1. 先不要 push！如果已 push 看 Q3

# 2. 查看刚提交的 commit hash
git log --oneline -3

# 3. 切换到正确的分支
git switch 正确的分支名

# 4. 把那个 commit 拿过来（cherry-pick）
git cherry-pick <commit-hash>

# 5. 回到错误的分支，撤销那个 commit
git switch 错误的分支名
git reset --hard HEAD~1
```

### Q3: 已经 push 到错误分支了

```bash
# 1. 按 Q2 的步骤把 commit 移到正确分支并 push

# 2. 撤销错误分支上的提交
git push origin --force-with-lease 错误的分支名
# ⚠️ 仅在确认是个人分支且无其他人基于此分支时使用！
```

### Q4: 如何放弃所有本地修改？

```bash
# ⚠️ 这会丢失所有未提交的修改！请先确认！

# 放弃工作区所有修改
git checkout .

# 同时放弃暂存区和工作区
git reset --hard HEAD

# 还会删除未跟踪的文件
git clean -fd
```

### Q5: commit 信息写错了

```bash
# 修改最近一次 commit 的信息（还没 push）
git commit --amend -m "新的提交信息"

# 如果已经 push 了：
git commit --amend -m "新的提交信息"
git push --force-with-lease
# ⚠️ 仅个人分支可用！
```

### Q6: 漏提交了文件

```bash
# 把漏掉的文件加入暂存区
git add 漏掉的文件

# 追加到上一次 commit（不改动提交信息）
git commit --amend --no-edit

# 如果已 push
git push --force-with-lease
```

### Q7: 如何恢复到之前的某个版本？

```bash
# 查看历史找到目标版本
git log --oneline

# 方式一：安全回退（新建撤销commit）
git revert <要撤销的commit-hash>

# 方式二：硬回退（⚠️ 危险，会删除历史）
git reset --hard <目标commit-hash>
git push --force-with-lease
```

### Q8: 如何查看某个文件是谁最后修改的？

```bash
# 查看文件每一行的最后修改者和时间
git blame <文件路径>

# 示例：查看 Login.vue 的修改历史
git blame frontend/src/views/Login.vue
```

---

## 附录：taffy 项目 Git 初始化步骤（仓库管理员）

如果你是仓库创建者，按以下步骤初始化：

```bash
# 1. 初始化本地仓库
cd taffy
git init
git add .
git commit -m "chore: 初始化项目结构"

# 2. 在 GitHub 上创建远程仓库
#    登录 GitHub → New repository → 命名 taffy → Create

# 3. 关联远程仓库并推送
git remote add origin git@github.com:Xujunteng/taffy.git
git branch -M main
git push -u origin main

# 4. 创建 develop 分支
git switch -c develop
git push -u origin develop

# 5. 设置分支保护规则
#    GitHub 仓库 → Settings → Branches → Add rule
#    - Branch name pattern: main
#    - ☑ Require a pull request before merging
#    - ☑ Require approvals (1)
#    对 develop 同样设置

# 6. 添加团队成员
#    GitHub 仓库 → Settings → Collaborators → Add people
#    → 输入成员的 GitHub 用户名 → 发送邀请

# 7. 通知团队成员克隆项目开始开发
```

---

## 推荐工具

| 工具 | 用途 | 链接 |
|------|------|------|
| **VS Code Git 插件** | VS Code 自带的 Git 图形化操作 | 已内置 |
| **GitLens** (VS Code 插件) | 增强的 Git 可视化 | VS Code 扩展商店 |
| **Git Graph** (VS Code 插件) | 分支图可视化 | VS Code 扩展商店 |
| **Sourcetree** | 独立的 Git 图形化客户端 | [sourcetreeapp.com](https://www.sourcetreeapp.com/) |
| **GitHub Desktop** | GitHub 官方桌面客户端 | [desktop.github.com](https://desktop.github.com/) |
| **Oh My Zsh Git 插件** | 终端 Git 状态提示（macOS/Linux） | [ohmyz.sh](https://ohmyz.sh/) |

---

> 📅 文档最后更新：2026年7月7日
>
> 💡 本指南结合 taffy（直播助手网站）项目的实际四人分工编写，涵盖了 Git 和 GitHub 协作开发的核心内容。
> 如有疑问，可在团队群内讨论或提交 Issue。