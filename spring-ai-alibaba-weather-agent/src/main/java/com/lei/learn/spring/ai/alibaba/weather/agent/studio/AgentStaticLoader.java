package com.lei.learn.spring.ai.alibaba.weather.agent.studio;

import com.alibaba.cloud.ai.agent.studio.loader.AgentLoader;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.agent.BaseAgent;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Static Agent Loader for programmatically provided agents.
 *
 * <p>This loader takes a static list of pre-created agent instances and makes them available
 * through the AgentLoader interface. Perfect for cases where you already have agent instances and
 * just need a convenient way to wrap them in an AgentLoader.
 *
 * <p>This class is not a Spring component by itself - instances are created programmatically and
 * then registered as beans via factory methods.
 */
@Component
class AgentStaticLoader implements AgentLoader {

    private final Map<String, BaseAgent> agents = new ConcurrentHashMap<>();

    public AgentStaticLoader(BaseAgent agent) {

        GraphRepresentation representation = agent.getAndCompileGraph().stateGraph.getGraph(GraphRepresentation.Type.PLANTUML);
        System.out.println(representation.content());

        this.agents.put("research_agent", agent);
    }

    @Override
    @Nonnull
    public List<String> listAgents() {
        return agents.keySet().stream().toList();
    }

    @Override
    public BaseAgent loadAgent(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Agent name cannot be null or empty");
        }

        BaseAgent agent = agents.get(name);
        if (agent == null) {
            throw new NoSuchElementException("Agent not found: " + name);
        }

        return agent;
    }
}
