package com.lei.learn.spring.ai.memory;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * <p>
 * ai_chat_memory
 * </p>
 *
 * @author 伍磊
 */
@Document("ai_chat_memory")
public record Conversation(Integer userId,
                           String conversationId,
                           org.springframework.ai.chat.memory.repository.mongo.Conversation.Message message,
                           Instant timestamp) {
}

