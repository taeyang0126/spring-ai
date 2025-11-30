package com.lei.learn.spring.ai;

import org.springframework.ai.openai.api.OpenAiApi;

import static com.lei.learn.spring.ai.env.EnvSupport.OPENAI_API_KEY;
import static com.lei.learn.spring.ai.env.EnvSupport.OPENAI_BASE_URL;

/**
 * <p>
 * OpenAiApi
 * </p>
 *
 * @author 伍磊
 */
public class OpenAiApiBase {

    public static OpenAiApi openAiApi;

    static {
        openAiApi = OpenAiApi.builder()
                .apiKey(OPENAI_API_KEY)
                .baseUrl(OPENAI_BASE_URL)
                .build();
    }

}
