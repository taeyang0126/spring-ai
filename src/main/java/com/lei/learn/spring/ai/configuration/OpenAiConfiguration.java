package com.lei.learn.spring.ai.configuration;

import com.lei.learn.spring.ai.advisor.UserContextAdvisor;
import com.lei.learn.spring.ai.memory.CustomerMongoChatMemoryRepository;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.retry.support.RetryTemplate;

/**
 * <p>
 * OpenAiConfiguration
 * </p>
 *
 * @author 伍磊
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OpenAiProperties.class)
@RequiredArgsConstructor
public class OpenAiConfiguration {

    private final OpenAiProperties openAiProperties;

    @Bean
    @ConditionalOnMissingBean
    public ToolCallingManager toolCallingManager() {
        return ToolCallingManager.builder().build();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryTemplate retryTemplate() {
        return RetryTemplate.defaultInstance();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }

    @Bean
    @Primary
    public ChatMemoryRepository customChatMemoryRepository(MongoTemplate mongoTemplate) {
        return CustomerMongoChatMemoryRepository.builder()
                .mongoTemplate(mongoTemplate)
                .build();
    }


    @Bean
    @ConditionalOnMissingBean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

    @Bean("textChatModel")
    public ChatModel textChatModel(OpenAiApi openAiApi,
                                   ToolCallingManager toolCallingManager,
                                   RetryTemplate retryTemplate,
                                   ObservationRegistry observationRegistry) {

        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(openAiProperties.getTextModel())
                .maxTokens(openAiProperties.getMaxTokens())
                .temperature(openAiProperties.getTemperature())
                .build();
        return new OpenAiChatModel(
                openAiApi, chatOptions,
                toolCallingManager, retryTemplate,
                observationRegistry
        );
    }

    @Bean("fullChatModel")
    public ChatModel fullChatModel(OpenAiApi openAiApi,
                                   ToolCallingManager toolCallingManager,
                                   RetryTemplate retryTemplate,
                                   ObservationRegistry observationRegistry) {

        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(openAiProperties.getFullModel())
                .maxTokens(openAiProperties.getMaxTokens())
                .temperature(openAiProperties.getTemperature())
                .build();
        return new OpenAiChatModel(
                openAiApi, chatOptions,
                toolCallingManager, retryTemplate,
                observationRegistry
        );
    }


    @Bean("textChatClient")
    public ChatClient textChatClient(ChatMemory chatMemory,
                                     @Qualifier("textChatModel") ChatModel chatModel) {
        log.info("[textChatClient] init | start");
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        // 消息上下文
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 用户信息处理
                        new UserContextAdvisor(),
                        // 日志
                        SimpleLoggerAdvisor.builder()
                                .order(Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER).build()
                        // ToolCallAdvisor 将工具调用循环实现为顾问链的一部分，而不是依赖模型内部的工具执行。这使得链中的其他顾问能够拦截并观察工具调用过程。
                        // ToolCallAdvisor 不支持 stream
                        // ToolCallAdvisor.builder().advisorOrder(Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER + 100).build()
                        // StructuredOutputValidationAdvisor 结构化输出，如不是指定的结构化则会进行重试
                        // StructuredOutputValidationAdvisor.builder().outputType().maxRepeatAttempts(3).advisorOrder(Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER + 1000).build()
                )
                .build();
    }

    /**
     * 全模态 chatClient
     *
     * @param chatMemory
     * @param chatModel
     * @return
     */
    @Bean("fullChatClient")
    public ChatClient fullChatClient(ChatMemory chatMemory,
                                     @Qualifier("fullChatModel") ChatModel chatModel) {

        log.info("[fullChatClient] init | start");
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        // 消息上下文
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 用户信息处理
                        new UserContextAdvisor(),
                        // 日志
                        SimpleLoggerAdvisor.builder()
                                .order(Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER).build()
                )
                .build();
    }

}
