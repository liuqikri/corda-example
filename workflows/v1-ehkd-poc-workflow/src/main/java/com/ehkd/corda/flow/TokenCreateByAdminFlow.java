package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;

import com.ehkd.corda.contract.TokenContract;
import com.ehkd.corda.payload.Token;
import com.ehkd.corda.state.TokenState;
import com.ehkd.corda.utils.CordaUtils;
import lombok.extern.log4j.Log4j2;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.transactions.SignedTransaction;
import java.time.Instant;
import java.util.*;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@StartableByRPC
@Log4j2
public class TokenCreateByAdminFlow extends BaseFlow<TokenState> {

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

    public TokenCreateByAdminFlow(Token token) {
        super();
        this.batchId = token.getBatchId();
        this.name = token.getName();
        this.symbol = token.getSymbol();
        this.decimals = token.getDecimals();
        this.amount = token.getAmount();
        this.sponsor = token.getSponsor();
        this.owner = token.getOwner();
        this.issuer = token.getIssuer();
        this.releaseDate = token.getReleaseDate();
        this.expiryDate = token.getExpiryDate();
        this.tokenType = token.getTokenType();
        this.merchantList = token.getMerchantList();
        this.lockedBy = token.getLockedBy();
        this.isVoucher = token.getIsVoucher();
        command = new TokenContract.Commands.BatchCreate();
    }


    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        List<StateAndRef<TokenState>> inputList = new ArrayList<>();
        List<String> ownerList = Arrays.asList(owner.split(","));
        List<TokenState> outputList = new ArrayList<>();
        UniqueIdentifier batchId = new UniqueIdentifier();
        for (String ownerId:ownerList) {
            TokenState newState = new TokenState(
                    batchId,
                    name,
                    symbol,
                    decimals,
                    amount,
                    sponsor,
                    ownerId,
                    issuer,
                    releaseDate,
                    expiryDate,
                    tokenType,
                    merchantList,
                    lockedBy,
                    isVoucher,
                    CordaUtils.getServiceHubAllParties(getServiceHub())
            );
            outputList.add(newState);
        }
        if(outputList.size() > 0 ){
            return defaultBatchFlow(outputList, inputList);
        }else{
            throw new FlowException("owner list is empty");
        }

    }
}
