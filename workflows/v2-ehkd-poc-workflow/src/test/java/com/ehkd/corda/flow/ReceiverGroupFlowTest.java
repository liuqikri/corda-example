package com.ehkd.corda.flow;

import com.ehkd.corda.payload.ReceiverGroup;
import com.ehkd.corda.state.ReceiverGroupState;
import com.ehkd.corda.util.TestParties;
import com.ehkd.corda.util.TestUtil;
import liquibase.util.StringUtils;
import net.corda.core.contracts.StateAndRef;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
public class ReceiverGroupFlowTest {

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
        String str = StringUtils.join(list, ",");
        List<String> arr = Arrays.asList(str.split(","));
        ReceiverGroup receiverGroup = new ReceiverGroup("123456789", list);
        TestUtil.runFlow(tn, tn.partyA.startFlow(new ReceiverGroupCreateFlow(receiverGroup)));
        List<StateAndRef<ReceiverGroupState>> result = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(ReceiverGroupState.class).getStates());
        Assert.assertEquals(1, result.size());
    }
}
