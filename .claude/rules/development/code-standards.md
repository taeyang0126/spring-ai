# 代码规范

遵循 [《阿里巴巴 Java 开发手册》](https://github.com/alibaba/p3c)

## 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | UpperCamelCase | `ChatController` |
| 方法名 | lowerCamelCase，动词开头 | `getUserById()` |
| 变量名 | lowerCamelCase | `userName` |
| 常量名 | 全大写，下划线分隔 | `MAX_RETRY_COUNT` |
| 包名 | 全小写，点分隔 | `com.lei.learn.spring.ai` |

## 注释规范

- **Javadoc**：所有 public 类和方法必须添加
- **行内注释**：复杂逻辑必须说明
- **语言**：使用中文
- **位置**：注释在代码上方

```java
/**
 * 聊天控制器
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
        // 检查参数有效性
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
    }
}
```

## 代码风格

- **缩进**：4 个空格（不用 Tab）
- **大括号**：左括号不换行（K&R 风格）
- **行长度**：不超过 120 字符
- **类型推断**：优先使用 `var`

```java
// ✅ 正确
if (condition) {
    doSomething();
}

var message = "Hello";
var documents = vectorStore.similaritySearch(query);

// ❌ 错误
if (condition)
{
    doSomething();
}
```

## 最佳实践

1. **避免魔法值** - 使用常量定义
2. **集合初始化** - 指定容量：`new HashMap<>(4)`
3. **不可变对象** - 优先使用 final 字段
4. **Optional** - 替代 null 返回值
5. **record** - 简单数据载体（Java 16+）

```java
// 推荐
private static final int ADULT_AGE = 18;

public record ChatRequest(String message, String conversationId) {}

public Optional<User> findById(String id) {
    return repository.findById(id);
}
```

## 日志规范

- **DEBUG**：调试信息
- **INFO**：重要信息
- **WARN**：警告，不影响运行
- **ERROR**：错误，需立即处理

```java
log.debug("[component] 操作描述 | param={}", param);
log.info("[component] 操作成功 | result={}", result);
log.error("[component] 操作失败 | error={}", error);
```
