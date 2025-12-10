package com.lei.learn.spring.ai.alibaba.graph;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.model.ChatModel;

/**
 * <p>
 * chat model init
 * </p>
 *
 * @author 伍磊
 */
public class ChatModelInit {

    public static ChatModel init() {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .build();

        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel("qwen-plus")
                .build();

        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(chatOptions)
                .build();
    }

}
