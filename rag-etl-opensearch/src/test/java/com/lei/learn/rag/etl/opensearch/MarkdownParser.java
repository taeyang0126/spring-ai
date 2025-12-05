package com.lei.learn.rag.etl.opensearch;

import com.lei.learn.etl.core.pipeline.markdown.MarkdownRagPipeline;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * SpringBootTest
 * </p>
 *
 * @author 伍磊
 */
@SpringBootTest
public class MarkdownParser {

    @Autowired
    private VectorStore vectorStore;

    static final String filename = "FileIO.md";
    static final String link = "https://taeyang0126.github.io/post/java/FileIO";

    @Test
    public void parse() throws InterruptedException {

        var documentResource = new ClassPathResource("document/" + filename);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filename", filename);
        metadata.put("link", link);

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(300)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();

        MarkdownRagPipeline.builder()
                // 设置为 true，则遇到横线会划分为一个新的文档
                .withHorizontalRuleCreateDocument(false)
                // 设置为 true 时，代码块将包含在与周围文本相同的 Document 中。设置为 false 时，代码块将创建单独的 Document 对象。
                .withIncludeCodeBlock(true)
                // 设置为 true 时，引用块将与周围文本包含在同一个 Document 中。设置为 false 时，引用块将创建单独的 Document 对象。
                .withIncludeBlockquote(true)
                // 定义元数据
                .withAdditionalMetadata("filename", filename)
                .withAdditionalMetadata("link", link)
                .build()
                .fromResource(documentResource)
                .withTextSplitter(splitter)
                .toVectorStore(vectorStore);

        // 4. 强制刷新（确保后续可查）
        Optional<OpenSearchClient> nativeClientOpt = vectorStore.getNativeClient();
        nativeClientOpt.ifPresent(client -> {
            try {
                client.indices().refresh(r -> r.index("spring-ai-document-index"));
            } catch (IOException e) {
                // ignore in test
            }
        });

        List<Document> results = vectorStore.similaritySearch("mmap");
        for (Document result : results) {
            System.out.println(result.getScore() + " : " + result.getText());
        }
    }

    @Test
    public void search() {

        // 简单问答，直接查询
        SearchRequest searchRequest = SearchRequest.builder()
                // 原始查询文本
                .query("mmap")
                // 最多返回多少个最相似的文档片段（chunks）
                .topK(5)
                // 只返回相似度 ≥ 此阈值的文档
                .similarityThreshold(0.55)
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);
        for (Document result : results) {
            System.out.println(result.getScore() + " : " + result.getText());
        }

        // 生产实践
        // 实现两阶段：
        // 1. topK=20 低门槛召回
        // 2. 用 LLM 或 re-ranker 精筛
    }

    @Test
    public void test_delete_native() throws IOException {
        Optional<OpenSearchClient> nativeClient = vectorStore.getNativeClient();
        OpenSearchClient openSearchClient = nativeClient.get();
        DeleteByQueryResponse deleteByQueryResponse = openSearchClient.deleteByQuery(b -> b
                .index("spring-ai-document-index")
                .query(q -> q.term(t -> t.field("metadata.link.keyword").value(FieldValue.of(link))))
        );
        System.out.println(deleteByQueryResponse.deleted());
    }


}
