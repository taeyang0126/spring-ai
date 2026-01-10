package com.lei.learn.etl.core.pipeline.markdown;

import com.lei.learn.etl.core.pipeline.ResourceLoadingStage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

/**
 * <p>
 * MarkdownRagPipeline.Builder 单元测试
 * </p>
 *
 * @author 伍磊
 */
@DisplayName("MarkdownRagPipeline.Builder 单元测试")
class MarkdownRagPipelineBuilderTest {

    /**
     * 使用反射获取私有字段 additionalMetadata 的值
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getAdditionalMetadata(MarkdownRagPipeline pipeline) {
        try {
            Field field = MarkdownRagPipeline.class.getDeclaredField("additionalMetadata");
            field.setAccessible(true);
            return (Map<String, Object>) field.get(pipeline);
        } catch (Exception e) {
            throw new RuntimeException("无法访问 additionalMetadata 字段", e);
        }
    }

    @Nested
    @DisplayName("默认配置测试")
    class DefaultConfigTests {

        @Test
        @DisplayName("验证默认配置值")
        void testDefaultConfigValues() {
            // When
            ResourceLoadingStage stage = MarkdownRagPipeline.builder().build();
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) stage;

            // Then
            assertThat(pipeline.horizontalRuleCreateDocument).isFalse();
            assertThat(pipeline.includeCodeBlock).isTrue();
            assertThat(pipeline.includeBlockquote).isTrue();

            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).isNotNull();
            assertThat(metadata).isEmpty();
        }

        @Test
        @DisplayName("使用 defaultConfig() 静态方法创建实例")
        void testDefaultConfigStaticMethod() {
            // When
            var pipeline = MarkdownRagPipeline.defaultConfig();

            // Then
            assertThat(pipeline).isNotNull();
            assertThat(pipeline).isInstanceOf(MarkdownRagPipeline.class);
        }

        @Test
        @DisplayName("Builder 可重复使用创建多个实例")
        void testBuilderReusability() {
            // Given
            var builder = MarkdownRagPipeline.builder();

            // When
            MarkdownRagPipeline pipeline1 = (MarkdownRagPipeline) builder.withHorizontalRuleCreateDocument(true).build();
            MarkdownRagPipeline pipeline2 = (MarkdownRagPipeline) builder.withHorizontalRuleCreateDocument(false).build();

            // Then
            assertThat(pipeline1.horizontalRuleCreateDocument).isTrue();
            assertThat(pipeline2.horizontalRuleCreateDocument).isFalse();
        }
    }

    @Nested
    @DisplayName("水平线配置测试")
    class HorizontalRuleConfigTests {

        @Test
        @DisplayName("设置横线创建新文档为 true")
        void testWithHorizontalRuleCreateDocumentTrue() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withHorizontalRuleCreateDocument(true)
                    .build();

            // Then
            assertThat(pipeline.horizontalRuleCreateDocument).isTrue();
        }

        @Test
        @DisplayName("设置横线创建新文档为 false")
        void testWithHorizontalRuleCreateDocumentFalse() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withHorizontalRuleCreateDocument(false)
                    .build();

            // Then
            assertThat(pipeline.horizontalRuleCreateDocument).isFalse();
        }

        @Test
        @DisplayName("多次设置横线配置，最后设置生效")
        void testMultipleHorizontalRuleSettings() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withHorizontalRuleCreateDocument(true)
                    .withHorizontalRuleCreateDocument(false)
                    .withHorizontalRuleCreateDocument(true)
                    .build();

            // Then
            assertThat(pipeline.horizontalRuleCreateDocument).isTrue();
        }
    }

    @Nested
    @DisplayName("代码块配置测试")
    class CodeBlockConfigTests {

        @Test
        @DisplayName("设置包含代码块为 true")
        void testWithIncludeCodeBlockTrue() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withIncludeCodeBlock(true)
                    .build();

            // Then
            assertThat(pipeline.includeCodeBlock).isTrue();
        }

        @Test
        @DisplayName("设置包含代码块为 false")
        void testWithIncludeCodeBlockFalse() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withIncludeCodeBlock(false)
                    .build();

            // Then
            assertThat(pipeline.includeCodeBlock).isFalse();
        }

        @Test
        @DisplayName("默认包含代码块为 true")
        void testDefaultIncludeCodeBlock() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder().build();

            // Then
            assertThat(pipeline.includeCodeBlock).isTrue();
        }
    }

    @Nested
    @DisplayName("引用块配置测试")
    class BlockquoteConfigTests {

        @Test
        @DisplayName("设置包含引用块为 true")
        void testWithIncludeBlockquoteTrue() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withIncludeBlockquote(true)
                    .build();

            // Then
            assertThat(pipeline.includeBlockquote).isTrue();
        }

        @Test
        @DisplayName("设置包含引用块为 false")
        void testWithIncludeBlockquoteFalse() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withIncludeBlockquote(false)
                    .build();

            // Then
            assertThat(pipeline.includeBlockquote).isFalse();
        }

        @Test
        @DisplayName("默认包含引用块为 true")
        void testDefaultIncludeBlockquote() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder().build();

            // Then
            assertThat(pipeline.includeBlockquote).isTrue();
        }
    }

    @Nested
    @DisplayName("元数据处理测试")
    class MetadataTests {

        @Test
        @DisplayName("添加单个元数据")
        void testWithSingleAdditionalMetadata() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withAdditionalMetadata("author", "test-author")
                    .build();

            // Then
            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).hasSize(1);
            assertThat(metadata).contains(entry("author", "test-author"));
        }

        @Test
        @DisplayName("添加多个元数据")
        void testWithMultipleAdditionalMetadata() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withAdditionalMetadata("author", "test-author")
                    .withAdditionalMetadata("category", "tech")
                    .withAdditionalMetadata("version", "1.0")
                    .withAdditionalMetadata("published", true)
                    .build();

            // Then
            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).hasSize(4);
            assertThat(metadata).contains(
                    entry("author", "test-author"),
                    entry("category", "tech"),
                    entry("version", "1.0"),
                    entry("published", true)
            );
        }

        @Test
        @DisplayName("添加 Map 形式的元数据")
        void testWithMapAdditionalMetadata() {
            // Given
            Map<String, Object> metadata = new HashMap<>(4);
            metadata.put("key1", "value1");
            metadata.put("key2", 123);
            metadata.put("key3", true);

            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withAdditionalMetadata(metadata)
                    .build();

            // Then
            Map<String, Object> result = getAdditionalMetadata(pipeline);
            assertThat(result).hasSize(3);
            assertThat(result).contains(
                    entry("key1", "value1"),
                    entry("key2", 123),
                    entry("key3", true)
            );
        }

        @Test
        @DisplayName("添加 null key 应抛出异常")
        void testWithNullKeyThrowsException() {
            // When & Then
            assertThatThrownBy(() ->
                    MarkdownRagPipeline.builder()
                            .withAdditionalMetadata(null, "value")
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("key must not be null");
        }

        @Test
        @DisplayName("添加 null value 应抛出异常")
        void testWithNullValueThrowsException() {
            // When & Then
            assertThatThrownBy(() ->
                    MarkdownRagPipeline.builder()
                            .withAdditionalMetadata("key", null)
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("value must not be null");
        }

        @Test
        @DisplayName("添加 null Map 应抛出异常")
        void testWithNullMapThrowsException() {
            // When & Then
            assertThatThrownBy(() ->
                    MarkdownRagPipeline.builder()
                            .withAdditionalMetadata((Map<String, Object>) null)
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("additionalMetadata must not be null");
        }

        @Test
        @DisplayName("重复添加相同 key 的元数据应覆盖旧值")
        void testDuplicateKeyOverwritesValue() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withAdditionalMetadata("key", "original-value")
                    .withAdditionalMetadata("key", "new-value")
                    .build();

            // Then
            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).hasSize(1);
            assertThat(metadata.get("key")).isEqualTo("new-value");
        }

        @Test
        @DisplayName("使用 Map 替换所有元数据")
        void testReplaceMetadataWithMap() {
            // Given
            Map<String, Object> initialMetadata = new HashMap<>(4);
            initialMetadata.put("old-key", "old-value");

            Map<String, Object> newMetadata = new HashMap<>(4);
            newMetadata.put("new-key1", "new-value1");
            newMetadata.put("new-key2", "new-value2");

            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withAdditionalMetadata(initialMetadata)
                    .withAdditionalMetadata(newMetadata)
                    .build();

            // Then
            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).hasSize(2);
            assertThat(metadata).doesNotContainKey("old-key");
            assertThat(metadata).contains(
                    entry("new-key1", "new-value1"),
                    entry("new-key2", "new-value2")
            );
        }

        @Test
        @DisplayName("添加不同类型的元数据值")
        void testDifferentMetadataValueTypes() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withAdditionalMetadata("stringKey", "stringValue")
                    .withAdditionalMetadata("intKey", 42)
                    .withAdditionalMetadata("longKey", 123456789L)
                    .withAdditionalMetadata("booleanKey", true)
                    .withAdditionalMetadata("doubleKey", 3.14)
                    .build();

            // Then
            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).hasSize(5);
            assertThat(metadata.get("stringKey")).isInstanceOf(String.class);
            assertThat(metadata.get("intKey")).isInstanceOf(Integer.class);
            assertThat(metadata.get("longKey")).isInstanceOf(Long.class);
            assertThat(metadata.get("booleanKey")).isInstanceOf(Boolean.class);
            assertThat(metadata.get("doubleKey")).isInstanceOf(Double.class);
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class ChainingTests {

        @Test
        @DisplayName("完整的链式调用构建")
        void testCompleteChaining() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withHorizontalRuleCreateDocument(true)
                    .withIncludeCodeBlock(false)
                    .withIncludeBlockquote(false)
                    .withAdditionalMetadata("key1", "value1")
                    .withAdditionalMetadata("key2", "value2")
                    .build();

            // Then
            assertThat(pipeline.horizontalRuleCreateDocument).isTrue();
            assertThat(pipeline.includeCodeBlock).isFalse();
            assertThat(pipeline.includeBlockquote).isFalse();

            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).hasSize(2);
        }
    }

    @Nested
    @DisplayName("实际使用场景测试")
    class RealWorldScenarios {

        @Test
        @DisplayName("场景：处理技术文档（包含代码块）")
        void testTechnicalDocumentScenario() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withIncludeCodeBlock(true)
                    .withIncludeBlockquote(true)
                    .withAdditionalMetadata("category", "technical")
                    .withAdditionalMetadata("language", "java")
                    .withAdditionalMetadata("version", "21")
                    .build();

            // Then
            assertThat(pipeline.includeCodeBlock).isTrue();
            assertThat(pipeline.includeBlockquote).isTrue();

            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).contains(
                    entry("category", "technical"),
                    entry("language", "java"),
                    entry("version", "21")
            );
        }

        @Test
        @DisplayName("场景：处理博客文章（不包含代码块）")
        void testBlogPostScenario() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withIncludeCodeBlock(false)
                    .withIncludeBlockquote(true)
                    .withAdditionalMetadata("type", "blog")
                    .withAdditionalMetadata("author", "John Doe")
                    .build();

            // Then
            assertThat(pipeline.includeCodeBlock).isFalse();
            assertThat(pipeline.includeBlockquote).isTrue();

            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).contains(
                    entry("type", "blog"),
                    entry("author", "John Doe")
            );
        }

        @Test
        @DisplayName("场景：处理分隔文档（按横线分隔）")
        void testSeparatedDocumentScenario() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withHorizontalRuleCreateDocument(true)
                    .withAdditionalMetadata("format", "separated")
                    .build();

            // Then
            assertThat(pipeline.horizontalRuleCreateDocument).isTrue();

            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).contains(entry("format", "separated"));
        }

        @Test
        @DisplayName("场景：最小化配置（使用所有默认值）")
        void testMinimalConfigurationScenario() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder().build();

            // Then
            assertThat(pipeline.horizontalRuleCreateDocument).isFalse();
            assertThat(pipeline.includeCodeBlock).isTrue();
            assertThat(pipeline.includeBlockquote).isTrue();

            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).isEmpty();
        }
    }

    @Nested
    @DisplayName("边界值测试")
    class BoundaryTests {

        @Test
        @DisplayName("空字符串元数据值")
        void testEmptyStringMetadataValue() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withAdditionalMetadata("key", "")
                    .build();

            // Then
            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).contains(entry("key", ""));
        }

        @Test
        @DisplayName("零值元数据")
        void testZeroMetadataValues() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withAdditionalMetadata("int-zero", 0)
                    .withAdditionalMetadata("long-zero", 0L)
                    .withAdditionalMetadata("double-zero", 0.0)
                    .build();

            // Then
            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).contains(
                    entry("int-zero", 0),
                    entry("long-zero", 0L),
                    entry("double-zero", 0.0)
            );
        }

        @Test
        @DisplayName("特殊字符元数据键")
        void testSpecialCharactersInMetadataKey() {
            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) MarkdownRagPipeline.builder()
                    .withAdditionalMetadata("key-with-dash", "value1")
                    .withAdditionalMetadata("key_with_underscore", "value2")
                    .withAdditionalMetadata("key.with.dot", "value3")
                    .build();

            // Then
            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).contains(
                    entry("key-with-dash", "value1"),
                    entry("key_with_underscore", "value2"),
                    entry("key.with.dot", "value3")
            );
        }

        @Test
        @DisplayName("大量元数据添加")
        void testLargeMetadataAddition() {
            // When
            var builder = MarkdownRagPipeline.builder();
            for (int i = 0; i < 100; i++) {
                builder.withAdditionalMetadata("key-" + i, "value-" + i);
            }
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) builder.build();

            // Then
            Map<String, Object> metadata = getAdditionalMetadata(pipeline);
            assertThat(metadata).hasSize(100);
            assertThat(metadata).contains(entry("key-0", "value-0"));
            assertThat(metadata).contains(entry("key-99", "value-99"));
        }
    }

    @Nested
    @DisplayName("Builder 实例独立性测试")
    class BuilderIndependenceTests {

        @Test
        @DisplayName("不同 Builder 实例创建独立对象")
        void testDifferentBuildersCreateIndependentObjects() {
            // Given
            var builder1 = MarkdownRagPipeline.builder()
                    .withHorizontalRuleCreateDocument(true)
                    .withAdditionalMetadata("key", "value1");

            var builder2 = MarkdownRagPipeline.builder()
                    .withHorizontalRuleCreateDocument(false)
                    .withAdditionalMetadata("key", "value2");

            // When
            MarkdownRagPipeline pipeline1 = (MarkdownRagPipeline) builder1.build();
            MarkdownRagPipeline pipeline2 = (MarkdownRagPipeline) builder2.build();

            // Then
            assertThat(pipeline1.horizontalRuleCreateDocument).isTrue();
            assertThat(pipeline2.horizontalRuleCreateDocument).isFalse();

            Map<String, Object> metadata1 = getAdditionalMetadata(pipeline1);
            Map<String, Object> metadata2 = getAdditionalMetadata(pipeline2);
            assertThat(metadata1.get("key")).isEqualTo("value1");
            assertThat(metadata2.get("key")).isEqualTo("value2");
        }

        @Test
        @DisplayName("构建后修改元数据 Map 不影响已构建对象")
        void testMetadataMapImmutabilityAfterBuild() {
            // Given
            Map<String, Object> metadata = new HashMap<>(4);
            metadata.put("key1", "value1");

            var builder = MarkdownRagPipeline.builder()
                    .withAdditionalMetadata(metadata);

            // When
            MarkdownRagPipeline pipeline = (MarkdownRagPipeline) builder.build();
            metadata.put("key2", "value2"); // 修改原始 Map

            // Then - 已构建的对象不应受影响
            Map<String, Object> pipelineMetadata = getAdditionalMetadata(pipeline);
            assertThat(pipelineMetadata).hasSize(1);
            assertThat(pipelineMetadata).doesNotContainKey("key2");
        }
    }
}
