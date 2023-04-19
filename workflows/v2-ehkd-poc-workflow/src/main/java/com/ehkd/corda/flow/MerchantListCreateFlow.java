package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.contract.MerchantListContract;
import com.ehkd.corda.payload.MerchantList;
import com.ehkd.corda.state.MerchantListState;
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
public class MerchantListCreateFlow extends BaseFlow<MerchantListState> {
    String merchantListId;
    List<String> merchantList;

    public MerchantListCreateFlow(MerchantList merchantList) {
        super();
        this.merchantListId = merchantList.getMerchantListId();
        this.merchantList = merchantList.getMerchantList();
        command = new MerchantListContract.Commands.Create();
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        MerchantListState newState = new MerchantListState(
                merchantListId,
                merchantList,
                CordaUtils.getServiceHubAllParties(getServiceHub())
        );
        return defaultFlow(newState, null);
    }
}
