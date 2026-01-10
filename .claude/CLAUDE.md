# CLAUDE.md

Spring AI 学习项目文档。

> **沟通约定**：AI 始终使用中文回复，包括代码注释、提交信息和文档说明

## 快速导航

### 📖 项目
- [项目指南](rules/project/overview.md) - 概述、模块、命令、环境

### 🏗️ 架构
- [架构指南](rules/architecture/guide.md) - 模式、配置

### 💻 开发
- [开发指南](rules/development/guide.md) - 命名、风格、注释、Lombok

### 🔄 工作流
- [Git 规范](rules/workflow/git-conventions.md) - 提交格式、示例

## 核心摘要

### 命名
- 类：UpperCamelCase
- 方法/变量：lowerCamelCase
- 常量：全大写_分隔

### Git 提交
```
<type>(<scope>): <subject>
```
**类型**：`feat`/`fix`/`test`/`docs`/`refactor`/`chore`

**示例**：
- `feat: 添加用户认证`
- `fix(spring-ai-example): 修复超时`

### 环境变量
```bash
export OPENAI_API_KEY=your-key
export OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode
export MONGO_HOST=localhost
export MONGO_USER=user
export MONGO_PWD=password
```

### 端口
9999（主应用）、9001（MCP）、8001（智能体）、7001（OpenSearch）

### Java 21
record、虚拟线程、模式匹配

---

**技术栈**：Spring Boot 3.5.8、Spring AI 1.1.0-M4、Java 21