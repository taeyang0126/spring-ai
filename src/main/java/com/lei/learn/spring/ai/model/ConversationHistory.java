package com.lei.learn.spring.ai.model;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * <p>
 * ConversationHistory
 * </p>
 *
 * @author 伍磊
 */
public record ConversationHistory(
        String conversationId,
        List<Message> messages
) {}
