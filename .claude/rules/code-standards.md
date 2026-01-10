# 代码规范

## Alibaba Java 规范

本项目严格遵循 [《阿里巴巴 Java 开发手册》](https://github.com/alibaba/p3c) 规范。

### 命名规范

**类命名**
- 使用 UpperCamelCase（帕斯卡命名法）
- 示例：`ChatController`、`UserRepository`

**方法命名**
- 使用 lowerCamelCase（驼峰命名法）
- 动词开头
- 示例：`getUserById()`、`validateInput()`

**变量命名**
- 使用 lowerCamelCase（驼峰命名法）
- 示例：`userName`、`maxCount`

**常量命名**
- 全部大写
- 单词间用下划线分隔
- 示例：`MAX_RETRY_COUNT`、`DEFAULT_TIMEOUT`

**包命名**
- 全部小写
- 单词间用点分隔
- 示例：`com.lei.learn.spring.ai.controller`

### 注释规范

**Javadoc 注释**
- 所有 public 类必须添加类注释
- 所有 public 方法必须添加方法注释
- 注释说明"做什么"、"为什么"、"注意点"

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
 * @since 1.0.0
 */
public class ChatController {

    /**
     * 发送聊天消息
     *
     * @param request 聊天请求
     * @return 聊天响应
     * @throws IllegalArgumentException 请求参数无效时抛出
     */
    public ChatResponse chat(ChatRequest request) {
        // 实现
    }
}
```

**行内注释**
- 复杂逻辑必须添加注释说明
- 注释使用中文编写
- 注释放在代码上方，与代码对齐

```java
// 检查会话 ID 是否有效
if (conversationId == null || conversationId.isEmpty()) {
    throw new IllegalArgumentException("会话 ID 不能为空");
}

// 从向量存储中检索相关文档
List<Document> documents = vectorStore.similaritySearch(query);
```

### 代码风格

**缩进**
- 使用 4 个空格
- 不使用 Tab

**大括号**
- 左括号不换行（K&R 风格）
- 右括号换行
- 即使只有一行语句也使用大括号

```java
// ✅ 正确
if (condition) {
    doSomething();
}

// ❌ 错误
if (condition)
{
    doSomething();
}
```

**行长度**
- 每行代码长度不超过 120 个字符
- 超出时进行换行，缩进 8 个空格

```java
// ✅ 正确
String result = someVeryLongMethodName(param1, param2,
        param3, param4);

// ❌ 错误
String result = someVeryLongMethodName(param1, param2, param3, param4);
```

**类型推断**
- 优先使用 `var` 进行局部变量类型推断（Java 10+）
- 类型明确时使用 `var`
- 类型不明确时显式声明类型

```java
// ✅ 推荐
var message = "Hello";
var documents = vectorStore.similaritySearch(query);

// ✅ 类型不明确时显式声明
ChatClient chatClient = ChatClient.builder(chatModel).build();
```

## 最佳实践

### 避免魔法值

使用常量定义魔法值：

```java
// ❌ 错误
if (user.getAge() > 18) {
    // 成年人逻辑
}

// ✅ 正确
private static final int ADULT_AGE = 18;

if (user.getAge() > ADULT_AGE) {
    // 成年人逻辑
}
```

### 集合初始化

初始化集合时指定容量：

```java
// ✅ 正确
Map<String, Object> metadata = new HashMap<>(4);
List<Document> documents = new ArrayList<>(100);

// ❌ 错误
Map<String, Object> metadata = new HashMap<>();
List<Document> documents = new ArrayList<>();
```

### 不可变对象

优先使用不可变对象：

```java
// ✅ 推荐
public final class User {
    private final String name;
    private final int age;
}

// ❌ 避免
public class User {
    private String name;
    private int age;
}
```

### Optional 使用

使用 `Optional` 替代 null 返回值：

```java
// ✅ 推荐
public Optional<User> findById(String id) {
    return repository.findById(id);
}

// ❌ 避免
public User findById(String id) {
    return repository.findById(id); // 可能返回 null
}
```

### record 使用

使用 record 定义简单数据载体（Java 16+）：

```java
// ✅ 推荐
public record ChatRequest(String message, String conversationId) {
}

// ❌ 冗余
@Data
@AllArgsConstructor
public class ChatRequest {
    private String message;
    private String conversationId;
}
```

## 异常处理

### 异常捕获

捕获具体异常，避免捕获 Exception：

```java
// ✅ 正确
try {
    vectorStore.add(documents);
} catch (VectorStoreException e) {
    log.error("向量存储失败", e);
    throw new RagProcessingException("文档处理失败", e);
}

// ❌ 错误
try {
    vectorStore.add(documents);
} catch (Exception e) {
    log.error("出错了", e);
}
```

### 异常消息

提供有用的错误信息：

```java
// ✅ 正确
throw new IllegalArgumentException(
    String.format("文件不存在：%s", filePath));

// ❌ 错误
throw new IllegalArgumentException("文件错误");
```

## 日志规范

### 日志级别

- **ERROR**：错误，需要立即处理
- **WARN**：警告，不影响系统运行
- **INFO**：重要信息
- **DEBUG**：调试信息

### 日志格式

使用结构化日志格式：

```java
log.debug("[component] 操作描述 | param={}, result={}", param, result);
log.info("[component] 操作成功 | duration={}ms", duration);
log.error("[component] 操作失败 | error={}", error.getMessage(), error);
```

### 日志最佳实践

```java
// ✅ 正确
if (log.isDebugEnabled()) {
    log.debug("处理文档：{}", document.getId());
}

// ❌ 错误（避免字符串拼接）
log.debug("处理文档：" + document.getId());
```

## 代码审查要点

1. **可读性**：代码是否易于理解
2. **命名**：命名是否清晰、准确
3. **注释**：是否有必要的注释
4. **异常处理**：异常处理是否合理
5. **日志**：是否有适当的日志
6. **性能**：是否存在性能问题
7. **安全**：是否存在安全隐患
8. **测试**：是否有足够的测试覆盖