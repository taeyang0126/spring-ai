package com.lei.learn.spring.ai.alibaba.weather.agent;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * <p>
 * WeatherAgentApplication
 * </p>
 *
 * @author 伍磊
 */
@SpringBootApplication
public class WeatherAgentApplication implements CommandLineRunner {

    @Autowired
    private ReactAgent agent;

    public static void main(String[] args) {
        SpringApplication.run(WeatherAgentApplication.class, args);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventListener(Environment environment) {
        return event -> {
            String port = environment.getProperty("server.port", "8080");
            String contextPath = environment.getProperty("server.servlet.context-path", "");
            String accessUrl = "http://localhost:" + port + contextPath + "/chatui/index.html";
            System.out.println("\n🎉========================================🎉");
            System.out.println("✅ Application is ready!");
            System.out.println("🚀 Chat with you agent: " + accessUrl);
            System.out.println("🎉========================================🎉\n");
        };
    }

    @Override
    public void run(String... args) throws Exception {
/*        String threadId = UUID.randomUUID().toString();
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata("user_id", 1)
                .build();

        // 第一次调用
        AssistantMessage response = agent.call("My name is Tom, what is the weather outside?", runnableConfig);
        System.out.println(response.getText());

        response = agent.call("What is my name?", runnableConfig);
        System.out.println(response.getText());

        response = agent.call("thank you!", runnableConfig);
        System.out.println(response.getText());*/


    }
}
