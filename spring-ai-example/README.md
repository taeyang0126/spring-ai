# spring-ai-example

Spring AI 核心功能示例模块，演示 Spring AI 的各种核心功能和应用场景。

## 功能特性

### 核心功能

1. **文本聊天**
    - 支持普通文本对话
    - 基于对话 ID 的上下文记忆
    - 用户上下文管理

2. **流式响应**
    - 基于 Server-Sent Events (SSE) 的实时流式响应
    - 支持图片上传的多模态对话
    - 异步处理，提升用户体验

3. **多模态支持**
    - 支持图片上传
    - 图片与文本混合输入
    - 自动识别图片 MIME 类型

4. **对话记忆**
    - 基于 `MessageWindowChatMemory` 的对话历史管理
    - 支持多轮对话上下文保持
    - MongoDB 持久化存储对话历史
    - 支持按对话 ID 或用户 ID 查询历史记录

5. **用户上下文**
    - 自定义 `UserContextAdvisor` 管理用户信息
    - 支持用户 ID 注入到对话上下文

6. **Function Calling（工具调用）**
    - 支持自定义工具函数
    - 内置时间工具 `DateTimeTools`：获取当前时间、设置闹钟
    - 内置用户工具 `UserTools`：查询用户信息（支持按用户名、邮箱、手机号查询）
    - AI 可根据用户问题自动调用相应工具
    - 支持 `ToolContext` 获取上下文信息

7. **动态模型选择**
    - 支持通过配置动态选择文本模型或多模态模型
    - 灵活切换不同场景下的模型使用

8. **MCP 集成**
    - 支持 Model Context Protocol (MCP)
    - 可通过 MCP 连接外部工具服务器
    - 可连接 mcp-weather-server 模块提供的天气服务

## 技术栈

- **Spring AI OpenAI Starter**: OpenAI 模型集成
- **MongoDB**: 对话记忆持久化存储
- **MCP Client**: Model Context Protocol 客户端
- **Lombok**: 简化 Java 代码

## 环境要求

- JDK 21 或更高版本
- Maven 3.6+
- MongoDB 数据库
- OpenAI API Key（或兼容的 API，如阿里云 DashScope）

## 快速开始

### 1. 配置环境变量

```bash
export OPENAI_API_KEY=your-api-key-here
export MONGO_HOST=localhost
export MONGO_USER=your-mongo-username
export MONGO_PWD=your-mongo-password
```

### 2. 修改配置（可选）

编辑 `src/main/resources/application.yml`：

```yaml
ai:
  openai:
    max-tokens: 1000
    temperature: 0.5
    text-model: qwen3-max          # 文本模型
    full-model: qwen3-omni-flash   # 多模态模型
    chat-model-type: text          # 默认使用的模型类型：text 或 full

spring:
  data:
    mongodb:
      uri: mongodb://${MONGO_USER}:${MONGO_PWD}@${MONGO_HOST}:27017/spring-ai?authSource=admin
  ai:
    mcp:
      client:
        streamable-http:
          connections:
            my-weather-server:
              url: http://localhost:9001
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://dashscope.aliyuncs.com/compatible-mode
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

### 4. 访问应用

- 应用默认运行在 `http://localhost:9999`
- 访问 `http://localhost:9999/chat.html` 使用 Web 界面
- API 端点：
  - `POST /chat/content` - 普通文本对话
  - `POST /chat/stream` - 流式响应对话
  - `POST /chat/history` - 查询对话历史

## 配置说明

### 配置项

| 配置项                                         | 说明           | 默认值              |
|---------------------------------------------|--------------|------------------|
| `server.port`                               | 服务端口         | 9999             |
| `spring.ai.openai.api-key`                  | API 密钥       | 从环境变量读取          |
| `spring.ai.openai.base-url`                 | API 基础地址     | DashScope 兼容地址   |
| `spring.data.mongodb.uri`                   | MongoDB 连接地址 | 从环境变量读取          |
| `ai.openai.max-tokens`                      | 最大 token 数   | 1000             |
| `ai.openai.temperature`                     | 温度参数         | 0.5              |
| `ai.openai.text-model`                      | 文本模型名称       | qwen3-max        |
| `ai.openai.full-model`                      | 多模态模型名称      | qwen3-omni-flash |
| `ai.openai.chat-model-type`                 | 默认模型类型       | text             |
| `spring.servlet.multipart.max-file-size`    | 最大文件大小       | 10MB             |
| `spring.servlet.multipart.max-request-size` | 最大请求大小       | 50MB             |

### 模型配置

本模块支持两种模型配置：

- **textChatModel**: 用于纯文本对话，使用 `qwen3-max`，支持 Function Calling
- **fullChatModel**: 用于多模态对话（文本+图片），使用 `qwen3-omni-flash`

通过 `ai.openai.chat-model-type` 配置项可以设置默认使用的模型类型：
- `text`: 使用文本模型（支持工具调用）
- `full`: 使用多模态模型（支持图片）

## 项目结构

```
spring-ai-example/
├── src/
│   ├── main/
│   │   ├── java/com/lei/learn/spring/ai/
│   │   │   ├── advisor/          # Advisor 实现
│   │   │   ├── configuration/    # 配置类
│   │   │   ├── controller/       # 控制器
│   │   │   ├── env/              # 环境支持
│   │   │   ├── mcp/              # MCP 客户端
│   │   │   ├── memory/           # 对话记忆管理
│   │   │   ├── model/            # 数据模型
│   │   │   ├── repository/       # 数据访问层
│   │   │   ├── support/          # 常量和枚举定义
│   │   │   ├── tool/             # Function Calling 工具
│   │   │   ├── utils/            # 工具类
│   │   │   └── Application.java  # 主应用类
│   │   └── resources/
│   │       ├── application.yml   # 配置文件
│   │       └── static/           # 静态资源（chat.html）
│   └── test/                     # 测试代码
└── pom.xml
```

## 核心实现

### 1. 自定义 MongoDB 对话记忆仓库

项目实现了 `CustomerMongoChatMemoryRepository`，扩展了 Spring AI 的 `ChatMemoryRepository` 接口：

- **持久化存储**：对话历史保存到 MongoDB 的 `ai_chat_memory` 集合
- **用户维度管理**：每条对话记录关联用户 ID，支持按用户查询
- **对话分组**：支持按 `conversationId` 分组管理对话
- **消息窗口**：默认保留最近 20 条消息，避免上下文过长

### 2. 对话记忆配置

```java
@Bean
public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
    return MessageWindowChatMemory.builder()
            .maxMessages(20)  // 最多保留 20 条消息
            .chatMemoryRepository(chatMemoryRepository)
            .build();
}
```

### 3. 双模型配置

```java
@Bean("textChatClient")
public ChatClient textChatClient(ChatClient.Builder builder) {
    // 文本模型，支持 Function Calling
}

@Bean("fullChatClient")
public ChatClient fullChatClient(ChatClient.Builder builder) {
    // 多模态模型，支持图片输入
}
```

## 注意事项

1. **文件上传限制**: 默认最大文件大小为 10MB，可在 `application.yml` 中调整。

2. **对话记忆**: 使用 `MessageWindowChatMemory` 结合 MongoDB 持久化存储，对话历史会保存到数据库中，应用重启后不会丢失。

3. **用户上下文**: `UserContextUtils.getCurrentUserId()` 需要根据实际业务实现用户身份获取逻辑。

4. **模型选择**:
    - 通过 `ai.openai.chat-model-type` 配置默认模型类型
    - `text` 模型支持 Function Calling，性能更好
    - `full` 模型支持图片输入

5. **Function Calling**:
    - 仅文本模型（textChatClient）支持工具调用
    - 多模态模型（fullChatClient）不支持 Function Calling

6. **MCP 集成**:
    - 需要单独启动 `mcp-weather-server` 模块（端口 9001）
    - 确保 MCP 服务器地址配置正确
