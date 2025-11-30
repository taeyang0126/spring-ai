package com.lei.learn.spring.ai.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * <p>
 * UserContextUtils
 * </p>
 *
 * @author 伍磊
 */
public final class UserContextUtils {

    public static final String X_USER_ID = "x-userId";

    public static Integer getCurrentUserId() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }

            HttpServletRequest request = attributes.getRequest();
            String userIdStr = request.getHeader(X_USER_ID);

            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                return null;
            }

            return Integer.valueOf(userIdStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
