package com.lei.learn.spring.ai.support;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * ModelType
 * </p>
 *
 * @author 伍磊
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ModelType {

    TEXT("text", "文本模型"),
    FULL("full", "多模态模型"),

    ;

    private final String type;
    private final String desc;
}
