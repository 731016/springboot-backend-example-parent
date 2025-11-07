package com.xiaofei.springbootinit.example.kafka.utils;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
@Slf4j
public class RandomDataUtils {

    /**
     * 生成指定范围内的随机BigDecimal
     * @param min 最小值
     * @param max 最大值
     * @param scale 小数位数
     * @return 随机BigDecimal
     */
    public static BigDecimal randomBigDecimal(double min, double max, int scale) {
        return NumberUtil.round(
                NumberUtil.toBigDecimal(RandomUtil.randomDouble(min, max)),
                scale);
    }

    /**
     * 生成1-100之间的随机BigDecimal，保留2位小数
     */
    public static BigDecimal randomValue() {
        return randomBigDecimal(1, 100, 2);
    }

    /**
     * 生成指定范围内的随机BigDecimal列表
     * @param min 最小值
     * @param max 最大值
     * @param scale 小数位数
     * @param size 列表大小
     */
    public static List<BigDecimal> randomBigDecimalList(double min, double max, int scale, int size) {
        return RandomUtil.randomEleList(
                Stream.generate(() -> randomBigDecimal(min, max, scale))
                        .limit(size)
                        .collect(Collectors.toList()),
                size
        );
    }

    /**
     * 生成带波动的随机值
     * @param baseValue 基准值
     * @param fluctuation 波动范围（百分比）
     * @param scale 小数位数
     */
    public static BigDecimal randomWithFluctuation(BigDecimal baseValue, double fluctuation, int scale) {
        double factor = 1 + RandomUtil.randomDouble(-fluctuation, fluctuation);
        return NumberUtil.round(
                NumberUtil.toBigDecimal(baseValue.doubleValue() * factor),
                scale
        );
    }

    /**
     * 生成正态分布的随机BigDecimal
     * @param mean 均值
     * @param standardDeviation 标准差
     * @param scale 小数位数
     */
    public static BigDecimal randomNormalDistribution(double mean, double standardDeviation, int scale) {
        double value = RandomUtil.randomDouble(mean - 3 * standardDeviation, mean + 3 * standardDeviation);
        return NumberUtil.round(NumberUtil.toBigDecimal(value), scale);
    }
}
