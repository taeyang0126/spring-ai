package com.lei.learn.mcp.warther.server.service;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import org.springaicommunity.mcp.annotation.McpProgressToken;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * WeatherService
 * </p>
 *
 * @author 伍磊
 */
@Service
public class WeatherService {

    public record WeatherResponse(Current current) {
        public record Current(LocalDateTime time, int interval, double temperature_2m) {
        }
    }

    @McpTool(description = "获取指定位置的温度（摄氏度）")
    public String getTemperature(
            McpSyncServerExchange exchange, // 用于和客户端通信（发日志、进度、调用采样等）
            @McpToolParam(description = "位置的纬度") double latitude,
            @McpToolParam(description = "位置的经度") double longitude,
            @McpProgressToken String progressToken) { // 用于向客户端报告长时间任务的进度（比如“已完成50%”）

        // 向客户端发送一条 DEBUG 级别日志
        exchange.loggingNotification(LoggingMessageNotification.builder()
                .level(LoggingLevel.INFO)
                .data("调用 getTemperature 工具，纬度：" + latitude + "，经度：" + longitude)
                .meta(Map.of())
                .build());

        // 调用外部 API 获取天气
        WeatherResponse weatherResponse = RestClient.create()
                .get()
                .uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
                        latitude, longitude)
                .retrieve()
                .body(WeatherResponse.class);


        String epicPoem = "MCP 客户端未提供采样功能。";

        if (exchange.getClientCapabilities().sampling() != null) {
            // 进度 50% 发送 50% 进度通知
            exchange.progressNotification(new ProgressNotification(progressToken, 0.5, 1.0, "开始生成诗歌"));

            String samplingMessage = """
                当前天气预报（温度单位为摄氏度）：%s。
                位置坐标为：纬度 %s，经度 %s。
                请以莎士比亚风格创作一首关于此天气的史诗级诗歌。
                """.formatted(weatherResponse.current().temperature_2m(), latitude, longitude);

            McpSchema.CreateMessageResult samplingResponse = exchange.createMessage(McpSchema.CreateMessageRequest.builder()
                    .systemPrompt("你是一位诗人！")
                    .messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER, new McpSchema.TextContent(samplingMessage))))
                    .build()); // (5)

            epicPoem = ((McpSchema.TextContent) samplingResponse.content()).text();
        }

        // 进度 100%
        exchange.progressNotification(new ProgressNotification(progressToken, 1.0, 1.0, "任务已完成"));

        return """
            天气诗歌：%s			
            描述天气：%s°C，位置坐标：(%s, %s)		
            """.formatted(epicPoem, weatherResponse.current().temperature_2m(), latitude, longitude);
    }
}
