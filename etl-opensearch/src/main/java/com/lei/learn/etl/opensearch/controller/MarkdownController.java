package com.lei.learn.etl.opensearch.controller;

import com.lei.learn.etl.opensearch.model.MarkdownProcessRequest;
import com.lei.learn.etl.opensearch.model.MarkdownProcessResponse;
import com.lei.learn.etl.opensearch.service.MarkdownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * Markdown 文档处理控制器
 * </p>
 *
 * @author 伍磊
 */
@Slf4j
@RestController
@RequestMapping("/api/markdown")
@RequiredArgsConstructor
public class MarkdownController {

    private final MarkdownService markdownService;

    /**
     * 处理本地 Markdown 文件
     *
     * @param request 处理请求
     * @return 处理响应
     */
    @PostMapping("/process")
    public MarkdownProcessResponse processMarkdown(@RequestBody MarkdownProcessRequest request) {
        log.info("收到 Markdown 文件处理请求: {}", request.getFilePath());
        return markdownService.processMarkdownFile(request);
    }

}
