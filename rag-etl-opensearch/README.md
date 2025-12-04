# rag-etl-opensearch

基于 OpenSearch 向量存储的 RAG（检索增强生成）示例，使用阿里云 DashScope Embedding 模型，集成 rag-etl-core 核心模块实现文档 ETL 处理和向量存储。

## 核心技术

### 向量数据库
- **OpenSearch**: 作为向量存储数据库，提供高效的向量相似性搜索

### AI 模型
- **阿里云 DashScope Embedding 模型**: 用于文本向量化，将文档内容转换为向量表示

### 核心依赖
- **rag-etl-core**: 提供通用的 RAG ETL 管道组件，支持 Markdown 文档标准化处理

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
rag-etl-opensearch/
├── src/
│   └── main/
│       ├── java/com/lei/learn/etl/opensearch/
│       │   ├── controller/                   # REST API 控制器
│       │   ├── service/                      # 业务服务层
│       │   └── RagETLOpenSearchApplication.java
│       └── resources/
│           ├── certs/                        # OpenSearch CA 证书
│           └── document/                     # 示例 Markdown 文档
└── pom.xml
```

**核心说明**: 本模块专注于 OpenSearch 向量存储集成和 DashScope 模型应用，实际的 RAG Pipeline 实现完全依赖 rag-etl-core 模块。

## 核心实现

### 架构说明

本模块作为 OpenSearch 向量存储的实现层，主要职责：

1. **OpenSearch 向量存储配置**: 配置和管理 OpenSearch 向量数据库连接
2. **DashScope Embedding 模型集成**: 使用阿里云 DashScope 进行文本向量化
3. **REST API 提供**: 通过 HTTP 接口暴露文档处理能力
4. **rag-etl-core 集成**: 核心 ETL 处理逻辑完全依赖 rag-etl-core 模块

### 向量存储配置

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

### 文档处理流程

1. **API 接收请求**: 通过 REST API 接收文档处理请求
2. **调用 rag-etl-core**: 使用 MarkdownRagPipeline 进行标准化的文档处理
3. **向量存储**: 处理后的向量存储到 OpenSearch 数据库
4. **返回结果**: 向客户端返回处理状态和结果

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

使用上述 API 接口处理本地 Markdown 文件：

```bash
curl -X POST http://localhost:7001/api/markdown/process \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "/path/to/your/document.md",
    "metadata": {
      "author": "作者姓名"
    }
  }'
```

### 直接编程集成 rag-etl-core

```java
@Autowired
private VectorStore vectorStore;

public void processMarkdown(String filePath) {
    // 直接使用 rag-etl-core 的 MarkdownRagPipeline
    MarkdownRagPipeline.builder()
            .withHorizontalRuleCreateDocument(false)
            .withIncludeCodeBlock(true)
            .withIncludeBlockquote(true)
            .withAdditionalMetadata("source", "manual-upload")
            .build()
            .fromFile(filePath)
            .toVectorStore(vectorStore);
}
```

**说明**: 以上代码展示了如何直接使用 rag-etl-core 模块进行文档处理，这是推荐的方式，rag-etl-opensearch 模块主要提供 API 包装。

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

1. **OpenSearch 配置**:
   - 需要先启动 OpenSearch 服务
   - 正确配置连接信息和证书
   - 确保向量索引已创建

2. **DashScope Embedding 模型**:
   - 需要配置阿里云 DashScope API Key
   - 确保 API 调用配额充足
   - 注意模型调用频率限制

3. **rag-etl-core 依赖**:
   - 本模块依赖 rag-etl-core 进行核心文档处理
   - 详细配置和使用方式请参考 rag-etl-core 模块文档

4. **性能优化**:
   - 合理设置文本分割大小以优化向量质量
   - 考虑使用批处理提高处理效率
   - 监控 OpenSearch 索引大小和查询性能


## 扩展开发

### 与其他向量数据库集成

可以替换 OpenSearch 为其他向量数据库：
- Pinecone
- Weaviate
- Chroma
- Qdrant

替换方式：
1. 修改 Spring AI 配置
2. 更新 VectorStore Bean 配置
3. 保持 rag-etl-core 接口不变

### 自定义 Embedding 模型

可以替换 DashScope 模型为其他 Embedding 服务：
- OpenAI Embeddings
- Azure OpenAI
- 本地部署的 Embedding 模型

## RAG 应用场景

本模块结合 OpenSearch 向量存储和 DashScope Embedding 模型，适用于：

- **文档问答**: 基于 OpenSearch 向量搜索的智能问答
- **语义搜索**: 基于 DashScope 语义理解的文档搜索
- **知识库构建**: 企业文档知识库系统
- **内容推荐**: 基于向量相似性的内容推荐系统

## 学习资源

- [Spring AI OpenSearch 向量存储](https://docs.spring.io/spring-ai/reference/api/vectordbs/opensearch.html)
- [DashScope Embedding 模型](https://help.aliyun.com/zh/dashscope/developer-reference/text-embedding)
- [rag-etl-core 模块文档](../rag-etl-core/README.md)
