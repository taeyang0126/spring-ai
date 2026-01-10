# 项目指南

Spring AI 多模块学习项目。

## 技术栈
Spring Boot 3.5.8、Spring AI 1.1.0-M4、Java 21

## 模块结构

| 模块 | 端口 | 说明 |
|------|------|------|
| `spring-ai-example` | 9999 | 核心功能：对话、多模态、函数调用、MCP、MongoDB |
| `mcp-weather-server` | 9001 | MCP 天气服务 |
| `spring-ai-alibaba-weather-agent` | 8001 | 阿里云智能体 |
| `rag-etl-core` | - | RAG ETL 抽象库 |
| `rag-etl-opensearch` | 7001 | OpenSearch 向量存储 |
| `spring-ai-alibaba-graph` | - | 智能体工作流 |

## 常用命令

### 构建
```bash
mvn clean install              # 构建全部
mvn clean install -pl spring-ai-example  # 构建指定模块
mvn clean install -DskipTests  # 跳过测试
```

### 运行
```bash
mvn spring-boot:run -pl spring-ai-example
```

### 测试
```bash
mvn test                       # 所有测试
mvn test -pl spring-ai-example  # 指定模块
mvn test -Dtest=ChatTests      # 指定测试类
```

## 环境变量
```bash
export OPENAI_API_KEY=your-key
export OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode
export MONGO_HOST=localhost
export MONGO_USER=your-user
export MONGO_PWD=your-password
export OPENSEARCH_HOST=localhost
export OPENSEARCH_USERNAME=admin
export OPENSEARCH_PASSWORD=your-password
```

## 学习路径
1. spring-ai-example → 基础
2. mcp-weather-server → MCP
3. rag-etl-core → RAG 抽象
4. rag-etl-opensearch → 向量存储
5. spring-ai-alibaba-graph → 智能体
