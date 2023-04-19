package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.contract.TokenContract;
import com.ehkd.corda.state.TokenState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;

/**
 * This is the flow that a Heartbeat state runs when it consumes itself to create a new Heartbeat
 * state on the ledger.
 */
@InitiatedBy(TokenSendP2PFlow.class)
public class AutoRedeemFlow extends BaseFlow<TokenState>{
    private StateAndRef<TokenState> tokenStateRef;

    public AutoRedeemFlow(StateAndRef<TokenState> tokenStateRef) {
        this.tokenStateRef = tokenStateRef;
        command = new TokenContract.Commands.Beat();
    }

    private FlowSession counterpartySession;

    public AutoRedeemFlow(StateAndRef<TokenState> tokenStateRef, FlowSession counterpartySession) {
        this.tokenStateRef = tokenStateRef;
        this.counterpartySession = counterpartySession;
    }


    public AutoRedeemFlow( FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }



    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {

        SignedTransaction signedTransaction = defaultFlowNotOutput(tokenStateRef);
        return signedTransaction;

    }
}
