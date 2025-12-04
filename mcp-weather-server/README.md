# mcp-weather-server

Model Context Protocol (MCP) 服务器实现示例，演示如何创建 MCP 工具服务器。

## 功能特性

- 提供天气查询工具服务
- 可被 MCP 客户端调用
- 默认运行在端口 9001
- 演示 MCP 服务器的标准实现

## 技术栈

- **Spring AI MCP Server WebMVC**: MCP 服务器 WebMVC 实现
- **Spring Boot**: 3.5.8
- **Java**: 21

## 环境要求

- JDK 21 或更高版本
- Maven 3.6+

## 快速开始

### 1. 启动应用

```bash
mvn spring-boot:run
```

### 2. 验证服务

服务默认运行在端口 9001，可以通过 MCP 客户端连接使用。

## 配置说明

### 默认配置

- **端口**: 9001
- **协议**: HTTP
- **MCP 版本**: 符合 Model Context Protocol 规范

## 项目结构

```
mcp-weather-server/
├── src/
│   └── main/
│       └── java/com/lei/learn/mcp/warther/server/
│           └── McpServerApplication.java  # 主应用类
└── pom.xml
```

## 使用方式

### 作为 MCP 服务器

本模块可以作为独立的 MCP 服务器运行，为其他应用提供工具服务。

### 与 spring-ai-example 集成

在 `spring-ai-example` 模块中配置 MCP 客户端连接：

```yaml
spring:
  ai:
    mcp:
      client:
        streamable-http:
          connections:
            my-weather-server:
              url: http://localhost:9001
```

## 注意事项

1. **端口配置**: 默认使用端口 9001，确保端口未被占用。

2. **MCP 协议**: 本服务器实现符合 Model Context Protocol 规范，可被任何兼容的 MCP 客户端调用。

3. **服务依赖**: 本模块可以独立运行，不依赖其他模块。