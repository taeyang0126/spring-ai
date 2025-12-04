package com.lei.learn.etl.opensearch.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Markdown 文档处理请求
 * </p>
 *
 * @author 伍磊
 */
@Data
public class MarkdownProcessRequest {

    /**
     * 文件路径（本地文件系统路径）
     */
    private String filePath;

    /**
     * 文本分割器配置
     */
    private SplitterConfig splitterConfig;

    /**
     * Markdown 配置
     */
    private MarkdownConfig markdownConfig;

    /**
     * 额外的元数据
     */
    private Map<String, Object> metadata = new HashMap<>();

    @Data
    public static class SplitterConfig {
        /**
         * 分块大小
         */
        private Integer chunkSize = 800;

        /**
         * 最小分块字符数
         */
        private Integer minChunkSizeChars = 300;

        /**
         * 最小嵌入长度
         */
        private Integer minChunkLengthToEmbed = 5;

        /**
         * 最大分块数量
         */
        private Integer maxNumChunks = 10000;

        /**
         * 是否保留分隔符
         */
        private Boolean keepSeparator = true;
    }

    @Data
    public static class MarkdownConfig {
        /**
         * 遇到横线是否创建新文档
         */
        private Boolean horizontalRuleCreateDocument = false;

        /**
         * 是否包含代码块
         */
        private Boolean includeCodeBlock = true;

        /**
         * 是否包含引用块
         */
        private Boolean includeBlockquote = true;
    }
}
