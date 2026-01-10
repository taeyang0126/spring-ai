# 架构模式

## 1. Advisor 模式（横切关注点）

Spring AI 广泛使用 Advisor 来修改 LLM 行为。

### 内置 Advisor

- `MessageChatMemoryAdvisor` - 对话历史管理
- `QuestionAnswerAdvisor` - 简单 RAG 问答
- `RetrievalAugmentationAdvisor` - 高级 RAG，支持重排序
- `PromptChatMemoryAdvisor` - 提示词记忆管理

### 自定义 Advisor

- `UserContextAdvisor` - 用户上下文注入（本项目实现）

### 使用示例

```java
ChatClient.builder(chatModel)
    .defaultAdvisors(
        new MessageChatMemoryAdvisor(chatMemory),
        new UserContextAdvisor(userContext)
    )
    .build();
```

## 2. 双 ChatClient 模式

主模块定义了两个专门的 `ChatClient` Bean：

### textChatClient

- **用途**：纯文本模型
- **能力**：支持函数调用和工具
- **模型**：qwen3-max
- **配置位置**：`ChatClientConfiguration.java`

### fullChatClient

- **用途**：多模态模型（文本 + 图片）
- **限制**：不支持函数调用
- **模型**：qwen3-omni-flash
- **适用场景**：图片理解、视觉问答

### 选择策略

```java
// 需要函数调用 → 使用 textChatClient
textChatClient.prompt()
    .functions("getCurrentTime", "getUserProfile")
    .call()
    .content();

// 需要图片理解 → 使用 fullChatClient
fullChatClient.prompt()
    .user(userMessage, imageResource)
    .call()
    .content();
```

## 3. RAG 管道模式

`rag-etl-core` 模块定义了用于文档处理的抽象 `RagPipeline`。

### 三阶段流程

```
资源加载 → 文本分割 → 向量存储
   ↓          ↓          ↓
Resource  TextSplitter  VectorStore
```

### 接口设计

```java
ResourceLoadingStage → fromResource/fromFile
     ↓
TextSplittingStage → withTextSplitter
     ↓
VectorStoringStage → toVectorStore
```

### 实现示例

```java
MarkdownRagPipeline.builder()
    .fromFile("/path/to/document.md")
    .withTextSplitter(tokenTextSplitter)
    .toVectorStore(vectorStore);
```

## 4. 智能体工作流模式（Spring AI Alibaba）

`spring-ai-alibaba-graph` 模块展示了 LangGraph 风格的工作流。

### 核心组件

- `StateGraph` - 智能体状态机
- `State` - 状态数据容器
- `NodeFunction` - 节点处理函数
- `EdgeCondition` - 边条件逻辑
- `MemorySaver` - 记忆持久化

### 状态管理

- `ReplaceStrategy` - 替换状态
- `AppendStrategy` - 追加状态
- `MergeStrategy` - 合并状态

### 流式输出

```java
StreamingOutput output = (state, out) -> {
    out.write(chunk);
};
```

## 5. MCP 集成

模型上下文协议用于外部工具连接。

### 客户端配置

```yaml
spring:
  ai:
    mcp:
      client:
        servers:
          - name: weather-server
            stdio:
              command: java -jar mcp-weather-server.jar
```

### 服务器实现

- 位置：`mcp-weather-server` 模块
- 协议：STDIO 通信
- 工具：天气查询相关函数

### 工具使用

LLM 可以动态调用 MCP 暴露的工具，无需额外配置。

## 架构原则

1. **模块化**：每个模块职责单一，高内聚低耦合
2. **可扩展**：基于接口和抽象类，易于扩展
3. **配置驱动**：通过配置文件控制行为
4. **测试友好**：核心逻辑与基础设施分离