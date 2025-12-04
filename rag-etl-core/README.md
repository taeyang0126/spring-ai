# RAG ETL Core

## 项目简介

RAG ETL Core 是一个提供通用 RAG（检索增强生成）ETL 管道组件和接口的核心模块。该模块基于 Spring AI 构建，专注于文档处理和向量存储的标准化流程，特别针对 Markdown 文档提供了完整的处理管道。

## 技术特性

- **模块化设计**：采用管道模式，支持可插拔的组件架构
- **Markdown 支持**：专门优化的 Markdown 文档处理器
- **灵活配置**：提供丰富的配置选项满足不同需求
- **Spring AI 集成**：完全基于 Spring AI 生态系统构建
- **批处理支持**：内置批处理机制，提高大数据量处理性能

## 核心组件

### 1. 数据模型

#### MarkdownProcessRequest
Markdown 文档处理请求对象，包含以下配置：

- **filePath**: 文件路径（本地文件系统路径）
- **splitterConfig**: 文本分割器配置
  - `chunkSize`: 分块大小（默认 800）
  - `minChunkSizeChars`: 最小分块字符数（默认 300）
  - `minChunkLengthToEmbed`: 最小嵌入长度（默认 5）
  - `maxNumChunks`: 最大分块数量（默认 10000）
  - `keepSeparator`: 是否保留分隔符（默认 true）
- **markdownConfig**: Markdown 配置
  - `horizontalRuleCreateDocument`: 遇到横线是否创建新文档（默认 false）
  - `includeCodeBlock`: 是否包含代码块（默认 true）
  - `includeBlockquote`: 是否包含引用块（默认 true）
- **metadata**: 额外的元数据

#### MarkdownProcessResponse
Markdown 文档处理响应对象：

- **success**: 是否成功
- **message**: 处理消息
- **documentCount**: 处理的文档数量
- **chunkCount**: 处理的分块数量
- **filePath**: 文件路径
- **processingTime**: 处理耗时（毫秒）

### 2. 管道接口

#### ResourceLoadingStage
资源加载阶段接口：

```java
TextSplittingStage fromResource(Resource resource);
TextSplittingStage fromFile(File file);
TextSplittingStage fromFile(String filepath);
```

#### TextSplittingStage
文本分割阶段接口：

```java
VectorStoringStage withTextSplitter(TextSplitter splitter);
```

#### VectorStoringStage
向量存储阶段接口：

```java
void toVectorStore(VectorStore vectorStore);
```

### 3. 核心实现

#### RagPipeline
抽象管道基类，实现了完整的 ETL 流程：

1. **读取**：使用 DocumentReader 读取文档
2. **分割**：使用 TextSplitter 分割文本
3. **存储**：批量存储到 VectorStore

#### MarkdownRagPipeline
专门处理 Markdown 文档的管道实现：

```java
// 基本用法
MarkdownRagPipeline.defaultConfig()
    .fromFile("path/to/markdown/file.md")
    .withTextSplitter(splitter)
    .toVectorStore(vectorStore);

// 高级配置
MarkdownRagPipeline.builder()
    .withHorizontalRuleCreateDocument(true)
    .withIncludeCodeBlock(true)
    .withIncludeBlockquote(true)
    .withAdditionalMetadata("source", "manual")
    .build()
    .fromFile("path/to/file.md")
    .withTextSplitter(splitter)
    .toVectorStore(vectorStore);
```

## 依赖项

```xml
<!-- Spring AI 向量存储顾问 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-advisors-vector-store</artifactId>
</dependency>

<!-- Markdown 文档读取器 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-markdown-document-reader</artifactId>
</dependency>

<!-- Spring Boot Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

## 使用示例

### 基本 Markdown 文档处理

```java
@Autowired
private VectorStore vectorStore;

public void processMarkdownFile(String filePath) {
    MarkdownRagPipeline.defaultConfig()
        .fromFile(filePath)
        .toVectorStore(vectorStore);
}
```

### 配置化处理

```java
public void processMarkdownWithConfig(String filePath) {
    MarkdownProcessRequest request = new MarkdownProcessRequest();
    request.setFilePath(filePath);

    MarkdownProcessRequest.SplitterConfig splitterConfig = new MarkdownProcessRequest.SplitterConfig();
    splitterConfig.setChunkSize(1000);
    splitterConfig.setMinChunkSizeChars(500);
    request.setSplitterConfig(splitterConfig);

    MarkdownProcessRequest.MarkdownConfig markdownConfig = new MarkdownProcessRequest.MarkdownConfig();
    markdownConfig.setHorizontalRuleCreateDocument(true);
    markdownConfig.setIncludeCodeBlock(true);
    request.setMarkdownConfig(markdownConfig);

    // 使用配置创建自定义管道
    Map<String, Object> metadata = Map.of("category", "documentation");
    MarkdownRagPipeline.builder()
        .withHorizontalRuleCreateDocument(true)
        .withIncludeCodeBlock(true)
        .withAdditionalMetadata(metadata)
        .build()
        .fromFile(filePath)
        .withTextSplitter(customSplitter)
        .toVectorStore(vectorStore);
}
```

### 批量处理多个文件

```java
public void processMultipleFiles(List<String> filePaths) {
    for (String filePath : filePaths) {
        try {
            MarkdownRagPipeline.defaultConfig()
                .fromFile(filePath)
                .toVectorStore(vectorStore);
        } catch (Exception e) {
            log.error("处理文件失败: {}", filePath, e);
        }
    }
}
```

## 配置说明

### Markdown 处理配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `horizontalRuleCreateDocument` | boolean | false | 遇到横线（---）时是否创建新文档 |
| `includeCodeBlock` | boolean | true | 是否包含代码块内容 |
| `includeBlockquote` | boolean | true | 是否包含引用块内容 |

### 文本分割配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `chunkSize` | Integer | 800 | 每个分块的最大字符数 |
| `minChunkSizeChars` | Integer | 300 | 分块的最小字符数 |
| `minChunkLengthToEmbed` | Integer | 5 | 最小嵌入长度 |
| `maxNumChunks` | Integer | 10000 | 最大分块数量 |
| `keepSeparator` | Boolean | true | 是否保留分隔符 |

## 注意事项

1. **文件路径**：支持绝对路径和相对路径，确保文件存在且为普通文件（非目录）

2. **资源管理**：模块自动管理资源释放，无需手动关闭

3. **批处理**：默认批处理大小为 10，可通过重写 `getBatchSize()` 方法自定义

4. **异常处理**：处理过程中遇到异常会记录详细日志并抛出，请确保捕获处理

5. **内存考虑**：处理大文件时注意内存使用，可调整批处理大小和分块配置
