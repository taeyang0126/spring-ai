# 模块结构

## 模块列表

| 模块 | 端口 | 用途 |
|--------|------|---------|
| `spring-ai-example` | 9999 | Spring AI 核心功能：对话、多模态、函数调用、MCP 集成、MongoDB 记忆 |
| `mcp-weather-server` | 9001 | 提供天气工具的 MCP 服务器 |
| `spring-ai-alibaba-weather-agent` | 8001 | 集成 DashScope 的阿里云智能体 |
| `rag-etl-core` | - | 抽象 RAG ETL 管道组件（库模块） |
| `rag-etl-opensearch` | 7001 | OpenSearch 向量存储及 RAG ETL 实现 |
| `spring-ai-alibaba-graph` | - | LangGraph 风格的智能体工作流，支持状态管理 |

## 模块依赖关系

```
spring-ai-example (主应用)
├── rag-etl-core (核心抽象)
├── rag-etl-opensearch (向量存储实现)
└── mcp-weather-server (MCP 服务，独立运行)

spring-ai-alibaba-weather-agent (独立应用)
└── 依赖阿里云 DashScope API

spring-ai-alibaba-graph (独立模块)
└── 展示智能体工作流
```

## 端口分配

- 9999 - 主应用
- 9001 - MCP 天气服务
- 8001 - 阿里云智能体
- 7001 - OpenSearch RAG 服务

**注意**：同时运行多个模块时，确保端口不冲突。
