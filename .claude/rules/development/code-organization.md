# 代码组织

## 标准包结构

```
com.lei.learn.spring.ai/
├── advisor/          # 自定义 Advisor
├── configuration/    # Spring @Configuration
├── controller/       # REST 控制器
├── env/              # 环境变量支持
├── mcp/              # MCP 客户端/服务端
├── memory/           # 对话记忆
├── model/            # DTO 和领域模型
├── repository/       # 数据访问
├── support/          # 常量和枚举
├── tool/             # 函数调用工具
└── utils/            # 工具类
```

## rag-etl-core 结构

```
com.lei.learn.etl.core/
├── model/            # 数据模型
├── pipeline/         # 管道定义
│   ├── RagPipeline.java              # 抽象基类
│   ├── ResourceLoadingStage.java     # 资源加载接口
│   ├── TextSplittingStage.java       # 文本分割接口
│   ├── VectorStoringStage.java       # 向量存储接口
│   └── markdown/
│       └── MarkdownRagPipeline.java   # Markdown 实现
```

## Lombok 使用

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

常用注解：
- `@Data` - getter/setter/toString/equals
- `@Builder` - 构建器模式
- `@AllArgsConstructor/@NoArgsConstructor` - 构造器
- `@Slf4j` - 日志对象
- `@RequiredArgsConstructor` - 必需参数构造器
