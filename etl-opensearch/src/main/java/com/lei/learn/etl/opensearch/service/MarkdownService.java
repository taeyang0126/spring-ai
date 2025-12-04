package com.lei.learn.etl.opensearch.service;

import com.lei.learn.etl.opensearch.model.MarkdownProcessRequest;
import com.lei.learn.etl.opensearch.model.MarkdownProcessResponse;
import com.lei.learn.etl.opensearch.pipeline.markdown.MarkdownRagPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

/**
 * <p>
 * Markdown 文档处理服务
 * </p>
 *
 * @author 伍磊
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarkdownService {

    private final VectorStore vectorStore;

    /**
     * 处理本地 Markdown 文件
     *
     * @param request 处理请求
     * @return 处理响应
     */
    public MarkdownProcessResponse processMarkdownFile(MarkdownProcessRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证文件路径
            String filePath = request.getFilePath();
            if (filePath == null || filePath.trim().isEmpty()) {
                return buildErrorResponse("文件路径不能为空", filePath, startTime);
            }

            File file = new File(filePath);
            if (!file.exists()) {
                return buildErrorResponse("文件不存在: " + filePath, filePath, startTime);
            }

            if (!file.isFile()) {
                return buildErrorResponse("路径不是文件: " + filePath, filePath, startTime);
            }

            if (!filePath.toLowerCase().endsWith(".md")) {
                return buildErrorResponse("文件不是 Markdown 格式: " + filePath, filePath, startTime);
            }

            // 2. 构建文本分割器
            TokenTextSplitter splitter = buildTextSplitter(request.getSplitterConfig());

            // 3. 构建 MarkdownRagPipeline
            MarkdownRagPipeline.Builder pipelineBuilder = MarkdownRagPipeline.builder();

            // 配置 Markdown 选项
            if (request.getMarkdownConfig() != null) {
                MarkdownProcessRequest.MarkdownConfig config = request.getMarkdownConfig();
                if (config.getHorizontalRuleCreateDocument() != null) {
                    pipelineBuilder.withHorizontalRuleCreateDocument(config.getHorizontalRuleCreateDocument());
                }
                if (config.getIncludeCodeBlock() != null) {
                    pipelineBuilder.withIncludeCodeBlock(config.getIncludeCodeBlock());
                }
                if (config.getIncludeBlockquote() != null) {
                    pipelineBuilder.withIncludeBlockquote(config.getIncludeBlockquote());
                }
            }

            // 添加元数据
            if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
                for (Map.Entry<String, Object> entry : request.getMetadata().entrySet()) {
                    pipelineBuilder.withAdditionalMetadata(entry.getKey(), entry.getValue());
                }
            }

            // 添加默认元数据
            pipelineBuilder.withAdditionalMetadata("filename", file.getName());
            pipelineBuilder.withAdditionalMetadata("filepath", file.getAbsolutePath());

            // 4. 执行 RAG Pipeline
            log.info("开始处理 Markdown 文件: {}", filePath);
            pipelineBuilder.build()
                    .fromFile(file)
                    .withTextSplitter(splitter)
                    .toVectorStore(vectorStore);

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Markdown 文件处理完成: {}, 耗时: {}ms", filePath, processingTime);

            // 5. 构建成功响应
            return MarkdownProcessResponse.builder()
                    .success(true)
                    .message("文档处理成功")
                    .filePath(filePath)
                    .processingTime(processingTime)
                    .build();

        } catch (Exception e) {
            log.error("处理 Markdown 文件失败: {}", request.getFilePath(), e);
            long processingTime = System.currentTimeMillis() - startTime;
            return MarkdownProcessResponse.builder()
                    .success(false)
                    .message("处理失败: " + e.getMessage())
                    .filePath(request.getFilePath())
                    .processingTime(processingTime)
                    .build();
        }
    }

    /**
     * 构建文本分割器
     */
    private TokenTextSplitter buildTextSplitter(MarkdownProcessRequest.SplitterConfig config) {
        if (config == null) {
            config = new MarkdownProcessRequest.SplitterConfig();
        }

        return TokenTextSplitter.builder()
                .withChunkSize(config.getChunkSize() != null ? config.getChunkSize() : 800)
                .withMinChunkSizeChars(config.getMinChunkSizeChars() != null ? config.getMinChunkSizeChars() : 300)
                .withMinChunkLengthToEmbed(config.getMinChunkLengthToEmbed() != null ? config.getMinChunkLengthToEmbed() : 5)
                .withMaxNumChunks(config.getMaxNumChunks() != null ? config.getMaxNumChunks() : 10000)
                .withKeepSeparator(config.getKeepSeparator() != null ? config.getKeepSeparator() : true)
                .build();
    }

    /**
     * 构建错误响应
     */
    private MarkdownProcessResponse buildErrorResponse(String message, String filePath, long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        return MarkdownProcessResponse.builder()
                .success(false)
                .message(message)
                .filePath(filePath)
                .processingTime(processingTime)
                .build();
    }
}
