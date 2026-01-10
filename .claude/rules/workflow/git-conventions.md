# Git 提交规范

遵循 **Conventional Commits**

## 格式

```
<type>(<scope>): <subject>
```

## Type

| 类型 | 说明 |
|-----|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `docs` | 文档 |
| `style` | 格式 |
| `refactor` | 重构 |
| `perf` | 性能 |
| `test` | 测试 |
| `chore` | 构建/工具 |
| `ci` | CI/CD |

## Scope

- `rag-etl-core`
- `rag-etl-opensearch`
- `spring-ai-example`
- `mcp-weather-server`
- `spring-ai-alibaba-*`
- `build`
- `deps`

## Subject

- 中文
- 动词开头
- 首字母小写
- ≤ 50 字符
- 无句号

## 示例

```bash
# 简单
git commit -m "feat: 添加用户认证"

# 带范围
git commit -m "feat(rag-etl-core): 添加 PDF 读取器"

# 详细（HEREDOC）
git commit -m "$(cat <<'EOF'
fix(spring-ai-example): 修复对话历史丢失

- 修复 ConversationId 传递
- 优化查询逻辑
- 添加测试

Closes #123

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
EOF
)"
```

## 常用命令

```bash
git log --oneline -10      # 历史
git show <commit-hash>      # 详情
git commit --amend          # 修改最后提交
git reset --soft HEAD~1     # 撤销最后提交
```