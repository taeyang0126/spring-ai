# 项目概述

Spring AI 多模块学习项目，展示对话、多模态、函数调用、MCP 集成、RAG 管道和智能体工作流。

**技术栈**：Spring Boot 3.5.8、Spring AI 1.1.0-M4、Java 21

## 模块结构

| 模块 | 端口 | 说明 |
|------|------|------|
| `spring-ai-example` | 9999 | 核心功能：对话、多模态、函数调用、MCP 集成、MongoDB 记忆 |
| `mcp-weather-server` | 9001 | MCP 天气服务 |
| `spring-ai-alibaba-weather-agent` | 8001 | 阿里云智能体 |
| `rag-etl-core` | - | RAG ETL 抽象库 |
| `rag-etl-opensearch` | 7001 | OpenSearch 向量存储 |
| `spring-ai-alibaba-graph` | - | LangGraph 风格智能体工作流 |

## 依赖关系

```
spring-ai-example
├── rag-etl-core (抽象)
├── rag-etl-opensearch (向量存储实现)
└── mcp-weather-server (独立服务)
```

## 学习路径

1. spring-ai-example → 基础功能
2. mcp-weather-server → MCP 集成
3. rag-etl-core → RAG 抽象
4. rag-etl-opensearch → 向量存储实现
5. spring-ai-alibaba-graph → 智能体工作流
