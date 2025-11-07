package com.xiaofei.springbootinit.example.kafka.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
@Slf4j
public class TimeUtils {

    /**
     * 判断当前时间是否在工作时间范围内（09:00-18:30）
     */
    public static boolean isInWorkingHours() {
        return isInWorkingHours(DateUtil.date());
    }

    /**
     * 判断指定时间是否在工作时间范围内（09:00-18:30）
     */
    public static boolean isInWorkingHours(Date date) {
        if (date == null) {
            return false;
        }

        try {
            // 获取当天的日期字符串
            String today = DateUtil.format(date, "yyyy-MM-dd");

            // 构建当天的开始和结束时间点
            Date startTime = DateUtil.parse(today + " 09:00:00");
            Date endTime = DateUtil.parse(today + " 18:30:00");

            // 判断是否在时间范围内
            return DateUtil.isIn(date, startTime, endTime);

        } catch (Exception e) {
            log.error("时间判断异常", e);
            return false;
        }
    }

    /**
     * 获取下一个工作时间
     */
    public static Date getNextWorkingTime() {
        DateTime now = DateUtil.date();

        // 如果当前时间早于9点，返回今天9点
        DateTime todayStart = DateUtil.parse(DateUtil.today() + " 09:00:00");
        if (now.isBefore(todayStart)) {
            return todayStart;
        }

        // 如果当前时间晚于18:30，返回明天9点
        DateTime todayEnd = DateUtil.parse(DateUtil.today() + " 18:30:00");
        if (now.isAfter(todayEnd)) {
            return DateUtil.offsetDay(todayStart, 1);
        }

        // 如果在工作时间内，返回当前时间
        return now;
    }

    /**
     * 获取剩余工作时间（分钟）
     */
    public static long getRemainingWorkingMinutes() {
        DateTime now = DateUtil.date();
        DateTime todayEnd = DateUtil.parse(DateUtil.today() + " 18:30:00");

        // 如果已经过了今天的工作时间
        if (now.isAfter(todayEnd)) {
            return 0;
        }

        // 如果还没到工作时间
        DateTime todayStart = DateUtil.parse(DateUtil.today() + " 09:00:00");
        if (now.isBefore(todayStart)) {
            return 0;
        }

        // 计算剩余分钟数
        return DateUtil.between(now, todayEnd, DateUnit.MINUTE);
    }

    /**
     * 格式化工作时间范围
     */
    public static String formatWorkingHours(Date date) {
        if (date == null) {
            date = DateUtil.date();
        }

        String dateStr = DateUtil.format(date, "yyyy-MM-dd");
        String startTimeStr = dateStr + " 09:00:00";
        String endTimeStr = dateStr + " 18:30:00";

        return String.format("%s 至 %s", startTimeStr, endTimeStr);
    }
}
