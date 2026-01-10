# CLAUDE.md

Spring AI 学习项目完整文档。

> **沟通约定**：AI 始终使用中文回复，包括代码注释、提交信息和文档说明

## 快速导航

### 📖 项目相关
- [项目概述](rules/project/overview.md) - 技术栈和模块说明
- [常用命令](rules/project/commands.md) - Maven 构建和测试命令

### 🏗️ 架构相关
- [架构模式](rules/architecture/patterns.md) - Advisor、RAG、MCP 等核心模式
- [环境配置](rules/architecture/configuration.md) - 环境变量和配置文件

### 💻 开发相关
- [代码组织](rules/development/code-organization.md) - 包结构和 Lombok 配置
- [代码规范](rules/development/code-standards.md) - 命名、注释、编码风格

### 🔄 工作流相关
- [Git 提交规范](rules/workflow/git-conventions.md) - Conventional Commits 格式

## 核心规范摘要

### 命名规范
- **类名**：UpperCamelCase（帕斯卡命名法）
- **方法/变量**：lowerCamelCase（驼峰命名法）
- **常量**：全大写，下划线分隔

### Git 提交格式
```
<type>(<scope>): <subject>
```

**类型**：`feat`/`fix`/`test`/`docs`/`refactor`/`chore`

**示例**：
- `feat: 添加用户认证功能`
- `fix(spring-ai-example): 修复登录超时问题`
- `test: 添加用户服务单元测试`

### 环境变量
```bash
export OPENAI_API_KEY=your-api-key
export OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode
export MONGO_HOST=localhost
export MONGO_USER=your-mongo-username
export MONGO_PWD=your-mongo-password
```

## 重要说明

1. **端口分配**：9999（主应用）、9001（MCP）、8001（智能体）、7001（OpenSearch）
2. **模块独立性**：MCP 功能需要同时启动客户端和服务端
3. **Java 21**：使用 record、虚拟线程、模式匹配等现代特性
4. **中文注释**：代码库包含中文注释和文档

## 技术栈

- **Spring Boot**: 3.5.8
- **Spring AI**: 1.1.0-M4
- **Java**: 21
- **构建工具**: Maven

---

**详细文档**：查看 [rules/](rules/) 目录
