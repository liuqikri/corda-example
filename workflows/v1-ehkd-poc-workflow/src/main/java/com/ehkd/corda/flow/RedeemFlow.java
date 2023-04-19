package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.contract.TokenContract;
import com.ehkd.corda.schema.PersistentToken;
import com.ehkd.corda.state.TokenState;
import com.ehkd.corda.utils.CordaUtils;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.StateRef;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.SchedulableFlow;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.Builder;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;

import java.time.Instant;
import java.util.Collections;

/**
 * This is the flow that a Heartbeat state runs when it consumes itself to create a new Heartbeat
 * state on the ledger.
 */
@InitiatingFlow
@SchedulableFlow
public class RedeemFlow extends BaseFlow<TokenState>{
    private final StateRef stateRef;

    public RedeemFlow(StateRef stateRef) {
        this.stateRef = stateRef;
        command = new TokenContract.Commands.Beat();
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {

        StateAndRef<TokenState> input = getServiceHub().toStateAndRef(stateRef);
        SignedTransaction signedTransaction = defaultFlowNotOutput(input);
        //TODO call backend url
        return signedTransaction;
//        QueryCriteria queryCriteria1 = null;
//        try {
//            queryCriteria1 = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentToken.class.getField("id"),
//                    input.getState().getData().getId().getId()),
//                    Vault.StateStatus.UNCONSUMED);
//        } catch (NoSuchFieldException e) {
//            throw new RuntimeException(e);
//        }
//        Vault.Page<TokenState> result1 = getServiceHub().getVaultService().queryBy(TokenState.class, queryCriteria1);
//
//        QueryCriteria queryCriteria2 = null;
//        try {
//            queryCriteria2 = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentToken.class.getField("id"),
//                    input.getState().getData().getId().getId()),
//                    Vault.StateStatus.ALL);
//        } catch (NoSuchFieldException e) {
//            throw new RuntimeException(e);
//        }
//        Vault.Page<TokenState> result2 = getServiceHub().getVaultService().queryBy(TokenState.class, queryCriteria2);



    }
}
