# Spring AI

## 项目简介

Spring AI 学习项目，包括：

- 文本对话
- 流式响应（SSE）
- 多模态支持（图片上传）
- 对话记忆管理
- 用户上下文管理

## 技术栈

- **Java**: 24
- **Spring Boot**: 3.5.8
- **Spring AI**: 1.1.0
- **Maven**: 项目构建工具
- **Lombok**: 简化 Java 代码
- **Spring Boot Actuator**: 应用监控

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

5. **用户上下文**
    - 自定义 `UserContextAdvisor` 管理用户信息
    - 支持用户 ID 注入到对话上下文

## 快速开始

### 环境要求

- JDK 21 或更高版本
- Maven 3.6+
- OpenAI API Key（或兼容的 API，如阿里云 DashScope）

### 配置步骤

1. **克隆项目**

```bash
git clone <repository-url>
cd spring-ai
```

2. **配置环境变量**

设置 `OPENAI_API_KEY` 环境变量：

```bash
export OPENAI_API_KEY=your-api-key-here
```

或者在 `application.yml` 中直接配置（不推荐用于生产环境）。

3. **修改配置**

编辑 `src/main/resources/application.yml`，根据你的需求调整配置：

```yaml
ai:
  openai:
    max-tokens: 1000
    temperature: 0.5
    text-model: qwen3-max          # 文本模型
    full-model: qwen3-omni-flash   # 多模态模型

spring:
  ai:
    chat:
      client:
        # 关闭自动注入 client.builder，只注入 ClientModel
        enabled: false
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://dashscope.aliyuncs.com/compatible-mode
```

4. **运行应用**

```bash
mvn spring-boot:run
```

或者先编译再运行：

```bash
mvn clean package
java -jar target/spring-ai-1.0-SNAPSHOT.jar
```

5. **访问应用**

- 应用默认运行在 `http://localhost:9999`
- 访问 `http://localhost:9999/chat.html` 使用 Web 界面
- API 端点：`http://localhost:9999/chat/*`

## 配置说明

### application.yml 配置项

| 配置项                                         | 说明         | 默认值              |
|---------------------------------------------|------------|------------------|
| `server.port`                               | 服务端口       | 9999             |
| `spring.ai.openai.api-key`                  | API 密钥     | 从环境变量读取          |
| `spring.ai.openai.base-url`                 | API 基础地址   | DashScope 兼容地址   |
| `ai.openai.max-tokens`                      | 最大 token 数 | 1000             |
| `ai.openai.temperature`                     | 温度参数       | 0.5              |
| `ai.openai.text-model`                      | 文本模型名称     | qwen3-max        |
| `ai.openai.full-model`                      | 多模态模型名称    | qwen3-omni-flash |
| `spring.servlet.multipart.max-file-size`    | 最大文件大小     | 10MB             |
| `spring.servlet.multipart.max-request-size` | 最大请求大小     | 50MB             |

### 模型配置

项目支持两种模型配置：

- **textChatModel**: 用于纯文本对话，使用 `qwen3-max`
- **fullChatModel**: 用于多模态对话（文本+图片），使用 `qwen3-omni-flash`

## 项目结构

```
spring-ai/
├── src/
│   ├── main/
│   │   ├── java/com/lei/learn/spring/ai/
│   │   │   ├── advisor/              # Advisor 实现
│   │   │   ├── configuration/        # 配置类
│   │   │   ├── controller/           # 控制器
│   │   │   ├── model/                # 数据模型
│   │   │   ├── utils/                # 工具类
│   │   │   └── Application.java      # 主应用类
│   │   └── resources/
│   │       ├── application.yml        # 配置文件
│   │       └── static/               # 静态资源
│   └── test/                         # 测试代码
├── pom.xml                           # Maven 配置
└── README.md                         # 项目说明
```

## 注意事项

1. **API 密钥安全**: 生产环境请使用环境变量或密钥管理服务，不要将密钥硬编码在配置文件中。

2. **文件上传限制**: 默认最大文件大小为 10MB，可在 `application.yml` 中调整。

3. **对话记忆**: 当前使用 `MessageWindowChatMemory`，对话历史保存在内存中，应用重启后会丢失。

4. **用户上下文**: `UserContextUtils.getCurrentUserId()` 需要根据实际业务实现用户身份获取逻辑。

5. **模型选择**:
    - 文本对话使用 `textChatClient`（性能更好）
    - 需要图片支持时使用 `fullChatClient`

## 测试

运行测试：

```bash
mvn test
```

