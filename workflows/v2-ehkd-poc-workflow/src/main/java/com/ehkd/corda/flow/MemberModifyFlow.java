package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.contract.MemberContract;
import com.ehkd.corda.payload.Member;
import com.ehkd.corda.state.MemberState;
import lombok.extern.log4j.Log4j2;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.StateRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.transactions.SignedTransaction;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@Log4j2
@StartableByRPC
public class MemberModifyFlow extends BaseFlow<MemberState> {
    StateRef ref;
    String userId;
    String type;
    String industry;

    public MemberModifyFlow(StateRef ref, Member member) {
        super();
        this.ref = ref;
        this.userId = member.getUserId();
        this.type = member.getType();
        this.industry = member.getIndustry();
        command = new MemberContract.Commands.Update();
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        StateAndRef<MemberState> memberState = getServiceHub().<MemberState>toStateAndRef(ref);
        MemberState newMemberState = memberState.getState().getData().deepCopy();
        if(userId != null) {
            newMemberState.setUserId(userId);
        }
        if(type != null) {
            newMemberState.setType(type);
        }
        if(industry != null) {
            newMemberState.setIndustry(industry);
        }
        UniqueIdentifier linearId = memberState.getState().getData().getLinearId();
        if(linearId != null){
            newMemberState.setLinearId(linearId);
        }
        return defaultFlow(newMemberState, memberState);
    }
}
