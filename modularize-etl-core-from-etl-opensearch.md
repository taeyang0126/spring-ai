### 抽取 rag-etl-core 模块行动计划

基于对 `etl-opensearch` 模块的深入分析，我制定了以下重构计划：

#### 当前项目状态分析
- ✅ 已分析 etl-opensearch 的完整代码结构
- ✅ 识别了可抽取的通用组件和特定于 OpenSearch 的组件
- ✅ 确认了现有的管道设计模式已经具有良好的模块化基础

#### 重构目标
从 `etl-opensearch` 中抽取通用的 RAG ETL 组件，创建 `rag-etl-core` 模块，使后续可以轻松支持 Elasticsearch 等其他向量存储系统。

#### 详细实施步骤

**步骤 1: 创建 rag-etl-core 模块结构** ↻
- 在项目根目录下创建 `rag-etl-core` 模块目录
- 创建标准的 Maven 模块结构（src/main/java, src/main/resources, pom.xml）
- 配置模块依赖和项目基本信息
- pom 依赖满足最低需求，不要增加特定的模型、向量库

**步骤 2: 移动通用管道组件到 rag-etl-core**
- 移动 `RagPipeline` 抽象类（核心管道逻辑）
- 移动所有管道阶段接口：
  - `ResourceLoadingStage`
  - `TextSplittingStage` 
  - `VectorStoringStage`
- 更新包名为 `com.lei.learn.etl.core.pipeline.*`

**步骤 3: 移动通用模型类**
- 移动请求/响应模型类到 core 模块
- 移动 Markdown 特定的配置也移到 core

**步骤 4: 移动 MarkdownRagPipeline *
- 将 MarkdownRagPipeline 移动到 core

**步骤 5: 更新 etl-opensearch 模块**
- 修改 `etl-opensearch` 的 pom.xml，添加对 `rag-etl-core` 的依赖
- 更新所有导入语句的包名
- 保留 OpenSearch 特定的配置和实现

**步骤 6: 更新父级 POM**
- 在父级 `pom.xml` 中添加 `rag-etl-core` 模块声明
- 确保模块依赖关系正确

**步骤 7: 测试和验证**
- 编译整个项目确保没有依赖问题
- 运行测试确保功能正常
- 验证 OpenSearch ETL 功能仍然工作

#### 模块设计原则
1. **通用性**: rag-etl-core 专注于管道逻辑，不依赖特定向量存储
2. **可扩展性**: 提供接口和抽象类，方便新存储系统的接入
3. **配置驱动**: 通过配置控制行为，最小化硬编码
4. **向后兼容**: 不破坏现有的 OpenSearch 功能

#### 预期的最终结构
```
spring-ai/
├── rag-etl-core/                    # 新创建的通用核心模块
│   ├── src/main/java/com/lei/learn/etl/core/
│   │   ├── pipeline/                # 通用管道组件
│   │   └── model/                   # 通用模型
│   └── pom.xml
├── etl-opensearch/                  # 更新后依赖 core 模块
├── etl-elasticsearch/               # 未来可快速创建的模块
└── ...
```

这个计划将为项目提供良好的模块化架构，支持多种向量存储系统的快速接入。