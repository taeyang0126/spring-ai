# CLAUDE.md

> **Claude Code 项目配置和规范文档**

本文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

## 快速开始

### 沟通约定

- AI **始终使用中文**回复
- 代码注释、提交信息、文档说明使用中文
- 严格遵循项目规范和约定

### 文档位置

完整的规范文档位于 `.claude/rules/` 目录：

- **项目概述** → [.claude/rules/project-overview.md](.claude/rules/project-overview.md)
- **模块结构** → [.claude/rules/module-structure.md](.claude/rules/module-structure.md)
- **常用命令** → [.claude/rules/common-commands.md](.claude/rules/common-commands.md)
- **架构模式** → [.claude/rules/architecture-patterns.md](.claude/rules/architecture-patterns.md)
- **环境配置** → [.claude/rules/environment-config.md](.claude/rules/environment-config.md)
- **代码规范** → [.claude/rules/code-standards.md](.claude/rules/code-standards.md)
- **Git 规范** → [.claude/rules/git-commit-conventions.md](.claude/rules/git-commit-conventions.md)
- **重要说明** → [.claude/rules/important-notes.md](.claude/rules/important-notes.md)

### 文档索引

查看完整的规则文档索引：[.claude/rules/README.md](.claude/rules/README.md)

## 项目概述

这是一个用于学习 Spring AI 的**多模块 Maven 项目**，展示了各种 AI 能力：

- 🗣️ 对话和聊天
- 🖼️ 多模态交互（文本 + 图片）
- 🔧 函数调用和工具使用
- 🔌 MCP（模型上下文协议）集成
- 📚 RAG（检索增强生成）管道
- 🤖 智能体工作流
- 💾 对话记忆管理

### 技术栈

- **Spring Boot**: 3.5.8
- **Spring AI**: 1.1.0-M4
- **Java**: 21
- **构建工具**: Maven

## 模块列表

| 模块 | 端口 | 用途 |
|--------|------|---------|
| `spring-ai-example` | 9999 | Spring AI 核心功能 |
| `mcp-weather-server` | 9001 | MCP 天气服务 |
| `spring-ai-alibaba-weather-agent` | 8001 | 阿里云智能体 |
| `rag-etl-core` | - | RAG 核心抽象库 |
| `rag-etl-opensearch` | 7001 | OpenSearch RAG 实现 |
| `spring-ai-alibaba-graph` | - | 智能体工作流 |

## 常用命令

### 构建与运行

```bash
# 构建整个项目
mvn clean install

# 运行指定模块
mvn spring-boot:run -pl spring-ai-example

# 快速构建（跳过测试）
mvn clean install -DskipTests
```

### 测试

```bash
# 运行所有测试
mvn test

# 运行指定模块的测试
mvn test -pl spring-ai-example
```

## 环境配置

### 必需的环境变量

```bash
# Spring AI（OpenAI 兼容 API）
export OPENAI_API_KEY=your-api-key-here
export OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode

# MongoDB（spring-ai-example 模块）
export MONGO_HOST=localhost
export MONGO_USER=your-mongo-username
export MONGO_PWD=your-mongo-password

# OpenSearch（rag-etl-opensearch 模块）
export OPENSEARCH_HOST=localhost
export OPENSEARCH_USERNAME=admin
export OPENSEARCH_PASSWORD=your-opensearch-password
```

## 代码规范

本项目遵循 [《阿里巴巴 Java 开发手册》](https://github.com/alibaba/p3c) 规范。

### 核心原则

- 类名使用 UpperCamelCase
- 方法名、变量名使用 lowerCamelCase
- 常量名全部大写，单词间用下划线分隔
- 所有 public 类和方法必须添加 Javadoc 注释
- 优先使用 `var` 进行类型推断
- 使用 record 定义简单数据载体

详细规范：[.claude/rules/code-standards.md](.claude/rules/code-standards.md)

## Git 提交规范

本项目遵循 **Conventional Commits**（约定式提交）规范。

### 提交格式

```
<type>(<scope>): <subject>
```

### 类型说明

- `feat` - 新功能
- `fix` - Bug 修复
- `test` - 添加或修改测试
- `docs` - 文档变更
- `refactor` - 重构
- `chore` - 构建/工具变动

### 示例

```bash
git commit -m "feat(rag-etl-core): 添加 PDF 文档读取器"
git commit -m "fix: 修复登录超时问题"
git commit -m "test: 添加用户服务单元测试"
```

详细规范：[.claude/rules/git-commit-conventions.md](.claude/rules/git-commit-conventions.md)

## 重要说明

### 端口冲突

各模块运行在不同端口（9999、9001、8001、7001）- 运行多个模块时确保无冲突

### 模块依赖

- MCP 功能需要同时启动客户端和服务端
- RAG 功能需要 OpenSearch 支持
- 对话记忆需要 MongoDB

### API 兼容性

项目通过 `OPENAI_BASE_URL` 配置使用 DashScope（阿里云）作为 OpenAI 兼容 API

### 中文注释

代码库包含中文注释和文档 - 这是为了中文团队有意设计的

### Java 21 特性

项目使用了现代 Java 特性，包括 record、虚拟线程和模式匹配

## 学习路径

1. **开始**：阅读 [project-overview.md](.claude/rules/project-overview.md)
2. **构建**：参考 [common-commands.md](.claude/rules/common-commands.md)
3. **编码**：遵循 [code-standards.md](.claude/rules/code-standards.md)
4. **提交**：按照 [git-commit-conventions.md](.claude/rules/git-commit-conventions.md)

## 获取帮助

- 查看完整文档：[.claude/rules/](.claude/rules/)
- 阅读重要说明：[.claude/rules/important-notes.md](.claude/rules/important-notes.md)
- 查看文档索引：[.claude/rules/README.md](.claude/rules/README.md)

---

**项目版本**：1.0-SNAPSHOT
**文档更新**：2025-01-10
**维护团队**：Spring AI 学习小组
