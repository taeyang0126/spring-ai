package com.lei.learn.spring.ai.alibaba.weather.agent;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.function.BiFunction;

/**
 * <p>
 * BasicWeatherAgent
 * </p>
 *
 * @author 伍磊
 */
public class BasicWeatherAgent {

    public static void main(String[] args) throws GraphRunnerException {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .build();

        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel("qwen3-max")
                .build();

        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(chatOptions)
                .build();

        // 定义天气查询工具
        class WeatherTool implements BiFunction<String, ToolContext, String> {
            @Override
            public String apply(String city, ToolContext toolContext) {
                return "It's always sunny in " + city + "!";
            }
        }

        ToolCallback weatherTool = FunctionToolCallback.builder("get_waather", new WeatherTool())
                .description("Get weather for a given city")
                .inputType(String.class)
                .build();

        // 创建 agent
        ReactAgent agent = ReactAgent.builder()
                .name("weather_agent")
                .model(chatModel)
                .tools(weatherTool)
                .systemPrompt("You are a helpful assistant")
                .saver(new MemorySaver())
                .build();

        AssistantMessage response = agent.call("what is the weather in San Francisco");
        System.out.println(response.getText());
    }
}
