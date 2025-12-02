package com.lei.learn.spring.ai.image;

import com.lei.learn.spring.ai.OpenAiApiBase;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.content.Media;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.MimeTypeUtils;

/**
 * <p>
 * ImageTests
 * </p>
 *
 * @author 伍磊
 */
public class ImageTests {

    private static final String MODEL = "qwen3-omni-flash";
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
        var imageResource = new ClassPathResource("images/user.jpg");

        var userMessage = UserMessage.builder()
                .text("说下你从这个图片看到了什么?")
                .media(new Media(MimeTypeUtils.IMAGE_JPEG, imageResource))
                .build();

        String content = ChatClient.create(chatModel)
                .prompt()
                .messages(userMessage)
                .call()
                .content();
        System.out.println(content);

    }

}
