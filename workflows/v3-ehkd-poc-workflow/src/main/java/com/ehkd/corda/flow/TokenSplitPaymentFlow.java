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
@StartableByRPC
@Log4j2
public class TokenSplitPaymentFlow extends BaseFlow<TokenState> {

    String inputBatchId;
    Long amount;
    String fromUserId;
    String toUserId;

    public TokenSplitPaymentFlow(TokenSendVoucher tokenPayment) {
        super();
        this.inputBatchId = tokenPayment.getInputBatchId();
        this.amount = tokenPayment.getAmount();
        this.fromUserId = tokenPayment.getFromUserId();
        this.toUserId = tokenPayment.getToUserId();
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
                queryCriteria = queryCriteria.and( new QueryCriteria.VaultCustomQueryCriteria( Builder.equal(PersistentToken.class.getField("owner"), fromUserId),
                        Vault.StateStatus.UNCONSUMED));

            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            Vault.Page<TokenState> result = getServiceHub().getVaultService().queryBy(TokenState.class, queryCriteria);
            for(int i =0 ; i < result.getStates().size();i++){
                prevRef.add( result.getStates().get(i));
            }


        QueryCriteria queryCriteriaMember = null;
        try {
            queryCriteriaMember = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentMember.class.getField("userId"), UUID.fromString(toUserId)),
                    Vault.StateStatus.UNCONSUMED);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        Vault.Page<MemberState> resultMember = getServiceHub().getVaultService().queryBy(MemberState.class, queryCriteriaMember);
        String memberType = resultMember.getStates().get(0).getState().getData().getType();
        String memberIndustry = resultMember.getStates().get(0).getState().getData().getIndustry();

        Long amountTotalVoucher = 0L;
        Long amountTotalP2P = 0L;

        UniqueIdentifier batchIdVoucher = new UniqueIdentifier();
        String nameVoucher = "";
        String symbolVoucher = "";
        String sponsorVoucher = "";
        String issuerVoucher = "";
        Instant releaseDateVoucher = Instant.now();
        Instant expiryDateVoucher = Instant.now();
        String tokenTypeVoucher = "";
        String merchantListVoucher = "";
        String lockedByVoucher = "";
        Integer isVoucherVoucher = 1;

        UniqueIdentifier batchIdP2P = new UniqueIdentifier();
        String nameP2P = "";
        String symbolP2P = "";
        String sponsorP2P = "";
        String issuerP2P = "";
        Instant releaseDateP2P = Instant.now();
        Instant expiryDateP2P = Instant.now();
        String tokenTypeP2P = "";
        String merchantListP2P = "";
        String lockedByP2P = "";
        Integer isVoucherP2P = 0;
        boolean isAddDateVoucher = false;
        boolean isAddDateP2P = false;
        List<TokenState> outputList = new ArrayList<>();
        String batchId = "";
        for (StateAndRef<TokenState> data : prevRef ) {
            if(data.getState().getData().getIsVoucher() == 1) {
                amountTotalVoucher = data.getState().getData().getAmount() + amountTotalVoucher;
                if(batchId.equals("")){
                     batchId = data.getState().getData().getBatchId().toString();
                }else if (!batchId.equals(data.getState().getData().getBatchId().toString())){
                    throw new FlowException("The voucher token must be the same batch id");
                }
                if(Instant.now().compareTo(data.getState().getData().getExpiryDate()) > 0 ){
                    throw new FlowException("The voucher token has expired");
                }
                if(!isAddDateVoucher){
                    List<String> merchantListIds = Arrays.asList(data.getState().getData().getMerchantList().split(","));
                    boolean containMerchantList = false;
                    for (String merchantListId:merchantListIds) {
                        QueryCriteria queryCriteriaMerchant = null;
                        try {
                            queryCriteriaMerchant = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentMerchantList.class.getField("merchantListId"), merchantListId),
                                    Vault.StateStatus.UNCONSUMED);
                        } catch (NoSuchFieldException e) {
                            throw new RuntimeException(e);
                        }
                        Vault.Page<MerchantListState> resultMerchant = getServiceHub().getVaultService().queryBy(MerchantListState.class, queryCriteriaMerchant);
                        if(resultMerchant.getStates().size() <= 0){
                            throw new FlowException("Can not find merchantList by merchantListId");
                        }
                        List<String> merchantList = resultMerchant.getStates().get(0).getState().getData().getMerchantList();
                        if(merchantList.contains(toUserId)){
                            containMerchantList = true;
                        }
                    }
                    if(!containMerchantList){
                        throw new FlowException("The voucher token does not support the current merchant");
                    }
                    batchIdVoucher = data.getState().getData().getBatchId();
                    nameVoucher = data.getState().getData().getName();
                    symbolVoucher = data.getState().getData().getSymbol();
                    sponsorVoucher = data.getState().getData().getSponsor();
                    issuerVoucher = data.getState().getData().getIssuer();
                    releaseDateVoucher = data.getState().getData().getReleaseDate();
                    expiryDateVoucher = data.getState().getData().getExpiryDate();
                    tokenTypeVoucher = data.getState().getData().getTokenType();
                    merchantListVoucher = data.getState().getData().getMerchantList();
                    lockedByVoucher = data.getState().getData().getLockedBy();
                    isVoucherVoucher = data.getState().getData().getIsVoucher();
                    isAddDateVoucher = true;
                }
            }
            if(data.getState().getData().getIsVoucher() == 0) {
                amountTotalP2P = data.getState().getData().getAmount() + amountTotalP2P;
                if(!Objects.equals(memberIndustry, data.getState().getData().getTokenType())){
                    throw new FlowException("P2P Token type does not match the payee industry");
                }
                if(!isAddDateP2P){
                    batchIdP2P = data.getState().getData().getBatchId();
                    nameP2P = data.getState().getData().getName();
                    symbolP2P = data.getState().getData().getSymbol();
                    sponsorP2P = data.getState().getData().getSponsor();
                    issuerP2P = data.getState().getData().getIssuer();
                    releaseDateP2P = data.getState().getData().getReleaseDate();
                    expiryDateP2P = data.getState().getData().getExpiryDate();
                    tokenTypeP2P = data.getState().getData().getTokenType();
                    merchantListP2P = data.getState().getData().getMerchantList();
                    lockedByP2P = data.getState().getData().getLockedBy();
                    isVoucherP2P = data.getState().getData().getIsVoucher();
                    isAddDateP2P = true;
                }
            }
        }

        Long total = amountTotalVoucher+amountTotalP2P;
        Long splitAmount = amount/2 ;


        if(amountTotalVoucher > 0L && memberType.equals("user"))
        {
            throw new FlowException("Voucher Token can not send to user");
        }
        if(amountTotalVoucher.compareTo(amount) >= 0 && amountTotalP2P>0){
            throw new FlowException("Token combination selection error");
        }
        else if( total.compareTo(amount) < 0)
        {
            throw new FlowException("The selected token combination has insufficient balance");
        }
        else if(total.compareTo(amount) == 0){
            if(amountTotalVoucher.compareTo(0L) == 0){
                TokenState outputStateTo = new TokenState(
                        new UniqueIdentifier(),
                        batchIdP2P,
                        nameP2P,
                        symbolP2P,
                        2,
                        splitAmount,
                        sponsorP2P,
                        toUserId,
                        issuerP2P,
                        releaseDateP2P,
                        expiryDateP2P,
                        tokenTypeP2P,
                        merchantListP2P,
                        lockedByP2P,
                        isVoucherP2P,
                        CordaUtils.getServiceHubAllParties(getServiceHub())
                );

                TokenState outputStateToLock = new TokenState(
                        new UniqueIdentifier(),
                        batchIdP2P,
                        nameP2P,
                        symbolP2P,
                        2,
                        splitAmount,
                        sponsorP2P,
                        toUserId,
                        issuerP2P,
                        releaseDateP2P,
                        expiryDateP2P,
                        tokenTypeP2P,
                        merchantListP2P,
                        fromUserId,
                        isVoucherP2P,
                        CordaUtils.getServiceHubAllParties(getServiceHub())
                );

                outputList.add(outputStateTo);
                outputList.add(outputStateToLock);

            }else {
                TokenState outputStateTo = new TokenState(
                        new UniqueIdentifier(),
                        batchIdVoucher,
                        nameVoucher,
                        symbolVoucher,
                        2,
                        splitAmount,
                        sponsorVoucher,
                        toUserId,
                        issuerVoucher,
                        releaseDateVoucher,
                        expiryDateVoucher,
                        tokenTypeVoucher,
                        merchantListVoucher,
                        lockedByVoucher,
                        isVoucherVoucher,
                        CordaUtils.getServiceHubAllParties(getServiceHub())
                );

                TokenState outputStateToLock = new TokenState(
                        new UniqueIdentifier(),
                        batchIdVoucher,
                        nameVoucher,
                        symbolVoucher,
                        2,
                        splitAmount,
                        sponsorVoucher,
                        toUserId,
                        issuerVoucher,
                        releaseDateVoucher,
                        expiryDateVoucher,
                        tokenTypeVoucher,
                        merchantListVoucher,
                        fromUserId,
                        isVoucherVoucher,
                        CordaUtils.getServiceHubAllParties(getServiceHub())
                );
                outputList.add(outputStateTo);
                outputList.add(outputStateToLock);
            }
        }
        else if (total.compareTo(amount) > 0){
            if(amountTotalVoucher.compareTo(0L) == 0) {
                TokenState outputStateTo = new TokenState(
                        new UniqueIdentifier(),
                        batchIdP2P,
                        nameP2P,
                        symbolP2P,
                        2,
                        splitAmount,
                        sponsorP2P,
                        toUserId,
                        issuerP2P,
                        releaseDateP2P,
                        expiryDateP2P,
                        tokenTypeP2P,
                        merchantListP2P,
                        lockedByP2P,
                        isVoucherP2P,
                        CordaUtils.getServiceHubAllParties(getServiceHub())
                );
                TokenState outputStateToLock = new TokenState(
                        new UniqueIdentifier(),
                        batchIdP2P,
                        nameP2P,
                        symbolP2P,
                        2,
                        splitAmount,
                        sponsorP2P,
                        toUserId,
                        issuerP2P,
                        releaseDateP2P,
                        expiryDateP2P,
                        tokenTypeP2P,
                        merchantListP2P,
                        fromUserId,
                        isVoucherP2P,
                        CordaUtils.getServiceHubAllParties(getServiceHub())
                );
                outputList.add(outputStateTo);
                outputList.add(outputStateToLock);
            }else{
                TokenState outputStateTo = new TokenState(
                        new UniqueIdentifier(),
                        batchIdVoucher,
                        nameVoucher,
                        symbolVoucher,
                        2,
                        splitAmount,
                        sponsorVoucher,
                        toUserId,
                        issuerVoucher,
                        releaseDateVoucher,
                        expiryDateVoucher,
                        tokenTypeVoucher,
                        merchantListVoucher,
                        lockedByVoucher,
                        isVoucherVoucher,
                        CordaUtils.getServiceHubAllParties(getServiceHub())
                );

                TokenState outputStateToLock = new TokenState(
                        new UniqueIdentifier(),
                        batchIdVoucher,
                        nameVoucher,
                        symbolVoucher,
                        2,
                        splitAmount,
                        sponsorVoucher,
                        toUserId,
                        issuerVoucher,
                        releaseDateVoucher,
                        expiryDateVoucher,
                        tokenTypeVoucher,
                        merchantListVoucher,
                        fromUserId,
                        isVoucherVoucher,
                        CordaUtils.getServiceHubAllParties(getServiceHub())
                );
                outputList.add(outputStateTo);
                outputList.add(outputStateToLock);
            }

            Long amountForm = total - amount;

            if(amountTotalP2P.compareTo(0L) == 0) {
                TokenState outputStateFrom = new TokenState(
                        new UniqueIdentifier(),
                        batchIdVoucher,
                        nameVoucher,
                        symbolVoucher,
                        2,
                        amountForm,
                        sponsorVoucher,
                        fromUserId,
                        issuerVoucher,
                        releaseDateVoucher,
                        expiryDateVoucher,
                        tokenTypeVoucher,
                        merchantListVoucher,
                        lockedByVoucher,
                        isVoucherVoucher,
                        CordaUtils.getServiceHubAllParties(getServiceHub())
                );
                outputList.add(outputStateFrom);
            }else{
                TokenState outputStateFrom = new TokenState(
                        new UniqueIdentifier(),
                        batchIdP2P,
                        nameP2P,
                        symbolP2P,
                        2,
                        amountForm,
                        sponsorP2P,
                        fromUserId,
                        issuerP2P,
                        releaseDateP2P,
                        expiryDateP2P,
                        tokenTypeP2P,
                        merchantListP2P,
                        lockedByP2P,
                        isVoucherP2P,
                        CordaUtils.getServiceHubAllParties(getServiceHub())
                );
                outputList.add(outputStateFrom);
            }

        }
        SignedTransaction signedTransaction;
        try {
            signedTransaction = defaultBatchFlow(outputList, prevRef);
        }catch (FlowException e){
            throw new FlowException("payment error");
        }
        List<StateAndRef<TokenState>> redeemInputList = signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class);
        for (StateAndRef<TokenState> tokenStateRef:redeemInputList) {
            if(tokenStateRef.getState().getData().getOwner().equals(toUserId)){
                //call auto redeem flow
                SignedTransaction signedTransaction1 = subFlow(new AutoRedeemFlow(tokenStateRef));
            }
        }
        return signedTransaction;
    }

}
