package com.lei.learn.spring.ai.chat;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.lei.learn.spring.ai.OpenAiApiBase;
import com.lei.learn.spring.ai.advisor.UserContextAdvisor;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;

/**
 * <p>
 * ChatTests
 * </p>
 *
 * @author 伍磊
 */
public class ChatTests {

    private static final String MODEL = "qwen-max";
    private static ChatModel chatModel;

    @BeforeAll
    public static void setUp() {
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(MODEL)
                .build();
        chatModel = new OpenAiChatModel(
                OpenAiApiBase.openAiApi, chatOptions,
                ToolCallingManager.builder().build(), RetryTemplate.defaultInstance(),
                ObservationRegistry.create()
        );
    }

    @AfterAll
    public static void tearDown() {
        chatModel = null;
    }

    @Test
    public void test() {
        ChatResponse response = chatModel.call(
                new Prompt("你能做什么?")
        );
        System.out.println(response);
    }

    @Test
    public void test_chat_structured_output() {
        // Prompt 类充当容器，用于存放一系列有序的 Message 对象和一个请求 ChatOptions

        /*
          标准json提示词:
          Your response should be in JSON format.
          The data structure for the JSON should match this Java class: java.util.HashMap
          Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.
         */
        // 结构化输出
        //   @JsonPropertyOrder({"actor", "movies"}) 属性排序
        ActorsFilms actorsFilms = ChatClient.create(chatModel)
                .prompt()
                .user(u -> u.text("生成演员 {actor} 的 5 部电影作品列表。")
                        .param("actor", "姜文"))
                .call()
                .entity(ActorsFilms.class);
        System.out.println(actorsFilms);
        List<ActorsFilms> list = ChatClient.create(chatModel)
                .prompt()
                .user(u -> u.text("生成演员列表 {actor} 中每个演员的 5 部电影作品列表。")
                        .param("actor", "姜文,周星驰,唐国强"))
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
        System.out.println(list);
    }

    @Test
    public void test_chat_memory() {
        // 【spring ai 默认配置的 ChatMemory 类型】MessageWindowChatMemory 会维护一个消息窗口，窗口大小不超过指定上限。当消息数量超过上限时，系统会删除较旧的消息，但会保留系统消息。默认窗口大小为 20 条消息。
        // MessageWindowChatMemory 底层依赖 ChatMemoryRepository 的能力

        // ------ChatMemoryRepository-------

        /*
        JdbcChatMemoryRepository
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-chat-memory-repository-jdbc</artifactId>
        </dependency>
         */

        // 【默认】InMemoryChatMemoryRepository
        // ChatMemoryRepository repository = new InMemoryChatMemoryRepository();

        // 内存 memory
        ChatMemory chatMemory = MessageWindowChatMemory.builder().build();

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new UserContextAdvisor())
                .build();

        String conversationId = "007";
        ChatResponse chatResponse = chatClient.prompt()
                .user("我的名字是tom")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .chatResponse();
        System.out.println(chatResponse.getMetadata().getId() + " -> " +
                chatResponse.getResult().getOutput().getText());

        chatResponse = chatClient.prompt()
                .user("我叫什么?")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .chatResponse();
        System.out.println(chatResponse.getMetadata().getId() + " -> " +
                chatResponse.getResult().getOutput().getText());


    }

    @JsonPropertyOrder({"actor", "movies"})
    record ActorsFilms(String actor, List<String> movies) {

    }
}
