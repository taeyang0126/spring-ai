package com.lei.learn.spring.ai.env;

/**
 * <p>
 * EnvSupport
 * </p>
 *
 * @author 伍磊
 */
public final class EnvSupport {

    public static String OPENAI_API_KEY;
    public static String OPENAI_BASE_URL;

    static {
        OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
        OPENAI_BASE_URL = System.getenv()
                .getOrDefault("OPENAI_BASE_URL", "https://dashscope.aliyuncs.com/compatible-mode");
    }

}
