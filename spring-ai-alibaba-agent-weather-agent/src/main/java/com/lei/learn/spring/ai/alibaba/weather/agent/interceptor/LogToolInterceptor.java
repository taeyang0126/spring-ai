package com.lei.learn.spring.ai.alibaba.weather.agent.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * LogToolInterceptor
 * </p>
 *
 * @author 伍磊
 */
@Log4j2
public class LogToolInterceptor extends ToolInterceptor {

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        log.info("ToolInterceptor: Tool {} is called!", request.getToolName());
        return handler.call(request);
    }

    @Override
    public String getName() {
        return "LogToolInterceptor";
    }
}
