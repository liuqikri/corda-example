package com.ehkd.corda.flow;

import com.ehkd.corda.payload.Member;
import com.ehkd.corda.state.MemberState;
import com.ehkd.corda.util.TestParties;
import com.ehkd.corda.util.TestUtil;
import net.corda.core.contracts.StateAndRef;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
public class MemberFlowTest {

    TestParties tn = new TestParties();

    @BeforeEach
    public void setup() {
        tn.setup();
    }

    @AfterEach
    public void tearDown() {
        tn.tearDown();
    }

    @Test
    public void createFlow() throws ExecutionException, InterruptedException {
        Member member = new Member("123456789", "man", "IT");
        TestUtil.runFlow(tn, tn.partyA.startFlow(new MemberCreateFlow(member)));
        List<StateAndRef<MemberState>> result = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(MemberState.class).getStates());
        Assert.assertEquals(1, result.size());
    }


    @Test
    public void updateFlow() throws ExecutionException, InterruptedException {
        Member member = new Member("123456789", "man", "IT");
        TestUtil.runFlow(tn, tn.partyA.startFlow(new MemberCreateFlow(member)));
        List<StateAndRef<MemberState>> result = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(MemberState.class).getStates());
        Assert.assertEquals(1, result.size());
    }
}
