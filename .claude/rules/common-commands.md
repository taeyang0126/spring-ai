# 常用命令

## 构建与运行

### 构建项目

```bash
# 构建整个项目（包括所有模块）
mvn clean install

# 构建指定模块
mvn clean install -pl spring-ai-example

# 构建指定模块及其依赖
mvn clean install -pl spring-ai-example -am

# 快速构建（跳过测试）
mvn clean install -DskipTests

# 只编译不打包
mvn clean compile
```

### 运行模块

```bash
# 运行指定模块
mvn spring-boot:run -pl spring-ai-example

# 运行时指定配置文件
mvn spring-boot:run -pl spring-ai-example -Dspring-boot.run.profiles=dev

# 运行时指定 JVM 参数
mvn spring-boot:run -pl spring-ai-example -Dspring-boot.run.jvmArguments="-Xmx512m"
```

## 测试

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行指定模块的测试
mvn test -pl spring-ai-example

# 运行指定测试类
mvn test -Dtest=ChatTests

# 运行指定测试方法
mvn test -Dtest=ChatTests#test_chat_memory

# 运行并生成测试报告
mvn test -pl rag-etl-core

# 跳过测试
mvn clean install -DskipTests
```

### 测试选项

```bash
# 并行运行测试
mvn test -DforkCount=4

# 打印测试输出
mvn test -X

# 停止在第一个失败
mvn test -DfailFast=true
```

## 依赖管理

```bash
# 查看依赖树
mvn dependency:tree

# 查看指定模块的依赖树
mvn dependency:tree -pl spring-ai-example

# 分析依赖
mvn dependency:analyze

# 更新依赖（检查新版本）
mvn versions:display-dependency-updates
```

## 其他实用命令

```bash
# 清理构建产物
mvn clean

# 查看有效的 POM 配置
mvn help:effective-pom

# 查看插件配置
mvn help:effective-pom -pl spring-ai-example

# 列出所有模块
mvn -pl ':spring-ai-example' -amd
```
