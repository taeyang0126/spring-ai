# spring-ai-alibaba-weather-agent

基于 Spring AI Alibaba 的智能体（Agent）框架示例，实现天气查询智能体。

## 功能特性

- **ReactAgent 智能体实现**: 基于 Spring AI Alibaba Agent Framework
- **用户位置工具**: 获取用户当前位置信息
- **天气查询工具**: 根据位置查询天气信息
- **工具调用拦截器**: 日志记录和监控
- **内置 Chat UI 界面**: 开箱即用的聊天界面

## 技术栈

- **Spring AI Alibaba Agent Framework**: 智能体框架
- **Spring AI Alibaba DashScope**: 阿里云 DashScope 模型集成
- **Spring AI Alibaba Studio**: Chat UI 支持
- **Spring Boot**: 3.5.8
- **Java**: 21
- **Lombok**: 简化 Java 代码

## 环境要求

- JDK 21 或更高版本
- Maven 3.6+
- 阿里云 DashScope API Key

## 快速开始

### 1. 配置环境变量

```bash
export DASHSCOPE_API_KEY=your-dashscope-api-key
```

### 2. 启动应用

```bash
mvn spring-boot:run
```

### 3. 访问 Chat UI

应用启动后会自动显示访问地址，默认访问：

```
http://localhost:8001/chatui/index.html
```

## 配置说明

### 默认配置

- **端口**: 8001
- **Chat UI 路径**: `/chatui/index.html`
- **模型**: 阿里云 DashScope

### 自定义配置

可以在 `src/main/resources/application.yml` 中自定义配置：

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
```

## 项目结构

```
spring-ai-alibaba-weather-agent/
├── src/
│   └── main/
│       └── java/com/lei/learn/spring/ai/alibaba/weather/agent/
│           ├── config/                       # Agent 配置
│           ├── interceptor/                  # 拦截器
│           ├── model/                        # 数据模型
│           ├── studio/                       # Studio 支持
│           ├── tools/                        # Agent 工具
│           ├── BasicWeatherAgent.java
│           └── WeatherAgentApplication.java
└── pom.xml
```

## 核心实现

### ReactAgent 配置

使用 Spring AI Alibaba 的 Agent 框架，实现智能体自动推理和工具调用：

```java
@Bean
public ReactAgent reactAgent(ChatModel chatModel, ChatMemory chatMemory) {
    return ReactAgent.builder()
            .chatModel(chatModel)
            .chatMemory(chatMemory)
            .tools(tools)
            .build();
}
```

### 工具实现

#### 用户位置工具

获取用户当前位置信息。

#### 天气查询工具

根据位置查询天气信息。

### 工具调用拦截器

实现了 `LogToolInterceptor`，用于记录工具调用的日志信息。

## 使用示例

### 通过 Chat UI 交互

1. 访问 `http://localhost:8001/chatui/index.html`
2. 在聊天框中输入问题，例如：
   - "今天天气怎么样？"
   - "明天会下雨吗？"
3. Agent 会自动调用相应的工具获取信息并回答

### 编程方式调用

```java
String threadId = UUID.randomUUID().toString();
RunnableConfig runnableConfig = RunnableConfig.builder()
        .threadId(threadId)
        .addMetadata("user_id", 1)
        .build();

AssistantMessage response = agent.call("今天天气怎么样？", runnableConfig);
System.out.println(response.getText());
```

## 注意事项

1. **API Key**: 需要配置阿里云 DashScope API Key，可在[阿里云控制台](https://dashscope.console.aliyun.com/)获取。

2. **Chat UI**: 应用启动后会自动显示 Chat UI 访问地址，可以直接在浏览器中使用。

3. **Agent 框架**: 使用 Spring AI Alibaba 的 ReactAgent 框架，支持自动工具调用和推理，无需手动编排工具调用逻辑。

4. **工具扩展**: 可以通过实现工具接口来添加新的工具，Agent 会自动识别并在需要时调用。

5. **对话记忆**: Agent 支持对话记忆功能，可以保持多轮对话的上下文。

## 扩展开发

### 添加新工具

1. 在 `tools` 包下创建新的工具类
2. 实现工具接口
3. 在 Agent 配置中注册工具

### 自定义拦截器

可以实现自定义拦截器来监控和处理工具调用过程。