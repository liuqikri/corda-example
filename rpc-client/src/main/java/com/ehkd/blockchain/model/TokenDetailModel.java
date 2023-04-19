package com.ehkd.blockchain.model;

import com.ehkd.blockchain.util.DateUtils;
import com.ehkd.blockchain.util.NumberUtils;
import com.ehkd.corda.state.TokenState;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class TokenDetailModel {

    String tokenId;
    String name;
    String symbol;
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

    public TokenDetailModel() {
    }

    public TokenDetailModel(TokenState tokenState){
        if(tokenState != null) {
            this.tokenId = tokenState.getBatchId().getId().toString();
            this.name = tokenState.getName();
            this.symbol = tokenState.getSymbol();
            this.amount = tokenState.getAmount();
            this.sponsor = tokenState.getSponsor();
            this.owner = tokenState.getOwner();
            this.issuer = tokenState.getIssuer();
            this.releaseDate = DateUtils.instantToDate(tokenState.getReleaseDate());
            this.expiryDate = DateUtils.instantToDate(tokenState.getExpiryDate());
            this.tokenType = tokenState.getTokenType();
            this.merchantList = tokenState.getMerchantList();
            this.lockedBy = tokenState.getLockedBy();
            this.isVoucher = tokenState.getIsVoucher();
        }
    }
}
