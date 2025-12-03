package com.lei.learn.etl.opensearch;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void parse() {

        // 1. 加载文档
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                // 设置为 true，则遇到横线会划分为一个新的文档
                .withHorizontalRuleCreateDocument(true)
                // 设置为 true 时，代码块将包含在与周围文本相同的 Document 中。设置为 false 时，代码块将创建单独的 Document 对象。
                .withIncludeCodeBlock(true)
                // 设置为 true 时，引用块将与周围文本包含在同一个 Document 中。设置为 false 时，引用块将创建单独的 Document 对象。
                .withIncludeBlockquote(true)
                // 定义元数据
                .withAdditionalMetadata("filename", "Arthas.md")
                .withAdditionalMetadata("link",
                        "https://taeyang0126.github.io/post/arthas%2FArthas")
                .build();

        var documentResource = new ClassPathResource("document/Arthas.md");
        var reader = new MarkdownDocumentReader(documentResource, config);
        List<Document> documents = reader.get();

        // 2. 分割文档为块
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(documents);

        // 3. 将块添加到向量存储
        final int batchSize = 10;
        List<Document> insertList = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            if ((i + 1) % batchSize == 0) {
                vectorStore.add(insertList);
                insertList.clear();
            } else {
                insertList.add(chunks.get(i));
            }
        }

        // 现在可以使用向量存储进行检索
        List<Document> results = vectorStore.similaritySearch("spring context");
        System.out.println(results);

    }


}
