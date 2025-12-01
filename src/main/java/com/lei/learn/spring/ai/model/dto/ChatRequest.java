package com.lei.learn.spring.ai.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * ChatRequest
 * </p>
 *
 * @author 伍磊
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {

    /**
     * 对话 id
     */
    private String conversationId;

    /**
     * 用户输入
     */
    private String userInput;

    /**
     * 图片
     */
    private List<MultipartFile> images;

    public void check() {
        if (conversationId == null || conversationId.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }


}
