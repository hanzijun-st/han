package com.qianlima.offline.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 抽奖算法
 */
public class LotteryUtil {

    /**
     * 抽奖算法
     *
     * @param originRates 原始的概率列表，保证顺序和实际物品对应
     * @return 物品的索引
     */
    public static int lottery(List<Double> originRates) {
        // 计算总概率，这样可以保证不一定总概率是1
        double sumRate = 0d;
        for (double rate : originRates) {
            sumRate += rate;
        }
        // 计算每个物品在总概率的基础下的概率情况
        List<Double> sortOriginRates = new ArrayList<>();
        double tempSumRate = 0d;
        for (double rate : originRates) {
            tempSumRate += rate;
            sortOriginRates.add(tempSumRate / sumRate);
        }
        // 根据区块值来获取抽取到的物品索引
        double nextDouble = Math.random();
        sortOriginRates.add(nextDouble);
        Collections.sort(sortOriginRates);
        return sortOriginRates.indexOf(nextDouble);
    }
}