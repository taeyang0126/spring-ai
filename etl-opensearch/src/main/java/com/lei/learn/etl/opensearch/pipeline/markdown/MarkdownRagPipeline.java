package com.lei.learn.etl.opensearch.pipeline.markdown;

import com.lei.learn.etl.opensearch.pipeline.RagPipeline;
import com.lei.learn.etl.opensearch.pipeline.ResourceLoadingStage;
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
        this.additionalMetadata = new HashMap<>(builder.additionalMetadata);
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

        private boolean horizontalRuleCreateDocument = false;

        private boolean includeCodeBlock = false;

        private boolean includeBlockquote = false;

        private Map<String, Object> additionalMetadata = new HashMap<>();

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

        public MarkdownRagPipeline.Builder withAdditionalMetadata(String key, Object value) {
            Assert.notNull(key, "key must not be null");
            Assert.notNull(value, "value must not be null");
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
