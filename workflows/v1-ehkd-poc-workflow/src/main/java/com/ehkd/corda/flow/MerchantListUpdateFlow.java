package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.contract.MerchantListContract;
import com.ehkd.corda.contract.ReceiverGroupContract;
import com.ehkd.corda.state.MerchantListState;
import com.ehkd.corda.state.ReceiverGroupState;
import lombok.extern.log4j.Log4j2;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.StateRef;
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
public class MerchantListUpdateFlow extends BaseFlow<MerchantListState> {

    StateRef ref;

    List<String> merchantList;

    public MerchantListUpdateFlow(StateRef ref, List<String> merchantList) {
        super();
        this.merchantList = merchantList;
        this.ref = ref;
        command = new MerchantListContract.Commands.Update();
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        StateAndRef<MerchantListState> merchantListState = getServiceHub().toStateAndRef(ref);
        MerchantListState newMerchantListState = merchantListState.getState().getData().deepCopy();
        if (this.merchantList != null) {
            newMerchantListState.setMerchantList(this.merchantList);
        }
        return defaultFlow(newMerchantListState, merchantListState);
    }
}
