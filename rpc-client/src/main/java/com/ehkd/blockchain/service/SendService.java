package com.ehkd.blockchain.service;

import com.ehkd.blockchain.sdk.CordaService;
import com.ehkd.blockchain.util.CordaResult;
import net.corda.core.transactions.SignedTransaction;

import java.util.List;

public class SendService {

    private SendService() {
    }
    public SendService(CordaService cordaService) {
        this.cordaService = cordaService;
    }

    CordaService cordaService;

    //转账
    public CordaResult<String> sendVoucher(String tokenId, Long amount, String fromUserId, String toUserId){
        if(tokenId == null || tokenId.equals("")){
            return CordaResult.error("Token Id can not be empty");
        }
        if(amount == null || amount <= 0L){
            return CordaResult.error("The token amount must be greater than 0");
        }
        if(fromUserId == null || fromUserId.equals("")){
            return CordaResult.error("Token fromUserId can not be empty");
        }
        if(toUserId == null || toUserId.equals("")){
            return CordaResult.error("Token toUserId can not be empty");
        }
        try {
            SignedTransaction signedTransaction = cordaService.sendVoucher(tokenId, amount, fromUserId, toUserId);
            return CordaResult.success(signedTransaction.getId().toString());
        } catch (Exception e){
            return CordaResult.error(e.getMessage());
        }
    }

    public CordaResult<String> sendP2PToken(String tokenType, Long amount, String fromUserId, String toUserId){
        if(tokenType == null || tokenType.equals("")){
            return CordaResult.error("Token Id can not be empty");
        }
        if(amount == null || amount <= 0L){
            return CordaResult.error("The token amount must be greater than 0");
        }
        if(fromUserId == null || fromUserId.equals("")){
            return CordaResult.error("Token fromUserId can not be empty");
        }
        if(toUserId == null || toUserId.equals("")){
            return CordaResult.error("Token toUserId can not be empty");
        }
        try {
            SignedTransaction signedTransaction = cordaService.sendP2PToken(tokenType, amount, fromUserId, toUserId);
            return CordaResult.success(signedTransaction.getId().toString());
        } catch (Exception e){
            return CordaResult.error(e.getMessage());
        }
    }



    /*
    //分期付款，上期
    public CordaResult<String>  splitPayment(List<String> tokenIdList, Long amount, String fromUserId, String toUserId){
        try {
            SignedTransaction signedTransaction = cordaService.tokenSplitPayment(tokenIdList, amount, fromUserId, toUserId);
            System.out.println(signedTransaction.toString());
            return CordaResult.success(signedTransaction.toString());
        } catch (Exception e){
            return CordaResult.error(e.getMessage());
        }
    }
    //分期付款，下期
    public CordaResult<String>  splitPaymentFinal(String txHash){
        try{
            SignedTransaction signedTransaction = cordaService.tokenSplitPaymentFinal(txHash);
            System.out.println(signedTransaction.toString());
            return CordaResult.success(signedTransaction.toString());
        } catch (Exception e){
            return CordaResult.error(e.getMessage());
        }
    }
    */
}
