package com.lei.learn.spring.ai.alibaba.graph;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.junit.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * Spring AI Alibaba Graph 流式输出测试类
 *
 * <p>演示如何使用 StateGraph 构建支持流式输出的智能体工作流，
 * 展示了 ReplaceStrategy 和 AppendStrategy 在状态管理中的使用，
 * 以及框架对 Flux 对象的自动订阅与消费机制。</p>
 *
 * @author 伍磊
 */
public class StreamOutputTests {

    /** 状态键名：查询内容 */
    private static final String STATE_KEY_QUERY = "query";

    /** 状态键名：消息列表 */
    private static final String STATE_KEY_MESSAGES = "messages";

    /** 状态键名：处理结果 */
    private static final String STATE_KEY_RESULT = "result";

    /** 节点名称：流式输出节点 */
    private static final String NODE_STREAM = "stream";

    /** 节点名称：结果处理节点 */
    private static final String NODE_PROCESS_STREAM = "process_stream";

    /** 线程ID */
    private static final String THREAD_ID = "streaming_thread";

    /** 测试查询内容 */
    private static final String TEST_QUERY = "请用一句话介绍 Spring AI";

    /**
     * 测试 Graph 的流式输出功能
     *
     * <p>构建一个包含两个节点的图：stream 节点调用 LLM 获取流式响应，
     * process_stream 节点处理最终结果。使用 KeyStrategyFactory 配置
     * 不同状态键的更新策略，验证框架能够正确处理节点返回的 Flux 对象。</p>
     *
     * @throws GraphStateException 图执行失败时抛出
     */
    @Test
    public void testStreamOutput() throws GraphStateException {
        ChatModel chatModel = ChatModelInit.init();

        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> keyStrategyMap = new HashMap<>(4);
            keyStrategyMap.put(STATE_KEY_QUERY, new ReplaceStrategy());
            keyStrategyMap.put(STATE_KEY_MESSAGES, new AppendStrategy());
            keyStrategyMap.put(STATE_KEY_RESULT, new AppendStrategy());
            return keyStrategyMap;
        };

        var streamNode = node_async(state -> {
            String query = (String) state.value(STATE_KEY_QUERY).orElse("");
            Flux<ChatResponse> flux = ChatClient.create(chatModel)
                    .prompt()
                    .user(query)
                    .stream()
                    .chatResponse();
            // 框架自动处理流式响应
            return Map.of(STATE_KEY_MESSAGES, flux);
        });

        var processStreamingNode = node_async(state -> {
            // 请注意，虽然上一个节点返回的是Flux对象，但是在引擎运行到当前节点时，
            // 框架已经完成了对上一个节点Flux对象的自动订阅与消费，并将最终的结果汇总后添加到了 messages key 中（基于 AppendStrategy）
            var messages = state.value(STATE_KEY_MESSAGES).orElse(List.of());
            String result = "流式响应已处理完成: " + messages;
            return Map.of(STATE_KEY_RESULT, result);
        });

        // 构建图
        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode(NODE_STREAM, streamNode)
                .addNode(NODE_PROCESS_STREAM, processStreamingNode)
                .addEdge(START, NODE_STREAM)
                .addEdge(NODE_STREAM, NODE_PROCESS_STREAM)
                .addEdge(NODE_PROCESS_STREAM, END);

        // 编译图
        CompiledGraph graph = stateGraph.compile(CompileConfig.builder().build());

        // 创建配置
        RunnableConfig config = RunnableConfig.builder()
                .threadId(THREAD_ID)
                .build();

        // 使用流式方式执行图
        System.out.println("开始流式输出... ");

        graph.stream(Map.of(STATE_KEY_QUERY, TEST_QUERY), config)
                .doOnNext(output -> {
                    if (output instanceof StreamingOutput<?> streamingOutput) {
                        // 流式输出
                        String chunk = streamingOutput.chunk();
                        if (chunk != null && !chunk.isEmpty()) {
                            System.out.print(chunk); // 实时打印流式内容
                        }
                    } else {
                        // 普通节点输出
                        String nodeId = output.node();
                        Map<String, Object> state = output.state().data();
                        System.out.println("节点 '" + nodeId + "' 执行完成");
                        if (state.containsKey("result")) {
                            System.out.println("最终结果: " + state.get("result"));
                        }
                    }
                })
                .doOnComplete(() -> System.out.println("流式输出完成"))
                .doOnError(e -> System.err.println("流式输出错误: " + e.getMessage()))
                .blockLast();

    }
}
