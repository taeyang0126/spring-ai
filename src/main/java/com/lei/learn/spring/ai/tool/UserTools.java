package com.lei.learn.spring.ai.tool;

import com.lei.learn.spring.ai.model.User;
import com.lei.learn.spring.ai.repository.UserRepository;
import com.lei.learn.spring.ai.support.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * <p>
 * 用户工具
 * </p>
 *
 * @author 伍磊
 */
@Log4j2
@AllArgsConstructor
public class UserTools {

    private final UserRepository userRepository;

    @Tool(description = "查询当前用户信息，要返回userId、username、email、phoneNumber，同时对于邮箱和手机号等敏感信息需要脱敏")
    public User findCurrentUser(ToolContext toolContext) {
        Object userId = toolContext.getContext().get(Constants.USER_ID);
        if (null == userId) {
            return null;
        }
        User user = userRepository.findByUserId((Integer) userId).orElse(null);
        log.info("[tool] findByUserId | userId={} | result={}", userId, user);
        return user;
    }

    @Tool(description = "根据邮箱查询用户信息，要返回userId、username、email、phoneNumber，同时对于邮箱和手机号等敏感信息需要脱敏")
    public User findByEmail(@ToolParam(description = "邮箱") String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        log.info("[tool] findByEmail | email={} | result={}", email, user);
        return user;
    }

    @Tool(description = "根据用户名查询用户信息，要返回userId、username、email、phoneNumber，同时对于邮箱和手机号等敏感信息需要脱敏")
    public User findByUsername(@ToolParam(description = "用户名") String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        log.info("[tool] findByUsername | username={} | result={}", username, user);
        return user;
    }

    @Tool(description = "根据手机号查询用户信息，要返回userId、username、email、phoneNumber，同时对于邮箱和手机号等敏感信息需要脱敏")
    public User findByPhoneNumber(@ToolParam(description = "手机号") String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        log.info("[tool] findByUsername | phoneNumber={} | result={}", phoneNumber, user);
        return user;
    }


}
