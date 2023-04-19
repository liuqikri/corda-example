package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.contract.ReceiverGroupContract;
import com.ehkd.corda.payload.ReceiverGroup;
import com.ehkd.corda.state.ReceiverGroupState;
import com.ehkd.corda.utils.CordaUtils;
import lombok.extern.log4j.Log4j2;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.StateRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.transactions.SignedTransaction;

import java.time.Instant;
import java.util.List;


/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@StartableByRPC
@Log4j2
public class ReceiverGroupUpdateFlow extends BaseFlow<ReceiverGroupState> {

    StateRef ref;

    List<String> members;

    public ReceiverGroupUpdateFlow(StateRef ref, List<String> members) {
        super();
        this.members = members;
        this.ref = ref;
        command = new ReceiverGroupContract.Commands.Update();
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        StateAndRef<ReceiverGroupState> receiverGroupState = getServiceHub().toStateAndRef(ref);
        ReceiverGroupState newReceiverGroupState = receiverGroupState.getState().getData().deepCopy();
        if (this.members != null) {
            newReceiverGroupState.setMembers(this.members);
        }
        return defaultFlow(newReceiverGroupState, receiverGroupState);
    }
}
