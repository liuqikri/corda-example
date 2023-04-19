package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.utils.CordaUtils;
import net.corda.core.contracts.CommandData;
import lombok.extern.log4j.Log4j2;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.Party;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@Log4j2
public abstract class BaseFlow<T extends ContractState> extends FlowLogic<SignedTransaction>  {
    public static ProgressTracker.Step GENERATING_APPLICATION_TRANSACTION
            = new ProgressTracker.Step("Generating application transaction.");

    public static ProgressTracker.Step SIGNING_TRANSACTION
            = new ProgressTracker.Step("Signing transaction with our key.");

    public static ProgressTracker.Step FINALIZING
            = new ProgressTracker.Step("Recording and distributing transaction.");

    public static ProgressTracker tracker() {
        return new ProgressTracker(
                GENERATING_APPLICATION_TRANSACTION,
                SIGNING_TRANSACTION,
                FINALIZING
        );
    }

    public ProgressTracker progressTracker = tracker();
    protected CommandData command;

    @Suspendable
    public SignedTransaction defaultFlow (T outputState, StateAndRef<T> prev) throws FlowException {
        Party notary = CordaUtils.getServiceHubPreferredNotary(getServiceHub());
        if(prev != null && prev.getState() != null && prev.getState().getNotary() != null) {
            notary = prev.getState().getNotary();
        }
        progressTracker.setCurrentStep(GENERATING_APPLICATION_TRANSACTION);
        PublicKey myKey = CordaUtils.getServiceHubMyIdentity(getServiceHub()).getOwningKey();
        TransactionBuilder tx = new TransactionBuilder(notary);
        if(prev != null) {
            tx.addInputState(prev);
        }
        tx.addOutputState(outputState);
        tx.addCommand(command, myKey);
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        Instant currentTime = getServiceHub().getClock().instant();
        tx.setTimeWindow(currentTime, Duration.ofSeconds(60));
        SignedTransaction stx = getServiceHub().signInitialTransaction(tx);
        progressTracker.setCurrentStep(FINALIZING);
        SignedTransaction subF = subFlow(new FinalizeFlow(stx));
        QueryCriteria queryCriteria = null;
        return subF;
    }


    @Suspendable
    public SignedTransaction defaultFlowNotOutput ( StateAndRef<T> prev) throws FlowException {
        Party notary = CordaUtils.getServiceHubPreferredNotary(getServiceHub());
        if(prev != null && prev.getState() != null && prev.getState().getNotary() != null) {
            notary = prev.getState().getNotary();
        }
        progressTracker.setCurrentStep(GENERATING_APPLICATION_TRANSACTION);
        PublicKey myKey = CordaUtils.getServiceHubMyIdentity(getServiceHub()).getOwningKey();
        TransactionBuilder tx = new TransactionBuilder(notary);
        if(prev != null) {
            tx.addInputState(prev);
        }
        tx.addCommand(command, myKey);
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        Instant currentTime = getServiceHub().getClock().instant();
        tx.setTimeWindow(currentTime, Duration.ofSeconds(60));
        SignedTransaction stx = getServiceHub().signInitialTransaction(tx);
        progressTracker.setCurrentStep(FINALIZING);
        SignedTransaction subF = subFlow(new FinalizeFlow(stx));
        QueryCriteria queryCriteria = null;
        return subF;
    }

    @Suspendable
    public SignedTransaction defaultBatchFlow (List<T> outputStateList,
                                               List<StateAndRef<T>> prevStateList) throws FlowException {
        Party notary = CordaUtils.getServiceHubPreferredNotary(getServiceHub());

        for(StateAndRef<T> prev : prevStateList) {
            if(prev != null && prev.getState() != null && prev.getState().getNotary() != null) {
                notary = prev.getState().getNotary();
            }
        }

        progressTracker.setCurrentStep(GENERATING_APPLICATION_TRANSACTION);
        PublicKey myKey = CordaUtils.getServiceHubMyIdentity(getServiceHub()).getOwningKey();

        TransactionBuilder tx = new TransactionBuilder(notary);

        for(StateAndRef<T> prev : prevStateList) {
            tx.addInputState(prev);
        }

        for(T outputState : outputStateList) {
            tx.addOutputState(outputState);
        }

        tx.addCommand(command, myKey);

        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        Instant currentTime = getServiceHub().getClock().instant();
        tx.setTimeWindow(currentTime, Duration.ofSeconds(60));
        SignedTransaction stx = getServiceHub().signInitialTransaction(tx);

        progressTracker.setCurrentStep(FINALIZING);
        return subFlow(new FinalizeFlow(stx));
    }

}
