package com.lei.learn.etl.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Markdown 文档处理响应
 * </p>
 *
 * @author 伍磊
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkdownProcessResponse {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 处理的文档数量
     */
    private Integer documentCount;

    /**
     * 处理的分块数量
     */
    private Integer chunkCount;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;
}
