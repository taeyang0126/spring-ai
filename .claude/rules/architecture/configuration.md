# 环境配置

## 必需环境变量

```bash
# Spring AI（OpenAI 兼容 API）
export OPENAI_API_KEY=your-api-key-here
export OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode

# MongoDB（spring-ai-example）
export MONGO_HOST=localhost
export MONGO_USER=your-mongo-username
export MONGO_PWD=your-mongo-password

# OpenSearch（rag-etl-opensearch）
export OPENSEARCH_HOST=localhost
export OPENSEARCH_USERNAME=admin
export OPENSEARCH_PASSWORD=your-password
```

## 配置文件

位置：`spring-ai-example/src/main/resources/application.yml`

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
```

## 测试基础设施

- 基类：`OpenAiApiBase.java`
- 位置：`spring-ai-example/src/test/java/com/lei/learn/spring/ai/`
- 功能：提供从环境变量配置的静态 `OpenAiApi` 实例