package com.lei.learn.spring.ai.tool;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;

/**
 * <p>
 * DateTimeTools
 * </p>
 *
 * @author 伍磊
 */
@Log4j2
public class DateTimeTools {

    @Tool(description = "获取用户所在时区的当前日期和时间")
    public String getCurrentDateTime() {
        log.info("[tool] getCurrentDateTime | calling");
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }
}
