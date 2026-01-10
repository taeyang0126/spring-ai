# 常用命令

## 构建

```bash
# 构建整个项目
mvn clean install

# 构建指定模块
mvn clean install -pl spring-ai-example

# 跳过测试
mvn clean install -DskipTests
```

## 运行

```bash
# 运行指定模块
mvn spring-boot:run -pl spring-ai-example
```

## 测试

```bash
# 运行所有测试
mvn test

# 运行指定模块测试
mvn test -pl spring-ai-example

# 运行指定测试类
mvn test -Dtest=ChatTests
```

## 依赖

```bash
# 查看依赖树
mvn dependency:tree

# 查看有效配置
mvn help:effective-pom
```
