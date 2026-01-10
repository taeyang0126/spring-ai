# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

> **沟通约定**：
> - AI **始终使用中文**回复，包括代码注释、提交信息和文档说明

## 项目概述

这是一个用于学习 Spring AI 的**多模块 Maven 项目**，展示了各种 AI 能力，包括对话、多模态交互、函数调用、MCP 集成、RAG 管道和智能体工作流。项目使用 Spring Boot 3.5.8、Spring AI 1.1.0-M4 和 Java 21。

## 模块结构

| 模块 | 端口 | 用途 |
|--------|------|---------|
| `spring-ai-example` | 9999 | Spring AI 核心功能：对话、多模态、函数调用、MCP 集成、MongoDB 记忆 |
| `mcp-weather-server` | 9001 | 提供天气工具的 MCP 服务器 |
| `spring-ai-alibaba-weather-agent` | 8001 | 集成 DashScope 的阿里云智能体 |
| `rag-etl-core` | - | 抽象 RAG ETL 管道组件（库模块） |
| `rag-etl-opensearch` | 7001 | OpenSearch 向量存储及 RAG ETL 实现 |
| `spring-ai-alibaba-graph` | - | LangGraph 风格的智能体工作流，支持状态管理 |

## 常用命令

### 构建与运行
```bash
# 构建整个项目
mvn clean install

# 构建指定模块
mvn clean install -pl spring-ai-example

# 运行指定模块
mvn spring-boot:run -pl spring-ai-example

# 快速构建（跳过测试）
mvn clean install -DskipTests
```

### 测试
```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=ChatTests

# 运行指定测试方法
mvn test -Dtest=ChatTests#test_chat_memory

# 运行指定模块的测试
mvn test -pl spring-ai-example
```

## 架构模式

### 1. Advisor 模式（横切关注点）
Spring AI 广泛使用 Advisor 来修改 LLM 行为：
- `MessageChatMemoryAdvisor` - 对话历史管理
- `UserContextAdvisor` - 用户上下文注入（自定义实现）
- `QuestionAnswerAdvisor` - 简单 RAG 问答
- `RetrievalAugmentationAdvisor` - 高级 RAG，支持重排序

### 2. 双 ChatClient 模式
主模块定义了两个专门的 `ChatClient` Bean：
- `textChatClient` - 纯文本模型，支持函数调用和工具
- `fullChatClient` - 多模态模型（文本 + 图片），不支持函数调用

配置位置：`spring-ai-example/src/main/java/com/lei/learn/spring/ai/configuration/ChatClientConfiguration.java`

### 3. RAG 管道模式
`rag-etl-core` 模块定义了用于文档处理的抽象 `RagPipeline`：
- 阶段：资源加载 → 文本分割 → 向量存储
- 由 `rag-etl-opensearch` 模块实现，使用 OpenSearch 向量存储
- 支持可配置的分割器和元数据处理

### 4. 智能体工作流模式（Spring AI Alibaba）
`spring-ai-alibaba-graph` 模块展示了 LangGraph 风格的工作流：
- `StateGraph` 用于构建智能体状态机
- `KeyStrategy`（ReplaceStrategy、AppendStrategy）用于状态管理
- 流式输出支持，使用 `StreamingOutput`
- 使用 `MemorySaver` 持久化记忆

### 5. MCP 集成
模型上下文协议用于外部工具连接：
- 客户端配置在 `application.yml` 的 `spring.ai.mcp.client` 下
- 服务器实现在 `mcp-weather-server` 模块中
- 通过 MCP 暴露的工具可被 LLM 调用

## 环境配置

### 必需的环境变量
```bash
# Spring AI（OpenAI 兼容 API）
export OPENAI_API_KEY=your-api-key-here
export OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode  # 可选

# MongoDB（spring-ai-example 模块）
export MONGO_HOST=localhost
export MONGO_USER=your-mongo-username
export MONGO_PWD=your-mongo-password

# OpenSearch（rag-etl-opensearch 模块）
export OPENSEARCH_HOST=localhost
export OPENSEARCH_USERNAME=admin
export OPENSEARCH_PASSWORD=your-opensearch-password
```

### 测试基础设施
测试使用 `OpenAiApiBase` 类模式进行 API 初始化：
- 位置：`spring-ai-example/src/test/java/com/lei/learn/spring/ai/OpenAiApiBase.java`
- 提供从环境变量配置的静态 `OpenAiApi` 实例
- 测试类继承或引用此基类以保持一致的设置

## 配置模式

### 聊天模型选项
模型通过 `OpenAiChatOptions` 配置：
- `text-model`：qwen3-max（支持函数调用）
- `full-model`：qwen3-omni-flash（多模态）
- 通过 `ai.openai.chat-model-type` 属性切换模型

### 对话记忆
- `MessageWindowChatMemory`，可配置最大消息数（默认：20）
- 通过 `CustomerMongoChatMemoryRepository` 实现 MongoDB 持久化
- 通过 `CONVERSATION_ID` Advisor 参数限定对话范围

### 工具/函数调用
- 通过 `FunctionToolCallback` 实现
- 内置工具：`DateTimeTools`、`UserTools`
- 通过客户端配置暴露 MCP 工具
- 仅文本模型支持函数调用

## 代码组织

### 包结构（每个模块）
```
com.lei.learn.spring.ai/
├── advisor/          # 自定义 Advisor 实现
├── configuration/    # Spring @Configuration 类
├── controller/       # REST 控制器
├── env/              # 环境变量支持
├── mcp/              # MCP 客户端/服务端代码
├── memory/           # 对话记忆实现
├── model/            # DTO 和领域模型
├── repository/       # 数据访问层
├── support/          # 常量和枚举
├── tool/             # 函数调用工具
└── utils/            # 工具类
```

### Lombok 配置
- Lombok 1.18.42 在 `maven-compiler-plugin` 中配置为注解处理器
- 广泛用于减少样板代码（数据类、构建器、日志）

## 代码规范

### Alibaba Java 规范
本项目严格遵循 [《阿里巴巴 Java 开发手册》](https://github.com/alibaba/p3c) 规范：

**命名规范**
- 类名使用 UpperCamelCase（帕斯卡命名法）
- 方法名、变量名使用 lowerCamelCase（驼峰命名法）
- 常量名全部大写，单词间用下划线分隔
- 包名全部小写，单词间用点分隔

**注释规范**
- 所有 public 类和方法必须添加 Javadoc 注释
- 复杂逻辑必须添加行内注释说明
- 注释使用中文编写，与项目文档风格一致

**代码风格**
- 缩进使用 4 个空格（不使用 Tab）
- 大括号左括号不换行（K&R 风格）
- 每行代码长度不超过 120 个字符
- 优先使用 `var` 进行局部变量类型推断（Java 10+）

**最佳实践**
- 避免使用魔法值，使用常量定义
- 集合初始化时指定容量
- 优先使用不可变对象
- 使用 `Optional` 替代 null 返回值
- 使用 record 定义简单数据载体（Java 16+）

## Git 提交规范

本项目遵循 **Conventional Commits**（约定式提交）规范，以确保提交历史清晰、可读且易于维护。

### 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type（类型）

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

### Scope（范围）

Scope 表示提交影响的模块或组件：

- `rag-etl-core` - RAG ETL 核心模块
- `rag-etl-opensearch` - OpenSearch 向量存储模块
- `spring-ai-example` - 主示例模块
- `mcp-weather-server` - MCP 天气服务端
- `spring-ai-alibaba-*` - 阿里云 Spring AI 相关模块
- `build` - 构建配置
- `deps` - 依赖管理

### Subject（主题）

- 使用中文描述
- 以动词开头，如"添加"、"修复"、"更新"
- 首字母小写
- 结尾不加句号
- 限制在 50 个字符以内

### Body（正文）

- 对本次提交的详细描述
- 列出主要变更点（使用 `-` 开头）
- 说明"为什么"而不是"是什么"

### Footer（脚注）

- 关联 Issue：`Closes #123` 或 `Fixes #456`
- 破坏性变更：以 `BREAKING CHANGE:` 开头

### 提交示例

```bash
# 简单提交
git commit -m "feat: 添加用户头像上传功能"

# 带范围的提交
git commit -m "feat(rag-etl-core): 添加 PDF 文档读取器"

# 详细提交（多行）
git commit -m "fix(spring-ai-example): 修复对话历史记忆丢失问题

- 修复 ConversationId 未正确传递的问题
- 优化 MongoDBChatMemoryRepository 查询逻辑
- 添加单元测试验证修复

Closes #123"

# 使用 HEREDOC 格式（推荐）
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

### 常用命令

```bash
# 查看最近的提交历史
git log --oneline -10

# 查看提交详情
git show <commit-hash>

# 修改最后一次提交信息
git commit --amend

# 查看某个文件的提交历史
git log --follow -- <file-path>
```

## 重要说明

1. **端口冲突**：各模块运行在不同端口（9999、9001、8001、7001）- 运行多个模块时确保无冲突

2. **模块独立性**：每个模块可独立运行，但某些功能需要同时启动客户端和服务端（例如 MCP 需要同时运行 `spring-ai-example` 和 `mcp-weather-server`）

3. **API 兼容性**：项目通过 `OPENAI_BASE_URL` 配置使用 DashScope（阿里云）作为 OpenAI 兼容 API

4. **中文注释**：代码库包含中文注释和文档 - 这是为了中文团队有意设计的

5. **Java 21 特性**：项目使用了现代 Java 特性，包括 record、虚拟线程和模式匹配
