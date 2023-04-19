package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.contract.TokenContract;
import com.ehkd.corda.schema.PersistentToken;
import com.ehkd.corda.state.TokenState;
import lombok.extern.log4j.Log4j2;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.Builder;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;

import java.util.*;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@InitiatingFlow
@StartableByRPC
@Log4j2
public class TokenUnLockFlow extends BaseFlow<TokenState> {

    String tokenId;

    public TokenUnLockFlow(String tokenId) {
        super();
        this.tokenId = tokenId;
        command = new TokenContract.Commands.TokenPayment();
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        QueryCriteria queryCriteria = null;
        try {
            queryCriteria = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentToken.class.getField("batchId"), UUID.fromString(tokenId)),
                    Vault.StateStatus.UNCONSUMED);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        Vault.Page<TokenState> result = getServiceHub().getVaultService().queryBy(TokenState.class, queryCriteria);
       if(result.getStates().size() > 0){
           TokenState tokenState = result.getStates().get(0).getState().getData();
           tokenState.setLockedBy("");
           SignedTransaction signedTransaction;
           try {
               signedTransaction = defaultFlow(tokenState, result.getStates().get(0));
           }catch (FlowException e){
               throw new FlowException("payment error");
           }
           List<StateAndRef<TokenState>> redeemInputList = signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class);
           for (StateAndRef<TokenState> tokenStateRef:redeemInputList) {
                   SignedTransaction signedTransaction1 = subFlow(new AutoRedeemFlow(tokenStateRef));
           }
           return signedTransaction;


       }else{
           throw new FlowException("can not find token state record by tokenId:"+tokenId);
       }



    }

}
