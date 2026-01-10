# 架构指南

## 核心模式

### 1. Advisor 模式
- `MessageChatMemoryAdvisor` - 对话历史
- `QuestionAnswerAdvisor` - 简单 RAG
- `RetrievalAugmentationAdvisor` - 高级 RAG
- `UserContextAdvisor` - 用户上下文（自定义）

### 2. 双 ChatClient
- `textChatClient` - 文本模型，支持函数调用（qwen3-max）
- `fullChatClient` - 多模态模型，不支持函数调用（qwen3-omni-flash）

### 3. RAG 管道
三阶段：资源加载 → 文本分割 → 向量存储

```java
MarkdownRagPipeline.builder()
    .fromFile("/path/to/doc.md")
    .withTextSplitter(splitter)
    .toVectorStore(vectorStore);
```

### 4. 智能体工作流
- `StateGraph` - 状态机
- `KeyStrategy` - ReplaceStrategy、AppendStrategy
- `MemorySaver` - 持久化

### 5. MCP 集成
- 客户端：`application.yml` 配置 `spring.ai.mcp.client`
- 服务端：`mcp-weather-server` 模块

## 配置要点

### 聊天模型
- `text-model`：qwen3-max（函数调用）
- `full-model`：qwen3-omni-flash（多模态）
- 切换：`ai.openai.chat-model-type` 属性

### 对话记忆
- `MessageWindowChatMemory`（默认 20 条）
- MongoDB 持久化
- `CONVERSATION_ID` 参数隔离会话

### 工具调用
- `FunctionToolCallback` 实现
- 内置工具：`DateTimeTools`、`UserTools`
- 仅文本模型支持