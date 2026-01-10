# 开发指南

遵循 [《阿里巴巴 Java 开发手册》](https://github.com/alibaba/p3c)

## 包结构

```
com.lei.learn.spring.ai/
├── advisor/          # Advisor
├── configuration/    # 配置类
├── controller/       # 控制器
├── env/              # 环境变量
├── mcp/              # MCP
├── memory/           # 记忆
├── model/            # 模型
├── repository/       # 数据访问
├── support/          # 常量
├── tool/             # 工具
└── utils/            # 工具类
```

## 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | UpperCamelCase | `ChatController` |
| 方法 | lowerCamelCase，动词 | `getUserById()` |
| 变量 | lowerCamelCase | `userName` |
| 常量 | 全大写，下划线 | `MAX_COUNT` |

## 代码风格

- **缩进**：4 空格（不用 Tab）
- **大括号**：左括号不换行
- **行长度**：≤ 120 字符
- **类型推断**：优先 `var`

```java
// ✅ 正确
if (condition) {
    doSomething();
}
var message = "Hello";

// ❌ 错误
if (condition)
{
    doSomething();
}
```

## 注释规范

- **Javadoc**：所有 public 类和方法必须添加
- **行内注释**：复杂逻辑必须说明
- **语言**：中文

```java
/**
 * 聊天控制器
 *
 * @author 伍磊
 */
public class ChatController {
    /**
     * 发送消息
     *
     * @param request 请求
     * @return 响应
     */
    public ChatResponse chat(ChatRequest request) {
        // 参数校验
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
    }
}
```

## Lombok

常用注解：
- `@Data` - getter/setter/toString/equals
- `@Builder` - 构建器
- `@AllArgsConstructor/@NoArgsConstructor` - 构造器
- `@Slf4j` - 日志
- `@RequiredArgsConstructor` - 必需参数构造器

## 最佳实践

1. **避免魔法值** - 用常量
2. **集合初始化** - 指定容量：`new HashMap<>(4)`
3. **不可变对象** - 优先 final
4. **Optional** - 替代 null
5. **record** - 简单数据载体

```java
// 推荐
private static final int MAX_COUNT = 100;
public record ChatRequest(String message, String id) {}
public Optional<User> findById(String id) {
    return repository.findById(id);
}
```

## 日志

- `DEBUG` - 调试
- `INFO` - 重要信息
- `WARN` - 警告
- `ERROR` - 错误

```java
log.debug("[component] 操作 | param={}", param);
log.info("[component] 成功 | result={}", result);
log.error("[component] 失败 | error={}", error);
```