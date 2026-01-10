package com.lei.learn.spring.ai.configuration;

import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankModel;
import com.lei.learn.spring.ai.advisor.UserContextAdvisor;
import com.lei.learn.spring.ai.memory.CustomerMongoChatMemoryRepository;
import com.lei.learn.spring.ai.rag.ReRankDocumentPostProcessor;
import com.lei.learn.spring.ai.repository.UserRepository;
import com.lei.learn.spring.ai.tool.DateTimeTools;
import com.lei.learn.spring.ai.tool.UserTools;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.retry.support.RetryTemplate;

import java.util.stream.Collectors;

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


    /**
     * QuestionAnswerAdvisor
     * 使用向量检索，将检索到的文档作为上下文添加到上下文
     * 1. 只回答检索到的文档，不要回答其他内容
     *
     * @param vectorStore 向量存储
     * @return QuestionAnswerAdvisor
     */
    @Bean
    public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        PromptTemplate customPromptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template("""
                        用户提问: <query>
                        上下文信息如下:
                        ---------------------
                        <question_answer_context>
                        ---------------------
                        
                        根据上下文信息且没有先验知识，回答查询。
                        
                        遵循以下规则：
                        
                        1. 如果答案不在上下文中，只需说你不知道，不要提供其他信息。
                        2. 避免使用"根据上下文..."或"提供的信息..."等语句。
                        """)
                .build();


        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(3)
                        .similarityThreshold(0.7d)
                        .build()
                )
                .build();
    }

    @Bean
    public ReRankDocumentPostProcessor reRankDocumentPostProcessor(DashScopeRerankModel reRankChatModel) {
        return new ReRankDocumentPostProcessor(reRankChatModel);
    }

    @Bean
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(VectorStore vectorStore,
                                                                     @Qualifier("textChatModel") ChatModel chatModel,
                                                                     ReRankDocumentPostProcessor reRankDocumentPostProcessor) {
        PromptTemplate promptTemplate = new PromptTemplate("""
                Context information is below.
                Here, `<text></text>` encloses the context content, and `<link></link>` encloses a link to the corresponding content.
                
                ---------------------
                {context}
                ---------------------
                
                Given the context information and no prior knowledge, answer the query.
                
                Follow these rules:
                
                1. If the answer is not in the context, just say that you don't know.
                2. Avoid statements like "Based on the context..." or "The provided information...".
                3. If there is a link, please include it in your reply.
                4. Most importantly, if the answer is not provided in the context, please be clear that you do not know.
                
                Query: {query}
                
                Answer:
                """);

        // 默认检索到的文档不能为空，当发生这种情况时， 它会指示模型不要回答用户查询
        return RetrievalAugmentationAdvisor.builder()
                // 1. 使用大语言模型重写查询
                .queryTransformers(
                        RewriteQueryTransformer.builder()
                                .chatClientBuilder(ChatClient.builder(chatModel))
                                .build()
                )
                // 2. 粗粒度检索
                .documentRetriever(
                        VectorStoreDocumentRetriever.builder()
                                .vectorStore(vectorStore)
                                .similarityThreshold(0.5d)
                                .topK(10)
                                .build()
                )
                // 3. 使用 reRankModel 重排
                .documentPostProcessors(reRankDocumentPostProcessor)
                // 4. 上下文重新定义，添加link
                .queryAugmenter(
                        ContextualQueryAugmenter.builder()
                                .promptTemplate(promptTemplate)
                                .documentFormatter(documents -> documents.stream()
                                        .map(t -> "<text>" + t.getText() + "</text>, <link>" + t.getMetadata().get("link") + "</link>")
                                        .collect(Collectors.joining(System.lineSeparator()))
                                ).build()
                )
                .build();
    }

    @Bean("textChatClient")
    public ChatClient textChatClient(ChatMemory chatMemory,
                                     @Qualifier("textChatModel") ChatModel chatModel,
                                     UserRepository userRepository,
                                     ToolCallbackProvider mcpToolProvider,
                                     QuestionAnswerAdvisor questionAnswerAdvisor,
                                     RetrievalAugmentationAdvisor retrievalAugmentationAdvisor
    ) {
        log.info("[textChatClient] init | start");
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        // 消息上下文
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 用户信息处理
                        new UserContextAdvisor(),
                        // 日志
                        SimpleLoggerAdvisor.builder()
                                .order(Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER).build(),
                        // QuestionAnswerAdvisor
                        // questionAnswerAdvisor
                        retrievalAugmentationAdvisor
                        // ToolCallAdvisor 将工具调用循环实现为顾问链的一部分，而不是依赖模型内部的工具执行。这使得链中的其他顾问能够拦截并观察工具调用过程。
                        // ToolCallAdvisor 不支持 stream
                        // ToolCallAdvisor.builder().advisorOrder(Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER + 100).build()
                        // StructuredOutputValidationAdvisor 结构化输出，如不是指定的结构化则会进行重试
                        // StructuredOutputValidationAdvisor.builder().outputType().maxRepeatAttempts(3).advisorOrder(Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER + 1000).build()
                )
                // 默认工具会在所有由同一ChatClient.Builder实例构建的 ChatClient 实例执行的所有聊天请求之间共享
                // 要求 tool 最好是无状态可共享的
                .defaultTools(new DateTimeTools(), new UserTools(userRepository))
                // mcp
                .defaultToolCallbacks(mcpToolProvider)
                .build();
    }

    /**
     * 全模态 chatClient
     * 不支持 Function Call，只有明确宣称支持 function calling 的模型才可用
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
