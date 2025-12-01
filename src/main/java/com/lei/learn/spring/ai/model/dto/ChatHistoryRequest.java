package com.lei.learn.spring.ai.model.dto;

import lombok.Data;

/**
 * <p>
 * ChatHistoryRequest
 * </p>
 *
 * @author 伍磊
 */
@Data
public class ChatHistoryRequest {

    private Integer userId;
    private String conversationId;

    public void validate() {
        if (userId == null && conversationId == null) {
            throw new IllegalArgumentException("Either userId or conversationId must be provided");
        }
    }

}
