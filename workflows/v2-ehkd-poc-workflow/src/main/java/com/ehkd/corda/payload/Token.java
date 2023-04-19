package com.ehkd.corda.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.corda.core.serialization.CordaSerializable;

import java.time.Instant;
import java.util.Date;

/**
 * @author kevin wu
 * @date 2023/3/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@CordaSerializable
public class Token {

    String tokenId;
    String batchId;
    String name;
    String symbol;
    Integer decimals;
    Long amount;
    String sponsor;
    String owner;
    String issuer;
    Instant releaseDate;
    Instant expiryDate;
    String tokenType;
    String merchantList;
    String lockedBy;
    Integer isVoucher;
}
