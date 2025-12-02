package com.lei.learn.spring.ai.alibaba.weather.agent.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;

import java.util.function.BiFunction;

/**
 * <p>
 * Tool
 * </p>
 *
 * @author 伍磊
 */
public interface Tool<I, O> extends BiFunction<I, ToolContext, O> {

    ToolCallback toolCallback();

}
