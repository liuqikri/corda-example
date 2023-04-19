package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.contract.TokenContract;
import com.ehkd.corda.state.TokenState;
import lombok.extern.log4j.Log4j2;
import net.corda.core.contracts.*;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.transactions.SignedTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@StartableByRPC
@Log4j2
public class TokenSplitPaymentFinalFlow extends BaseFlow<TokenState> {

    String txHash;

    public TokenSplitPaymentFinalFlow(String txHash) {
        super();
        this.txHash = txHash;
        command = new TokenContract.Commands.TokenSplitPayment();
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {

        SignedTransaction transaction = getServiceHub().getValidatedTransactions().getTransaction(SecureHash.create(txHash));
        List<StateAndRef<TokenState>> prevRef  = transaction.getCoreTransaction().outRefsOfType(TokenState.class);
        List<StateAndRef<TokenState>> inputList = new ArrayList<>();

        List<TokenState> outputList = new ArrayList<>();
        for (StateAndRef<TokenState> tokenStateStateAndRef : prevRef) {
            TokenState tokenState = tokenStateStateAndRef.getState().getData();
            if (!tokenState.getLockedBy().equals("")) {
                inputList.add(tokenStateStateAndRef);
                tokenState.setLockedBy("");
                outputList.add(tokenState);
            }
        }
        return defaultBatchFlow(outputList,inputList);
    }

}
