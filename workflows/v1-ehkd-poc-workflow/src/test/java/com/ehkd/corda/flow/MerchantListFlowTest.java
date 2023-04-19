package com.ehkd.corda.flow;

import com.ehkd.corda.payload.MerchantList;
import com.ehkd.corda.state.MerchantListState;
import com.ehkd.corda.util.TestParties;
import com.ehkd.corda.util.TestUtil;
import net.corda.core.contracts.StateAndRef;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
public class MerchantListFlowTest {

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
        List<String> list = new ArrayList<>();
        list.add("123");
        list.add("234");
        list.add("345");

        MerchantList merchantList = new MerchantList("123456789", list);
        TestUtil.runFlow(tn, tn.partyA.startFlow(new MerchantListCreateFlow(merchantList)));
        List<StateAndRef<MerchantListState>> result = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(MerchantListState.class).getStates());
        Assert.assertEquals(1, result.size());

        List<String> list1 = new ArrayList<>();
        list1.add("2222");
        list1.add("3333");
        list1.add("4444");
        TestUtil.runFlow(tn, tn.partyA.startFlow(new MerchantListUpdateFlow(result.get(0).getRef(),list1)));
        List<StateAndRef<MerchantListState>> result1 = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(MerchantListState.class).getStates());
        Assert.assertEquals(1, result1.size());
    }
}
