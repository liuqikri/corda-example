package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.contract.MemberContract;
import com.ehkd.corda.payload.Member;
import com.ehkd.corda.state.MemberState;
import com.ehkd.corda.utils.CordaUtils;
import lombok.extern.log4j.Log4j2;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.transactions.SignedTransaction;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@StartableByRPC
@Log4j2
public class MemberCreateFlow extends BaseFlow<MemberState> {
    String userId;
    String type;
    String name;
    String industry;

    public MemberCreateFlow(Member member) {
        super();
        this.userId = member.getUserId();
        this.type = member.getType();
        this.name = member.getName();
        this.industry = member.getIndustry();
        command = new MemberContract.Commands.Create();
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        MemberState newState = new MemberState(
                userId,
                type,
                name,
                industry,
                CordaUtils.getServiceHubAllParties(getServiceHub())
        );
        return defaultFlow(newState, null);
    }
}
