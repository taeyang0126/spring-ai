package com.lei.learn.spring.ai.memory;

import com.lei.learn.spring.ai.model.Conversation;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lei.learn.spring.ai.support.Constants.USER_ID;

/**
 * <p>
 * 自定义 CustomerMongoChatMemoryRepository
 * </p>
 *
 * @author 伍磊
 */
@Log4j2
public class CustomerMongoChatMemoryRepository implements ChatMemoryRepository {

    private final MongoTemplate mongoTemplate;

    private CustomerMongoChatMemoryRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<String> findConversationIds() {
        return this.mongoTemplate.query(Conversation.class)
                .distinct("conversationId").as(String.class).all();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        var messages = this.mongoTemplate.query(
                        Conversation.class)
                .matching(Query.query(Criteria.where("conversationId").is(conversationId))
                        .with(Sort.by("timestamp").descending()));
        return messages.stream().map(CustomerMongoChatMemoryRepository::mapMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        deleteByConversationId(conversationId);

        // 处理 userId，给所有信息都加上 userId
        // 一段对话 -> 一个用户操作的
        Integer userId;
        if (!messages.isEmpty()) {
            Message firstMessage = messages.getFirst();
            Map<String, Object> metadata = firstMessage.getMetadata();
            if (null != metadata && metadata.containsKey(USER_ID)) {
                userId = (Integer) metadata.get(USER_ID);
            } else {
                userId = null;
            }
        } else {
            userId = null;
        }


        var conversations = messages.stream()
                .map(message -> new Conversation(
                        userId,
                        conversationId,
                        new org.springframework.ai.chat.memory.repository.mongo.Conversation.Message(
                                message.getText(), message.getMessageType().name(),
                                message.getMetadata()),
                        Instant.now()))
                .toList();
        this.mongoTemplate.insert(conversations,
                Conversation.class);

    }

    @Override
    public void deleteByConversationId(String conversationId) {
        this.mongoTemplate.remove(Query.query(Criteria.where("conversationId").is(conversationId)),
                Conversation.class);
    }

    public Map<String, List<Message>> findByUserId(Integer userId) {
        var messages = this.mongoTemplate.query(
                        Conversation.class)
                .matching(Query.query(Criteria.where("userId").is(userId))
                        .with(Sort.by("timestamp").descending()));

        return messages.stream()
                .collect(Collectors.groupingBy(
                        Conversation::conversationId,
                        Collectors.mapping(
                                CustomerMongoChatMemoryRepository::mapMessage,
                                Collectors.toList()
                        )
                ));
    }

    public static Message mapMessage(Conversation conversation) {
        return switch (conversation.message().type()) {
            case "USER" -> UserMessage.builder()
                    .text(conversation.message().content())
                    .metadata(conversation.message().metadata())
                    .build();
            case "ASSISTANT" -> AssistantMessage.builder()
                    .content(conversation.message().content())
                    .properties(conversation.message().metadata())
                    .build();
            case "SYSTEM" -> SystemMessage.builder()
                    .text(conversation.message().content())
                    .metadata(conversation.message().metadata())
                    .build();
            default -> {
                log.warn("Unsupported message type: {}", conversation.message().type());
                throw new IllegalStateException(
                        "Unsupported message type: " + conversation.message().type());
            }
        };
    }

    public static CustomerMongoChatMemoryRepository.Builder builder() {
        return new CustomerMongoChatMemoryRepository.Builder();
    }

    public final static class Builder {

        private MongoTemplate mongoTemplate;

        private Builder() {
        }

        public CustomerMongoChatMemoryRepository.Builder mongoTemplate(
                MongoTemplate mongoTemplate) {
            this.mongoTemplate = mongoTemplate;
            return this;
        }

        public CustomerMongoChatMemoryRepository build() {
            return new CustomerMongoChatMemoryRepository(this.mongoTemplate);
        }

    }
}
