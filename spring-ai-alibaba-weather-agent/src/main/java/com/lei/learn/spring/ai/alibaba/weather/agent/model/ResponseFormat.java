package com.lei.learn.spring.ai.alibaba.weather.agent.model;

import lombok.Data;

/**
 * <p>
 * ResponseFormat
 * </p>
 *
 * @author 伍磊
 */
@Data
public class ResponseFormat {

    // 一个双关语响应（始终必需）
    private String punnyResponse;

    // 如果可用的话，关于天气的任何有趣信息
    private String weatherConditions;


}
