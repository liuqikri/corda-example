package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.contract.TokenContract;
import com.ehkd.corda.payload.Token;
import com.ehkd.corda.state.TokenState;
import com.ehkd.corda.utils.CordaUtils;
import lombok.extern.log4j.Log4j2;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.transactions.SignedTransaction;

import java.time.Instant;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@StartableByRPC
@Log4j2
public class TokenCreateByUserFlow extends BaseFlow<TokenState> {

    String name;
    String symbol;
    Integer decimals;
    Long amount;
    String sponsor;
    String owner;
    String issuer;
    Instant releaseDate;
    String tokenType;
    String merchantList;
    String lockedBy;
    Integer isVoucher;

    public TokenCreateByUserFlow(Token token) {
        super();
        this.name = token.getName();
        this.symbol = token.getSymbol();
        this.decimals = token.getDecimals();
        this.amount = token.getAmount();
        this.sponsor = token.getSponsor();
        this.owner = token.getOwner();
        this.issuer = token.getIssuer();
        this.releaseDate = token.getReleaseDate();
        this.tokenType = token.getTokenType();
        this.merchantList = token.getMerchantList();
        this.lockedBy = token.getLockedBy();
        this.isVoucher = token.getIsVoucher();
        command = new TokenContract.Commands.Create();
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        TokenState newState = new TokenState(
                new UniqueIdentifier(),
                name,
                symbol,
                decimals,
                amount,
                sponsor,
                owner,
                issuer,
                releaseDate,
                null,
                tokenType,
                merchantList,
                lockedBy,
                isVoucher,
                CordaUtils.getServiceHubAllParties(getServiceHub())
        );
        return defaultFlow(newState, null);
    }
}
