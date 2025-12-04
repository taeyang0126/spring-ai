# Spring AI

## 项目简介

Spring AI 学习项目，这是一个多模块的 Maven 项目，包含多个示例模块，涵盖 Spring AI 的各种核心功能和应用场景。

## 技术栈

- **Java**: 21
- **Spring Boot**: 3.5.8
- **Spring AI**: 1.1.0-M4
- **Spring AI Alibaba**: 1.1.0.0-M5
- **Maven**: 项目构建工具（多模块项目）
- **Lombok**: 简化 Java 代码

## 项目模块

本项目包含以下五个模块：

| 模块 | 说明                                                 | 详细文档 |
|------|----------------------------------------------------|----------|
| [spring-ai-example](./spring-ai-example) | Spring AI 核心功能示例：文本对话、多模态、Function Calling、MCP 集成等 | [README](./spring-ai-example/README.md) |
| [mcp-weather-server](./mcp-weather-server) | MCP 服务器实现示例，提供天气查询工具服务                             | [README](./mcp-weather-server/README.md) |
| [spring-ai-alibaba-weather-agent](./spring-ai-alibaba-weather-agent) | 基于 Spring AI Alibaba 的智能体框架示例，实现天气查询 Agent         | [README](./spring-ai-alibaba-weather-agent/README.md) |
| [rag-etl-core](./rag-etl-core) | RAG ETL 核心模块，提供通用的管道组件和接口，支持 Markdown 文档处理         | [README](./rag-etl-core/README.md) |
| [rag-etl-opensearch](./rag-etl-opensearch) | OpenSearch + DashScope 向量存储示例，演示文档 ETL 处理和向量存储 | [README](./rag-etl-opensearch/README.md) |



## 快速开始

### 环境要求

- JDK 21 或更高版本
- Maven 3.6+
- MongoDB 数据库（spring-ai-example 模块需要）
- OpenSearch（rag-etl-opensearch 模块需要）
- OpenAI API Key 或阿里云 DashScope API Key

### 构建项目

```bash
git clone <repository-url>
cd spring-ai
mvn clean install
```

## 注意事项

1. **API 密钥安全**: 生产环境请使用环境变量或密钥管理服务，不要将密钥硬编码在配置文件中。

2. **模块依赖**: 各模块相对独立，可以单独运行。如果需要使用 MCP 功能，需要同时启动 `spring-ai-example` 和 `mcp-weather-server`。

3. **端口冲突**: 注意各模块的默认端口：
   - `spring-ai-example`: 9999
   - `mcp-weather-server`: 9001
   - `spring-ai-alibaba-weather-agent`: 8001
   - `rag-etl-opensearch`: 7001
   - `rag-etl-core`: 无独立运行端口（核心依赖模块）

更多详细的注意事项和配置说明，请参考各模块的 README 文档。


## 学习资源

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/index.html)
- [Spring AI Alibaba 官方文档](https://java2ai.com/)



