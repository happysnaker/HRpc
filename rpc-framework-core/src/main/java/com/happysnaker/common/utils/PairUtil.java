package com.happysnaker.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PairUtil<K, V> {
    private K first;
    private V second;

    public static<k, v> PairUtil of(k k1, v v1) {
        return new PairUtil<k, v>(k1, v1);
    }
}
