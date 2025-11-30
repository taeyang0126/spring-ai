package com.lei.learn.spring.ai.advisor;

import com.lei.learn.spring.ai.utils.UserContextUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

import static com.lei.learn.spring.ai.support.Constants.USER_ID;

/**
 * <p>
 * UserInfoAdvisor
 * </p>
 *
 * @author 伍磊
 */
@Log4j2
public class UserContextAdvisor implements CallAdvisor, StreamAdvisor {

    private static void handlerRequest(ChatClientRequest chatClientRequest) {
        if (!chatClientRequest.context().containsKey(USER_ID)) {
            Integer currentUserId = UserContextUtils.getCurrentUserId();
            if (null == currentUserId) {
                log.warn("[UserContextAdvisor] userId is null");
            } else {
                chatClientRequest.context().put(USER_ID, currentUserId);
            }
        }
        // userId 添加到元数据里面
        Object userId = chatClientRequest.context().get(USER_ID);
        if (null != userId) {
            chatClientRequest.prompt().getUserMessage().getMetadata()
                    .put(USER_ID, userId);
        }
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest,
                                         CallAdvisorChain callAdvisorChain) {
        handlerRequest(chatClientRequest);
        return callAdvisorChain.nextCall(chatClientRequest);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                                 StreamAdvisorChain streamAdvisorChain) {
        handlerRequest(chatClientRequest);
        return streamAdvisorChain.nextStream(chatClientRequest);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER - 1;
    }


}
