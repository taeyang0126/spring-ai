package com.lei.learn.etl.core.pipeline;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * <p>
 * RagPipeline 抽象类单元测试
 * </p>
 *
 * @author 伍磊
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RagPipeline 抽象类单元测试")
class RagPipelineTest {

    @Mock
    private DocumentReader mockDocumentReader;

    @Mock
    private VectorStore mockVectorStore;

    @Mock
    private TextSplitter mockTextSplitter;

    /**
     * 测试用的 RagPipeline 实现
     */
    private static class TestRagPipeline extends RagPipeline {

        private final DocumentReader reader;

        private final int batchSize;

        public TestRagPipeline(DocumentReader reader, int batchSize) {
            this.reader = reader;
            this.batchSize = batchSize;
        }

        public TestRagPipeline(DocumentReader reader) {
            this(reader, 10);
        }

        @Override
        protected DocumentReader getReader() {
            return this.reader;
        }

        @Override
        protected int getBatchSize() {
            return this.batchSize;
        }
    }

    @Nested
    @DisplayName("fromResource 方法测试")
    class FromResourceTests {

        @Test
        @DisplayName("使用有效的 Resource 创建管道")
        void testFromResourceWithValidResource() {
            // Given
            Resource resource = new ClassPathResource("test.md");
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When
            TextSplittingStage result = pipeline.fromResource(resource);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isSameAs(pipeline);
            assertThat(pipeline.resource).isSameAs(resource);
        }

        @Test
        @DisplayName("使用 null Resource 应抛出异常")
        void testFromResourceWithNullResource() {
            // Given
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When & Then
            assertThatThrownBy(() -> pipeline.fromResource(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Resource must not be null");
        }
    }

    @Nested
    @DisplayName("fromFile(File) 方法测试")
    class FromFileWithFileTests {

        @Test
        @DisplayName("使用有效的 File 创建管道")
        void testFromFileWithValidFile(@TempDir Path tempDir) throws IOException {
            // Given
            File testFile = tempDir.resolve("test.md").toFile();
            try (FileWriter writer = new FileWriter(testFile)) {
                writer.write("# Test Document\n\nThis is a test.");
            }

            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When
            TextSplittingStage result = pipeline.fromFile(testFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isSameAs(pipeline);
            assertThat(pipeline.resource).isNotNull();
        }

        @Test
        @DisplayName("使用 null File 应抛出异常")
        void testFromFileWithNullFile() {
            // Given
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When & Then
            assertThatThrownBy(() -> pipeline.fromFile((File) null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("File must not be null");
        }

        @Test
        @DisplayName("使用不存在的 File 应抛出异常")
        void testFromFileWithNonExistentFile() {
            // Given
            File nonExistentFile = new File("/nonexistent/path/file.md");
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When & Then
            assertThatThrownBy(() -> pipeline.fromFile(nonExistentFile))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("File does not exist");
        }

        @Test
        @DisplayName("使用目录应抛出异常")
        void testFromFileWithDirectory(@TempDir Path tempDir) {
            // Given
            File directory = tempDir.toFile();
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When & Then
            assertThatThrownBy(() -> pipeline.fromFile(directory))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("File must not be a directory");
        }
    }

    @Nested
    @DisplayName("fromFile(String) 方法测试")
    class FromFileWithStringTests {

        @Test
        @DisplayName("使用有效的文件路径创建管道")
        void testFromFileWithValidFilePath(@TempDir Path tempDir) throws IOException {
            // Given
            File testFile = tempDir.resolve("test.md").toFile();
            try (FileWriter writer = new FileWriter(testFile)) {
                writer.write("# Test Document\n\nThis is a test.");
            }

            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When
            TextSplittingStage result = pipeline.fromFile(testFile.getAbsolutePath());

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isSameAs(pipeline);
            assertThat(pipeline.resource).isNotNull();
        }

        @Test
        @DisplayName("使用 null 文件路径应抛出异常")
        void testFromFileWithNullPath() {
            // Given
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When & Then
            assertThatThrownBy(() -> pipeline.fromFile((String) null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("File path must not be null");
        }

        @Test
        @DisplayName("使用不存在的文件路径应抛出异常")
        void testFromFileWithNonExistentPath() {
            // Given
            String nonExistentPath = "/nonexistent/path/file.md";
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When & Then
            assertThatThrownBy(() -> pipeline.fromFile(nonExistentPath))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("File does not exist");
        }

        @Test
        @DisplayName("使用目录路径应抛出异常")
        void testFromFileWithDirectoryPath(@TempDir Path tempDir) {
            // Given
            String directoryPath = tempDir.toAbsolutePath().toString();
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When & Then
            assertThatThrownBy(() -> pipeline.fromFile(directoryPath))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("File must not be a directory");
        }
    }

    @Nested
    @DisplayName("withTextSplitter 方法测试")
    class WithTextSplitterTests {

        @Test
        @DisplayName("设置 TextSplitter")
        void testWithTextSplitter() {
            // Given
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When
            VectorStoringStage result = pipeline.withTextSplitter(mockTextSplitter);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isSameAs(pipeline);
            assertThat(pipeline.splitter).isSameAs(mockTextSplitter);
        }

        @Test
        @DisplayName("设置 null TextSplitter（允许）")
        void testWithNullTextSplitter() {
            // Given
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When
            VectorStoringStage result = pipeline.withTextSplitter(null);

            // Then
            assertThat(result).isNotNull();
            assertThat(pipeline.splitter).isNull();
        }

        @Test
        @DisplayName("多次设置 TextSplitter，最后设置生效")
        void testMultipleTextSplitterSettings() {
            // Given
            TextSplitter splitter1 = mock(TextSplitter.class);
            TextSplitter splitter2 = mock(TextSplitter.class);
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When
            pipeline.withTextSplitter(splitter1);
            pipeline.withTextSplitter(splitter2);

            // Then
            assertThat(pipeline.splitter).isSameAs(splitter2);
        }
    }

    @Nested
    @DisplayName("toVectorStore 方法测试")
    class ToVectorStoreTests {

        @Test
        @DisplayName("使用有效的 VectorStore 执行管道")
        void testToVectorStoreWithValidStore() {
            // Given
            List<Document> documents = createTestDocuments(5);
            when(mockDocumentReader.get()).thenReturn(documents);

            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When
            pipeline.toVectorStore(mockVectorStore);

            // Then
            verify(mockDocumentReader).get();
            verify(mockVectorStore, times(1)).add(any());
            assertThat(pipeline.vectorStore).isSameAs(mockVectorStore);
        }

        @Test
        @DisplayName("使用 null VectorStore 应抛出异常")
        void testToVectorStoreWithNullStore() {
            // Given
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When & Then
            assertThatThrownBy(() -> pipeline.toVectorStore(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("VectorStore must not be null");
        }

        @Test
        @DisplayName("DocumentReader 返回 null 应抛出异常")
        void testToVectorStoreWithNullReader() {
            // Given
            TestRagPipeline pipeline = new TestRagPipeline(null);

            // When & Then
            assertThatThrownBy(() -> pipeline.toVectorStore(mockVectorStore))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("DocumentReader must not be null");
        }

        @Test
        @DisplayName("DocumentReader 返回空列表应抛出异常")
        void testToVectorStoreWithEmptyDocuments() {
            // Given
            when(mockDocumentReader.get()).thenReturn(new ArrayList<>());
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When & Then
            assertThatThrownBy(() -> pipeline.toVectorStore(mockVectorStore))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found document");
        }

        @Test
        @DisplayName("没有 TextSplitter 时直接存储原始文档")
        void testToVectorStoreWithoutSplitter() {
            // Given
            List<Document> documents = createTestDocuments(3);
            when(mockDocumentReader.get()).thenReturn(documents);

            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);
            pipeline.withTextSplitter(null);

            // When
            pipeline.toVectorStore(mockVectorStore);

            // Then
            verify(mockTextSplitter, never()).apply(any());
            verify(mockVectorStore).add(documents);
        }

        @Test
        @DisplayName("有 TextSplitter 时先分割再存储")
        void testToVectorStoreWithSplitter() {
            // Given
            List<Document> documents = createTestDocuments(2);
            List<Document> chunks = createTestDocuments(10);

            when(mockDocumentReader.get()).thenReturn(documents);
            when(mockTextSplitter.apply(documents)).thenReturn(chunks);

            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);
            pipeline.withTextSplitter(mockTextSplitter);

            // When
            pipeline.toVectorStore(mockVectorStore);

            // Then
            verify(mockTextSplitter).apply(documents);
            verify(mockVectorStore).add(chunks);
        }
    }

    @Nested
    @DisplayName("批次处理测试")
    class BatchProcessingTests {

        @Test
        @DisplayName("文档数量小于批次大小时一次性处理")
        void testProcessingWithSmallerDocumentCount() {
            // Given
            List<Document> documents = createTestDocuments(5);
            when(mockDocumentReader.get()).thenReturn(documents);

            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader, 10);

            // When
            pipeline.toVectorStore(mockVectorStore);

            // Then
            verify(mockVectorStore, times(1)).add(any());
        }

        @Test
        @DisplayName("文档数量等于批次大小时一次性处理")
        void testProcessingWithEqualDocumentCount() {
            // Given
            List<Document> documents = createTestDocuments(10);
            when(mockDocumentReader.get()).thenReturn(documents);

            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader, 10);

            // When
            pipeline.toVectorStore(mockVectorStore);

            // Then
            verify(mockVectorStore, times(1)).add(any());
        }

        @Test
        @DisplayName("文档数量大于批次大时分批处理")
        void testProcessingWithLargerDocumentCount() {
            // Given
            List<Document> documents = createTestDocuments(25);
            when(mockDocumentReader.get()).thenReturn(documents);

            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader, 10);

            // When
            pipeline.toVectorStore(mockVectorStore);

            // Then
            verify(mockVectorStore, times(3)).add(any());
        }

        @Test
        @DisplayName("自定义批次大小验证")
        void testCustomBatchSize() {
            // Given
            List<Document> documents = createTestDocuments(20);
            when(mockDocumentReader.get()).thenReturn(documents);

            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader, 5);

            // When
            pipeline.toVectorStore(mockVectorStore);

            // Then
            verify(mockVectorStore, times(4)).add(any());
        }

        @Test
        @DisplayName("部分批次失败应抛出异常")
        void testPartialBatchFailure() {
            // Given
            List<Document> documents = createTestDocuments(20);
            when(mockDocumentReader.get()).thenReturn(documents);

            // 第一次和第三次成功，第二次失败
            doNothing().doThrow(new RuntimeException("Batch failed")).doNothing()
                    .when(mockVectorStore).add(any());

            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader, 10);

            // When & Then
            assertThatThrownBy(() -> pipeline.toVectorStore(mockVectorStore))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("部分批次处理失败");
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class ChainingTests {

        @Test
        @DisplayName("完整链式调用：fromFile -> withTextSplitter -> toVectorStore")
        void testCompleteChaining(@TempDir Path tempDir) throws IOException {
            // Given
            File testFile = tempDir.resolve("test.md").toFile();
            try (FileWriter writer = new FileWriter(testFile)) {
                writer.write("# Test\n\nContent");
            }

            List<Document> documents = createTestDocuments(3);
            List<Document> chunks = createTestDocuments(5);
            when(mockDocumentReader.get()).thenReturn(documents);
            when(mockTextSplitter.apply(documents)).thenReturn(chunks);

            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When
            pipeline.fromFile(testFile.getAbsolutePath())
                    .withTextSplitter(mockTextSplitter)
                    .toVectorStore(mockVectorStore);

            // Then
            assertThat(pipeline.resource).isNotNull();
            assertThat(pipeline.splitter).isSameAs(mockTextSplitter);
            assertThat(pipeline.vectorStore).isSameAs(mockVectorStore);
            verify(mockVectorStore).add(chunks);
        }

        @Test
        @DisplayName("链式调用返回正确的阶段接口")
        void testChainingReturnsCorrectStages(@TempDir Path tempDir) throws IOException {
            // Given
            File testFile = tempDir.resolve("test.md").toFile();
            try (FileWriter writer = new FileWriter(testFile)) {
                writer.write("# Test");
            }

            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When
            TextSplittingStage stage1 = pipeline.fromFile(testFile.getAbsolutePath());
            VectorStoringStage stage2 = stage1.withTextSplitter(mockTextSplitter);

            // Then
            assertThat(stage1).isInstanceOf(TextSplittingStage.class);
            assertThat(stage2).isInstanceOf(VectorStoringStage.class);
        }
    }

    @Nested
    @DisplayName("getBatchSize 方法测试")
    class GetBatchSizeTests {

        @Test
        @DisplayName("默认批次大小为 10")
        void testDefaultBatchSize() {
            // Given
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader);

            // When
            int batchSize = pipeline.getBatchSize();

            // Then
            assertThat(batchSize).isEqualTo(10);
        }

        @Test
        @DisplayName("自定义批次大小")
        void testCustomBatchSize() {
            // Given
            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader, 25);

            // When
            int batchSize = pipeline.getBatchSize();

            // Then
            assertThat(batchSize).isEqualTo(25);
        }

        @Test
        @DisplayName("批次大小为 1 时每批处理一个文档")
        void testBatchSizeOfOne() {
            // Given
            List<Document> documents = createTestDocuments(5);
            when(mockDocumentReader.get()).thenReturn(documents);

            TestRagPipeline pipeline = new TestRagPipeline(mockDocumentReader, 1);

            // When
            pipeline.toVectorStore(mockVectorStore);

            // Then
            verify(mockVectorStore, times(5)).add(any());
        }
    }

    /**
     * 创建测试用的文档列表
     */
    private List<Document> createTestDocuments(int count) {
        List<Document> documents = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            documents.add(new Document("Test content " + i));
        }
        return documents;
    }
}
