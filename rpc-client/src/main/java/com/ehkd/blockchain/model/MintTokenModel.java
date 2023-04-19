package com.ehkd.blockchain.model;

import com.ehkd.corda.state.TokenState;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MintTokenModel {

    String txHash;
    String tokenId;
    String name;
    String symbol;
    Long amount;
    Integer count;
}
