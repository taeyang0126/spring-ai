# 代码组织

## 标准包结构（每个模块）

```
com.lei.learn.spring.ai/              # 主模块包结构
├── advisor/                          # 自定义 Advisor 实现
│   └── UserContextAdvisor.java
├── configuration/                    # Spring @Configuration 类
│   ├── ChatClientConfiguration.java
│   ├── OpenAiConfiguration.java
│   └── VectorStoreConfiguration.java
├── controller/                       # REST 控制器
│   └── ChatController.java
├── env/                              # 环境变量支持
│   └── OpenAiEnv.java
├── mcp/                              # MCP 客户端/服务端代码
│   ├── client/                       # MCP 客户端
│   └── server/                       # MCP 服务端
├── memory/                           # 对话记忆实现
│   └── CustomerMongoChatMemoryRepository.java
├── model/                            # DTO 和领域模型
│   ├── request/                      # 请求 DTO
│   └── response/                     # 响应 DTO
├── repository/                       # 数据访问层
│   └── ChatDocumentRepository.java
├── support/                          # 常量和枚举
│   └── Constants.java
├── tool/                             # 函数调用工具
│   ├── DateTimeTools.java
│   └── UserTools.java
└── utils/                            # 工具类
    └── JsonUtils.java
```

## 跨模块包结构

### rag-etl-core（核心抽象库）

```
com.lei.learn.etl.core/
├── model/                            # 数据模型
│   └── MarkdownProcessRequest.java
├── pipeline/                         # 管道定义
│   ├── RagPipeline.java             # 抽象基类
│   ├── ResourceLoadingStage.java    # 资源加载接口
│   ├── TextSplittingStage.java      # 文本分割接口
│   ├── VectorStoringStage.java      # 向量存储接口
│   └── markdown/
│       └── MarkdownRagPipeline.java  # Markdown 实现
```

### rag-etl-opensearch（向量存储实现）

```
com.lei.learn.rag.etl.opensearch/
├── config/                          # OpenSearch 配置
├── repository/                      # 向量存储实现
└── service/                         # ETL 服务
```

## 分层架构

```
┌─────────────────────────────────────┐
│  Controller 层（Web 接口）           │
├─────────────────────────────────────┤
│  Service 层（业务逻辑）              │
├─────────────────────────────────────┤
│  Repository 层（数据访问）           │
├─────────────────────────────────────┤
│  Infrastructure 层（基础设施）        │
│  - VectorStore、MongoDB、外部 API    │
└─────────────────────────────────────┘
```

## 命名约定

### 包命名

- 全部小写
- 单词间用点分隔
- 使用域名反转规则
- 示例：`com.lei.learn.spring.ai.controller`

### 类命名

- 使用 UpperCamelCase（帕斯卡命名法）
- 接口可使用 I 前缀（可选）
- 示例：`ChatController`、`IChatService`

### 方法命名

- 使用 lowerCamelCase（驼峰命名法）
- 动词开头
- 示例：`getUserById()`、`validateInput()`

### 变量命名

- 使用 lowerCamelCase（驼峰命名法）
- 布尔变量以 is/has/can 开头
- 示例：`isValid`、`hasPermission`

### 常量命名

- 全部大写
- 单词间用下划线分隔
- 示例：`MAX_RETRY_COUNT`、`DEFAULT_TIMEOUT`

## Lombok 配置

### 注解处理器配置

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>${maven-compiler-plugin.version}</version>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### Lombok 使用

- **@Data** - 数据类（getter/setter/toString/equals）
- **@Builder** - 构建器模式
- **@AllArgsConstructor** - 全参构造器
- **@NoArgsConstructor** - 无参构造器
- **@Slf4j** - 日志对象
- **@RequiredArgsConstructor** - 必需参数构造器（配合 final 字段）

### 示例

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String message;
    private String conversationId;
    private Map<String, Object> context;
}
```

## 测试代码组织

### 测试包结构

```
src/test/java/
├── com/lei/learn/spring/ai/
│   ├── ChatTests.java               # 功能测试
│   ├── OpenAiApiBase.java          # 测试基类
│   └── controller/
│       └── ChatControllerTest.java # 控制器测试
```

### 测试命名约定

- 测试类：`XxxTests` 或 `XxxTest`
- 测试方法：`test_方法名_场景`
- 示例：`test_chat_with_memory`

### 测试分层

```
单元测试（Unit Tests）
    ↓
集成测试（Integration Tests）
    ↓
端到端测试（E2E Tests）
```

## 资源文件组织

### 配置文件

```
src/main/resources/
├── application.yml                 # 主配置文件
├── application-dev.yml             # 开发环境配置
├── application-prod.yml            # 生产环境配置
└── logback-spring.xml              # 日志配置
```

### 静态资源

```
src/main/resources/
├── static/                         # 静态文件
│   ├── css/
│   ├── js/
│   └── images/
└── templates/                      # 模板文件
```

## 依赖管理

### 模块间依赖

```xml
<!-- rag-etl-opensearch 依赖 rag-etl-core -->
<dependency>
    <groupId>com.lei.learn</groupId>
    <artifactId>rag-etl-core</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 依赖版本管理

在父 POM 的 `<dependencyManagement>` 中统一管理版本。

## 代码复用策略

1. **抽象基类**：`rag-etl-core` 提供抽象
2. **接口定义**：定义清晰的接口契约
3. **工具类**：通用工具放在 `utils` 包
4. **配置类**：共享配置放在独立的配置模块