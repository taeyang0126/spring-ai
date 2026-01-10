# Git 提交规范

遵循 **Conventional Commits**（约定式提交）

## 格式

```
<type>(<scope>): <subject>

<body>
```

## Type 类型

| 类型 | 说明 |
|-----|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `docs` | 文档变更 |
| `style` | 代码格式 |
| `refactor` | 重构 |
| `perf` | 性能优化 |
| `test` | 添加或修改测试 |
| `chore` | 构建/工具变动 |
| `ci` | CI/CD 配置 |

## Scope 范围

- `rag-etl-core` - RAG ETL 核心模块
- `rag-etl-opensearch` - OpenSearch 向量存储
- `spring-ai-example` - 主示例模块
- `mcp-weather-server` - MCP 天气服务
- `spring-ai-alibaba-*` - 阿里云模块
- `build` - 构建配置
- `deps` - 依赖管理

## Subject 主题

- 中文描述
- 动词开头："添加"、"修复"、"更新"
- 首字母小写
- 不超过 50 字符
- 结尾不加句号

## 示例

```bash
# 简单
git commit -m "feat: 添加用户头像上传"

# 带范围
git commit -m "feat(rag-etl-core): 添加 PDF 读取器"

# 详细（HEREDOC 推荐）
git commit -m "$(cat <<'EOF'
fix(spring-ai-example): 修复对话历史丢失

- 修复 ConversationId 传递问题
- 优化 MongoDB 查询逻辑
- 添加单元测试

Closes #123

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
EOF
)"
```

## 常用命令

```bash
# 查看历史
git log --oneline -10

# 查看详情
git show <commit-hash>

# 修改最后一次提交
git commit --amend

# 撤销最后一次提交（保留变更）
git reset --soft HEAD~1
```
