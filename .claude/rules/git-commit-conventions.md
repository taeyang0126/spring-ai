# Git 提交规范

本项目遵循 **Conventional Commits**（约定式提交）规范。

## 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

## Type（类型）

| 类型 | 说明 | 示例 |
|-----|------|------|
| `feat` | 新功能 | feat: 添加用户认证功能 |
| `fix` | Bug 修复 | fix: 修复登录超时问题 |
| `docs` | 文档变更 | docs: 更新 README 安装说明 |
| `style` | 代码格式（不影响功能） | style: 统一代码缩进为 2 空格 |
| `refactor` | 重构（既不是新功能也不是修复） | refactor: 重构订单处理流程 |
| `perf` | 性能优化 | perf: 优化数据库查询性能 |
| `test` | 添加或修改测试 | test: 添加用户服务单元测试 |
| `chore` | 构建过程或辅助工具的变动 | chore: 升级 Maven 依赖版本 |
| `ci` | CI/CD 配置变更 | ci: 添加 GitHub Actions 工作流 |
| `revert` | 回滚先前的提交 | revert: 回滚 commit abc123 |

## Scope（范围）

Scope 表示提交影响的模块或组件：

- `rag-etl-core` - RAG ETL 核心模块
- `rag-etl-opensearch` - OpenSearch 向量存储模块
- `spring-ai-example` - 主示例模块
- `mcp-weather-server` - MCP 天气服务端
- `spring-ai-alibaba-*` - 阿里云 Spring AI 相关模块
- `build` - 构建配置
- `deps` - 依赖管理

## Subject（主题）

- 使用中文描述
- 以动词开头，如"添加"、"修复"、"更新"
- 首字母小写
- 结尾不加句号
- 限制在 50 个字符以内

## Body（正文）

- 对本次提交的详细描述
- 列出主要变更点（使用 `-` 开头）
- 说明"为什么"而不是"是什么"

## Footer（脚注）

- 关联 Issue：`Closes #123` 或 `Fixes #456`
- 破坏性变更：以 `BREAKING CHANGE:` 开头

## 提交示例

### 简单提交

```bash
git commit -m "feat: 添加用户头像上传功能"
```

### 带范围的提交

```bash
git commit -m "feat(rag-etl-core): 添加 PDF 文档读取器"
```

### 详细提交（多行）

```bash
git commit -m "fix(spring-ai-example): 修复对话历史记忆丢失问题

- 修复 ConversationId 未正确传递的问题
- 优化 MongoDBChatMemoryRepository 查询逻辑
- 添加单元测试验证修复

Closes #123"
```

### 使用 HEREDOC 格式（推荐）

```bash
git commit -m "$(cat <<'EOF'
test(rag-etl-core): 补充完整的单元测试

- 添加 MarkdownProcessRequestTest 测试类（9 个测试用例）
- 添加 MarkdownProcessResponseTest 测试类（19 个测试用例）
- 添加 MarkdownRagPipelineBuilderTest 测试类（32 个测试用例）
- 添加 RagPipelineTest 测试类（29 个测试用例）
- 优化异常处理，增强错误信息
- 改进日志级别和批次处理逻辑

测试覆盖：默认配置、边界值、异常场景、链式调用、批次处理

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
EOF
)"
```

## 常用命令

### 查看提交历史

```bash
# 查看最近的提交历史
git log --oneline -10

# 查看提交详情
git show <commit-hash>

# 查看某个文件的提交历史
git log --follow -- <file-path>

# 查看分支图
git log --graph --oneline --all
```

### 修改提交

```bash
# 修改最后一次提交信息
git commit --amend

# 修改最后一次提交（包含暂存区的变更）
git commit --amend --no-edit

# 修改指定提交（谨慎使用）
git rebase -i HEAD~3
```

### 撤销提交

```bash
# 撤销最后一次提交（保留变更）
git reset --soft HEAD~1

# 撤销最后一次提交（丢弃变更）
git reset --hard HEAD~1

# 回滚到指定提交
git revert <commit-hash>
```

## 提交最佳实践

### 1. 原子化提交

每次提交只做一件事：

```bash
# ❌ 错误：一次提交包含多个变更
git commit -m "feat: 添加聊天功能和修复 Bug"

# ✅ 正确：分两次提交
git commit -m "feat: 添加聊天功能"
git commit -m "fix: 修复用户输入验证"
```

### 2. 提交频率

- 完成一个功能点就提交
- 不要堆积太多变更
- 保持提交历史清晰

### 3. 提交信息质量

- 提交信息要准确描述变更
- 使用中文，便于团队理解
- 包含必要的上下文信息

### 4. 暂存策略

```bash
# 查看变更
git status
git diff

# 暂存特定文件
git add path/to/file.java

# 暂存所有变更
git add .

# 交互式暂存
git add -i
```

## 分支管理

### 分支命名

- `main` - 主分支，保持稳定
- `develop` - 开发分支
- `feature/xxx` - 功能分支
- `bugfix/xxx` - Bug 修复分支
- `hotfix/xxx` - 紧急修复分支

### 分支操作

```bash
# 创建新分支
git checkout -b feature/chat-function

# 合并分支
git merge feature/chat-function

# 删除分支
git branch -d feature/chat-function
```

## 代码审查提交

在提交代码前进行自我审查：

1. [ ] 代码符合规范
2. [ ] 有相应的测试
3. [ ] 提交信息清晰
4. [ ] 无调试代码
5. [ ] 注释完整
6. [ ] 无语法警告
