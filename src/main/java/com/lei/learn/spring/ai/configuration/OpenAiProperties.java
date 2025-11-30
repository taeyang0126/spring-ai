package com.lei.learn.spring.ai.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * OpenAiProperties
 * </p>
 *
 * @author 伍磊
 */
@Data
@ConfigurationProperties(prefix = "ai.openai")
public class OpenAiProperties {

    private double temperature;
    private int maxTokens;

    /**
     * 文本模型
     */
    private String textModel;

    /**
     * 全模态模型
     */
    private String fullModel;

}
