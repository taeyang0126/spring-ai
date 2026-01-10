# 重要说明

## 1. 端口分配

各模块运行在不同端口，避免端口冲突：

| 模块 | 端口 | 说明 |
|-----|------|-----|
| spring-ai-example | 9999 | 主应用端口 |
| mcp-weather-server | 9001 | MCP 服务端口 |
| spring-ai-alibaba-weather-agent | 8001 | 阿里云智能体端口 |
| rag-etl-opensearch | 7001 | OpenSearch 服务端口 |

**注意**：同时运行多个模块时，确保端口不冲突。如需修改端口，请编辑 `application.yml`。

## 2. 模块独立性

每个模块可独立运行，但某些功能需要同时启动客户端和服务端：

### MCP 功能需要同时启动

```bash
# 终端 1：启动 MCP 天气服务
java -jar mcp-weather-server/target/mcp-weather-server.jar

# 终端 2：启动主应用
mvn spring-boot:run -pl spring-ai-example
```

### RAG 功能需要 OpenSearch

```bash
# 启动 OpenSearch
docker-compose up -d opensearch

# 启动 RAG 服务
mvn spring-boot:run -pl rag-etl-opensearch
```

## 3. API 兼容性

项目通过 `OPENAI_BASE_URL` 配置使用 DashScope（阿里云）作为 OpenAI 兼容 API。

### 配置示例

```bash
export OPENAI_API_KEY=sk-xxx
export OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode
```

### 支持的模型

- **qwen3-max** - 文本生成，支持函数调用
- **qwen3-omni-flash** - 多模态理解

## 4. 中文注释

代码库包含中文注释和文档，这是为了中文团队有意设计的。

### 注释规范

- 所有 public 类和方法必须添加 Javadoc 注释
- 复杂逻辑必须添加行内注释说明
- 注释使用中文编写

### 示例

```java
/**
 * <p>
 * 聊天控制器
 * </p>
 * <p>
 * 提供 AI 对话接口，支持多模态交互和函数调用
 * </p>
 *
 * @author 伍磊
 */
public class ChatController {

    /**
     * 发送聊天消息
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    public ChatResponse chat(ChatRequest request) {
        // 检查请求参数有效性
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        // ...
    }
}
```

## 5. Java 21 特性

项目使用了现代 Java 特性，包括：

### Record（Java 16+）

```java
public record ChatRequest(String message, String conversationId) {
}
```

### 模式匹配（Java 21）

```java
if (obj instanceof String s) {
    // 可以直接使用 s
}
```

### 虚拟线程（Java 21）

```java
Thread.ofVirtual().start(() -> {
    // 虚拟线程中的任务
});
```

### var 类型推断（Java 10+）

```java
var message = "Hello";
var documents = vectorStore.similaritySearch(query);
```

### Text Blocks（Java 15+）

```java
String json = """
    {
        "message": "Hello",
        "user": "World"
    }
    """;
```

## 6. 环境要求

### 必需软件

- JDK 21+
- Maven 3.8+
- MongoDB 7.0+（对话记忆）
- OpenSearch 2.x（RAG 功能）

### 可选软件

- Docker（容器化部署）
- Docker Compose（编排容器）
- IntelliJ IDEA 或 VS Code（IDE）

## 7. 开发注意事项

### 启动顺序

1. 启动基础设施（MongoDB、OpenSearch）
2. 启动 MCP 服务器（如需工具调用）
3. 启动应用模块

### 日志查看

```bash
# 实时查看日志
tail -f logs/application.log

# 查看错误日志
grep ERROR logs/application.log
```

### 性能调优

- 调整 JVM 参数：`-Xmx2g -Xms2g`
- 启用 JIT 编译优化
- 使用连接池管理数据库连接

## 8. 故障排查

### 常见问题

**问题 1：端口被占用**
```bash
# 查找占用端口的进程
lsof -i :9999

# 杀死进程
kill -9 <PID>
```

**问题 2：MongoDB 连接失败**
```bash
# 检查 MongoDB 是否运行
ps aux | grep mongod

# 检查环境变量
echo $MONGO_HOST
echo $MONGO_USER
```

**问题 3：OpenAI API 调用失败**
```bash
# 检查 API 密钥
echo $OPENAI_API_KEY

# 检查基础 URL
echo $OPENAI_BASE_URL

# 测试 API 连接
curl -X POST $OPENAI_BASE_URL/v1/chat/completions \
  -H "Authorization: Bearer $OPENAI_API_KEY"
```

## 9. 安全注意事项

### 敏感信息

- 不要将 API 密钥提交到 Git
- 使用环境变量管理敏感信息
- `.env` 文件已在 `.gitignore` 中

### 权限控制

- MongoDB 设置强密码
- OpenSearch 使用认证
- API 密钥定期轮换

## 10. 获取帮助

### 文档资源

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [阿里云 DashScope 文档](https://help.aliyun.com/zh/dashscope/)
- [OpenSearch 文档](https://opensearch.org/docs/)

### 问题反馈

- 在项目 Issues 中提问
- 查看 Wiki 获取更多信息
- 参考示例代码学习用法
