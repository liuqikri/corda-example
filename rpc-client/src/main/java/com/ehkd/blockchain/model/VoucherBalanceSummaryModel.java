package com.ehkd.blockchain.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class VoucherBalanceSummaryModel extends TokenBalanceSummaryModel {

    /**
     * token name
     */
    private String tokenName;

}
