package com.lei.learn.spring.ai.alibaba.weather.agent.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;

/**
 * <p>
 * UserLocationTool
 * </p>
 *
 * @author 伍磊
 */
public class UserLocationTool implements Tool<String, String> {

    @Override
    public String apply(
            @ToolParam(description = "User query") String query,
            ToolContext toolContext) {
        // 从上下文中获取用户信息
        String userId = (String) toolContext.getContext().get("user_id");
        return "1".equals(userId) ? "Florida" : "San Francisco";
    }

    @Override
    public ToolCallback toolCallback() {
        return FunctionToolCallback.builder("get_user_location", this)
                .description("Retrieve user location based on user ID")
                .inputType(String.class)
                .build();
    }
}