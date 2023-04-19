package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.contract.TokenContract;
import com.ehkd.corda.payload.TokenSendP2P;
import com.ehkd.corda.payload.TokenSendVoucher;
import com.ehkd.corda.schema.PersistentMember;
import com.ehkd.corda.schema.PersistentMerchantList;
import com.ehkd.corda.schema.PersistentToken;
import com.ehkd.corda.state.MemberState;
import com.ehkd.corda.state.MerchantListState;
import com.ehkd.corda.state.TokenState;
import com.ehkd.corda.utils.CordaUtils;
import lombok.extern.log4j.Log4j2;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.Builder;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;

import java.time.Instant;
import java.util.*;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@InitiatingFlow
@StartableByRPC
@Log4j2
public class TokenSendP2PFlow extends BaseFlow<TokenState> {

    String tokenType;
    Long amount;
    String fromUserId;
    String toUserId;

    public TokenSendP2PFlow(TokenSendP2P tokenSendP2P) {
        super();
        this.tokenType = tokenSendP2P.getTokenType();
        this.amount = tokenSendP2P.getAmount();
        this.fromUserId = tokenSendP2P.getFromUserId();
        this.toUserId = tokenSendP2P.getToUserId();
        command = new TokenContract.Commands.TokenPayment();
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {

        List<StateAndRef<TokenState>> prevRef = new ArrayList<>();
        QueryCriteria queryCriteria = null;
        try {
            queryCriteria = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentToken.class.getField("tokenType"), tokenType),
                    Vault.StateStatus.UNCONSUMED);
            queryCriteria = queryCriteria.and(new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentToken.class.getField("owner"), fromUserId),
                    Vault.StateStatus.UNCONSUMED));
            queryCriteria = queryCriteria.and(new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentToken.class.getField("isVoucher"), 0),
                    Vault.StateStatus.UNCONSUMED));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        Vault.Page<TokenState> result = getServiceHub().getVaultService().queryBy(TokenState.class, queryCriteria);
        if(result.getStates().size() <= 0){
            throw new FlowException("Can not find token with tokenType under fromUser ");
        }
        for (int i = 0; i < result.getStates().size(); i++) {
            prevRef.add(result.getStates().get(i));
        }
        String merchantListStr = result.getStates().get(0).getState().getData().getMerchantList();
        Integer isVoucher = result.getStates().get(0).getState().getData().getIsVoucher();

        if (isVoucher != 0) {
            throw new FlowException("This method only accepts p2p token");
        }

        QueryCriteria queryCriteriaMember = null;
        try {
            queryCriteriaMember = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentMember.class.getField("userId"), toUserId),
                    Vault.StateStatus.UNCONSUMED);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        Vault.Page<MemberState> resultMember = getServiceHub().getVaultService().queryBy(MemberState.class, queryCriteriaMember);
        if (resultMember.getStates().size() <= 0) {
            throw new FlowException("Can not find member by toUserId");
        }
        String memberType = resultMember.getStates().get(0).getState().getData().getType();
        if(memberType.equals("merchant")){
            String memberIndustry = resultMember.getStates().get(0).getState().getData().getIndustry();
            if (!memberIndustry.equals(tokenType)) {
                throw new FlowException("Token type is inconsistent with the merchant industry");
            }
        }

        Long amountTotal = 0L;
        UniqueIdentifier batchIdUid = new UniqueIdentifier();
        String name = "";
        String symbol = "";
        String sponsor = "";
        String issuer = "";
        Instant releaseDate = Instant.now();
        String merchantList = "";
        String lockedBy = "";


        boolean isAddData = false;

        List<TokenState> outputList = new ArrayList<>();
        for (StateAndRef<TokenState> data : prevRef) {
            amountTotal = data.getState().getData().getAmount() + amountTotal;
            if (!isAddData) {
                batchIdUid =  data.getState().getData().getBatchId();
                name = data.getState().getData().getName();
                symbol = data.getState().getData().getSymbol();
                sponsor = data.getState().getData().getSponsor();
                issuer = data.getState().getData().getIssuer();
                releaseDate = data.getState().getData().getReleaseDate();
                merchantList = data.getState().getData().getMerchantList();
                lockedBy = data.getState().getData().getLockedBy();
                isVoucher = data.getState().getData().getIsVoucher();
                isAddData = true;
            }
        }

        if (amountTotal.compareTo(amount) < 0) {
            throw new FlowException("The selected token combination has insufficient balance");
        } else if (amountTotal.compareTo(amount) == 0) {
            TokenState outputStateTo = new TokenState(
                    new UniqueIdentifier(),
                    batchIdUid,
                    name,
                    symbol,
                    2,
                    amount,
                    sponsor,
                    toUserId,
                    issuer,
                    releaseDate,
                    null,
                    tokenType,
                    merchantList,
                    lockedBy,
                    isVoucher,
                    CordaUtils.getServiceHubAllParties(getServiceHub())
            );
            outputList.add(outputStateTo);
        }else {
            TokenState outputStateTo = new TokenState(
                    new UniqueIdentifier(),
                    batchIdUid,
                    name,
                    symbol,
                    2,
                    amount,
                    sponsor,
                    toUserId,
                    issuer,
                    releaseDate,
                    null,
                    tokenType,
                    merchantList,
                    lockedBy,
                    isVoucher,
                    CordaUtils.getServiceHubAllParties(getServiceHub())
            );
            outputList.add(outputStateTo);

            Long amountForm = amountTotal - amount;
            TokenState outputStateFrom = new TokenState(
                    new UniqueIdentifier(),
                    batchIdUid,
                    name,
                    symbol,
                    2,
                    amountForm,
                    sponsor,
                    fromUserId,
                    issuer,
                    releaseDate,
                    null,
                    tokenType,
                    merchantList,
                    lockedBy,
                    isVoucher,
                    CordaUtils.getServiceHubAllParties(getServiceHub())
            );
            outputList.add(outputStateFrom);
        }
        SignedTransaction signedTransaction;
        try {
            signedTransaction = defaultBatchFlow(outputList, prevRef);
        }catch (FlowException e){
            throw new FlowException("send p2p token error");
        }
        if(memberType.equals("merchant")) {
            List<StateAndRef<TokenState>> redeemInputList = signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class);
            for (StateAndRef<TokenState> tokenStateRef : redeemInputList) {
                if (tokenStateRef.getState().getData().getOwner().equals(toUserId)) {
                    SignedTransaction signedTransaction1 = subFlow(new AutoRedeemFlow(tokenStateRef));
                }
            }
        }
        return signedTransaction;
    }

}
