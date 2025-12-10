package com.lei.learn.spring.ai.alibaba.graph;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.alibaba.cloud.ai.graph.store.stores.MemoryStore;
import org.junit.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * <p>
 * MemoryTests
 * 1. 测试短期和长期记忆的结合使用
 * </p>
 *
 * @author 伍磊
 */
public class MemoryTests {

    @Test
    public void test() throws GraphStateException {
        ChatModel chatModel = ChatModelInit.init();

        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> keyStrategyMap = new HashMap<>();
            keyStrategyMap.put("userId", new ReplaceStrategy());
            keyStrategyMap.put("messages", new AppendStrategy());
            keyStrategyMap.put("userPreferences", new ReplaceStrategy());
            return keyStrategyMap;
        };

        // 加载用户偏好（长期内存）
        var loadUserPreferences = AsyncNodeActionWithConfig.node_async((state, config) -> {
            String userId = (String) state.value("userId").orElse("");

            // 如果 userId 为空，则使用默认偏好
            if (userId.isEmpty()) {
                return Map.of("userPreferences", Map.of("theme", "default", "language", "zh"));
            }

            // 从 store 加载用户偏好
            Store store = config.store();
            if (store != null) {
                Optional<StoreItem> itemOpt = store.getItem(List.of("userPreferences"), userId);
                if (itemOpt.isPresent()) {
                    Map<String, Object> preferences = itemOpt.get().getValue();
                    return Map.of("userPreferences", preferences);
                }
            }

            // 未找到，返回默认偏好
            return Map.of("userPreferences", Map.of("theme", "dark", "language", "zh"));
        });

        // 聊天节点（使用短期和长期内存）
        var chatNode = node_async(state -> {
            List<Map<String, String>> messages =
                    (List<Map<String, String>>) state.value("messages").orElse(List.of());
            Map<String, Object> userPreferences =
                    (Map<String, Object>) state.value("userPreferences").orElse(List.of());

            // 构建包含用户偏好的提示
            String userPrompt = messages.get(messages.size() - 1).get("content");
            String enhancedPrompt = "用户偏好: " + userPreferences + " 用户问题:" + userPrompt;
            System.out.println("enhancedPrompt: " + enhancedPrompt);

            // 调用 AI
            ChatClient chatClient = ChatClient.create(chatModel);
            String response = chatClient.prompt()
                    .user(enhancedPrompt)
                    .call()
                    .content();
            return Map.of("messages", List.of(Map.of("role", "assistant", "content", response)));
        });

        // 构建图
        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode("load_preferences", loadUserPreferences)
                .addNode("chat", chatNode)
                .addEdge(START, "load_preferences")
                .addEdge("load_preferences", "chat")
                .addEdge("chat", END);

        // 配置检查点（短期内存）
        SaverConfig saverConfig = SaverConfig.builder()
                .register(new MemorySaver())
                .build();

        // 编译图
        CompiledGraph graph = stateGraph.compile(
                CompileConfig.builder()
                        .saverConfig(saverConfig)
                        .build()
        );

        // 创建长期记忆存储并预填充用户偏好
        MemoryStore memoryStore = new MemoryStore();
        Map<String, Object> preferencesData = new HashMap<>();
        preferencesData.put("theme", "light");
        preferencesData.put("language", "zh");
        preferencesData.put("timezone", "Asia/Shanghai");
        StoreItem preferencesItem = StoreItem.of(List.of("userPreferences"), "user_001", preferencesData);
        memoryStore.putItem(preferencesItem);

        // 运行图
        RunnableConfig config = RunnableConfig.builder()
                .threadId("combined_thread")
                .store(memoryStore)
                .build();

        // 第一轮对话（加载偏好并开始对话）
        Optional<OverAllState> stateOptional = graph.invoke(Map.of(
                "userId", "user_001",
                "messages", List.of(Map.of("role", "user", "content", "你好"))
        ), config);
        System.out.println(stateOptional);
        StateSnapshot stateSnapshot = graph.getState(config);
        System.out.println("first round state: " + stateSnapshot);

        // 第二轮对话（使用短期和长期记忆）
        Optional<OverAllState> stateOptional1 = graph.invoke(Map.of(
                "userId", "user_001",
                "messages", List.of(Map.of("role", "user", "content", "根据我的偏好给我一些建议"))
        ), config);

        stateSnapshot = graph.getState(config);
        System.out.println("second round state: " + stateSnapshot);

        List<Map<String, String>> messages =
                (List<Map<String, String>>) stateOptional1.get().value("messages").orElse(List.of());
        messages.forEach(System.out::println);



    }
}


