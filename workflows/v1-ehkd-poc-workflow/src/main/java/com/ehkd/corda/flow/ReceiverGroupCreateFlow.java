package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.contract.ReceiverGroupContract;
import com.ehkd.corda.payload.ReceiverGroup;
import com.ehkd.corda.state.ReceiverGroupState;
import com.ehkd.corda.utils.CordaUtils;
import lombok.extern.log4j.Log4j2;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.transactions.SignedTransaction;

import java.util.List;


/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@StartableByRPC
@Log4j2
public class ReceiverGroupCreateFlow extends BaseFlow<ReceiverGroupState> {
    String groupId;
    List<String> members;

    public ReceiverGroupCreateFlow(ReceiverGroup receiverGroup) {
        super();
        this.groupId = receiverGroup.getGroupId();
        this.members = receiverGroup.getMembers();
        command = new ReceiverGroupContract.Commands.Create();
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        ReceiverGroupState newState = new ReceiverGroupState(
                groupId,
                members,
                CordaUtils.getServiceHubAllParties(getServiceHub())
        );
        return defaultFlow(newState, null);
    }
}
