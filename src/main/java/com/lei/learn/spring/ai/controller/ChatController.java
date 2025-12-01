package com.lei.learn.spring.ai.controller;

import com.lei.learn.spring.ai.configuration.OpenAiProperties;
import com.lei.learn.spring.ai.memory.CustomerMongoChatMemoryRepository;
import com.lei.learn.spring.ai.model.dto.ChatHistoryRequest;
import com.lei.learn.spring.ai.model.dto.ChatRequest;
import com.lei.learn.spring.ai.model.vo.ConversationHistory;
import com.lei.learn.spring.ai.support.ModelType;
import com.lei.learn.spring.ai.utils.UserContextUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.lei.learn.spring.ai.support.Constants.USER_ID;

/**
 * <p>
 * ChatController
 * </p>
 *
 * @author 伍磊
 */
@Log4j2
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ExecutorService sseExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final ChatClient textChatClient;
    private final ChatClient fullChatClient;
    private final CustomerMongoChatMemoryRepository chatMemoryRepository;
    private final OpenAiProperties  openAiProperties;

    public ChatController(@Qualifier("textChatClient") ChatClient textChatClient,
                          @Qualifier("fullChatClient") ChatClient fullChatClient,
                          CustomerMongoChatMemoryRepository chatMemoryRepository,
                          OpenAiProperties openAiProperties) {
        this.textChatClient = textChatClient;
        this.fullChatClient = fullChatClient;
        this.chatMemoryRepository = chatMemoryRepository;
        this.openAiProperties = openAiProperties;
    }

    @PostMapping("/content")
    String generation(@RequestBody ChatRequest chatRequest) {
        chatRequest.check();
        ChatClient chatClient = ModelType.TEXT.equals(openAiProperties.getChatModelType()) ? textChatClient : fullChatClient;
        return chatClient.prompt()
                .user(chatRequest.getUserInput())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatRequest.getConversationId()))
                .call()
                .content();
    }

    @PostMapping(value = "/stream", produces = "text/event-stream", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SseEmitter stream(@RequestParam String conversationId,
                             @RequestParam String userInput,
                             @RequestParam(required = false) List<MultipartFile> images) {
        ChatRequest chatRequest = ChatRequest.builder()
                .conversationId(conversationId)
                .userInput(userInput)
                .images(images)
                .build();
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        try {
            chatRequest.check();
        } catch (Exception e) {
            emitter.completeWithError(e);
            return emitter;
        }

        // 异步线程处理，先获取到 userId
        Integer currentUserId = UserContextUtils.getCurrentUserId();
        if (null == currentUserId) {
            emitter.completeWithError(new IllegalArgumentException("请先登录!"));
            return emitter;
        }

        boolean hasImages = null != images && !images.isEmpty();
        ChatClient chatClient = ModelType.TEXT.equals(openAiProperties.getChatModelType()) ? textChatClient : fullChatClient;

        // 处理图片
        List<Media> mediaList;
        if (hasImages) {
            mediaList = new ArrayList<>();
            for (MultipartFile image : images) {
                // 使用 MimeTypeUtils 确认 MIME 类型
                try {
                    String mimeType = image.getContentType();
                    Resource imageResource = new InputStreamResource(image.getInputStream());
                    Media media = new Media(MimeTypeUtils.parseMimeType(mimeType), imageResource);
                    mediaList.add(media);
                } catch (IOException e) {
                    log.error("[chat-stream] image error", e);
                    emitter.completeWithError(new IllegalArgumentException("非法图片!"));
                    return emitter;
                }
            }
        } else {
            mediaList = new ArrayList<>();
        }


        Flux<String> flux = chatClient.prompt()
                .user(u -> {
                    u.text(chatRequest.getUserInput());
                    if (hasImages) {
                        u.media(mediaList.toArray(new Media[0]));
                    }
                })
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatRequest.getConversationId())
                        .param(USER_ID, currentUserId))
                .toolContext(Map.of(USER_ID, currentUserId))
                .stream()
                .content();

        flux.subscribeOn(Schedulers.fromExecutor(sseExecutor))
                .subscribe(data -> {
                            try {
                                // 方便前端处理换行
                                String safeData = data.replace("\n", "[NEWLINE]");
                                emitter.send(safeData, MediaType.TEXT_PLAIN);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );

        return emitter;
    }

    @PostMapping("/history")
    public List<ConversationHistory> getChatHistory(@RequestBody ChatHistoryRequest request) {
        request.validate();

        if (request.getConversationId() != null) {
            List<Message> messages =
                    chatMemoryRepository.findByConversationId(request.getConversationId());
            return List.of(new ConversationHistory(request.getConversationId(), messages));
        } else {
            var userMessagesMap =
                    chatMemoryRepository.findByUserId(request.getUserId());
            return userMessagesMap.entrySet().stream()
                    .map(entry -> new ConversationHistory(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }
    }

}
