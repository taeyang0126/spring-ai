# Text2SQL Agent 实现计划

## 一、需求概述

使用 Spring AI Alibaba 框架实现一个 Text2SQL Agent，支持：
- **目标数据库**：MySQL
- **表结构**：动态获取（运行时读取元数据）
- **架构模式**：StateGraph（多节点工作流）
- **输出方式**：两阶段确认（生成 SQL → 用户确认 → 执行）

---

## 二、模块设计

### 2.1 模块结构

```
spring-ai-alibaba-text2sql-agent/
├── pom.xml                                      # Maven 配置
├── src/main/java/com/lei/learn/spring/ai/text2sql/
│   ├── Text2SqlApplication.java                 # Spring Boot 启动类
│   ├── config/
│   │   ├── Text2SqlConfiguration.java           # Bean 配置
│   │   └── Text2SqlProperties.java              # 配置属性
│   ├── model/
│   │   ├── TableMetadata.java                   # 表元数据
│   │   ├── ColumnMetadata.java                  # 列元数据
│   │   └── AgentState.java                      # Agent 状态
│   ├── tool/
│   │   ├── SchemaTool.java                      # Schema 获取工具
│   │   └── SqlExecutorTool.java                 # SQL 执行工具
│   ├── utils/
│   │   └── SqlValidator.java                    # SQL 验证工具
│   ├── prompt/
│   │   └── Text2SqlPrompts.java                 # Prompt 模板
│   ├── agent/
│   │   └── Text2SqlAgent.java                   # Agent 主类（StateGraph）
│   ├── controller/
│   │   └── Text2SqlController.java              # REST 控制器
│   └── dto/
│       ├── QueryRequest.java                    # 查询请求
│       ├── SqlConfirmationRequest.java          # 确认请求
│       └── QueryResponse.java                   # 响应
├── src/main/resources/
│   └── application.yml                          # 应用配置
└── src/test/
    └── init.sql                                 # 测试数据库初始化脚本
```

### 2.2 Maven 依赖

```xml
<dependencies>
    <!-- Spring AI Alibaba Graph Core -->
    <dependency>
        <groupId>com.alibaba.cloud.ai</groupId>
        <artifactId>spring-ai-alibaba-graph-core</artifactId>
    </dependency>

    <!-- Spring AI Alibaba DashScope Starter -->
    <dependency>
        <groupId>com.alibaba.cloud.ai</groupId>
        <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
    </dependency>

    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot JDBC -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>

    <!-- MySQL Connector -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

### 2.3 根 pom.xml 修改

在 `/Users/wulei/IdeaProjects/ai/spring-ai/pom.xml` 的 `<modules>` 中添加：
```xml
<module>spring-ai-alibaba-text2sql-agent</module>
```

---

## 三、StateGraph 工作流设计

### 3.1 工作流图

```
                    ┌─────────────┐
                    │    START    │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ fetch_schema│  获取数据库元数据
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ generate_sql│  调用 LLM 生成 SQL
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │wait_confirm │ ◄──┐  等待用户确认
                    └──────┬──────┘    │
                           │           │
              ┌────────────┴────────┐  │
              │                      │  │
         用户确认               用户拒绝  │
              │                      │  │
              ▼                      └──┘
      ┌─────────────┐                 │
      │ execute_sql │                 │  执行 SQL 查询
      └──────┬──────┘                 │
             │                        │
             ▼                        │
      ┌─────────────┐                 │
      │format_result│                 │  格式化查询结果
      └──────┬──────┘                 │
             │                        │
             ▼                        │
      ┌─────────────┐                 │
      │    END      │                 │
      └─────────────┘                 │
                                     │
                        返回 generate_sql 节点
```

### 3.2 状态定义

```java
/**
 * Agent 状态
 *
 * @param question              用户问题（ReplaceStrategy）
 * @param schema                数据库 Schema JSON（ReplaceStrategy）
 * @param sql                   生成的 SQL（ReplaceStrategy）
 * @param results               查询结果（ReplaceStrategy）
 * @param confirmationStatus    确认状态：confirmed/rejected（ReplaceStrategy）
 * @param error                 错误信息（ReplaceStrategy）
 * @param messages              对话历史（AppendStrategy）
 * @param currentStep           当前步骤（ReplaceStrategy）
 */
public record AgentState(
    String question,
    String schema,
    String sql,
    List<Map<String, Object>> results,
    String confirmationStatus,
    String error,
    List<Map<String, String>> messages,
    String currentStep
) {}
```

### 3.3 KeyStrategy 配置

```java
KeyStrategyFactory keyStrategyFactory = () -> {
    Map<String, KeyStrategy> strategies = new HashMap<>();
    strategies.put("question", new ReplaceStrategy());
    strategies.put("schema", new ReplaceStrategy());
    strategies.put("sql", new ReplaceStrategy());
    strategies.put("results", new ReplaceStrategy());
    strategies.put("confirmationStatus", new ReplaceStrategy());
    strategies.put("error", new ReplaceStrategy());
    strategies.put("currentStep", new ReplaceStrategy());
    strategies.put("messages", new AppendStrategy());
    return strategies;
};
```

---

## 四、核心类设计

### 4.1 SchemaTool.java

```java
package com.lei.learn.spring.ai.text2sql.tool;

import com.alibaba.cloud.ai.tool.Tool;
import com.alibaba.cloud.ai.tool.ToolParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * 数据库 Schema 获取工具
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SchemaTool {

    private final DataSource dataSource;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取数据库表结构信息
     *
     * @return JSON 格式的 Schema
     */
    @Tool(description = "获取数据库表结构信息，包括表名、列名、数据类型、注释等")
    public String getSchema() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            List<Map<String, Object>> schema = new ArrayList<>();

            // 获取所有表
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                Map<String, Object> tableInfo = new LinkedHashMap<>();
                String tableName = tables.getString("TABLE_NAME");
                tableInfo.put("tableName", tableName);
                tableInfo.put("tableComment", tables.getString("REMARKS"));

                // 获取列信息
                List<Map<String, Object>> columns = new ArrayList<>();
                ResultSet cols = metaData.getColumns(null, null, tableName, null);
                while (cols.next()) {
                    Map<String, Object> colInfo = new LinkedHashMap<>();
                    colInfo.put("columnName", cols.getString("COLUMN_NAME"));
                    colInfo.put("dataType", cols.getString("TYPE_NAME"));
                    colInfo.put("columnSize", cols.getInt("COLUMN_SIZE"));
                    colInfo.put("nullable", cols.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    colInfo.put("comment", cols.getString("REMARKS"));
                    columns.add(colInfo);
                }
                tableInfo.put("columns", columns);
                schema.add(tableInfo);
            }

            return objectMapper.writeValueAsString(schema);

        } catch (Exception e) {
            log.error("[SchemaTool] 获取 Schema 失败", e);
            throw new RuntimeException("获取数据库 Schema 失败: " + e.getMessage(), e);
        }
    }
}
```

### 4.2 SqlExecutorTool.java

```java
package com.lei.learn.spring.ai.text2sql.tool;

import com.alibaba.cloud.ai.tool.Tool;
import com.alibaba.cloud.ai.tool.ToolParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * SQL 执行工具
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SqlExecutorTool {

    private final DataSource dataSource;

    /**
     * 执行查询 SQL
     *
     * @param sql SELECT 查询语句
     * @return 查询结果
     */
    @Tool(description = "执行 MySQL SELECT 查询语句，返回查询结果")
    public List<Map<String, Object>> executeQuery(
        @ToolParam(description = "要执行的 SELECT SQL 语句") String sql
    ) {
        log.info("[SqlExecutorTool] 执行 SQL: {}", sql);

        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }

            log.info("[SqlExecutorTool] 查询成功，返回 {} 条结果", results.size());

        } catch (SQLException e) {
            log.error("[SqlExecutorTool] SQL 执行失败: {}", sql, e);
            throw new RuntimeException("SQL 执行失败: " + e.getMessage(), e);
        }

        return results;
    }
}
```

### 4.3 SqlValidator.java

```java
package com.lei.learn.spring.ai.text2sql.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * SQL 安全验证工具
 */
@Slf4j
public class SqlValidator {

    /** 危险关键字黑名单 */
    private static final String[] DANGEROUS_KEYWORDS = {
        "DROP", "DELETE", "UPDATE", "INSERT", "TRUNCATE",
        "ALTER", "CREATE", "EXEC", "EXECUTE", "SCRIPT",
        "GRANT", "REVOKE"
    };

    /**
     * SQL 安全检查
     *
     * @param SQL 语句
     * @return 是否安全
     */
    public static boolean isSafe(String sql) {
        if (sql == null || sql.isBlank()) {
            return false;
        }

        String upperSql = sql.toUpperCase().trim();

        // 只允许 SELECT 查询
        if (!upperSql.startsWith("SELECT")) {
            log.warn("[SqlValidator] 拒绝非 SELECT 语句: {}", sql);
            return false;
        }

        // 黑名单关键字检查
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                log.warn("[SqlValidator] 检测到危险关键字 {}: {}", keyword, sql);
                return false;
            }
        }

        // 防止多语句注入
        if (sql.contains(";")) {
            log.warn("[SqlValidator] 检测到多语句注入: {}", sql);
            return false;
        }

        // 防止注释注入
        if (sql.contains("--") || sql.contains("/*")) {
            log.warn("[SqlValidator] 检测到注释注入: {}", sql);
            return false;
        }

        return true;
    }

    /**
     * 清理 SQL（去除 markdown 格式）
     *
     * @param 原始 SQL
     * @return 清理后的 SQL
     */
    public static String cleanSql(String sql) {
        if (sql == null) {
            return "";
        }
        return sql.replaceAll("```sql", "")
                  .replaceAll("```", "")
                  .trim();
    }

    /**
     * 检查是否为 SELECT 语句
     *
     * @param SQL 语句
     * @return 是否为 SELECT
     */
    public static boolean isSelect(String sql) {
        return sql != null && sql.toUpperCase().trim().startsWith("SELECT");
    }
}
```

### 4.4 Text2SqlPrompts.java

```java
package com.lei.learn.spring.ai.text2sql.prompt;

/**
 * Text2SQL Prompt 模板
 */
public class Text2SqlPrompts {

    /**
     * 构建 SQL 生成 Prompt
     *
     * @param question 用户问题
     * @param schema   数据库 Schema（JSON 格式）
     * @return Prompt
     */
    public static String buildSqlGenerationPrompt(String question, String schema) {
        return """
            你是一个专业的 MySQL 数据库专家。根据用户的自然语言问题和数据库 Schema，生成准确的 SQL 查询语句。

            ## 数据库 Schema
            %s

            ## 用户问题
            %s

            ## 要求
            1. 只生成 SELECT 查询语句，不要生成其他类型的 SQL
            2. 直接输出 SQL 语句，不要包含任何解释或 markdown 格式
            3. 确保语法正确，符合 MySQL 标准
            4. 使用表别名提高可读性
            5. 添加适当的 WHERE 条件过滤数据
            6. 对于字符串比较使用 LIKE，对于精确匹配使用 =
            7. 注意表之间的关联关系（根据外键判断）
            8. 限制返回结果数量（使用 LIMIT，默认 100 条）

            ## 输出格式
            直接输出 SQL 语句，例如：
            SELECT u.name, o.order_date FROM users u JOIN orders o ON u.id = o.user_id WHERE u.status = 'active' LIMIT 100
            """.formatted(schema, question);
    }

    /**
     * 构建结果格式化 Prompt
     *
     * @param results 查询结果
     * @return Prompt
     */
    public static String buildResultFormattingPrompt(List<Map<String, Object>> results) {
        String resultsJson = results.toString();

        return """
            请将以下 SQL 查询结果格式化为自然语言回复：

            ## 查询结果
            %s

            ## 要求
            1. 用简洁的自然语言总结查询结果
            2. 突出关键数据和趋势
            3. 如果结果为空，说明没有找到符合条件的数据
            4. 如果结果很多，可以统计总数并给出前几条示例
            5. 使用表格或列表形式展示数据（Markdown 格式）

            ## 输出格式
            直接输出格式化后的文本，使用 Markdown 格式。
            """.formatted(resultsJson);
    }

    /**
     * 构建重新生成 Prompt（用户拒绝后）
     *
     * @param previousSql 上一次生成的 SQL
     * @param feedback    用户反馈
     * @param schema      数据库 Schema
     * @return Prompt
     */
    public static String buildRegenerationPrompt(String previousSql, String feedback, String schema) {
        return """
            用户对之前生成的 SQL 不满意，请根据反馈重新生成。

            ## 数据库 Schema
            %s

            ## 之前生成的 SQL（有错误）
            %s

            ## 用户反馈
            %s

            请根据用户反馈重新生成 SQL，直接输出 SQL 语句。
            """.formatted(schema, previousSql, feedback);
    }
}
```

### 4.5 Text2SqlAgent.java

```java
package com.lei.learn.spring.ai.text2sql.agent;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.lei.learn.spring.ai.text2sql.prompt.Text2SqlPrompts;
import com.lei.learn.spring.ai.text2sql.tool.SchemaTool;
import com.lei.learn.spring.ai.text2sql.tool.SqlExecutorTool;
import com.lei.learn.spring.ai.text2sql.utils.SqlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Text2SQL Agent 主类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class Text2SqlAgent {

    private final ChatModel chatModel;
    private final SchemaTool schemaTool;
    private final SqlExecutorTool sqlExecutorTool;
    private final CompiledGraph graph;
    private final Map<String, RunnableConfig> sessionConfigs = new HashMap<>();

    public Text2SqlAgent(ChatModel chatModel, SchemaTool schemaTool, SqlExecutorTool sqlExecutorTool) {
        this.chatModel = chatModel;
        this.schemaTool = schemaTool;
        this.sqlExecutorTool = sqlExecutorTool;
        this.graph = buildGraph();
    }

    /**
     * 构建 StateGraph
     */
    private CompiledGraph buildGraph() {
        // KeyStrategy 配置
        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put("question", new ReplaceStrategy());
            strategies.put("schema", new ReplaceStrategy());
            strategies.put("sql", new ReplaceStrategy());
            strategies.put("results", new ReplaceStrategy());
            strategies.put("confirmationStatus", new ReplaceStrategy());
            strategies.put("error", new ReplaceStrategy());
            strategies.put("currentStep", new ReplaceStrategy());
            strategies.put("messages", new AppendStrategy());
            return strategies;
        };

        // 节点定义
        var fetchSchemaNode = node_async(state -> {
            log.info("[Text2SqlAgent] 开始获取 Schema");
            String schema = schemaTool.getSchema();
            return Map.of(
                "schema", schema,
                "currentStep", "schema_fetched"
            );
        });

        var generateSqlNode = node_async(state -> {
            String question = (String) state.value("question").orElse("");
            String schema = (String) state.value("schema").orElse("");

            log.info("[Text2SqlAgent] 生成 SQL，问题: {}", question);

            String prompt = Text2SqlPrompts.buildSqlGenerationPrompt(question, schema);
            ChatClient chatClient = ChatClient.create(chatModel);
            String rawSql = chatClient.prompt().user(prompt).call().content();
            String sql = SqlValidator.cleanSql(rawSql);

            log.info("[Text2SqlAgent] 生成的 SQL: {}", sql);

            return Map.of(
                "sql", sql,
                "currentStep", "sql_generated"
            );
        });

        var executeSqlNode = node_async(state -> {
            String sql = (String) state.value("sql").orElse("");

            log.info("[Text2SqlAgent] 执行 SQL: {}", sql);

            if (!SqlValidator.isSafe(sql)) {
                return Map.of(
                    "error", "SQL 安全检查失败",
                    "currentStep", "error"
                );
            }

            List<Map<String, Object>> results = sqlExecutorTool.executeQuery(sql);

            return Map.of(
                "results", results,
                "currentStep", "sql_executed"
            );
        });

        // 构建图
        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
            .addNode("fetch_schema", fetchSchemaNode)
            .addNode("generate_sql", generateSqlNode)
            .addNode("execute_sql", executeSqlNode)
            .addEdge(START, "fetch_schema")
            .addEdge("fetch_schema", "generate_sql")
            .addEdge("generate_sql", END);

        return stateGraph.compile(CompileConfig.builder().build());
    }

    /**
     * 提交查询
     */
    public Map<String, Object> submitQuery(String question) {
        String conversationId = UUID.randomUUID().toString();
        RunnableConfig config = RunnableConfig.builder()
            .threadId(conversationId)
            .build();

        sessionConfigs.put(conversationId, config);

        // 执行到 generate_sql 节点结束
        Optional<OverAllState> result = graph.invoke(
            Map.of("question", question),
            config
        );

        String sql = result.flatMap(s -> s.value("sql")).orElse("");
        String currentStep = result.flatMap(s -> s.value("currentStep")).orElse("");

        return Map.of(
            "conversationId", conversationId,
            "status", "awaiting_confirmation",
            "sql", sql,
            "currentStep", currentStep
        );
    }

    /**
     * 确认并执行
     */
    public Map<String, Object> confirmAndExecute(String conversationId, boolean confirmed, String feedback) {
        RunnableConfig config = sessionConfigs.get(conversationId);
        if (config == null) {
            throw new IllegalArgumentException("会话不存在: " + conversationId);
        }

        if (confirmed) {
            // 获取当前状态
            Optional<OverAllState> currentState = graph.getState(config);
            String sql = currentState.flatMap(s -> s.value("sql")).orElse("");

            // 直接执行
            List<Map<String, Object>> results = sqlExecutorTool.executeQuery(sql);

            return Map.of(
                "conversationId", conversationId,
                "status", "completed",
                "results", results
            );
        } else {
            // 重新生成（当前简化处理，返回让用户重新提交）
            return Map.of(
                "conversationId", conversationId,
                "status", "cancelled",
                "message", "请重新提交查询"
            );
        }
    }
}
```

### 4.6 Text2SqlController.java

```java
package com.lei.learn.spring.ai.text2sql.controller;

import com.lei.learn.spring.ai.text2sql.agent.Text2SqlAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Text2SQL REST 控制器
 */
@RestController
@RequestMapping("/text2sql")
@Slf4j
@RequiredArgsConstructor
public class Text2SqlController {

    private final Text2SqlAgent text2SqlAgent;

    /**
     * 提交查询
     */
    @PostMapping("/query")
    public Map<String, Object> submitQuery(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        log.info("[Text2SqlController] 收到查询: {}", question);

        return text2SqlAgent.submitQuery(question);
    }

    /**
     * 确认并执行
     */
    @PostMapping("/confirm")
    public Map<String, Object> confirmAndExecute(@RequestBody Map<String, Object> request) {
        String conversationId = (String) request.get("conversationId");
        Boolean confirmed = (Boolean) request.get("confirmed");
        String feedback = (String) request.get("feedback");

        log.info("[Text2SqlController] 收到确认: conversationId={}, confirmed={}", conversationId, confirmed);

        return text2SqlAgent.confirmAndExecute(conversationId, confirmed, feedback);
    }

    /**
     * 获取 Schema（调试用）
     */
    @GetMapping("/schema")
    public Map<String, String> getSchema(@Autowired SchemaTool schemaTool) {
        return Map.of("schema", schemaTool.getSchema());
    }
}
```

---

## 五、配置文件

### 5.1 application.yml

```yaml
spring:
  application:
    name: text2sql-agent

  datasource:
    url: jdbc:mysql://${TEXT2SQL_MYSQL_HOST:localhost}:${TEXT2SQL_MYSQL_PORT:3306}/${TEXT2SQL_MYSQL_DATABASE:text2sql_test}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${TEXT2SQL_MYSQL_USER:root}
    password: ${TEXT2SQL_MYSQL_PASSWORD:123456}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

server:
  port: ${TEXT2SQL_SERVER_PORT:8002}

logging:
  level:
    com.lei.learn.spring.ai.text2sql: DEBUG
    com.alibaba.cloud.ai: DEBUG
```

### 5.2 Text2SqlConfiguration.java

```java
package com.lei.learn.spring.ai.text2sql.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Text2SQL 配置类
 */
@Configuration
public class Text2SqlConfiguration {

    @Value("${AI_DASHSCOPE_API_KEY}")
    private String apiKey;

    @Value("${text2sql.model:qwen-plus}")
    private String model;

    @Bean
    public ChatModel chatModel() {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
            .apiKey(apiKey)
            .build();

        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
            .withModel(model)
            .build();

        return DashScopeChatModel.builder()
            .dashScopeApi(dashScopeApi)
            .defaultOptions(chatOptions)
            .build();
    }
}
```

---

## 六、测试数据库

### 6.1 初始化脚本（src/test/resources/init.sql）

```sql
-- 创建测试数据库
CREATE DATABASE IF NOT EXISTS text2sql_test
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE text2sql_test;

-- 删除已存在的表
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS customers;

-- 客户表
CREATE TABLE customers (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '客户ID',
    name VARCHAR(100) NOT NULL COMMENT '客户姓名',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    city VARCHAR(50) COMMENT '城市',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/inactive',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='客户表';

-- 产品表
CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '产品ID',
    name VARCHAR(200) NOT NULL COMMENT '产品名称',
    category VARCHAR(50) COMMENT '产品类别',
    price DECIMAL(10, 2) NOT NULL COMMENT '价格',
    stock INT DEFAULT 0 COMMENT '库存数量',
    description TEXT COMMENT '产品描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='产品表';

-- 订单表
CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    customer_id INT COMMENT '客户ID',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    total_amount DECIMAL(10, 2) COMMENT '订单总额',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '订单状态：pending/paid/shipped/completed/cancelled',
    FOREIGN KEY (customer_id) REFERENCES customers(id)
) COMMENT='订单表';

-- 订单明细表
CREATE TABLE order_items (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    order_id INT COMMENT '订单ID',
    product_id INT COMMENT '产品ID',
    quantity INT COMMENT '数量',
    price DECIMAL(10, 2) COMMENT '单价',
    subtotal DECIMAL(10, 2) COMMENT '小计',
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
) COMMENT='订单明细表';

-- 插入测试数据
INSERT INTO customers (name, email, phone, city, status) VALUES
    ('张三', 'zhangsan@example.com', '13800138001', '北京', 'active'),
    ('李四', 'lisi@example.com', '13800138002', '上海', 'active'),
    ('王五', 'wangwu@example.com', '13800138003', '广州', 'inactive'),
    ('赵六', 'zhaoliu@example.com', '13800138004', '深圳', 'active'),
    ('孙七', 'sunqi@example.com', '13800138005', '杭州', 'active');

INSERT INTO products (name, category, price, stock, description) VALUES
    ('iPhone 15 Pro', '电子产品', 7999.00, 50, '苹果最新手机'),
    ('MacBook Pro 14寸', '电子产品', 14999.00, 30, '苹果笔记本电脑'),
    ('AirPods Pro', '电子产品', 1899.00, 100, '苹果无线耳机'),
    ('Nike 运动鞋', '服装', 599.00, 200, '耐克运动鞋'),
    ('Adidas 运动服', '服装', 399.00, 150, '阿迪达斯运动服'),
    ('Sony 降噪耳机', '电子产品', 2299.00, 80, '索尼无线降噪耳机'),
    ('小米平板', '电子产品', 1999.00, 60, '小米平板电脑'),
    ('华为手表', '电子产品', 1299.00, 90, '华为智能手表');

INSERT INTO orders (customer_id, order_date, total_amount, status) VALUES
    (1, '2024-01-01 10:00:00', 9798.00, 'completed'),
    (1, '2024-01-05 14:30:00', 14999.00, 'completed'),
    (2, '2024-01-10 09:15:00', 1899.00, 'completed'),
    (2, '2024-01-15 16:20:00', 599.00, 'shipped'),
    (3, '2024-01-20 11:00:00', 399.00, 'pending'),
    (4, '2024-01-25 13:45:00', 2299.00, 'paid'),
    (5, '2024-02-01 10:30:00', 1999.00, 'completed'),
    (1, '2024-02-05 15:00:00', 1299.00, 'shipped');

INSERT INTO order_items (order_id, product_id, quantity, price, subtotal) VALUES
    (1, 1, 1, 7999.00, 7999.00),
    (1, 3, 1, 1899.00, 1899.00),
    (2, 2, 1, 14999.00, 14999.00),
    (3, 3, 1, 1899.00, 1899.00),
    (4, 4, 1, 599.00, 599.00),
    (5, 5, 1, 399.00, 399.00),
    (6, 6, 1, 2299.00, 2299.00),
    (7, 7, 1, 1999.00, 1999.00),
    (8, 8, 1, 1299.00, 1299.00);
```

---

## 七、环境变量

```bash
# DashScope API
export AI_DASHSCOPE_API_KEY=your-dashscope-api-key

# MySQL 配置
export TEXT2SQL_MYSQL_HOST=localhost
export TEXT2SQL_MYSQL_PORT=3306
export TEXT2SQL_MYSQL_DATABASE=text2sql_test
export TEXT2SQL_MYSQL_USER=root
export TEXT2SQL_MYSQL_PASSWORD=your-mysql-password

# 服务端口
export TEXT2SQL_SERVER_PORT=8002

# 模型配置（可选）
export TEXT2SQL_MODEL=qwen-plus
```

---

## 八、API 测试示例

### 8.1 提交查询

```bash
curl -X POST http://localhost:8002/text2sql/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "查询所有电子产品"
  }'
```

响应：
```json
{
  "conversationId": "abc-123-def",
  "status": "awaiting_confirmation",
  "sql": "SELECT * FROM products WHERE category = '电子产品' LIMIT 100",
  "currentStep": "sql_generated"
}
```

### 8.2 确认并执行

```bash
curl -X POST http://localhost:8002/text2sql/confirm \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": "abc-123-def",
    "confirmed": true
  }'
```

响应：
```json
{
  "conversationId": "abc-123-def",
  "status": "completed",
  "results": [
    {"id": 1, "name": "iPhone 15 Pro", "category": "电子产品", "price": 7999.00, "stock": 50},
    {"id": 2, "name": "MacBook Pro 14寸", "category": "电子产品", "price": 14999.00, "stock": 30}
  ]
}
```

### 8.3 获取 Schema（调试）

```bash
curl http://localhost:8002/text2sql/schema
```

---

## 九、实现顺序

1. **模块骨架** - 创建目录结构和 pom.xml
2. **数据模型** - TableMetadata、ColumnMetadata、DTO
3. **工具层** - SchemaTool、SqlExecutorTool、SqlValidator
4. **Prompt** - Text2SqlPrompts
5. **Agent** - Text2SqlAgent（StateGraph）
6. **接口** - Text2SqlController
7. **配置** - Text2SqlConfiguration、application.yml
8. **测试** - 准备测试数据库并验证

---

## 十、验证清单

- [ ] 模块编译成功
- [ ] 应用启动成功
- [ ] GET /text2sql/schema 返回正确的表结构 JSON
- [ ] POST /text2sql/query 生成有效的 SQL
- [ ] SQL 安全检查生效（拒绝危险语句）
- [ ] POST /text2sql/confirm 执行成功并返回结果
- [ ] 处理 SQL 执行错误的情况
- [ ] 多轮会话状态正确管理

---

## 十一、已知简化

当前计划中的简化处理（可在后续迭代中完善）：

1. **两阶段确认**：简化为 Agent 执行到 generate_sql 后暂停，由 Controller 决定是否继续
2. **重新生成**：用户拒绝时暂不支持自动重新生成，需重新提交
3. **结果格式化**：暂不使用 LLM 格式化结果，直接返回 JSON
4. **多轮对话**：暂不支持基于历史记录的上下文对话
5. **会话管理**：使用内存 Map 存储，重启后丢失

---

## 十二、关键参考文件

| 文件 | 用途 |
|------|------|
| `spring-ai-alibaba-graph/src/test/java/com/lei/learn/spring/ai/alibaba/graph/MemoryTests.java` | StateGraph + Store + AsyncNodeActionWithConfig |
| `spring-ai-alibaba-graph/src/test/java/com/lei/learn/spring/ai/alibaba/graph/OverAllStateTests.java` | KeyStrategy 配置 |
| `spring-ai-example/src/main/java/com/lei/learn/spring/ai/tool/DateTimeTools.java` | @Tool 注解模式 |
