package com.ehkd.blockchain.util;

import java.math.BigDecimal;

public class NumberUtils {

    public static BigDecimal longToBigDecimal(Long val, Integer decimals) {
        if(val == null) {
            return BigDecimal.ZERO;
        }
        if(decimals<1) {
            return BigDecimal.valueOf(val);
        } else {
            return BigDecimal.valueOf(val).divide(BigDecimal.valueOf(10).pow(decimals))
                    .setScale(decimals, BigDecimal.ROUND_HALF_UP);
        }
    }

}
