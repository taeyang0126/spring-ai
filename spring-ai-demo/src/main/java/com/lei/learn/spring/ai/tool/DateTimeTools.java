package com.lei.learn.spring.ai.tool;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @Tool(description = "设置用户闹钟，闹钟时间为指定时间，格式为 ISO-8601")
    public void setAlarm(@ToolParam(description = "ISO-8601 格式的时间") String time) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        log.info("[tool] Alarm set for {}", alarmTime);
    }
}
