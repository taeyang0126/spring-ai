# 环境配置

## 必需的环境变量

### Spring AI（OpenAI 兼容 API）

```bash
# API 密钥（必需）
export OPENAI_API_KEY=your-api-key-here

# API 基础 URL（可选，用于使用兼容 API）
export OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode
```

### MongoDB（spring-ai-example 模块）

```bash
# MongoDB 主机
export MONGO_HOST=localhost

# MongoDB 用户名
export MONGO_USER=your-mongo-username

# MongoDB 密码
export MONGO_PWD=your-mongo-password

# MongoDB 端口（可选，默认 27017）
export MONGO_PORT=27017

# MongoDB 数据库（可选，默认 spring_ai）
export MONGO_DATABASE=spring_ai
```

### OpenSearch（rag-etl-opensearch 模块）

```bash
# OpenSearch 主机
export OPENSEARCH_HOST=localhost

# OpenSearch 用户名
export OPENSEARCH_USERNAME=admin

# OpenSearch 密码
export OPENSEARCH_PASSWORD=your-opensearch-password

# OpenSearch 端口（可选，默认 9200）
export OPENSEARCH_PORT=9200

# OpenSearch 协议（可选，默认 https）
export OPENSEARCH_PROTOCOL=https
```

## 配置文件

### application.yml 示例

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: ${OPENAI_BASE_URL}
      chat:
        model: qwen3-max

  data:
    mongodb:
      host: ${MONGO_HOST}
      username: ${MONGO_USER}
      password: ${MONGO_PWD}
      port: ${MONGO_PORT}
      database: ${MONGO_DATABASE}
```

## 测试基础设施

测试使用 `OpenAiApiBase` 类模式进行 API 初始化。

### OpenAiApiBase

- **位置**：`spring-ai-example/src/test/java/com/lei/learn/spring/ai/OpenAiApiBase.java`
- **功能**：提供从环境变量配置的静态 `OpenAiApi` 实例
- **用途**：测试类继承或引用此基类以保持一致的设置

### 测试配置示例

```java
public class ChatTests extends OpenAiApiBase {

    @Test
    void test_chat() {
        // 使用配置好的 OpenAiApi 实例
    }
}
```

## 本地开发环境

### 推荐的开发工具

- JDK 21+
- Maven 3.8+
- IDE：IntelliJ IDEA 或 VS Code
- MongoDB 7.0+
- OpenSearch 2.x

### 启动顺序

1. 启动 MongoDB
2. 启动 OpenSearch（如需 RAG 功能）
3. 启动 MCP 服务器（如需工具调用）
4. 启动主应用

### 快速启动脚本

```bash
#!/bin/bash
# start-all.sh

# 1. 设置环境变量
source .env

# 2. 启动 MongoDB
docker-compose up -d mongodb

# 3. 启动 OpenSearch
docker-compose up -d opensearch

# 4. 启动 MCP 服务器
java -jar mcp-weather-server/target/mcp-weather-server.jar &

# 5. 启动主应用
mvn spring-boot:run -pl spring-ai-example
```

## 环境变量最佳实践

1. **使用 .env 文件**：本地开发使用 `.env` 文件，不要提交到 Git
2. **敏感信息**：API 密钥、密码等敏感信息不要硬编码
3. **默认值**：在 `application.yml` 中提供合理的默认值
4. **文档化**：在 README 中说明所需的环境变量

### .env.example

```bash
# 复制此文件为 .env 并填写实际值
OPENAI_API_KEY=sk-xxx
OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode
MONGO_HOST=localhost
MONGO_USER=admin
MONGO_PWD=password
OPENSEARCH_HOST=localhost
OPENSEARCH_USERNAME=admin
OPENSEARCH_PASSWORD=password
```
