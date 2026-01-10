package com.lei.learn.etl.core.pipeline.markdown;

import com.lei.learn.etl.core.pipeline.RagPipeline;
import com.lei.learn.etl.core.pipeline.ResourceLoadingStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * MarkdownRagPipeline
 * </p>
 *
 * @author 伍磊
 */
public class MarkdownRagPipeline extends RagPipeline {

    public final boolean horizontalRuleCreateDocument;

    public final boolean includeCodeBlock;

    public final boolean includeBlockquote;

    private final Map<String, Object> additionalMetadata;

    public MarkdownRagPipeline(MarkdownRagPipeline.Builder builder) {
        this.horizontalRuleCreateDocument = builder.horizontalRuleCreateDocument;
        this.includeCodeBlock = builder.includeCodeBlock;
        this.includeBlockquote = builder.includeBlockquote;
        this.additionalMetadata = new HashMap<>(builder.additionalMetadata.size());
        this.additionalMetadata.putAll(builder.additionalMetadata);
    }


    @Override
    protected DocumentReader getReader() {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(horizontalRuleCreateDocument)
                .withIncludeCodeBlock(includeCodeBlock)
                .withIncludeBlockquote(includeBlockquote)
                .withAdditionalMetadata(additionalMetadata)
                .build();
        return new MarkdownDocumentReader(super.resource, config);
    }

    public static ResourceLoadingStage defaultConfig() {
        return builder().build();
    }

    public static MarkdownRagPipeline.Builder builder() {
        return new Builder();
    }


    public static final class Builder {

        private static final Logger log = LoggerFactory.getLogger(Builder.class);

        private boolean horizontalRuleCreateDocument = false;

        /**
         * 是否包含代码块（默认 true，与 MarkdownConfig 保持一致）
         */
        private boolean includeCodeBlock = true;

        /**
         * 是否包含引用块（默认 true，与 MarkdownConfig 保持一致）
         */
        private boolean includeBlockquote = true;

        /**
         * 额外的元数据（默认容量 4）
         */
        private Map<String, Object> additionalMetadata = new HashMap<>(4);

        public MarkdownRagPipeline.Builder withHorizontalRuleCreateDocument(boolean horizontalRuleCreateDocument) {
            this.horizontalRuleCreateDocument = horizontalRuleCreateDocument;
            return this;
        }

        public MarkdownRagPipeline.Builder withIncludeCodeBlock(boolean includeCodeBlock) {
            this.includeCodeBlock = includeCodeBlock;
            return this;
        }

        public MarkdownRagPipeline.Builder withIncludeBlockquote(boolean includeBlockquote) {
            this.includeBlockquote = includeBlockquote;
            return this;
        }

        /**
         * 添加额外的元数据
         *
         * @param key   元数据键
         * @param value 元数据值
         * @return this
         * @throws IllegalArgumentException 如果 key 或 value 为 null
         */
        public MarkdownRagPipeline.Builder withAdditionalMetadata(String key, Object value) {
            Assert.notNull(key, "key must not be null");
            Assert.notNull(value, "value must not be null");

            // 检测重复键并记录警告
            if (this.additionalMetadata.containsKey(key)) {
                log.warn("元数据键 '{}' 已存在（旧值：'{}'），将被新值 '{}' 覆盖",
                         key, this.additionalMetadata.get(key), value);
            }

            this.additionalMetadata.put(key, value);
            return this;
        }

        public MarkdownRagPipeline.Builder withAdditionalMetadata(Map<String, Object> additionalMetadata) {
            Assert.notNull(additionalMetadata, "additionalMetadata must not be null");
            this.additionalMetadata = additionalMetadata;
            return this;
        }

        public ResourceLoadingStage build() {
            return new MarkdownRagPipeline(this);
        }

    }

}
