package com.ehkd.corda.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.ehkd.corda.contract.TokenContract;
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
public class TokenSendVoucherFlow extends BaseFlow<TokenState> {

    String inputBatchId;
    Long amount;
    String fromUserId;
    String toUserId;

    public TokenSendVoucherFlow(TokenSendVoucher tokenSendVoucher) {
        super();
        this.inputBatchId = tokenSendVoucher.getInputBatchId();
        this.amount = tokenSendVoucher.getAmount();
        this.fromUserId = tokenSendVoucher.getFromUserId();
        this.toUserId = tokenSendVoucher.getToUserId();
        command = new TokenContract.Commands.TokenPayment();
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {

        List<StateAndRef<TokenState>> prevRef = new ArrayList<>();
        QueryCriteria queryCriteria = null;
        try {
            queryCriteria = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentToken.class.getField("batchId"), UUID.fromString(inputBatchId)),
                    Vault.StateStatus.UNCONSUMED);
            queryCriteria = queryCriteria.and(new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentToken.class.getField("owner"), fromUserId),
                    Vault.StateStatus.UNCONSUMED));

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        Vault.Page<TokenState> result = getServiceHub().getVaultService().queryBy(TokenState.class, queryCriteria);
        if (result.getStates().size() <= 0) {
            throw new FlowException("Can not find token with inputBatchId under fromUser");
        }
        for (int i = 0; i < result.getStates().size(); i++) {
            prevRef.add(result.getStates().get(i));
        }
        String tokenType = result.getStates().get(0).getState().getData().getTokenType();
        String merchantListStr = result.getStates().get(0).getState().getData().getMerchantList();
        Integer isVoucher = result.getStates().get(0).getState().getData().getIsVoucher();

        if (isVoucher != 1) {
            throw new FlowException("This method only accepts voucher token ");
        }

        QueryCriteria queryCriteriaMember = null;
        try {
            queryCriteriaMember = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentMember.class.getField("userId"), toUserId),
                    Vault.StateStatus.UNCONSUMED);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        Vault.Page<MemberState> resultMember = getServiceHub().getVaultService().queryBy(MemberState.class, queryCriteriaMember);
        if(resultMember.getStates().size() <= 0){
            throw new FlowException("Can not find member with toUser under memberState ");
        }
        String memberType = resultMember.getStates().get(0).getState().getData().getType();
        if (!memberType.equals("merchant")) {
            throw new FlowException("The member type of payee is not a merchant");
        }

        if (tokenType != null && !tokenType.equals("")) {
            if (resultMember.getStates().size() <= 0) {
                throw new FlowException("Can not find member by toUserId");
            }
            String memberIndustry = resultMember.getStates().get(0).getState().getData().getIndustry();
            if (!memberIndustry.equals(tokenType)) {
                throw new FlowException("Token type is inconsistent with the merchant industry");
            }
        } else if (merchantListStr != null && !merchantListStr.equals("")) {
            List<String> merchantListIds = Arrays.asList(merchantListStr.split(","));
            boolean containMerchantList = false;
            for (String merchantListId : merchantListIds) {
                QueryCriteria queryCriteriaMerchant = null;
                try {
                    queryCriteriaMerchant = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentMerchantList.class.getField("merchantListId"), merchantListId),
                            Vault.StateStatus.UNCONSUMED);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                Vault.Page<MerchantListState> resultMerchant = getServiceHub().getVaultService().queryBy(MerchantListState.class, queryCriteriaMerchant);
                if (resultMerchant.getStates().size() <= 0) {
                    throw new FlowException("Can not find merchantList by merchantListId");
                }
                List<String> memberList = resultMerchant.getStates().get(0).getState().getData().getMerchantList();
                if (memberList.contains(toUserId)) {
                    containMerchantList = true;
                }
            }
            if (!containMerchantList) {
                throw new FlowException("The voucher token does not support the current merchant");
            }
        }

        Long amountTotal = 0L;
        UniqueIdentifier batchIdUid = new UniqueIdentifier();
        String name = "";
        String symbol = "";
        String sponsor = "";
        String issuer = "";
        Instant releaseDate = Instant.now();
        Instant expiryDate = Instant.now();
        String merchantList = "";
        String lockedBy = "";


        boolean isAddData = false;

        List<TokenState> outputList = new ArrayList<>();
        for (StateAndRef<TokenState> data : prevRef) {
            amountTotal = data.getState().getData().getAmount() + amountTotal;
            if (Instant.now().compareTo(data.getState().getData().getExpiryDate()) > 0) {
                throw new FlowException("The voucher token has expired,now:" + Instant.now() + "  expiryDate:" + data.getState().getData().getExpiryDate());
            }

            if (!isAddData) {
                batchIdUid =  data.getState().getData().getBatchId();
                name = data.getState().getData().getName();
                symbol = data.getState().getData().getSymbol();
                sponsor = data.getState().getData().getSponsor();
                issuer = data.getState().getData().getIssuer();
                releaseDate = data.getState().getData().getReleaseDate();
                expiryDate = data.getState().getData().getExpiryDate();
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
                    expiryDate,
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
                    expiryDate,
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
                        expiryDate,
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
            throw new FlowException("send token voucher error");
        }
         List<StateAndRef<TokenState>> redeemInputList = signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class);
        for (StateAndRef<TokenState> tokenStateRef:redeemInputList) {
            if(tokenStateRef.getState().getData().getOwner().equals(toUserId)){
                SignedTransaction signedTransaction1 = subFlow(new AutoRedeemFlow(tokenStateRef));
            }
        }
        return signedTransaction;
    }

}
