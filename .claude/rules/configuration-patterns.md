# 配置模式

## 聊天模型选项

模型通过 `OpenAiChatOptions` 配置。

### 可用模型

| 模型名称 | 类型 | 能力 | 用途 |
|---------|------|------|------|
| `qwen3-max` | 文本 | 函数调用、工具 | 复杂推理、工具调用 |
| `qwen3-omni-flash` | 多模态 | 文本 + 图片 | 图片理解、视觉问答 |

### 模型切换

通过 `ai.openai.chat-model-type` 属性切换：

```yaml
ai:
  openai:
    chat-model-type: text-model  # 或 full-model
```

### 配置示例

```java
@Bean
public ChatClient textChatClient(OpenAiChatModel chatModel) {
    return ChatClient.builder(chatModel)
        .defaultOptions(OpenAiChatOptions.builder()
            .withModel("qwen3-max")
            .withTemperature(0.7)
            .build())
        .build();
}
```

## 对话记忆

### MessageWindowChatMemory

- **类型**：滑动窗口记忆
- **默认大小**：20 条消息
- **用途**：保持有限的对话历史

### 持久化策略

通过 `CustomerMongoChatMemoryRepository` 实现 MongoDB 持久化。

### 会话隔离

通过 `CONVERSATION_ID` Advisor 参数限定对话范围：

```java
ChatClient.call()
    .prompt("用户问题")
    .advisors(a -> a
        .param(CONVERSATION_ID, "user-123"))
    .chatResponse();
```

## 工具/函数调用

### 实现方式

通过 `FunctionToolCallback` 实现工具调用。

### 内置工具

- `DateTimeTools` - 日期时间工具
- `UserTools` - 用户信息工具

### MCP 工具

通过客户端配置暴露 MCP 工具，LLM 可以动态调用。

### 工具调用限制

- **仅文本模型支持**：`textChatClient` 支持函数调用
- **多模态不支持**：`fullChatClient` 不支持函数调用

### 自定义工具示例

```java
@Component
public class WeatherTools {

    @Tool("获取指定城市的天气")
    public String getWeather(String city) {
        // 实现天气查询逻辑
        return weatherService.getWeather(city);
    }
}
```

## Advisor 配置

### Advisor 链

```java
ChatClient.builder(chatModel)
    .defaultAdvisors(
        new MessageChatMemoryAdvisor(chatMemory),
        new QuestionAnswerAdvisor(vectorStore),
        new UserContextAdvisor(userContext)
    )
    .build();
```

### Advisor 参数传递

```java
chatClient.prompt()
    .advisors(a -> a
        .param(CHAT_MEMORY_RETRIEVE_SIZE, 10)
        .param(CONVERSATION_ID, "session-123"))
    .call();
```

## RAG 配置

### 向量存储配置

```yaml
spring:
  ai:
    vectorstore:
      opensearch:
        client:
          hosts: ${OPENSEARCH_HOST}
          username: ${OPENSEARCH_USERNAME}
          password: ${OPENSEARCH_PASSWORD}
```

### 文档分割器配置

```java
TokenTextSplitter splitter = new TokenTextSplitter(
    800,    // chunk size
    300,    // min chunk size
    5,      // min chunk length to embed
    10000,  // max num chunks
    true    // keep separator
);
```

### 元数据处理

```java
Map<String, Object> metadata = new HashMap<>();
metadata.put("author", "张三");
metadata.put("category", "技术文档");
metadata.put("version", "1.0");
```

## 日志配置

### 日志级别

```yaml
logging:
  level:
    com.lei.learn.spring.ai: DEBUG
    org.springframework.ai: DEBUG
```

### 结构化日志

```java
@Slf4j
@Component
public class MyComponent {

    public void doSomething() {
        log.debug("[component] 执行操作 | param={}", param);
        log.info("[component] 操作成功 | result={}", result);
        log.error("[component] 操作失败 | error={}", error);
    }
}
```

## 性能调优

### 连接池配置

```yaml
spring:
  ai:
    openai:
      client:
        connect-timeout: 60s
        read-timeout: 300s
        max-connections: 50
```

### 缓存配置

```java
@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("llm-responses");
    }
}
```