package com.lei.learn.spring.ai.alibaba.weather.agent.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.lei.learn.spring.ai.alibaba.weather.agent.interceptor.LogToolInterceptor;
import com.lei.learn.spring.ai.alibaba.weather.agent.model.ResponseFormat;
import com.lei.learn.spring.ai.alibaba.weather.agent.tools.UserLocationTool;
import com.lei.learn.spring.ai.alibaba.weather.agent.tools.WeatherForLocationTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * AgentConfiguration
 * </p>
 *
 * @author 伍磊
 */
@Configuration
public class AgentConfiguration {

    private final ChatModel chatModel;

    public AgentConfiguration(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Bean
    public ReactAgent reactAgent() {
        String SYSTEM_PROMPT = """
                You are an expert weather forecaster, who speaks in puns.
                
                You have access to two tools:
                
                - get_weather_for_location: use this to get the weather for a specific location
                - get_user_location: use this to get the user's location
                
                If a user asks you for the weather, make sure you know the location.
                If you can tell from the question that they mean wherever they are,
                use the get_user_location tool to find their location.
                """;

        return ReactAgent.builder()
                .name("weather_agent")
                .description("This is a react weather agent")
                .model(chatModel)
                .systemPrompt(SYSTEM_PROMPT)
                .outputType(ResponseFormat.class)
                .saver(new MemorySaver())
                .tools(
                        new UserLocationTool().toolCallback(),
                        new WeatherForLocationTool().toolCallback()
                )
                .interceptors(new LogToolInterceptor())
                .build();
    }

}
