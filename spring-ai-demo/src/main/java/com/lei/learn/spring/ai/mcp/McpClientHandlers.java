package com.lei.learn.spring.ai.mcp;

import com.lei.learn.spring.ai.configuration.OpenAiProperties;
import com.lei.learn.spring.ai.support.ModelType;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import lombok.extern.log4j.Log4j2;
import org.springaicommunity.mcp.annotation.McpLogging;
import org.springaicommunity.mcp.annotation.McpProgress;
import org.springaicommunity.mcp.annotation.McpSampling;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * <p>
 * McpClientHandlers
 * </p>
 *
 * @author 伍磊
 */
@Log4j2
@Service
public class McpClientHandlers {

    private final ChatClient textChatClient;
    private final ChatClient fullChatClient;
    private final OpenAiProperties openAiProperties;

    public McpClientHandlers(@Lazy @Qualifier("textChatClient") ChatClient textChatClient,
                             @Lazy @Qualifier("fullChatClient") ChatClient fullChatClient,
                             @Lazy OpenAiProperties openAiProperties) {
        this.textChatClient = textChatClient;
        this.fullChatClient = fullChatClient;
        this.openAiProperties = openAiProperties;
    }

    /**
     * 进度处理器 ——接收服务器长期运行作的实时进度更新。当服务器调用 exchange.progressNotification(...) 时触发
     *
     * @param progressNotification
     */
    @McpProgress(clients = "my-weather-server")
    public void progressHandler(ProgressNotification progressNotification) {
        log.info("MCP PROGRESS: [{}] progress: {} total: {} message: {}",
                progressNotification.progressToken(), progressNotification.progress(),
                progressNotification.total(), progressNotification.message());
    }

    /**
     * 日志处理程序 ——接收服务器的结构化日志消息以进行调试和监控。当服务器调用 exchange.loggingNotification(...) 时触发。
     *
     * @param loggingMessage
     */
    @McpLogging(clients = "my-weather-server")
    public void loggingHandler(LoggingMessageNotification loggingMessage) {
        log.info("MCP LOGGING: [{}] {}", loggingMessage.level(), loggingMessage.data());
    }

    /**
     * 采样处理器 ——最强大的功能。它使服务器能够向客户端的大型语言模型请求 AI 生成的内容。
     * 用于双向 AI 交互、创意内容生成、动态响应。当服务器调用 exchange.createMessage（...） 并进行采样能力检查时触发。
     * todo-wl: 目前发现启用此功能出现了循环的问题，待解决
     *
     * @param llmRequest
     * @return
     */
/*    @McpSampling(clients = "my-weather-server")
    public CreateMessageResult samplingHandler(CreateMessageRequest llmRequest) {
        ChatClient chatClient = ModelType.TEXT.equals(openAiProperties.getChatModelType()) ? textChatClient : fullChatClient;

        log.info("MCP SAMPLING: {}", llmRequest);

        String llmResponse = chatClient
                .prompt()
                .system(llmRequest.systemPrompt())
                .user(((TextContent) llmRequest.messages().get(0).content()).text())
                .call()
                .content();

        return CreateMessageResult.builder().content(new TextContent(llmResponse)).build();
    }*/

}
