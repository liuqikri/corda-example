package com.ehkd.blockchain.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TokenBalanceSummaryModel {

    /**
     * token类型
     */
    private String tokenType;
    /**
     * 金额
     */
    private Long amount;

    public TokenBalanceSummaryModel() {
    }

    public TokenBalanceSummaryModel(String tokenType, Long amount) {
        this.tokenType = tokenType;
        this.amount = amount;
    }
}
