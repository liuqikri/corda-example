package com.ehkd.blockchain.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.Entity;
import java.util.Date;

@Entity
@Getter
@Setter
public class TokenEntity {

    String tokenId;
    String batchId;
    String name;
    String symbol;
    Integer decimals;
    Long amount;
    String sponsor;
    String owner;
    String issuer;
    Date releaseDate;
    Date expiryDate;
    String tokenType;
    String merchantList;
    String lockedBy;
    Integer isVoucher;

}
