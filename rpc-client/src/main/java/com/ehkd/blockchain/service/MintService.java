package com.ehkd.blockchain.service;

import com.ehkd.blockchain.model.MintTokenModel;
import com.ehkd.blockchain.sdk.CordaService;
import com.ehkd.blockchain.util.CordaResult;
import com.ehkd.corda.state.TokenState;
import liquibase.util.StringUtils;
import net.corda.core.transactions.SignedTransaction;
import org.springframework.validation.annotation.Validated;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Validated
public class MintService {


    CordaService cordaService;

    private MintService() {
    }
    public MintService(CordaService cordaService) {
        this.cordaService = cordaService;
    }

    //个人发行Token
    public CordaResult<MintTokenModel> mintByUser(String userId, String name, String symbol, Long amount, String tokenType) {
        if(tokenType == null || tokenType.equals("")){
            return CordaResult.error("Token type can not be empty");
        }
        if(name == null || name.equals("")){
            return CordaResult.error("Token name can not be empty");
        }
        if(amount == null || amount <= 0L){
            return CordaResult.error("Token amount can not be empty");
        }
        if(userId == null || userId.equals("")){
            return CordaResult.error("Token userId can not be empty");
        }

        try {
            SignedTransaction signedTransaction = cordaService.mintTokenByUser(userId, name, symbol, amount, tokenType);
            if (signedTransaction == null) {
                return CordaResult.error("创建失败");
            }
            MintTokenModel mintTokenModel = new MintTokenModel();
            mintTokenModel.setTxHash(signedTransaction.getId().toString());
            mintTokenModel.setTokenId(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).get(0).getState().getData().getBatchId().toString());
            mintTokenModel.setName(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).get(0).getState().getData().getName());
            mintTokenModel.setSymbol(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).get(0).getState().getData().getSymbol());
            mintTokenModel.setAmount(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).get(0).getState().getData().getAmount());
            mintTokenModel.setCount(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).size());
            return CordaResult.success(mintTokenModel);
        }catch (Exception e){
            return CordaResult.error(e.getMessage());
        }
    }

    //后台发行Token(商户、银行)
    public CordaResult<MintTokenModel> mintByAdmin(String name, String symbol, Long amount, String tokenType, String sponsorId, String issuerId,
                                                   Date releaseDate, Date expiryDate, List<String> merchantIds, List<String> receiverIds) {

        if(!(tokenType == null || tokenType.equals("")) && !(merchantIds == null || merchantIds.size()<=0) ){
            return CordaResult.error("TokenType and merchant List cannot both have values");
        }

        if(name == null || name.equals("")){
            return CordaResult.error("Token name can not be empty");
        }
        if(amount == null || amount <= 0L){
            return CordaResult.error("Token amount can not be empty");
        }
        if(sponsorId == null || sponsorId.equals("")){
            return CordaResult.error("Token sponsorId can not be empty");
        }
        if(issuerId == null || issuerId.equals("")){
            return CordaResult.error("Token issuerId can not be empty");
        }

        if(releaseDate == null || releaseDate.before(new Date())){
            return CordaResult.error("Token releaseDate can not be empty");
        }

        if(expiryDate == null || expiryDate.before(new Date())){
            return CordaResult.error("Token expiryDate can not be empty");
        }
        if(receiverIds == null || receiverIds.size()<=0){
            return CordaResult.error("Token receiverIds can not be empty");
        }
        String merchantListId= "";
        if(merchantIds != null && merchantIds.size()>0){
            merchantListId = UUID.randomUUID().toString();
            try {
                cordaService.createMerchantList(merchantListId, merchantIds);
            }catch (Exception a){
                return CordaResult.error(a.getMessage());
            }
        }
        String receiverIdStr = StringUtils.join(receiverIds,",");

        try {
            SignedTransaction signedTransaction = cordaService.mintTokenByAdmin(name, symbol, amount, tokenType, sponsorId, issuerId, releaseDate, expiryDate, merchantListId, receiverIdStr);
            if (signedTransaction == null) {
                return CordaResult.error("mint token by admin err");
            }
            MintTokenModel mintTokenModel = new MintTokenModel();
            mintTokenModel.setTxHash(signedTransaction.getId().toString());
            mintTokenModel.setTokenId(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).get(0).getState().getData().getBatchId().toString());
            mintTokenModel.setName(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).get(0).getState().getData().getName());
            mintTokenModel.setSymbol(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).get(0).getState().getData().getSymbol());
            mintTokenModel.setAmount(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).get(0).getState().getData().getAmount());
            mintTokenModel.setCount(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).size());
            return CordaResult.success(mintTokenModel);
        }catch (Exception e){
            return CordaResult.error(e.getMessage());
        }
    }

    public CordaResult<MintTokenModel> mintLockedToken(Long amount, String merchantId, String bankId) throws Exception {
        if(amount == null || amount <= 0L){
            return CordaResult.error("Token amount can not be empty");
        }
        if(merchantId == null || merchantId.equals("")){
            return CordaResult.error("Token merchantId can not be empty");
        }
        if(bankId == null || bankId.equals("")){
            return CordaResult.error("Token bankId can not be empty");
        }
        try {
            SignedTransaction signedTransaction = cordaService.mintLockedToken(merchantId,bankId,"Lock name","Lock symbol",amount,"Locked");
            if (signedTransaction == null) {
                return CordaResult.error("mint Lock token error");
            }
            MintTokenModel mintTokenModel = new MintTokenModel();
            mintTokenModel.setTxHash(signedTransaction.getId().toString());
            mintTokenModel.setTokenId(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).get(0).getState().getData().getBatchId().toString());
            mintTokenModel.setName(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).get(0).getState().getData().getName());
            mintTokenModel.setSymbol(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).get(0).getState().getData().getSymbol());
            mintTokenModel.setAmount(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).get(0).getState().getData().getAmount());
            mintTokenModel.setCount(signedTransaction.getCoreTransaction().outRefsOfType(TokenState.class).size());
            return CordaResult.success(mintTokenModel);
        }catch (Exception e){
            return CordaResult.error(e.getMessage());
        }
    }

    public CordaResult<String> unLockToken(String tokenId){
        if(tokenId == null || tokenId.equals("")){
            return CordaResult.error("Token Id can not be empty");
        }
        try {
            SignedTransaction signedTransaction = cordaService.unLockToken(tokenId);
            if (signedTransaction == null) {
                return CordaResult.error("mint Lock token error");
            }
            return CordaResult.success(signedTransaction.getId().toString());
        }catch (Exception e){
            return CordaResult.error(e.getMessage());
        }
    }
}
