package com.ehkd.blockchain.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class TokenMintDetailModel {

    String batchId;
    String name;
    String symbol;
    BigDecimal amount;
    String sponsor;
    String tokenType;
    Date releaseDate;
    Date expiryDate;

}
