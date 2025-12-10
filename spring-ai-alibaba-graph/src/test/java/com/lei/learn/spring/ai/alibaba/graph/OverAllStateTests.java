package com.lei.learn.spring.ai.alibaba.graph;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;
import static org.junit.Assert.assertEquals;

/**
 * <p>
 * OverAllStateTests
 * </p>
 *
 * @author 伍磊
 */
public class OverAllStateTests {

    @Test
    public void test_KeyStrategy() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> keyStrategyMap = new HashMap<>();
            keyStrategyMap.put("value", new ReplaceStrategy());  // 使用替换策略
            keyStrategyMap.put("messages", new AppendStrategy());  // 使用追加策略
            return keyStrategyMap;
        };

        // 节点 A：返回 value = "初始值"
        var nodeA = node_async(state -> {
            return Map.of("value", "初始值", "messages", "消息A");
        });

        // 节点 B：返回 value = "更新后的值"（会覆盖节点 A 的值）
        var nodeB = node_async(state -> {
            return Map.of("value", "更新后的值", "messages", "消息B");
        });

        // 构建图
        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode("node_a", nodeA)
                .addNode("node_b", nodeB)
                .addEdge(START, "node_a")
                .addEdge("node_a", "node_b")
                .addEdge("node_b", END);

        // 编译并执行
        CompiledGraph graph = stateGraph.compile();

        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId("replace-strategy-demo")
                .build();

        // 执行图
        Optional<OverAllState> stateOptional = graph.invoke(Map.of(), runnableConfig);

        // 获取最终状态
        assertEquals("更新后的值", stateOptional.get().value("value").orElse(null));
        assertEquals(List.of("消息A", "消息B"), stateOptional.get().value("messages").orElse(null));

    }

}
