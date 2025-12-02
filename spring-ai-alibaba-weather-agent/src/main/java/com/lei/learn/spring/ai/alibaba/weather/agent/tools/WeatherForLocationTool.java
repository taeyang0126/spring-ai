package com.lei.learn.spring.ai.alibaba.weather.agent.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;

/**
 * <p>
 * WeatherForLocationTool
 * </p>
 *
 * @author 伍磊
 */
public class WeatherForLocationTool implements Tool<String, String> {
    @Override
    public String apply(
            @ToolParam(description = "The city name") String city,
            ToolContext toolContext) {
        return "It's always sunny in " + city + "!";
    }

    @Override
    public ToolCallback toolCallback() {
        return FunctionToolCallback.builder("get_weather_for_location", this)
                .description("Get weather for a given city")
                .inputType(String.class)
                .build();
    }
}
