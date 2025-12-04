# etl-opensearch

基于 OpenSearch 的 RAG（检索增强生成）示例，演示文档 ETL 处理和向量存储。

## 功能特性

- **文档加载（Resource Loading）**: 支持从多种来源加载文档
- **文本分割（Text Splitting）**: 智能文本分割，优化向量存储
- **向量存储（Vector Storing）**: 向量化并存储到 OpenSearch
- **Markdown 文档处理**: 专门支持 Markdown 文档的处理
- **RAG Pipeline 实现**: 完整的 RAG 数据处理流程
- **REST API 接口**: 提供 HTTP 接口处理本地 Markdown 文件

## 技术栈

- **Spring AI OpenSearch Vector Store**: OpenSearch 向量存储集成
- **Spring AI Alibaba DashScope**: 阿里云 DashScope Embedding 模型
- **Markdown Document Reader**: Markdown 文档读取器
- **Spring Boot**: 3.5.8
- **Java**: 21
- **Lombok**: 简化 Java 代码

## 环境要求

- JDK 21 或更高版本
- Maven 3.6+
- OpenSearch 服务
- 阿里云 DashScope API Key

## 快速开始

### 1. 启动 OpenSearch

确保 OpenSearch 服务已启动并可访问。

### 2. 配置环境变量

```bash
export DASHSCOPE_API_KEY=your-dashscope-api-key
export OPEN_SEARCH_URI=https://localhost:9200
export OPEN_SEARCH_USER=admin
export OPEN_SEARCH_PWD=your-opensearch-password
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，配置 OpenSearch 连接信息：

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
    vectorstore:
      opensearch:
        url: ${OPEN_SEARCH_URI}
        username: ${OPEN_SEARCH_USER}
        password: ${OPEN_SEARCH_PWD}
```

### 4. 启动应用

```bash
mvn spring-boot:run
```

## 配置说明

### OpenSearch 配置

- **URL**: OpenSearch 服务地址
- **用户名**: OpenSearch 用户名
- **密码**: OpenSearch 密码
- **索引名称**: 向量存储的索引名称

### Embedding 配置

使用阿里云 DashScope 的 Embedding 模型进行文本向量化。

## 项目结构

```
etl-opensearch/
├── src/
│   └── main/
│       ├── java/com/lei/learn/etl/opensearch/
│       │   ├── controller/                   # 控制器
│       │   ├── model/                        # 数据模型
│       │   ├── pipeline/                     # ETL Pipeline
│       │   │   ├── markdown/                 # Markdown 处理
│       │   │   ├── RagPipeline.java          # RAG Pipeline 接口
│       │   │   ├── ResourceLoadingStage.java # 资源加载阶段
│       │   │   ├── TextSplittingStage.java   # 文本分割阶段
│       │   │   └── VectorStoringStage.java   # 向量存储阶段
│       │   ├── service/                      # 服务层
│       │   └── EtlOpenSearchApplication.java
│       └── resources/
│           └── certs/                        # OpenSearch CA 证书
└── pom.xml
```

## 核心实现

### RAG Pipeline

实现了完整的 RAG 数据处理流程：

#### 1. ResourceLoadingStage

负责加载文档资源，支持多种文档格式：
- Markdown 文档
- 文本文件
- 其他格式（可扩展）

#### 2. TextSplittingStage

智能文本分割，将长文档分割成适合向量化的片段：
- 保持语义完整性
- 控制片段大小
- 支持重叠分割

#### 3. VectorStoringStage

向量化并存储到 OpenSearch：
- 使用 DashScope Embedding 模型
- 存储到 OpenSearch 向量索引
- 支持批量处理

### Markdown 处理

专门实现了 `MarkdownRagPipeline`，优化 Markdown 文档的处理：
- 保留 Markdown 结构
- 智能分割章节
- 提取元数据

## API 接口

### 处理本地 Markdown 文件

**接口地址**: `POST /api/markdown/process`

**请求示例**:

```json
{
  "filePath": "/path/to/your/document.md",
  "splitterConfig": {
    "chunkSize": 800,
    "minChunkSizeChars": 300,
    "minChunkLengthToEmbed": 5,
    "maxNumChunks": 10000,
    "keepSeparator": true
  },
  "markdownConfig": {
    "horizontalRuleCreateDocument": false,
    "includeCodeBlock": true,
    "includeBlockquote": true
  },
  "metadata": {
    "author": "张三",
    "category": "技术文档",
    "link": "https://example.com/doc"
  }
}
```

**响应示例**:

```json
{
  "success": true,
  "message": "文档处理成功",
  "filePath": "/path/to/your/document.md",
  "processingTime": 1234
}
```

**使用 curl 调用**:

```bash
curl -X POST http://localhost:8080/api/markdown/process \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "/path/to/your/document.md",
    "metadata": {
      "author": "张三"
    }
  }'
```

## 使用示例

### 通过 API 处理 Markdown 文档

使用上述 API 接口即可处理本地 Markdown 文件。

### 编程方式处理 Markdown 文档

```java
@Autowired
private VectorStore vectorStore;

@Test
public void processMarkdown() {
    // 1. 加载文档资源
    var documentResource = new ClassPathResource("document/example.md");
    
    // 2. 配置文本分割器
    TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(800)
            .withMinChunkSizeChars(300)
            .withMinChunkLengthToEmbed(5)
            .withMaxNumChunks(10000)
            .withKeepSeparator(true)
            .build();
    
    // 3. 构建 MarkdownRagPipeline 并执行
    MarkdownRagPipeline.builder()
            // 设置为 true，则遇到横线会划分为一个新的文档
            .withHorizontalRuleCreateDocument(false)
            // 设置为 true 时，代码块将包含在与周围文本相同的 Document 中
            .withIncludeCodeBlock(true)
            // 设置为 true 时，引用块将与周围文本包含在同一个 Document 中
            .withIncludeBlockquote(true)
            // 定义元数据
            .withAdditionalMetadata("filename", "example.md")
            .withAdditionalMetadata("link", "https://example.com")
            .build()
            .fromResource(documentResource)
            .withTextSplitter(splitter)
            .toVectorStore(vectorStore);
}
```

### 向量搜索示例

```java
@Autowired
private VectorStore vectorStore;

@Test
public void search() {
    // 构建搜索请求
    SearchRequest searchRequest = SearchRequest.builder()
            // 原始查询文本
            .query("mmap")
            // 最多返回多少个最相似的文档片段（chunks）
            .topK(5)
            // 只返回相似度 ≥ 此阈值的文档
            .similarityThreshold(0.55)
            .build();
    
    // 执行搜索
    List<Document> results = vectorStore.similaritySearch(searchRequest);
    for (Document result : results) {
        System.out.println(result.getScore() + " : " + result.getText());
    }
}
```

## 注意事项

1. **OpenSearch 配置**: 需要先启动 OpenSearch 服务，并正确配置连接信息。

2. **Embedding 模型**: 使用阿里云 DashScope 的 Embedding 模型，需要配置 API Key。

3. **文档处理**: 支持 Markdown 文档的加载、分割和向量化存储。

4. **性能优化**:
   - 批量处理文档可以提高效率
   - 合理设置文本分割大小
   - 考虑使用异步处理

5. **索引管理**:
   - 定期清理无用索引
   - 监控索引大小
   - 优化查询性能


## 扩展开发

### 添加新的文档格式支持

1. pdf
2. 支持图片向量化

### 自定义文本分割策略

可以实现自定义的文本分割器，优化特定类型文档的分割效果。

### 集成其他向量数据库

可以替换 OpenSearch 为其他向量数据库，如 Pinecone、Weaviate 等。

## RAG 应用场景

本模块处理的向量数据可用于：

- **文档问答**: 基于文档内容回答问题
- **语义搜索**: 根据语义相似度搜索文档
- **知识库构建**: 构建企业知识库
- **内容推荐**: 基于内容相似度推荐

## 学习资源

- [RAG](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html)
- [ETL Pipeline](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html)
